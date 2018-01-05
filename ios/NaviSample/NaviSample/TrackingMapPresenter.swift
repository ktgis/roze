//
//  TrackingMapPresenter.swift
//  iRozeNavi
//
//  Created by 서유리 on 2017. 9. 14..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import geom
import roze
import gmaps


class TrackingMapPresenter: BaseMapPresenter {

    private var navigationManager: GNavigationManager
    private var gpsMarker: GMarker?
    
    private var markerImage: GImage? {
        guard let mapController = mapViewController else {
            return nil
        }
        
        let imageName = mapController.isGpsStatus ? "current_location_on" : "current_location_off"
        if let image = UIImage(imageNameWithBundlePath: imageName) {
            return GImage(uiImage: image)
        }
        
        logError("Could not load gps tracking image!")
        return nil
    }
    
    
    override init() {
        navigationManager = GNavigationManager.instance
        super.init()
        
        navigationManager.locationDelegate = self
        _ = navigationManager.startTracking()
    }
    
    override func start() {
        if let lastGps = navigationManager.lastGpsLocation {
            updateGpsMarker(utmk: GUtmk(from: lastGps))
        }
        
        mapViewController?.theme = .mapDefault
    }
    
    override func stop() {
        if let marker = gpsMarker {
            mapViewController?.removeOverlay(marker)
        }
    }
    
    override func didChangedGpsStatus(_ isOn: Bool) {
        if let marker = gpsMarker {
            guard let markerImage = markerImage else {
                return
            }
            marker.icon = markerImage
        }
    }
    
    override func didUpdatePivotLocation(_ location: GUtmk, rotation: Float) {
        updateGpsMarker(utmk: location)
    }
    
    override func resumeToCurrentLocation() {
        var coord: GUtmk?
        var angle: Double = 0.0
        
        if let location = GNavigationManager.instance.lastGpsLocation {
            coord = GUtmk(from: location)
            angle = location.course
        }
        
        let viewpointChange: GViewpointChange = GViewpointChange.builder()
            .rotate(to: Float(angle))
            .tilt(to: 0)
            .pan(to: coord)
            .pivot(pivot)
            .zoom(to: 11.0)
            .build()
        
        mapViewController?.moveMapTo(viewpointChange, duration: 700, animationType: .default)
    }
    
    private func updateGpsMarker(utmk: GUtmk) {
        guard let markerImage = self.markerImage else {
            return
        }
        
        if gpsMarker == nil {
            gpsMarker = GMarker(options: ["icon": markerImage])
            mapViewController?.addOverlay(gpsMarker!)
            
        }
        
        gpsMarker?.iconSize = CGPoint(x: 60.0 , y: 60.0)
        gpsMarker?.position = utmk
    }
    
    fileprivate func updateLocation(_ location: CLLocation) {
        guard let mapController = mapViewController else {
            return
        }
        
        if let utmk = GUtmk(from: location) {
            let viewpointChange: GViewpointChange = GViewpointChange.builder()
                .pivot(pivot)
                .pan(to: utmk)
                .rotate(to: Float(location.course))
                .build()
            mapController.moveMapTo(viewpointChange,
                                    duration: BaseMapPresenter.locationUpdateAnimationDuration,
                                    animationType: .linear)
        }
    }
    
}

extension TrackingMapPresenter: GLocationDelegate {
    
    /// 현재 위치 업데이트
    func didUpdateLocation(location: GGeoLocation) {
        guard let gpsLocation = location.location else {
            return
        }
        
        logDebug("Updated logcation: \(gpsLocation)")
        updateLocation(gpsLocation)
    }
}
