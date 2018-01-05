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

// Todo: RozeOptions과는 별도로 App 자체 옵션 관리가 필요하다.
//  저장 및 로딩 추가 구현 필요!
//  RozeOptions도 여기서 같이 관리하는게 좋지 않을까??
struct Preferences {
    
    /// 길안내 시 지도 테마 옵션
    enum MapThemeOptions {
        /// 자동 주야간 테마 변경
        case auto
        /// 항상 주간 테마
        case dayOnly
        /// 항상 야간 테마
        case nightOnly
    }
    
    /// 길안내 모드 시 적용할 지도 테마 옵션
    static var mapThemeOption: MapThemeOptions = .auto
    
    /// 길안내 모드 시 주행 속도에 따른 지도 자동 축적 적용 옵션
    static var isMapAutoLevel: Bool = true
}
