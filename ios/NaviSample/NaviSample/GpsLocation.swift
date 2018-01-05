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
import ObjectMapper
import CoreLocation

class GpsLocation: Mappable {
    
    var timestamp: Double!
    var lat: Double!
    var lng: Double!
    var altitude: Double!
    var course: Double!
    var speed: Double!
    var hAccuracy: Double!
    
    required init?(map: Map) {
    }
    
    func mapping(map: Map) {
        timestamp <- map["timestamp"]
        lat <- map["lat"]
        lng <- map["lng"]
        altitude <- map["alt"]
        course <- map["course"]
        speed <- map["speed"]
        hAccuracy <- map["hAccuracy"]
    }
    
    func toLocation(timeFix: TimeInterval = 0) -> CLLocation {
        let latlng = CLLocationCoordinate2D(latitude: lat, longitude: lng)
        let date = Date(timeIntervalSince1970: timestamp + timeFix)
        return CLLocation(coordinate: latlng,
                          altitude: altitude,
                          horizontalAccuracy: hAccuracy,
                          verticalAccuracy: -1,
                          course: course,
                          speed: speed,
                          timestamp: date)
    }
}
