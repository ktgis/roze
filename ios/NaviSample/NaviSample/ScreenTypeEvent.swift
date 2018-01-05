//
//  ScreenTypeEvent.swift
//  iRozeNavi
//
//  Created by 서유리 on 2017. 9. 22..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import roze

enum ScreenMode {
    case tracking, navigating //, routeShowing, placeShowing
}

class ScreenTypeEvent: RxEvent {
    var type: Int {
        return RxEventType.screenTypeEvent
    }
    
    let screenMode: ScreenMode
    let data: Any?
    
    convenience init(screenMode: ScreenMode) {
        self.init(screenMode: screenMode, data: nil)
    }
    
    init(screenMode: ScreenMode, data: Any?) {
        self.screenMode = screenMode
        self.data = data
    }
    
}
