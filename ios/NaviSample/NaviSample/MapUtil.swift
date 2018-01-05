//
//  MapUtil.swift
//  rozeNavi
//
//  Created by 서유리 on 2017. 7. 14..
//  Copyright © 2017년 kt. All rights reserved.
//

import Foundation
import UIKit
import gmaps

enum measureType : Int32 {
    case Kilometer = 0
    case time = 1
}

func changeDistanceMeasure(value: Int32, measureType: measureType) -> String {
    switch measureType {
    case .Kilometer:
        print(" changeDistanceMeasure value \(value)")
        let distanceText = value >= 1000 ? String(format: "%.1f km", Float(value)/1000.0) : String(format: "%d m", value)
        return distanceText
        
    case .time:
        var time: String = ""
        switch value {
        case 0..<60 :
            time = String(value) + "초"
        case 60..<3600 :
            time = String(Int(value / 60)) + "분"
        default:
            time = String(Int(value / 3600)) + "시간"
        }
        return time
    }
}


func setStrokeText(inputString: String) -> NSAttributedString {
    
    let textProperty = [NSStrokeColorAttributeName: UIColor.lightGray,
                      NSForegroundColorAttributeName: UIColor.black,
                      NSStrokeWidthAttributeName: -2.0,] as [String: Any]
    return NSAttributedString(string: inputString, attributes: textProperty)
}

enum MapTheme {
    case mapDefault, dayDrive, nightDrive
    
    var stylePath: String? {
        switch self {
        case .mapDefault:
            return DEFAULT_MAP_STYLE
        case .dayDrive:
            return Bundle(identifier: "com.kt.roze")?.path(forResource: "roze.bundle/day_drive.json", ofType: nil)
//            return Bundle.main.path(forResource: "day_drive.json", ofType: nil)
        case .nightDrive:
            return Bundle(identifier: "com.kt.roze")?.path(forResource: "roze.bundle/night_drive.json", ofType: nil)
//            return Bundle.main.path(forResource: "night_drive.json", ofType: nil)
        }
    }
    
    // Todo: 서버로 부터 정책을 받아, 일출/몰 시간 적용해야 함!
    static var dayStartDate: Date = {
        let gregorian = Calendar(identifier: .gregorian)
        var components = gregorian.dateComponents([.year, .month, .day, .hour, .minute, .second], from: Date())
        components.hour = 7
        components.minute = 0
        components.second = 0
        return gregorian.date(from: components)!
    }()
    
    static var nightStartDate: Date = {
        let gregorian = Calendar(identifier: .gregorian)
        var components = gregorian.dateComponents([.year, .month, .day, .hour, .minute, .second], from: Date())
        components.hour = 18
        components.minute = 0
        components.second = 0
        return gregorian.date(from: components)!
    }()
    
    static var driveMapTheme: MapTheme {
//        let currentHour = Calendar.current.component(.hour, from: Date())
//        let currentDate = Date().description(with: NSLocale(localeIdentifier: "ko_KR") as Locale)
        let currentDate = Date() // UTC 기준
        if currentDate >= MapTheme.dayStartDate, currentDate < MapTheme.nightStartDate {
            return .dayDrive
        }
        return .nightDrive
    }
}



extension UIColor {
    public convenience init?(hexString: String) {
        let r, g, b, a: CGFloat
        
        if hexString.hasPrefix("#") {
            let start = hexString.index(hexString.startIndex, offsetBy: 1)
            let hexColor = String(hexString[start...])
            
            if hexColor.count == 8 {
                let scanner = Scanner(string: hexColor)
                var hexNumber: UInt64 = 0

                if scanner.scanHexInt64(&hexNumber) {
                    r = CGFloat((hexNumber & 0xff000000) >> 24) / 255
                    g = CGFloat((hexNumber & 0x00ff0000) >> 16) / 255
                    b = CGFloat((hexNumber & 0x0000ff00) >> 8) / 255
                    a = CGFloat(hexNumber & 0x000000ff) / 255
                    
                    self.init(red: r, green: g, blue: b, alpha: a)
                    return
                }
            }
        }
        
        return nil
    }
}

extension UIImage {
    public convenience init?(imageNameWithBundlePath: String) {
        let resourcePath = "Frameworks/roze.framework/roze.bundle/\(imageNameWithBundlePath)"
        self.init(named: resourcePath)
        return
    }
}



