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
import RxSwift
import roze
import gmaps
import geom
import Toast_Swift
import AVFoundation
import GLKit


class RoadGuiderPresenter: BaseMapPresenter {
    
    private static let routeStartZoom: Float = 13
    
    fileprivate var naviInfoVIew: NaviInfoView!
    private static let routePathWidth: Float = 7.0
    private static let pathColor = UIColor.red
    private static let passedFillPathColor = UIColor.lightGray
    private static let strokePathColor = UIColor.darkGray
    
    private var carMarker: GMarker?
    private var destMarker: GMarker?
    fileprivate var routePath: GRoute?
    
    fileprivate var navigationManager: GNavigationManager
    fileprivate var routeSummary: GRouteSummary {
        didSet {
            guard let route = routeSummary.activeRoute else {
                self.links = nil
                self.turns = nil
                logError("Active route is empty!!")
                return
            }
            
            self.links = route.links
            self.turns = route.turns
        }
    }
    
    private var markersList = [GMarker]()
    private var safetyMarkerList = [GMarker]()
    private var safetySpotMarkerGuidances: [GSafetySpotGuidance] = [GSafetySpotGuidance]() ///lazy로 바꿀 수 있지않을까?
    
    private var mapThemeUpdator: Timer?
    fileprivate var zoomChanger: ZoomChanger?
    fileprivate var lastLocation: GUtmk?
    
    fileprivate var indicator: UIActivityIndicatorView!
    fileprivate var alert: UIAlertController!
    fileprivate var timer = Timer()
    fileprivate var indicatorTime = 3

    fileprivate var tbtPathsWithArrows: (firstTbtPath: GPath?, secondTbtPath: GPath?) = (nil, nil)
    fileprivate var links: [GLink]?
    fileprivate var turns: [GTurn]?

    private var accidentMarkers = [GMarker]()

    //가장 최근에 didChange 이벤트를 받은 첫번째 TurnGuidance
    fileprivate var firstTurnGuidance: GTurnGuidance?
    
    unowned fileprivate var soundEventHandler: GSoundGuidanceHandler
    
    override var pivot: CGPoint {
        guard let mapViewController = mapViewController else {
            return CGPoint(x: 0, y: 0)
        }
        
        return CGPoint(x:  mapViewController.mapWidth * 0.5, y: mapViewController.mapHeight * 0.8)
    }
    
    private var carMarkerImage: GImage? {
        guard let mapController = mapViewController else {
            return nil
        }
        
        let imageName = mapController.isGpsStatus ? "carMarker" : "current_location_off"
        if let image = UIImage(imageNameWithBundlePath: imageName) {
            return GImage(uiImage: image)
        }
        
        logError("Could not load gps routing image!")
        return nil
    }
    
    private var pathBufferWidth: Double {
        guard let mapController = mapViewController else {
            return 10.0
        }
        return Double(mapController.mapResolution * RoadGuiderPresenter.routePathWidth)
    }
    
    
    init(routeSummary: GRouteSummary, soundEventHandler: GSoundGuidanceHandler) {
        self.navigationManager = GNavigationManager.instance
        self.routeSummary = routeSummary
        self.soundEventHandler = soundEventHandler
    }
    
    override func start() {
        navigationManager.startRouting(routeSummary: routeSummary, routeDelegate: self)
        navigationManager.locationDelegate = self
        navigationManager.rerouteDelegate = self
        navigationManager.routeGuidanceDelegate = self
        navigationManager.remainEventDelegate = self
        navigationManager.soundGuidanceDelegate = soundEventHandler
        
        naviInfoVIew = NaviInfoView.instanceFromNib(name: "NaviInfoView") as! NaviInfoView
        naviInfoVIew.frame = mapViewController!.mapView.frame
        //mapViewController?.mapView.addSubview(naviInfoVIew)
        mapViewController?.view.addSubview(naviInfoVIew)
        
        setupOption()
        
        updateMapOverlays(routeSummary: routeSummary)
        
    }
    
    override func stop() {
        stopMapThemeUpdator()
        
        initMapView()
        if let naviView = naviInfoVIew {
            naviView.isHidden = true
        }
        
        naviInfoVIew.removeFromSuperview()
        naviInfoVIew = nil
    }
    
    override func didChangedGpsStatus(_ isOn: Bool) {
        if let marker = carMarker {
            guard let markerImage = carMarkerImage else {
                return
            }
            marker.icon = markerImage
        }
    }
    
    override func didChangedZoomLevel(_ zoomLevel: Int32) {
        routePath?.bufferWidth = pathBufferWidth
    }
    override func resumeToCurrentLocation() {
        var coord: GUtmk?
        var angle: Double = 0.0
        
        if let lastRouteLocation = navigationManager.lastRouteLocation {
            coord = lastRouteLocation.location
            angle = lastRouteLocation.angle
        } else {
            if let location = navigationManager.lastGpsLocation {
                coord = GUtmk(from: location)
                angle = location.course
            }
        }
        
        guard coord != nil else {
            return
        }
        
        let viewpointChange: GViewpointChange = GViewpointChange.builder()
            .rotate(to: Float(angle))
            //            .tilt(to: 0)
            .pan(to: coord)
            .pivot(pivot)
            .build()
        
        DispatchQueue.main.asyncAfter(deadline: DispatchTime.now() + 0.4) {
            self.mapViewController?.isUpdatableMapLocation = true
        }
        mapViewController?.moveMapTo(viewpointChange, duration: 500, animationType: .default)
    }
    /// 맵에 오버레이(path, markers)들을 초기화

    fileprivate func initMapView() {
        
        guard let mapController = mapViewController else {
            return
        }

        if carMarker != nil {
            mapController.mapView.remove(carMarker)
        }
        if !markersList.isEmpty {
            for marker in markersList {
                mapController.mapView.remove(marker)
            }
        }
        
        if !safetyMarkerList.isEmpty {
            for marker in safetyMarkerList {
                mapController.mapView.remove(marker)
            }
        }
        
        if routePath != nil {
            mapController.mapView.remove(routePath)
        }

        removeTurnArrows()
        
        if !accidentMarkers.isEmpty {
            for marker in accidentMarkers {
                mapController.mapView.remove(marker)
            }
        }
    }
    
    private func setupOption() {
        if Preferences.mapThemeOption == .auto {
            mapViewController?.theme = MapTheme.driveMapTheme
            startMapThemeUpdator()
        } else {
            mapViewController?.theme =
                Preferences.mapThemeOption == .dayOnly ? .dayDrive : .nightDrive
        }
        
        if Preferences.isMapAutoLevel {
            zoomChanger = ZoomChanger()
        }
        
        /// Todo: 주행 옵션들은 초기 setup뿐 아니라, 계속해서 observing하여 주행 중 옵션 변경 시 에도 적용 될 수 있도록 지원해야 한다!

    }
    //FIX_ME ::: overlays 제거
    func generic<T>(value: [T]) {
        guard let mapController = mapViewController else {
            return
        }
        let mapview = mapController.mapView
        for element in value {
            mapview!.remove(element as! GOverlay)
        }
    }
    
    fileprivate func updateMapOverlays(routeSummary: GRouteSummary) {
        
        initMapView()
        drawPath(route: routeSummary.activeRoute!)
        setupMarkers(routeSummary: routeSummary)
        
        if let accidents = navigationManager.accidentPOIList {
            updateAccidentMarker(accidents: accidents)
        }
        
        
    }
    /// 유고정보를 Map상에 표출
    private func updateAccidentMarker(accidents: [GAccident]) {
        guard let mapController = mapViewController else {
            return
        }
        
        
        for accident in accidents {
            guard let resName = accident.typeCode.accidentResourceName else {
                return
            }
            if let accidentMarker = createMarkerWithImage(markerResourceName: resName) {
                accidentMarker.position = accident.coord
                accidentMarker.iconSize = CGPoint(x: 30.0, y: 30.0)
                accidentMarkers.append(accidentMarker)
                mapController.addOverlay(accidentMarker)
            }
        }
    }
    
    override func didUpdatePivotLocation(_ location: GUtmk, rotation: Float) {
        updateCarMarker(position: location, rotation: rotation)
    }

    fileprivate func drawPath(route: GNavigationRoute) {
        guard let mapController = mapViewController else {
            return
        }
        
        routePath = GRoute(points: route.routePath,
                           bufferWidth: Double(mapController.mapView.getResolution() * RoadGuiderPresenter.routePathWidth))
        routePath!.fillColor = RoadGuiderPresenter.pathColor
        routePath!.passedFillColor = RoadGuiderPresenter.passedFillPathColor
        routePath!.strokeColor = RoadGuiderPresenter.strokePathColor
        routePath!.strokeWidth = 1
        mapController.addOverlay(routePath!)
    }
    
    
    private func setupMarkers(routeSummary: GRouteSummary) {
        guard let mapController = mapViewController else {
            return
        }
        guard let activeRoute = routeSummary.activeRoute else {
            return
        }
        
        
        // 자차 마커
        if let markerImage = carMarkerImage {
            carMarker = GMarker(options: ["icon": markerImage,
                                          "flat": true,
                                          "anchor": CGPoint(x: 0.5, y: 0.5),
                                          "iconSize": CGPoint(x: 80, y: 80)])
            mapController.addOverlay(carMarker!)
            
            if let lastGps = navigationManager.lastGpsLocation {
                carMarker?.position = GUtmk(from: lastGps)
                
                let viewpointChange: GViewpointChange = GViewpointChange.builder()
                    .pivot(pivot)
                    .pan(to: GUtmk(from: lastGps))
                    .zoom(to: 12)
                    .rotate(to: Float(lastGps.course >= 0 ? lastGps.course : 0))
                    .build()
                mapController.moveMapTo(viewpointChange, duration: 500)
            }
        }
        
        // 경유지 마커
        if !routeSummary.routePlan.waypoints.isEmpty {
            let waypoints = routeSummary.routePlan.waypoints.flatMap({$0})
            for waypoint in waypoints {
                if let marker = createMarkerWithImage(markerResourceName: "waypointPin") {
                    marker.anchor = CGPoint(x: 0.5, y: 1.0)
                    marker.iconSize = CGPoint(x: 30.0, y: 49.0)
                    marker.position = waypoint
                    marker.visible = true
                    
                    markersList.append(marker)
                    mapController.addOverlay(marker)
                }
            }
        }
        // 목적지 마커
        if let destMarker = createMarkerWithImage(markerResourceName: "destPin") {
            destMarker.anchor = CGPoint(x: 0.5, y: 1.0)
            destMarker.iconSize = CGPoint(x: 30.0, y: 49.0)
            destMarker.position = activeRoute.routePath.last
            destMarker.visible = true
            markersList.append(destMarker)
            mapController.addOverlay(destMarker)
        }
    }
    
    fileprivate func setSpotMarkers(isShow: Bool, safetySpotGuidances: [GSafetySpotGuidance]) {
        
        if isShow {
            addMarkerOnTheMap(safetySpotCuidances: safetySpotGuidances)
        } else {
            removeSafetySpotMarkers(safetySpotGuidances: safetySpotGuidances)
        }
    }
    
    /// 기존에 추가된 마커들을 이용하여 새로 추가되어야 하는 guidance정보만 추출
    private func addMarkerOnTheMap(safetySpotCuidances: [GSafetySpotGuidance]) {
        
        let newSafetyGuidance = safetySpotCuidances.flatMap { (item) -> GSafetySpotGuidance? in
            if !safetyMarkerList.contains(where: {item.safetySpot.coord.isEqual($0.position)}) {
                return item
            }
            return nil
        }
        
        safetySpotMarkerGuidances.append(contentsOf: newSafetyGuidance)
        for newGuidance in newSafetyGuidance {
            guard let resid = newGuidance.safetySpot.type.markerResourceName else {
                return
            }
            if let newMarker = createMarkerWithImage(markerResourceName: resid) {
                newMarker.position = newGuidance.safetySpot.coord
                newMarker.iconSize = CGPoint(x: 30.0, y: 30.0)
                safetyMarkerList.append(newMarker)
                mapViewController!.addOverlay(newMarker)
            }
        }
        
    }
    
    
    /// 마커생성
    private func createMarkerWithImage(markerResourceName: String) -> GMarker? {
        
        guard let iconImage = GImage(uiImage: UIImage(imageNameWithBundlePath: markerResourceName)) else {
            return nil
        }
        
        let marker = GMarker(options: ["icon" : iconImage])!
        marker.anchor = CGPoint(x: 0.5, y: 0.5)
        return marker
    }
    
    private func removeSafetySpotMarkers(safetySpotGuidances: [GSafetySpotGuidance]) {
        
        guard !safetySpotGuidances.isEmpty else {
            return
        }
        
        // 현재 마커리스트 중 safetySpotGuidances랑 위치가 동일한 마커(제거되야할 마커)를 따로 추출
        let delMarkerList = safetySpotGuidances.flatMap { (item) -> GMarker? in
            if let index = safetyMarkerList.index(where: {item.safetySpot.coord.isEqual($0.position)}) {
                return safetyMarkerList[index]
            }
            return nil
        }
        
        
        removeOverlays(overLayList: delMarkerList)
        
        for marker in delMarkerList {
            if let index = safetyMarkerList.index(of: marker) {
                safetyMarkerList.remove(at: index)
            }
        }
    }
    
    func removeOverlays(overLayList: [GMarker]) {
        guard !overLayList.isEmpty else {
            return
        }
        
        for overlay in overLayList {
            //            mapViewController!.mapView.remove(overlay)
            mapViewController!.removeOverlay(overlay)
        }
    }
    
    fileprivate func updateCarMarker(position: GUtmk, rotation: Float) {
        guard let mapController = mapViewController else {
            return
        }
        
        if carMarker == nil {
            if let markerImage = carMarkerImage {
                carMarker = GMarker(options: ["icon": markerImage,
                                              "flat": true,
                                              "anchor": CGPoint(x: 0.5, y: 0.5),
                                              "iconSize": CGPoint(x: 80, y: 80)])
                mapController.addOverlay(carMarker!)
            }
        }
        
        carMarker!.position = position
        carMarker!.rotation = rotation
        routePath?.splitCoord = position
    }
    
    fileprivate func updateLocation(utmk: GUtmk, course: Float) {
        guard let mapController = mapViewController else {
            return
        }
        
        lastLocation = utmk
        
        let viewpointChange: GViewpointChange = GViewpointChange.builder()
            .pivot(pivot)
            .tilt(to: 60)
            .pan(to: utmk)
            .rotate(to: course)
            .build()
        mapController.moveMapTo(viewpointChange,
                                duration: BaseMapPresenter.locationUpdateAnimationDuration,
                                animationType: .linear)
    }
    
    fileprivate func updateSpeed(speed: Double) {
        guard speed >= 0 else {
            return
        }
        
        let speed = GDistanceFormat.ms2kmh(speed)
        naviInfoVIew.speed.attributedText = setStrokeText(inputString: String(format: "%.0f", round(speed)))
//        naviInfoVIew.speed.text = String(format: "%.0f", round(speed))
        zoomChanger?.speed = speed
    }
    
    fileprivate func requestReroute(mode: GRouteMode) {
        navigationManager.reroute(mode: mode)
    }
    
    @objc fileprivate func decreaseTimer() {
        indicatorTime -= 1
        let okaction = alert.actions[1]
        
        if indicatorTime <= 0 {
            timer.invalidate()
            indicator.stopAnimating()
            mapViewController?.dismiss(animated: true, completion: nil)
        } else {
            okaction.setValue("OK \(indicatorTime)초", forKey: "title")
        }
    }
    
    private func startMapThemeUpdator() {
        let updateDate = mapViewController?.theme == .dayDrive ? MapTheme.nightStartDate : MapTheme.dayStartDate
        //        let updateDate = Date().addingTimeInterval(5)
        
        mapThemeUpdator = Timer(fireAt: updateDate,
                                interval: 0,
                                target: self,
                                selector: #selector(updateMapTheme),
                                userInfo: nil,
                                repeats: false)
        if let mapThemeUpdator = mapThemeUpdator {
            RunLoop.main.add(mapThemeUpdator, forMode: RunLoopMode.commonModes)
        }
    }
    
    private func stopMapThemeUpdator() {
        mapThemeUpdator?.invalidate()
        mapThemeUpdator = nil
    }
    
    @objc private func updateMapTheme() {
        self.mapViewController?.theme = .nightDrive
    }
    
    /// RoutePath에 turn arrow 표시
    fileprivate func updateTbtOnthePath(turnGuidances: [GTurnGuidance]) {
        let turnGuidanceCount = turnGuidances.count
        guard turnGuidanceCount > 0 else {
            return
        }
        
        removeTurnArrows()

        tbtPathsWithArrows.firstTbtPath = drawTbtOnthePath(turnLinkIndex: turnGuidances[0].linkIndex)
        
        if turnGuidanceCount > 1 {
            tbtPathsWithArrows.secondTbtPath = drawTbtOnthePath(turnLinkIndex: turnGuidances[1].linkIndex)
            mapViewController?.mapView.add(tbtPathsWithArrows.secondTbtPath)
        }
        
        mapViewController?.mapView.add(tbtPathsWithArrows.firstTbtPath)
    }
}

// MARK: - Turn Arrow
extension RoadGuiderPresenter {
    
    fileprivate func removeTurnArrows() {
        if let arrow = tbtPathsWithArrows.firstTbtPath {
            mapViewController?.removeOverlay(arrow)
            tbtPathsWithArrows.firstTbtPath = nil
        }
        
        if let arrow = tbtPathsWithArrows.secondTbtPath {
            mapViewController?.removeOverlay(arrow)
            tbtPathsWithArrows.secondTbtPath = nil
        }
    }
    
    /// map에 새로운 GPath 추가
    ///
    /// - Parameter turnLinkIndex: 추가되야 할 turn linkIndex
    /// - Returns: 신규로 생성해서 지도에 붙인 tbtPath
    fileprivate func drawTbtOnthePath(turnLinkIndex: Int) -> GPath? {
        guard let turn = turns?.first(where: {$0.linkIndex == turnLinkIndex}) else {
            return nil
        }
        
        if !isValidArrowTurnType(turn.type) {
            return nil
        }
        guard let tbtPath = createArrowPathAtTurn(turn) else {
            return nil
        }
        
//        mapViewController?.mapView.add(tbtPath)
        return tbtPath
    }
    
    private func isValidArrowTurnType(_ type: GRGType) -> Bool {
        switch type {
        case .startingPoint, .destination, .goStrait, .tg, .ic, .jc:
            return false
        default:
            return true
        }
    }
    
    /// rp상에 포함된 turn정보를 지도경로상에 표현할 tbt overlay로 생성
    ///
    /// - Parameter turn : tbt overlay를 생성할 turn정보
    /// - Return tbt overlay

    private func createArrowPathAtTurn(_ turn: GTurn) -> GPath? {
        guard let points = getPathPointsAtTurn(turn: turn) else {
            return nil
        }
        
        let mapViewResolution = (mapViewController?.mapView.getResolution())!

        let bufferWidth = (mapViewController?.mapView.viewpoint.zoom)! >= Float(11.0) ? mapViewResolution*6.0 : mapViewResolution*8.0
        
        if let arrow = GPath(points: points, bufferWidth: Double(bufferWidth)) {
            arrow.hasArrow = true
            arrow.strokeColor = UIColor(red: 0.0, green: 0.0, blue: 0.0, alpha: 200.0/255.0)
            arrow.strokeWidth = 2
            arrow.fillColor = UIColor.white
            arrow.hasLineCap = false
            arrow.visible = (mapViewController?.mapView.viewpoint.zoom)! >= Float(10)
            return arrow
        }
        
        return nil
    }
    
    /// tbt overlay 좌표s 생성한다.
    /// links 에서 입력된 turn에 해당하는 tbt overlay를 생성할 수 있도록 좌표s를 반환
    ///
    /// - Parameter turn: tbt overlay를 생성할 turn정보
    /// - Returns: 좌표s
    private func getPathPointsAtTurn(turn: GTurn) -> [GUtmk]? {
        guard let links = links else {
            return nil
        }

        // tbt를 생성할 수 없을 때 nil 리턴
        guard links.count > (turn.linkIndex + 1) else {
            return nil

        }
        
        guard let mapView = mapViewController?.mapView else {
            return nil
        }
        
        let mapResolution = mapView.getResolution()
        let length = (mapView.viewpoint.zoom < 11.0 ? 30.0 : 60.0) * mapResolution

        
        /// Todo: 여기 코드는 나중에 다시 보자... 일단 skip
//        if turn.type == .uturn {
//            let uturnLength = links[turn.linkIndex + 1].length
//            if uturnLength < 30 {
//                //uturn이기 때문에 링크 전체를 설정
//                coords += links[turn.linkIndex + 1].nodes
//                return makeTbtPathPoint(coords: coords,
//                                        frontLinkIndex: turn.linkIndex + 2,
//                                        backLinkIndex: turn.linkIndex,
//                                        pathInterval: maxLength/2)
//            }
//        }

        //기준이 될 좌표 설정
        return makeTbtPathPoint(coord: links[turn.linkIndex].nodes.last!,
                                frontLinkIndex: turn.linkIndex + 1,
                                backLinkIndex: turn.linkIndex,
                                pathInterval: Int(length/2))
    }
    
    /// tbt overlay point 구성
    /// tbt 발생위치를 기준으로 이전링크 앞링크를 interval 에 맞추어 검색하여 point를 추가
    ///
    /// - Parameter
    ///     - coords : 화살표의 중심점 좌표
    ///     - frontLinkIndex : 앞 링크 인덱스
    ///     - backLinkIndex : 이전 링크 인덱스
    ///     - pathInterval : 앞/뒤 화살표의 길이
    /// - Return tbt overlay point list
    private func makeTbtPathPoint(coord: GUtmk,
                                  frontLinkIndex: Int,
                                  backLinkIndex: Int,
                                  pathInterval: Int) -> [GUtmk] {
        var pathCoords = [ coord ]
        
        //꼬리 부분 생성후 list의 앞에 추가
        pathCoords.insert(contentsOf: makeTbtArrowBackPointList(linkIndex: backLinkIndex, interval: pathInterval), at: 0)
        //화살표 부분 생성후 뒤에 이어서 추가
        pathCoords.append(contentsOf: makeTbtArrowFrontPointList(linkIndex: frontLinkIndex, interval: pathInterval))
        
        return pathCoords
    }
    
    /// TBT overlay 화살표 부분 생성 path point 구성
    ///
    /// 주의!: 링크의 첫번째 노드 제외, 마지막 노드 포함(for 중복 노드 제외)
    ///
    /// - Parameter
    ///     - linkIndex : tbt 위치 linkindex
    ///     - interval : tbt overlay 화살표 부분 길이
    /// - Return 좌표s
    private func makeTbtArrowFrontPointList(linkIndex: Int, interval: Int) -> [GUtmk] {
        guard let links = self.links else {
            return [GUtmk]()
        }
        
        var pathCoords = [GUtmk]()
        var interval = interval
        var linkIndex = linkIndex
        
        while linkIndex < links.count, interval > links[linkIndex].length {
            //링크의 전체 노드 추가
            let nodes = ArraySlice(links[linkIndex].nodes).dropFirst() // 첫번째 노드 제외
            pathCoords.append(contentsOf: nodes)
            
            //남은길이의 링크의 길이만큼 제외
            interval -= Int(links[linkIndex].length)
            linkIndex += 1
        }
        
        // 더 이상 링크 없으면 계산 없이 리턴
        if linkIndex == links.count {
            return pathCoords
        }
        
        var nodes = ArraySlice(links[linkIndex].nodes)
        var point1 = nodes.first!
        var point2 = nodes.last!
        nodes = nodes.dropFirst() // 첫번째 노드 제외
        for node in nodes {
            point2 = node
            let distance = point1.distance(to: point2)
            
            // 남은 길이보다 길 경우 반복문 종료
            if interval < Int(distance) {
                break
            }
            
            // 노드를 추가
            pathCoords.append(point2)
            
            // 남은 길이에서 노드간 거리를 제외
            interval -= Int(distance)
            point1 = point2
        }
        
        // 반복문이 종료된 후 남은 길이가 있는경우 노드 방향으로 남은거리만큼의 임의의 노드를 계산
        if interval > 0 {
            let angle = Int(point2.angle(to: point1))
            
            //진행방향의 끝으로 지정
            pathCoords.append(getPointOverLineDistance(angle: angle, frompt: point1, interval: interval))
        }
        
        return pathCoords
    }
    
    /// TBT overlay 꼬리 부분 생성 path point 구성
    ///
    /// 주의!: 링크의 마지막 노드 제외, 첫번째 노드 포함(for 중복 노드 제외)
    ///
    /// - Parameter
    ///     - linkIndex: tbt 위치 linkindex
    ///     - interval: tbt overlay 화살표 부분 길이
    /// - Return 좌표s
    private func makeTbtArrowBackPointList(linkIndex: Int, interval: Int) -> [GUtmk] {
        guard let links = self.links else {
            return [GUtmk]()
        }
        
        var interval = interval
        var linkIndex = linkIndex
        var pathCoords = [GUtmk]()
        
        //꼬리 좌표 추가
        //node초기 비교 노드 설정
        
        
        //링크 전체가 tbt구간에 포함되는 경우 반복문으로 확인처리
        while linkIndex > 0, interval > links[linkIndex].length {
            //링크의 전체노드 추가
            let nodes = ArraySlice(links[linkIndex].nodes).dropLast() // 마지막 노드 제외
            pathCoords.insert(contentsOf: nodes, at: 0)
            
            //남은길이를 링크의 길이만큼 제외
            interval -= Int(links[linkIndex].length)
            linkIndex -= 1
        }
        
        // 더 이상 링크 없으면 계산 없이 리턴
        if linkIndex == 0 {
            return pathCoords
        }
        
        //tbt가 링크 일부분만 구성되는 경우 처리
        var nodes = ArraySlice(links[linkIndex].nodes.reversed())
        var point1 = nodes.first!
        var point2 = nodes.last!
        nodes = nodes.dropFirst() // 마지막 노드 제외
        for node in nodes {
            point2 = node
            
            let distance = point1.distance(to: point2)
            
            // 남은 길이보다 길 경우 반복문 종료
            if interval < Int(distance) {
                break
            }

            // 노드를 추가
            pathCoords.append(point2)
            
            // 남은 길이에서 노드간 거리를 제외
            interval -= Int(distance)
            point1 = point2
        }
        
        // 반복문이 종료된 후 남은 길이가 있는경우 노드 방향으로 남은거리만큼의 임의의 노드를 계산
        if interval > 0 {
            let angle = Int(point2.angle(to: point1))
            
            //진행방향의 끝으로 지정
            pathCoords.append(getPointOverLineDistance(angle: angle, frompt: point1, interval: interval))
        }
        
        return pathCoords
        
    }
    /// 기준 좌표에서 angle방향으로 interval 만큼의 거리의 좌표를 반환
    ///
    /// - Parameter
    ///     - angle : 진행 각도
    ///     - frompt : 기준 좌표
    ///     - interval : 거리(m)
    /// - Return 계산된 좌표
    private func getPointOverLineDistance(angle: Int, frompt: GUtmk, interval: Int) -> GUtmk {
        let radians = Double(GLKMathDegreesToRadians(Float(angle)))
        let x = frompt.x + Double(interval) * sin(radians)
        let y = frompt.y + Double(interval) * cos(radians)
        
        return GUtmk(x: x, y: y)
    }
}

// MARK: - RerouteDelegate
extension RoadGuiderPresenter: GRerouteDelegate {
    
    func didBeginReroute(mode: GRouteMode) {
        switch mode {
        case .deviatedReroute:
            if let err = navigationManager.requestRoutingSound(for: .deviationReroute) {
                logError("Error while requesting deviation rerouting sound : \(err.description)")
            }
        case .autoReroute:
            if let err = navigationManager.requestRoutingSound(for: .trafficReroute) {
                logError("Error while requesting traffic rerouting sound : \(err.description)")
            }
        default:
            return
        }
    }
    
    func didEndReroute(mode: GRouteMode, routeSummary: GRouteSummary) {
        guard let route = routeSummary.activeRoute else {
            // 발생해서는 안되는 상황임!!
            /// Todo: 실패한 상황에서는 tracking으로 전환 또는 reroute 해야지...
            return
        }
        
        self.routeSummary = routeSummary
        naviInfoVIew.layoutSubviews()
        routePath?.points = route.routePath
        updateMapOverlays(routeSummary: routeSummary)
    }
    
    func didFailReroute(mode: GRouteMode, error: GNavigationError) {
        if let mapController = mapViewController {
            var style = ToastStyle()
            style.backgroundColor = UIColor.gray.withAlphaComponent(0.8)
            mapController.view.makeToast("경로 재탐색에 실패 하였습니다. 다시 요청 합니다.", duration: 2.0, position: .center, style: style)
            
        }
        
        navigationManager.reroute(mode: mode)
    }
    
}

// MARK: - RouteDelegate
extension RoadGuiderPresenter: GRouteDelegate {
    
    /// 경로 안내 시작
    public func didStartRoute() {
        if let mapController = mapViewController {
            var style = ToastStyle()
            style.backgroundColor = UIColor.gray.withAlphaComponent(0.8)
            mapController.view.makeToast("경로 안내를 시작합니다.", duration: 2.0, position: .center, style: style)
            naviInfoVIew.isHidden = false
        }
        
        if let err = navigationManager.requestRoutingSound(for: .start) {
            logError("Error while requesting traffic rerouting sound : \(err.description)")
        }
    }
    
    /// 경로 안내 시작 실패
    public func didFailRouteStart(error: roze.GNavigationError) {
        if let mapController = mapViewController {
            var style = ToastStyle()
            style.backgroundColor = UIColor.gray.withAlphaComponent(0.8)
            mapController.view.makeToast("경로 안내를 시작할 수 없습니다.", duration: 2.0, position: .center, style: style)
        }
    }
    
    /// 교통정보 업데이트   
    public func didUpdateTraffic() {
        
    }
    
    /// 목적지, 경유지 도착
    /// - parameter index: 최종 목적지(-1), 경유지 index(0~)
    public func didArrived(index: Int) {
        if let mapController = mapViewController {
            var style = ToastStyle()
            style.backgroundColor = UIColor.gray.withAlphaComponent(0.5)
            mapController.view.makeToast((index == -1 ? "목적지에 도착 하였습니다." : "경유지에 도착 하였습니다.\n다음장소로 이동합니다."),
                                         duration: 3.0,
                                         position: .center,
                                         style: style)
            if index == -1 {
                if naviInfoVIew != nil {
                    naviInfoVIew.isHidden = true
                    RxEventQueue.instance.post(event: ScreenTypeEvent(screenMode: .tracking))
                    
                    initMapView()
                }
                //목적지 도착 시 로그파일 저장
                navigationManager.saveLocationLog()
            }  else {
                naviInfoVIew.hideArriveNearWaypoint()
                
            }
            
        }
        if index == -1 {
            if let err = navigationManager.requestRoutingSound(for: .end) {
                logError("Error while requesting traffic rerouting sound : \(err.description)")
            }
        }
    }
    
    /// 경로 이탈
    public func didDeviateRoute(location: CLLocation) {
        if let mapController = mapViewController {
            var style = ToastStyle()
            style.backgroundColor = UIColor.gray.withAlphaComponent(0.5)
            mapController.view.makeToast("경로를 이탈하여 재탐색 합니다.", duration: 2.0, position: .center, style: style)
        }
        
        requestReroute(mode: .deviatedReroute)
        soundEventHandler.clear()
    }
    
    /// 경로 진입 실패
    public func didNotEnterRoute(location: CLLocation) {
        if let mapController = mapViewController {
            var style = ToastStyle()
            style.backgroundColor = UIColor.gray.withAlphaComponent(0.5)
            mapController.view.makeToast("경로에 진입하지 못하여 재탐색 합니다.", duration: 2.0, position: .center, style: style)
        }
        
        requestReroute(mode: .didNotEnterReroute)
        soundEventHandler.clear()
    }
    
    
}

// MARK: - LocationDelegate
extension RoadGuiderPresenter: GLocationDelegate {
    
    func didUpdateLocation(location: GGeoLocation) {
        
        var utmk: GUtmk
        var course: Float
        if let routeLocation = location.routeLocation { // 최초 경로 진입 후에는 nil이 될 수 없음
            utmk = routeLocation.location
            course = Float(routeLocation.angle)
        } else { // 최초 경로 진입 전인 경우
            guard let gpsLocation = location.location else {
                logError("GPS location not found!")
                return
            }
            
            utmk = GUtmk(from: gpsLocation)
            course = Float(gpsLocation.course)
        }
        
        if (mapViewController?.isUpdatableMapLocation)! {
            updateLocation(utmk: utmk, course: course)
        } else {
            updateCarMarker(position: utmk, rotation: course)
        }
        
        if let gpsLocation = location.location {
            updateSpeed(speed: gpsLocation.speed)
        }
    }
    
}

// MARK: - RemainEventDelegate
extension RoadGuiderPresenter: GRemainEventDelegate {
    /// 목적지 까지 남은 시간 / 거리 정보를 전달
    func didChangeRemain(remainGuidance: GRemainGuidance) {
        naviInfoVIew.isHidden = false
        naviInfoVIew.updateRemain(remainGuidance: remainGuidance)
        
    }
}

// MARK: - RouteGuidanceDelegate
extension RoadGuiderPresenter: GRouteGuidanceDelegate {
    
    /// TBT 정보를 전달
    func didChangeTurn(turnGuidances: [GTurnGuidance]) {
        firstTurnGuidance = turnGuidances.first
        naviInfoVIew.didChangeTurn(turnGuidances: turnGuidances)
        updateTbtOnthePath(turnGuidances: turnGuidances)
    }
    
    /// 현재 위치에서 onTurnChangedEvent 전달된 첫 번째 TBT 까지의 거리를 전달
    func didChangeTurnDistance(distance: Int32) {
        
        // TODO: 변경된 남은거리에 해당하는 TurnGuidance 가 함께 전달되지 않으므로
        // 거리 변경 이벤트에서 turn 의 종류에 따른 분기를 타고 싶다면 아래와 같이 delegate 가 최근에 받은 TurnGuidance 객체를 참조하고 있어야 한다.
        // API 변경을 고려해볼 필요가 있다.
        guard let turnGuidance = firstTurnGuidance else {
            return
        }
        naviInfoVIew.didChangeTurnDistance(distance: distance, for: turnGuidance)
        
        guard let utmk = self.lastLocation, (mapViewController?.isUpdatableMapLocation)! else {
            return
        }
        
        zoomChanger?.updateTurnDistance(distance, zoomChanger: { (zoom, tilt) in
            let viewpointChange: GViewpointChange = GViewpointChange.builder()
                .pivot(self.pivot)
                .zoom(to: zoom)
                .tilt(to: tilt)
                .pan(to: utmk)
                .build()
            self.mapViewController?.moveMapTo(viewpointChange,
                                              duration: BaseMapPresenter.locationUpdateAnimationDuration,
                                              animationType: .linear)
        })
    }
    
    /// RoadView
    func didChangeRoadView(url: String?) {
        naviInfoVIew.roadImageView.isHidden = true
        if let urlstr = url {
            naviInfoVIew.updateRoadView(url: urlstr)
            naviInfoVIew.roadImageView.isHidden = false
        }
    }
    
    /// Lane변경
    func didChangeLane(lane: GLane?) {
        naviInfoVIew.updateLanePannel(lane: lane)
    }
    
    /// Lane 거리 변경 시 여기서
    /// distance : 차선정보 해제거리
    func didChangeLaneDistance(distance: Int32) {
        naviInfoVIew.didChangeLaneDistance(distance: distance)
    }
    
    /// 고속도로 정보변경 시 전달, 고속도로에서 빠져나온 경우 nil 이 1번 전달
    /// highwayGuidances 고속도로 정보 List
    func didChangeHighway(highwayGuidances: [GHighwayGuidance]?) {
        naviInfoVIew.updateHighwayView(guidances: highwayGuidances)
    }
    
    /// onHighwayChangedEvent에서 전달된 List의 첫번째 안내점의 위치 정보를 반환한다.
    /// distance 현재 위치에서 첫번째 안내점 까지의 거리(m)
    func didChangeHighwayDistance(distance: Int32) {
        naviInfoVIew.updateHighwayViewDistance(distance: distance)
    }
    
    /// 구간단속 카메라 정보를 전달한다.
    func didChangeIntervalSafetySpot(intervalGuidance: GIntervalSpeedSpotGuidance?) {
        naviInfoVIew.onIntervalSafetySpotChangedEvent(intervalGuidance: intervalGuidance)
    }
    
    /// SafetySpot 정보를 전달한다.
    func didChangeSafetySpot(isShow: Bool, safetySpotGuidances: [GSafetySpotGuidance]) {
        
        setSpotMarkers(isShow: isShow, safetySpotGuidances: safetySpotGuidances)
        naviInfoVIew.updateSafeSpotView(isShow: isShow, safetySpotGuidances: safetySpotGuidances)
    }
    
    /// 최저가 주유소 정보를 전달한다.
    func didChangeLowestGasStation(isShow: Bool, oilPriceGuidances: [GOilPriceGuidance]) {
        
    }
    
    /// 현재 최저가 주유소가 표시되고 있는 경우, 목록의 첫번째 주유소 안내점까지 거리가 update된다.
    func didChangeLowestGasStationDistance(distance: Int32) {
        
    }
    
    /// 경유지 근방에 진입 했을 때 해당 경유지 인덱스를 전달한다.
    func willArriveNearWaypoint(waypointIndex: Int) {
        naviInfoVIew.willArriveNearWaypoint(waypointIndex: waypointIndex)
    }
    
}



