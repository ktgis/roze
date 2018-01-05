//
//  SettingsUtil.swift
//  iRozeNavi
//
//  Created by moon on 2017. 11. 2..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import roze

class SettingsUtil {
    static func label(_ type: GCarType) -> String {
        switch type {
        case .motorcycle:
            return "이륜차"
        case .car:
            return "승용차, 소형승합차, 소형화물차"
        case .midCar:
            return "중형승합차, 중형화물차"
        case .heavyCar:
            return "대형승합차 대형화물차(2축)"
        case .heavyTruck:
            return "대형화물차(3축)"
        case .specificGoods:
            return "특수화물차(4축이상)"
        case .smallCar:
            return "경차"
        default:
            return "Unknown"
        }
    }
    
    static func label(_ type: GVoiceType) -> String {
        switch type {
        case .soft:
            return "차분"
        case .sporty:
            return "발랄"
        default:
            return "Unknown"
        }
    }
    
    static func label(_ type: GEnergyType) -> String {
        switch type {
        case .gasoline:
            return "휘발유"
        case .premiumGasoline:
            return "고급휘발유"
        case .diesel:
            return "경유"
        case .kerosene:
            return "등유"
        case .lpg:
            return "LPG"
        case .cng:
            return "CNG"
        case .lng:
            return "LNG"
        case .electricity:
            return "전기"
        default:
            return "Unknown"
        }
    }
}
