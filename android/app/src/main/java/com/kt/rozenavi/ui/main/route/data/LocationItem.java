/*
 *  Copyright (c) 2016 kt corp. All rights reserved.
 *
 *  This is a proprietary software of kt corp, and you may not use this file
 *  except in compliance with license agreement with kt corp. Any redistribution
 *  or use of this software, with or without modification shall be strictly
 *  prohibited without prior written approval of kt corp, and the copyright
 *   notice above does not evidence any actual or intended publication of such
 *  software.
 *
 */

package com.kt.rozenavi.ui.main.route.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 목적지 데이터 클래스
 * 경로편집 화면에서 사용하는 출발지/목적지/경유지 정보 데이터 클래스
 * 좌표, 목적지 명칭을 제공
 */
@AllArgsConstructor
public class LocationItem {
    /**
     * 목적지 x 좌표
     * utmk 좌표
     */
    @Getter
    @Setter
    private double x;
    /**
     * 목적지 y 좌표
     * utmk 좌표
     */
    @Getter
    @Setter
    private double y;
    /**
     * 목적지 명칭
     */
    @Getter
    @Setter
    private String name;
}
