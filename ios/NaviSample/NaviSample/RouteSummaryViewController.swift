//
//  RouteSummaryViewController.swift
//  iRozeNavi
//
//  Created by 서유리 on 2017. 9. 21..
//  Copyright © 2017년 kt. All rights reserved.
//
import Foundation
import UIKit
import roze
import Toast_Swift
import RxSwift
import gmaps

protocol RouteSummaryViewControllerDelegate: class {
    func passData(value: Any?)
}
class RouteSummaryViewController: UIViewController {
    var isGpsStatus = false {
        didSet {
            logDebug("change gps status: \(isGpsStatus)")
            mapPresenter?.didChangedGpsStatus(isGpsStatus)
        }
    }
    var zoomLevel: Int32 = 0 {
        didSet {
            if zoomLevel != oldValue {
                logDebug("zoom level changed: \(zoomLevel)")
                mapPresenter?.didChangedZoomLevel(zoomLevel)
            }
        }
    }
    
    var mapHeight: CGFloat {
        return mapView.frame.size.height
    }
    
    var mapWidth: CGFloat {
        return mapView.frame.size.width
    }
    
    var mapResolution: Float {
        return mapView.getResolution()
    }
    
    var viewpoint: GViewpoint {
        return mapView.viewpoint
    }
    
    var mapPresenter: RouteMapPresenter?
    
    var placeData: SearchPlaceData?
    var routeSummary: GRouteSummary?
    var waypointData: SearchPlaceData?
    
    @IBOutlet weak var destPlace: UITextField!
    @IBOutlet weak var startPlace: UITextField!
    @IBOutlet weak var waypointPlace: UITextField!
    private var screenModeSubscription: Disposable?
    
    @IBOutlet weak var wayPointView: UIView!
    @IBOutlet weak var routeResultTableView: UITableView!
    @IBOutlet weak var routePathInputView: UIStackView!
    @IBOutlet weak var mapView: GMapView!
   
    @IBOutlet weak var cancelWayPointBtn: UIButton!
    weak var delegate: RouteSummaryViewControllerDelegate?
    
    override func viewDidLoad() {
        super.viewDidLoad()

        mapView.delegate = self
      
        guard let data = placeData else {
            return
        }
        
        destPlace.text = data.name
        guard let lastLocation = GNavigationManager.instance.lastGpsLocation else {
            print("requestRoutePlan - lastGpsLocation 없어서 경로검색못해 ")
            self.view.makeToast("lastGpsLocation 없어서 경로검색못해", duration: 2.0, position: .bottom)
            return
        }
        
        let start = GUtmk(from: lastLocation)!
        //support multiple entrances
        var dests = [GUtmk]()
        dests.append(data.getLocation())
        if let multiEnts = data.multipleEntrances {
            dests.append(contentsOf: multiEnts.map(){mappableUtmk in return mappableUtmk.toGUtmk()})
        }
        
        requestRoute((start: start, wayPoints: nil, destination: dests))
    }
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }

    override func viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()

        print("RouteSummaryViewController print")
        let path = UIBezierPath(roundedRect: routePathInputView.bounds,
                                byRoundingCorners: [.topLeft, .topRight],
                                cornerRadii: CGSize(width: 15.0, height: 15.0))
        let mask = CAShapeLayer()
        mask.path = path.cgPath
        routePathInputView.layer.mask = mask

    }
    
    @IBAction func unwindToContainerVC(segue: UIStoryboardSegue) {
        if segue.identifier == SearchViewController.unwindSegueIdentifier {
            print("wayPoint")
            if let controller = segue.source as? SearchViewController {
                guard let routeSummary = self.routeSummary else {
                    print("routeSummary 없어서 경로검색못해 ")
                    self.view.makeToast("경로검색실패")
                    return
                }
                if let place = controller.placeData {
                    waypointPlace.text = place.name
                    //routeSummary.routePlan 에 최초에 요청했던 목적지목록이 계속 남아있으므로,
                    // 멀티입구점을 포함하는 목적지 routing 시에도 그냥 사용할 수 있음.
                    requestRoute((start: routeSummary.routePlan.start,
                                  wayPoints: [place.getLocation()],
                                  destination: routeSummary.routePlan.dests))
                } else {
                    waypointPlace.text = ""
                }
            }
        }
    }
    /*
    @IBAction func unwindToPrevVC(segue: UIStoryboardSegue) {
        
        if segue.identifier == "wayPoint" {
            print("wayPoint")
            if let controller = segue.source as? SearchViewController {
                guard let routeSummary = self.routeSummary else {
                    print("routeSummary 없어서 경로검색못해 ")
                    self.view.makeToast("경로검색실패")
                    return
                }
                if let place = controller.placeData {
                    waypointPlace.text = place.name
                    requestRoute((start: routeSummary.routePlan.start, wayPoints: [place.getLocation()], destination: routeSummary.routePlan.dests))
                } else {
                    waypointPlace.text = ""
                }
            }
        }
    }
    */
    @IBAction func cancelRouting(_ sender: Any) {
        dismiss(animated: true, completion: nil)
    }
    @IBAction func startNavigating(_ sender: Any) {
        if sender is UIView {
            let selectedIndexPath: IndexPath = routeResultTableView.indexPathForSelectedRow!
            routeSummary!.setActiveRoute(routeIndex: selectedIndexPath.row)
            delegate?.passData(value: self.routeSummary)
            
            self.dismiss(animated: true, completion: nil)
        }

    }

    @IBAction func switchPlace(_ sender: Any) {
        print("switchPlace")
        guard let routeSummary = self.routeSummary else {
            print("routeSummary 없어서 경로검색못해 ")
            return
        }
        
        let start = routeSummary.routePlan.dests.first!
        let wayPoints = waypointData != nil ? [waypointData!.getLocation()] : nil
        let dests = [(self.routeSummary?.routePlan.start)!]
        
        requestRoute((start: start, wayPoints: wayPoints, destination: dests))
        
        let tmp = destPlace.text
        destPlace.text = startPlace.text
        startPlace.text = tmp

        self.routeResultTableView.reloadData()
    }


    @IBAction func addWayPoint(_ sender: Any) {
        cancelWayPointBtn.becomeFirstResponder()
        wayPointView.isHidden = false
    }
    @IBAction func showDetailRoute(_ sender: Any) {
      
    }
    
    @IBAction func deleteWayPoint(_ sender: Any) {

        guard waypointPlace.text != "" else {
            return
        }
        waypointPlace.text = ""
        wayPointView.isHidden = true
        routePathInputView.layoutIfNeeded()
        
        requestRoute((start: (self.routeSummary?.routePlan.start)!,
                          wayPoints: nil, destination: (self.routeSummary?.routePlan.dests)!))
    }
    
    func requestRoute(_ param:(start: GUtmk, wayPoints: [GUtmk]? ,destination: [GUtmk])) {
        RouteSearch.requestRoutePlan((start: param.start, wayPoints: param.wayPoints, destination: param.destination),
                                     completion: { (routesummary, error) in
                                        if let error = error {
                                            logError("Fail to calculate route: \(error)")
                                            
                                        } else {
                                            self.routeSummary = routesummary!
                                            if let presenter = self.mapPresenter {
                                                presenter.stop()
                                            }
                                            self.mapPresenter = RouteMapPresenter(routeSummary: self.routeSummary!)
                                            self.mapPresenter?.routeMapViewController = self
                                            self.mapPresenter?.start()
                                            self.routeResultTableView.reloadData()
                                        }
        })
    }
    
    func addOverlay(_ overlay: GOverlay) {
        mapView.add(overlay)
    }
    
    func removeOverlay(_ overlay: GOverlay) {
        mapView.remove(overlay)
    }
    
    func moveMapTo(_ viewpointChange: GViewpointChange,
                   duration: Int32 = 0,
                   animationType: GAnimationTiming = .default) {
        mapView.animateViewpoint(viewpointChange, duration: duration, animationTiming: animationType)
    }
    
}

extension RouteSummaryViewController: UITableViewDataSource {
    
    func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }
    
    func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        
        guard let data = routeSummary?.routes else {
            return 0
        }
        guard data.count > 0 else {
            return 0
        }
        return data.count

    }
    
    func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "routeCell", for: indexPath)

        if let routeCell = cell as? RouteSummaryTableViewCell {
            let cellData = routeSummary!.routes[indexPath.row]

            routeCell.takingTime.text = changeDistanceMeasure(value: cellData.time, measureType: .time)
            routeCell.takingDistance.text = changeDistanceMeasure(value: cellData.distance,
                                                                  measureType: .Kilometer) + "km"
            routeCell.cost.text = String(cellData.totalToll) + "원"

            routeCell.routeType.text = String(routeSummary!.routePlan.routeTypes[indexPath.row].rawValue)

            let date = Date()
            let calendar = Calendar.current
            let arriveTime = date.addingTimeInterval(TimeInterval(cellData.time))
            let component = calendar.dateComponents([.hour, .minute, .second], from: arriveTime)

            if let hour = component.hour , let min = component.minute {
                let hourStr = hour > 0 ? String(hour) + "시 " : ""
                let minStr = min > 0 ? String(min) + "분" : ""
                routeCell.arrivingTime.text =  hourStr+minStr == "" ? "" : hourStr+minStr+" 도착"
            }

        }
        
        return cell
    }
}

extension RouteSummaryViewController: UITableViewDelegate {
    
    func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
        return 75.0
    }
    
    func tableView(_ tableView: UITableView, willDisplay cell: UITableViewCell,
                   forRowAt indexPath: IndexPath) {
        
        if indexPath.row == 0 {
            tableView.selectRow(at: indexPath, animated: false,
                                scrollPosition: UITableViewScrollPosition.none)
        }
    }
    
    func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
       RxEventQueue.instance.post(event: RouteSelectEvent(index: indexPath.row))
    }
}

extension RouteSummaryViewController: UITextFieldDelegate {
    func textFieldShouldBeginEditing(_ textField: UITextField) -> Bool {

        if textField == self.waypointPlace {
            var latestLoc: GLatLng? = nil
            if let lastGpsLoc = GNavigationManager.instance.lastGpsLocation {
                latestLoc = GLatLng(lat: lastGpsLoc.coordinate.latitude, lng: lastGpsLoc.coordinate.longitude)
            }
            if let searchVC = createSearchVC(refLocation: latestLoc) {
                self.present(searchVC, animated: false, completion: nil)
            }
        }
        return false
    }
}
extension RouteSummaryViewController: GMapViewDelegate {
    public func mapView(_ mapView: GMapView!, didChange viewpoint: GViewpoint!,
                        withGesture gesture: Bool) {
        
        self.zoomLevel = Int32(floorf(viewpoint.zoom))
    }
}

extension RouteSummaryViewController: GGpsSignalDelegate {
    func didChangeGpsStatus(isGpsOn: Bool) {
        self.isGpsStatus = isGpsOn
    }
}
