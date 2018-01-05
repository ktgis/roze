//
//  MapPresenter.swift
//  iRozeNavi
//
//  Created by 서유리 on 2017. 9. 14..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import gmaps
import roze

enum MapMode {
    case tracking
}

protocol MapPresenter {
    
    weak var mapViewController: MainViewController? { get set }
    
    func start()
    func stop()
    
    func didChangedGpsStatus(_ isOn: Bool)
    func didChangedZoomLevel(_ zoomLevel: Int32)
    
    
    /// map에서 pivot 위치 설정값
    var pivot: CGPoint { get }
    
    /// 위치 업데이트에 따른 현재 pivot 위치 변경 좌표
    ///
    /// - Parameter location: 업데이트 된 pivot 위치
    func didUpdatePivotLocation(_ location: GUtmk, rotation: Float)
    
    func resumeToCurrentLocation()
}

class BaseMapPresenter: MapPresenter {
    
    static let locationUpdateAnimationDuration: Int32 = 1000
    
    weak var mapViewController: MainViewController?
    
    
    /// default pivot is the center of map.
    ///
    /// override if needed a different pivot.
    var pivot: CGPoint {
        guard let mapViewController = mapViewController else {
            return CGPoint(x: 0, y: 0)
        }
        
        return CGPoint(x: mapViewController.mapWidth * 0.5, y: mapViewController.mapHeight * 0.5)
    }
    
    func start() {
        // override if needed
    }
    
    func stop() {
        // override if needed
    }
    
    func didChangedGpsStatus(_ isOn: Bool) {
        // override if needed
    }
    
    func didChangedZoomLevel(_ zoomLevel: Int32) {
        // override if needed
    }
    
    func didUpdatePivotLocation(_ location: GUtmk, rotation: Float) {
        // override if needed
        // 자차마커 등을 업데이트 할때 사용한다. 사용할 필요가 없으면 구현하지 않아도 된다.(ex. routeSummary UI)
    }
    
    func resumeToCurrentLocation() {
        // override if needed
        // 맵을 움직인 후, 현재위치로 돌아올 때 사용한다.
    }
    
}
