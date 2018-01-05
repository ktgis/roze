//
//  RouteSelectEvent.swift
//  rozeNavi
//
//  Created by 서유리 on 2017. 6. 22..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation

class RouteSelectEvent: RxEvent {
    var type: Int {
        return RxEventType.routeSelectEvent
    }
    
    let index: Int
    
    init(index: Int) {
        self.index = index
    }
    
}
