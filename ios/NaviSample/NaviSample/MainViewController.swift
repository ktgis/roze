/*
 * Copyright (c) 2017 kt corp. All rights reserved.
 *
 * This is a proprietary software of kt corp, and you may not use this file
 * except in compliance with license agreement with kt corp. Any redistribution
 * or use of this software, with or without modification shall be strictly
 * prohibited without prior written approval of kt corp, and the copyright
 * notice above does not evidence any actual or intended publication of such
 * software.
 */


import Foundation
import UIKit
import geom
import roze
import gmaps
import RxSwift



class MainViewController: UIViewController {
    
    @IBOutlet weak var searchBar: UISearchBar!
    @IBOutlet weak var mapView: GMapView!

    @IBOutlet weak var currentLocationbtn: UIButton!
    
    var isUpdatableMapLocation = true
    fileprivate var isFixedCurrentLocation = false
    /// 지도 상 pivot의 마지막 좌표
    fileprivate var lastPivotLocation: GUtmk? {
        didSet {
            if let mapPresenter = mapPresenter, let location = lastPivotLocation {
                mapPresenter.didUpdatePivotLocation(location, rotation: lastMapRotation)
            }
        }
    }
    /// 지도의 마지막 회전 각도
    fileprivate var lastMapRotation: Float = 0.0
    
    fileprivate var routesummary: GRouteSummary?
    var placeData: SearchPlaceData?

    private var screenModeSubscription: Disposable?
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
    
    var theme: MapTheme = .mapDefault {
        didSet {
            if theme != oldValue {
                guard let stylePath = theme.stylePath else {
                    logError("Could not find theme file!")
                    return
                }

                mapView.setStyleAndUpdateCurrentLayer(stylePath)
            }
        }
    }
    
    var mapPresenter: MapPresenter?
    
    let soundEventHandler = GSoundGuidanceHandler()
    fileprivate var timer = Timer()
    
    override func viewDidLoad() {
        super.viewDidLoad()

        mapPresenter = TrackingMapPresenter()
        mapPresenter?.mapViewController = self
        mapPresenter?.start()
        
        mapView.delegate = self as GMapViewDelegate
        zoomLevel = Int32(floor(mapView.viewpoint.zoom))
        
        GNavigationManager.instance.gpsSignalDelegate = self
        
        subscribeRxEvents()
    }
    deinit {
        unsubscribeRxEvents()
    }
    
    @IBAction func unwindToContainerVC(segue: UIStoryboardSegue) {
        
        searchBar.isHidden = false

        if segue.identifier == SearchViewController.unwindSegueIdentifier {
            print("unwind")
            //if segue.source is SearchViewController {
            if let searchVC = segue.source as? SearchViewController {
                self.placeData = searchVC.placeData
                DispatchQueue.main.async {
                    self.performSegue(withIdentifier: "RouteSummary", sender: self)
                }
                
            }
        }
    }
    
    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let controller = segue.destination as? RouteSummaryViewController {
            controller.placeData = placeData
            controller.delegate = self
        }
    }
    
    private func subscribeRxEvents() {
        let rxEventQueue = RxEventQueue.instance
        
        if screenModeSubscription == nil {
            screenModeSubscription = rxEventQueue.observe(eventType: RxEventType.screenTypeEvent)
                .observeOn(MainScheduler.instance)
                .subscribe(onNext: { event in
                    
                    if let event = event as? ScreenTypeEvent {
                        if let presenter = self.mapPresenter {
                            presenter.stop()
                        }
                        
                        switch event.screenMode {
                        case .tracking:
                            self.searchBar.isHidden = false
                            self.currentLocationbtn.isHidden = false
                            self.mapPresenter = TrackingMapPresenter()
//                        case .placeShowing:
//                            self.mapPresenter = PlaceMapPresenter(placeData: event.data as! SearchPlaceData)
//                        case .routeShowing:
//                            self.mapPresenter = RouteMapPresenter(routeSummary: event.data as! RouteSummary)
                        case .navigating:
                            self.currentLocationbtn.isHidden = true
                            self.mapPresenter = RoadGuiderPresenter(routeSummary: event.data as! GRouteSummary,
                                                                    soundEventHandler: self.soundEventHandler)
                        }
                        self.mapPresenter?.mapViewController = self
                        self.mapPresenter?.start()
                    }
                    
                })
        }
        
    }
    
    @IBAction func findCurrentPlace(_ sender: Any) {
//        (sender as! UIButton).isSelected = false
        logDebug(" --- find current place")
//        setCurrentLocation()
        decreaseTime()
//        mapView(mapView, didAnimationEnd: GAnimationState.complete)
    }
    
    
    func setCurrentLocation() {
        guard lastPivotLocation != nil else {
            return
        }
        
//        var coord: GUtmk?
//        var angle: Double = 0.0
        
        isUpdatableMapLocation = false
        self.currentLocationbtn.isHidden = true
        
       
//        let navigationManager = NavigationManager.instance
//        if let lastRouteLocation = navigationManager.lastRouteLocation {
//            coord = lastRouteLocation.location
//            angle = lastRouteLocation.angle
//        } else {
//            if let location = NavigationManager.instance.lastGpsLocation {
//                coord = GUtmk(from: location)
//                angle = location.course
//            }
//        }
//
//        guard coord != nil else {
//            return
//        }
        
       mapPresenter?.resumeToCurrentLocation()


    }
    
    /// 설정 화면을 push
    ///
    /// - Parameter sender: Any
    @IBAction func pushSettingsViewController(_ sender: Any) {
        logDebug(" --- navigate to settings..")
        let settingsSB = UIStoryboard.init(name: "Setting", bundle: nil)
        let vc = settingsSB.instantiateInitialViewController()
        if let settingsVC = vc as? SettingsViewController {
            self.navigationController?.pushViewController(settingsVC, animated: true)
        } else {
            logDebug("Can't instanciate SettingsViewController.")
        }
    }
    
    private func unsubscribeRxEvents() {
        screenModeSubscription?.dispose()
        screenModeSubscription = nil
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
    
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
    
    func scheduleTimer() {
        isUpdatableMapLocation = false
        
        timer.invalidate()
        timer = Timer.scheduledTimer(timeInterval: 3.0,
                                     target: self,
                                     selector: #selector(self.decreaseTime),
                                     userInfo: nil,
                                     repeats: false)
    }
    
    @objc private func decreaseTime() {
        timer.invalidate()
        self.setCurrentLocation()
    }
}

// MARK: - GMapViewDelegate
extension MainViewController: GMapViewDelegate {
    
    public func mapView(_ mapView: GMapView!, didChange viewpoint: GViewpoint!, 
                        withGesture gesture: Bool) {
        //temporary code, 맵초기화 시 맨 처음 didChange 이벤트가 main thread 가 아닌 곳에서 실행된다.
        // 동작은 하는 것 같지만 warning 이 뜬다.
        // mapSDK 에서 각 이벤트를 main thread 에서 발생되도록 보장하는 것이 맞다.
        //        DispatchQueue.main.async {
        self.zoomLevel = Int32(floorf(viewpoint.zoom))
        
        if gesture { // gesture로 움직였을때 처리할 동작이 있으면 여기서...
            /// Todo: 3초 후 현위치로 돌아오기 -> 지도를 움직였을때만
            self.scheduleTimer()
            self.currentLocationbtn.isHidden = false
            return
        }
        
        
        guard isUpdatableMapLocation else {
            return
        }
        // update location of a pivot.
        if let mapPresenter = self.mapPresenter {
            let pivotLocation: GCoord = mapView.coord(fromViewportPoint: mapPresenter.pivot)
            self.lastMapRotation = viewpoint.rotation
            self.lastPivotLocation = GUtmk.value(of: pivotLocation)
        }
        //        }
    }
    
    public func mapView(_ mapView: GMapView!, didAnimationEnd animationState: GAnimationState) {
//        if !isAbleToUpdateMarker {
//            isAbleToUpdateMarker = true
//        }
    }
}

// MARK: - GpsSignalDelegate
extension MainViewController: GGpsSignalDelegate {
    func didChangeGpsStatus(isGpsOn: Bool) {
        self.isGpsStatus = isGpsOn
    }
}

extension MainViewController: UISearchBarDelegate {
    
    func searchBarShouldBeginEditing(_ searchBar: UISearchBar) -> Bool {
        searchBar.resignFirstResponder()
        if let searchVC = createSearchVC(refLocation: viewpoint.center) {
            self.present(searchVC, animated: false, completion: nil)
        }
        return false
    }
        
}

extension MainViewController: RouteSummaryViewControllerDelegate {
    func passData(value: Any?) {
        if let routeSummary = value {
            searchBar.isHidden = true
            self.routesummary = routeSummary as? GRouteSummary
            RxEventQueue.instance.post(event: ScreenTypeEvent(screenMode: .navigating,
                                                              data: routeSummary))
        } else {
            searchBar.isHidden = false
            RxEventQueue.instance.post(event: ScreenTypeEvent(screenMode: .tracking,
                                                              data: nil))
        }
    }
}

