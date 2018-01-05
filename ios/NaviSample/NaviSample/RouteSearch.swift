//
//  RouteSearch.swift
//  iRozeNavi
//
//  Created by 서유리 on 2017. 10. 16..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import roze

struct RouteSearch {
    
    /// 요약경로 요청 결과 callback
    /// - Parameters:
    ///   - routeSummary: 요약 경로. 실패한 경우 nil이 전달된다.
    ///   - error: 성공한 경우 nil, 아니면 RozeError가 전달된다.
    public typealias RouteCalculateCompletion = (_ routeSummary: GRouteSummary?, _ error: GNavigationError?) -> Void

    static func requestRoutePlan(_ param:(start: GUtmk, wayPoints: [GUtmk]? ,destination: [GUtmk]),
                                 completion: @escaping RouteCalculateCompletion) {
        guard let lastLocation = GNavigationManager.instance.lastGpsLocation else {
            print("requestRoutePlan - lastGpsLocation 없어서 경로검색못해 ")
            return
        }
        
        let rozeOptions = GNavigationOptions.instance
        let routePlan = GRoutePlanBuilder { builder in
            builder.start = param.start
            if let waypoints = param.wayPoints {
                builder.waypoints = waypoints
            }
            builder.dests = param.destination
            
            if lastLocation.course >= 0 {
                builder.bearing = Int(lastLocation.course)
            }
            if lastLocation.horizontalAccuracy >= 0 {
                builder.accuracy = Float(lastLocation.horizontalAccuracy)
            }
            builder.altitude =  lastLocation.altitude
            builder.createTime = Int64(lastLocation.timestamp.timeIntervalSince1970 * 1000)
            builder.carType = rozeOptions.carType
            builder.hipass = rozeOptions.hasHipass
            }.build()
        
  
        if let routePlan = routePlan {
            let routeManager = GRouteManager()
            if !routeManager.isBusy {
                routeManager.calculateRoute(routePlan: routePlan, completion: { (routesummary, error) in
                    if let error = error {
                        logError("Fail to calculate route: \(error)")
                        completion(nil, error)
                        
                    }
                    completion(routesummary, nil)
                })
                
            }
        }
    }
}
