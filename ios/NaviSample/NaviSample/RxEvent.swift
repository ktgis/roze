//
//  RxEvent.swift
//  iRozeNavi
//
//  Created by 서유리 on 2017. 9. 22..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation

struct RxEventType {
    static let screenTypeEvent = 0
    static let routeSelectEvent = 1

}

protocol RxEvent {
    var type: Int { get }
}

