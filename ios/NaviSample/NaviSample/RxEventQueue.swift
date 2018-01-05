//
//  RxEventQueue.swift
//  iRozeNavi
//
//  Created by 서유리 on 2017. 9. 22..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import RxSwift



class RxEventQueue {
    
    static let instance: RxEventQueue = {
        return RxEventQueue()
    }()
    
    private init() {}
    
    var busDic = [Int: PublishSubject<RxEvent>]()
    
    func post(event: RxEvent) -> Void {
        if let bus = busDic[event.type] {
            bus.onNext(event)
        }
    }
    
    func observe(eventType: Int) -> Observable<RxEvent> {
        if let bus = busDic[eventType] {
            return bus
        }
        
        let newBus = PublishSubject<RxEvent>()
        busDic[eventType] = newBus
        return newBus.asObservable()
    }
    
}
