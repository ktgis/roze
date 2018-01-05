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
import gmaps
import roze
import geom

class RouteMapPresenter: BaseMapPresenter {
    /// BaseMapPresenter를 Extend하지만 사용은 하지않음
    /// UI변경과정에서 경로요약화면에서 새로운 지도를 띄우는데,
    /// mapview를 가진 mapViewcontroller가 MainViewcontroller 여서 사용안하기로함
    
    var routeMapViewController: RouteSummaryViewController?
    
    private static let routePathWidth: Float = 5.0
    private static let selectPathColor = UIColor.red
    private static let pathColor = UIColor.gray
    private static let boundsPadding: Int32 = 50
    
    private var routeSelectSubscription: Disposable?
    
    private let routeSummary: GRouteSummary
    private var routePaths = [GRoute]()
    private var startMarker: GMarker?
    private var endMarker: GMarker?
    
    var selectedRouteIndex: Int = 0 {
        didSet(oldValue) {
            if oldValue != selectedRouteIndex {
                updateRouteStyle()
            }
        }
    }
    
    private lazy var waypointMarkers: [GMarker]? = {
        return [GMarker]()
    }()
    
    private var pathBufferWidth: Double {
        guard let mapController = routeMapViewController else {
            return 10.0
        }
        return Double(mapController.mapResolution * RouteMapPresenter.routePathWidth)
    }
    
    init(routeSummary: GRouteSummary) {
        self.routeSummary = routeSummary
    }
    
    override func start() {
        subscribeRxEvents()
        
        showRoutes(routeSummary.routes)
        setMarkers(routePlan: routeSummary.routePlan)
        
    }
    
    override func stop() {
        unsubscribeRxEvents()
        
        if let marker = startMarker {
            routeMapViewController?.removeOverlay(marker)
        }
        
        if let marker = endMarker {
            routeMapViewController?.removeOverlay(marker)
        }
        
        if let markers = waypointMarkers {
            for marker in markers {
                routeMapViewController?.removeOverlay(marker)
            }
        }
        
        for path in routePaths {
            routeMapViewController?.removeOverlay(path)
        }
    }
    
    override func didChangedZoomLevel(_ zoomLevel: Int32) {
        setPathWidth()
    }
    
    private func subscribeRxEvents() {
        let eventQueue = RxEventQueue.instance
        
        if routeSelectSubscription == nil {
            routeSelectSubscription = eventQueue.observe(eventType: RxEventType.routeSelectEvent)
                .observeOn(MainScheduler.instance)
                .subscribe(onNext: { event in
                    if let event = event as? RouteSelectEvent {
//                        self.setPathColor(selectionIndex: event.index)
                        self.selectedRouteIndex = event.index
                    }
                })
        }
    }
    
    private func unsubscribeRxEvents() {
        routeSelectSubscription?.dispose()
        routeSelectSubscription = nil
    }
    
    private func updateRouteStyle() {
        for (i, path) in routePaths.enumerated() {
            if i == selectedRouteIndex {
                path.fillColor = RouteMapPresenter.selectPathColor
                routeMapViewController?.removeOverlay(path)
                routeMapViewController?.addOverlay(path)
            } else {
                path.fillColor = RouteMapPresenter.pathColor
            }
        }
    }
    
    private func setPathWidth() {
        for path in routePaths {
            path.bufferWidth = pathBufferWidth
        }
    }
    
    private func showRoutes(_ routes: [GNavigationRoute]) {
        guard !routes.isEmpty else {
            return
        }
        
        var totalBounds: GUtmkBounds?
        for route in routes {
            let routePath = route.routePath
            
            if let path = drawPath(routePath, fillColor: RouteMapPresenter.pathColor) {
                routePaths.append(path)

                let routeBounds = GUtmkBounds(coords: routePath)
                if let lastBounds = totalBounds {
                    totalBounds = lastBounds.union(with: routeBounds)
                } else {
                    totalBounds = routeBounds
                }
            }
        }
        
        updateRouteStyle()
        
//        totalBounds = totalBounds?.union(with: GUtmk(x: totalBounds!.left(),
//                                                     y: totalBounds!.bottom() - totalBounds!.height() * 0.8))

        let viewpointChangeFit: GViewpointChange = GViewpointChange.fit(totalBounds,
                                                                        padding: RouteMapPresenter.boundsPadding)
        
        routeMapViewController?.moveMapTo(viewpointChangeFit, duration: 1000)
        
    }

    private func drawPath(_ path: [GUtmk], fillColor: UIColor) -> GRoute? {
        guard let mapController = routeMapViewController else {
            return nil
        }
        let path: GRoute = GRoute(points: path, bufferWidth: pathBufferWidth)
        path.fillColor = fillColor
        mapController.addOverlay(path)
        
        return path
    }
    
    private func setMarkers(routePlan: GRoutePlan) {
        guard let mapController = routeMapViewController else {
            return
        }
        
        let startUtmk = routePlan.start
        let waypointUtmk =  routePlan.waypoints
        let endUtmk = routePlan.dests.first! 
        
        if startMarker == nil {
            let markerImage: GImage = GImage(uiImage: UIImage(imageNameWithBundlePath: "startPin"))
            startMarker = GMarker(options: ["icon": markerImage])
            mapController.addOverlay(startMarker!)
        }
        
        if waypointUtmk.count > 0 {
            
            for utmk in waypointUtmk {
                let markerImage: GImage = GImage(uiImage: UIImage(imageNameWithBundlePath: "waypointPin"))
                let marker = GMarker(options: ["icon": markerImage,
                                               "position": utmk])
                waypointMarkers?.append(marker!)
                mapController.addOverlay(marker!)
            }
        }
        
        if endMarker == nil {
            let markerImage: GImage = GImage(uiImage: UIImage(imageNameWithBundlePath: "destPin"))
            endMarker = GMarker(options: ["icon": markerImage])
            mapController.addOverlay(endMarker!)
        }
        
        
        startMarker!.options = ["position": startUtmk]
        endMarker!.options = ["position": endUtmk]
    
    }
    
}

