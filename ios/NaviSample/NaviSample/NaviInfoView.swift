//
//  NaviInfoView.swift
//  iRozeNavi
//
//  Created by 서유리 on 2017. 9. 20..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import UIKit
import roze
import gmaps
import Kingfisher



class NaviInfoView: UIView {
    private static let containerViewTagValue = 1
    private static let hipassLaneShowDistance = 500
    
    @IBOutlet weak var highwayView: HighwayView! {
        didSet {
            highwayView.isHidden = true
        }
    }
    @IBOutlet weak var tbtView: TBTView!
    @IBOutlet weak var speed: UILabel! {
        didSet {
            guard speed != nil else {
                return
            }
            speed.attributedText = setStrokeText(inputString: "0")
//            speed.text = "0"
        }
    }
    @IBOutlet weak var laneRemainDistance: UILabel! {
        didSet {
            guard laneRemainDistance != nil else {
                return
            }
            laneRemainDistance.text = "0"
        }
    }
    
    /// 일반 차선 정보 표시 (우회전 차선, 직진 차선 등)
    @IBOutlet weak var laneView: LaneView!
    
    /// hipass 차선 정보 표시
    @IBOutlet weak var hipassLaneView: LaneView!
    
    @IBOutlet weak var roadImageView: UIImageView!
    @IBOutlet weak var safetyView: SafetyView!
    
    @IBOutlet weak var remainTime: UILabel!
    @IBOutlet weak var remainDistance: UILabel!
    @IBOutlet weak var waypointView: UIView! {
        didSet {
            waypointView.isHidden = true    
        }
    }
    @IBOutlet weak var cancelbutton: UIButton!
    @IBOutlet weak var okbutton: UIButton! {
        didSet {
            okbutton.titleLabel?.text = "     "
        }
    }
    @IBOutlet weak var subMenuView: UIView!
    @IBOutlet weak var indicator: UIActivityIndicatorView! {
        didSet {
            indicator.isHidden = true
        }
    }
    public private(set) var wayPointIndex: Int = 0
    
    var laneViewHidden = true {
        didSet {
            laneView.isHidden = laneViewHidden
            laneRemainDistance.isHidden = laneViewHidden
            if !laneViewHidden {
                hipassLaneView.isHidden = true
            }
        }
    }
    var hipassLaneViewHidden = true {
        didSet {
            hipassLaneView.isHidden = hipassLaneViewHidden
            if !hipassLaneViewHidden {
                laneView.isHidden = true
                laneRemainDistance.isHidden = true
            }
        }
    }
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        
    }
    
    required init?(coder aDecoder: NSCoder) {
        //IB 를 위함
        super.init(coder: aDecoder)
        
    }
    
    class func instanceFromNib<T: UIView> (name:String) -> T {
        return UINib(nibName: name, bundle: nil).instantiate(withOwner: nil, options: nil)[0] as! T
    }
    
    override func layoutSubviews() {
        roadImageView.isHidden = true
        laneView.isHidden = laneViewHidden
        hipassLaneView.isHidden = hipassLaneViewHidden
        safetyView.isHidden = true
        highwayView.isHidden = true
        laneRemainDistance.isHidden = true
        self.subMenuView.isHidden = true
    } // "layoutSubviews" is best
    
    
    /// 목적지 까지 남은 시간, 남은 거리
    func updateRemain(remainGuidance: GRemainGuidance) {
        let timeInSecond = remainGuidance.remainTime
        let min = String((timeInSecond/60) % 60)
        let hour = String(timeInSecond/3600)
        let arrivalTime = (hour.count == 1 ? "0"+hour : hour) + ":" + (min.count == 1 ? "0"+min : min)
        let distance = changeDistanceMeasure(value: Int32(remainGuidance.remainDistance), measureType: .Kilometer)
        
        remainTime.text = arrivalTime
        remainDistance.text = distance
    }
    
    /// TBT뷰 업데이트
    func didChangeTurn(turnGuidances: [GTurnGuidance]) {
        tbtView.didChangeTurn(turnGuidances: turnGuidances)
        tbtView.isHidden = false
    }

    /// 현재 위치에서 onTurnChangedEvent 전달된 첫 번째 TBT 까지의 거리를 전달
    /// 가장 최근에 받은 didChangeTurn 이벤트의 첫번째 TurnGuidance (현재 보여지고 있는 1차 TBT) 가 함께 전달된다.
    /// 해당하는 TBT 가 TG 인 경우 hipass 차선 정보를 표시한다.
    func didChangeTurnDistance(distance: Int32, for guidance: GTurnGuidance) {
       tbtView.didChangeTurnDistance(distance: distance)
        
        // show hipass lane and toll info
        //hipassLaneView.isHidden = true
        hipassLaneViewHidden = true
        if guidance.turnCode == .tg,
            distance <= NaviInfoView.hipassLaneShowDistance,
            let hipassInfo = guidance.hipassLanes,
            let laneCnt = guidance.laneCnt {
            
            if hipassLaneView.updateHipassLanes(hipassLanes: hipassInfo, laneCnt: Int(laneCnt)) {
//                hipassLaneView.isHidden = false
                hipassLaneViewHidden = false
            }
        }
    }
    
    /// 로드뷰
    func updateRoadView(url: String) {
        let url = URL(string: url)!
        roadImageView.kf.setImage(with: url)
    }
    
    /// Lane뷰 업데이트
    func updateLanePannel(lane: GLane?) {
//        laneView.isHidden = true
//        laneRemainDistance.isHidden = true
        laneViewHidden = true
        guard let newlane = lane else {
            return
        }
        
        if laneView.updateLanePannel(lane: newlane) {
//            laneView.isHidden = false
            
//            laneRemainDistance.isHidden = false
            laneViewHidden = false
        }
    }
    
    func didChangeLaneDistance(distance: Int32) {
        if !laneView.isHidden {
            laneRemainDistance.isHidden = false
            laneRemainDistance.text = changeDistanceMeasure(value: distance, measureType: .Kilometer)
        }
    }
    func updateHighwayView(guidances: [GHighwayGuidance]? = nil) {
        
        if let gds = guidances {
            highwayView.setHighwayGuidances(guidance: gds)
            highwayView.isHidden = false
        } else {
            highwayView.isHidden = true
        }
    }
    
    func updateHighwayViewDistance(distance: Int32) {
        
        highwayView.updateDistance(distance: distance)
    }
    
    /// 안전운행 정보 표시
    /// - safetySpotGuidances: 표시 거리 이하로 들어온 모든 안전운행 안내점 정보
    func updateSafeSpotView(isShow: Bool, safetySpotGuidances: [GSafetySpotGuidance]) {
        
        if isShow {
            safetyView.updateSafetySpotView(safetySpotGuidances: safetySpotGuidances)
        }
        safetyView.isHidden = !isShow
        
    }
    
    /// 구간단속 정보 이벤트
    func onIntervalSafetySpotChangedEvent(intervalGuidance: GIntervalSpeedSpotGuidance? = nil) {
        
        guard let guidance = intervalGuidance else {
            safetyView.isHidden = true
            return
        }

        safetyView.isHidden = false
        safetyView.onIntervalSafetySpotChangedEvent(intervalGuidance: guidance)
        
    }
    @IBAction func saveRouteForTest(_ sender: Any) {
        UIView.animate(withDuration: 0.7) {
            self.subMenuView.isHidden = false
        }
    }
    @IBAction func didFinishNavigating(_ sender: Any) {
        

        GNavigationManager.instance.saveLocationLog()
        _ = GNavigationManager.instance.startTracking()
        self.makeToast("종료하고 저장함", duration: 2.0, position: .center)

        DispatchQueue.main.async {
            RxEventQueue.instance.post(event: ScreenTypeEvent(screenMode: .tracking))
            self.subMenuView.isHidden = true
        }

    }
    
    @IBAction func closePopup(_ sender: Any) {
        subMenuView.isHidden = true
    }
    
    @IBAction func requestReroute(_ sender: Any) {
        GNavigationManager.instance.reroute(mode: .userReroute)
        self.makeToast("경로를 재탐색합니다.", duration: 2.0, position: .center)
        subMenuView.isHidden = true
    }
    
    
    /// 입력된 값에 따라 safety 이미지 ResName을 반환
    func getLaneResource(safetyType: GRGType) -> String? {
        
        if let resName: String = safetyType.resourceName {
            return resName
        } else {
            return nil
        }
        
    }
    
    private var timer = Timer()
    private var indicatorTime = 3
    
    func hideArriveNearWaypoint() {
        UIView.animate(withDuration: 1.0, animations: {
            guard GNavigationManager.instance.rerouteForPassingWaypoint(waypointIndex: self.wayPointIndex,
                                                                       routemode: GRouteMode.userReroute) == nil else {
                                                                        return
            }
            
           
        }) { (hide) in
//            guard NavigationManager.instance.rerouteForPassingWaypoint(waypointIndex: 0,
//                                                                       routemode: RouteMode.userReroute) == nil else {
            self.stopTimerAndIndicator()
            self.indicator.isHidden = true
            self.waypointView.isHidden = true

        }
    }
    
//    func willArriveNearWaypoint(waypoints: [Waypoint]) {
    func willArriveNearWaypoint(waypointIndex: Int) {
//        guard waypoints.count > 0 else {
//            return
//        }
        wayPointIndex = waypointIndex
        UIView.animate(withDuration: 1.0, animations: {
            self.indicator.startAnimating()
            self.scheduleTimer()
        }) { (show) in
            self.indicator.isHidden = false
            self.waypointView.isHidden = false
        }

    }
    
    func scheduleTimer() {
        self.timer = Timer.scheduledTimer(timeInterval: 1.0,
                                          target: self,
                                          selector: #selector(self.decreaseTime),
                                          userInfo: nil,
                                          repeats: false)
    }
    
    @objc private func decreaseTime() {
        indicatorTime -= 1
        
        if indicatorTime <= 0 {
            hideArriveNearWaypoint()
            
        } else {
            okbutton.titleLabel?.text = "예 \(String(describing: indicatorTime))초"
            scheduleTimer()
        }
       
    }
    
    @IBAction func ok() {
        hideArriveNearWaypoint()
    }
    
    @IBAction func cancel(_ sender: Any) {
        stopTimerAndIndicator()
        self.indicator.isHidden = true
        self.waypointView.isHidden = true
    }
    
    func stopTimerAndIndicator() {
        self.timer.invalidate()
        indicator.stopAnimating()
    }
    
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        let view = super.hitTest(point, with: event)
        if let view = view {
            return view == self || view.tag == NaviInfoView.containerViewTagValue ? nil : view
        } else {
            return nil
        }
    }
}

