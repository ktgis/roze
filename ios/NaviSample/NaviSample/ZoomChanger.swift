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

class ZoomChanger {
    
    private static let zoomAnimationDuration: Float = 1000
    
    private static let lowRefSpeed: Double = 31
    
    private static let midRefSpeed: Double = 111
    
    private static let speedTypeChangeDuration: TimeInterval = 5.0 // 5sec
    
    private static let turnZoomChangeDistance: Int32 = 200
    
    private static let turnZoomChangeDistanceOfHighway: Int32 = 300
    

    var speed: Double = 0 {
        didSet {
            let timestamp = Date().timeIntervalSince1970
            let newSpeedType = SpeedType.typeOfSpeed(speed)
            if spotSpeedType != newSpeedType {
                spotSpeedType = newSpeedType
                lastSpeedTime = timestamp
            } else {
                if (timestamp - lastSpeedTime) > ZoomChanger.speedTypeChangeDuration {
                    speedType = newSpeedType
                }
            }
        }
    }
    
    private var spotSpeedType: SpeedType = .slow
    private var speedType: SpeedType = .slow
    private var lastSpeedTime: TimeInterval = Date().timeIntervalSince1970
    
    private enum SpeedType {
        case slow, normal, fast
        
        var zoom: Float {
            switch self {
            case .slow:
                return 12.5
            case .normal:
                return 12
            default:
                return 11
            }
        }
        
        static func typeOfSpeed(_ speed: Double) -> SpeedType {
            if speed < ZoomChanger.lowRefSpeed {
                return .slow
            } else if speed < ZoomChanger.midRefSpeed {
                return .normal
            }
            
            return .fast
        }
    }
    
    
    
    /// map zoom changer
    public typealias MapZoomChanger = (_ zoom: Float, _ tilt: Float) -> Void
    
    func updateTurnDistance(_ distance: Int32, zoomChanger: @escaping MapZoomChanger) {
        if isNearTurn(distance: distance) {
            zoomChanger(12.5, 25)
        } else {
            zoomChanger(speedType.zoom, 60)
        }
    }
    
    private func isNearTurn(distance: Int32) -> Bool {
        /// Todo: highway 인지 or 직진 안내 인지에 따라 다르게 판단이 필요한가??
        return distance <= ZoomChanger.turnZoomChangeDistance
    }

}
