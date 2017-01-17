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

package com.kt.rozenavi.utils;

import android.location.Location;

import com.kt.geom.model.LatLng;
import com.kt.geom.model.UTMK;

/**
 * 지도 기능을 구현하는데 필요한 공통 기능을 모아놓은 유틸리티 클래스
 * animation 시간, 지도 pivot, 좌표변환 등의 기능을 제공한다.
 */
public class MapUtils {
    /**
     * 지도 경로 표시 path객체 width 보정용 상수
     */
    public static final float ROUTE_PATH_WIDTH_IN_DP = 9;
    /**
     * 위치 이동정보가 수신되었을때 내차 마커 이동 애니메이션 시간
     */
    public static final int MAP_ANIMATION_DURATION_IN_MILLISECOND_LOCATION_UPDATE = 1000;
    /**
     * 경로 검색을 하였을때 경로 요약정보가 표시되는 화면에서 지도 줌이 변경되는 애니메이션 시간
     */
    public static final int MAP_ANIMATION_DURATION_IN_MILLISECOND_ROUTE_VIEW = 400;
    /**
     * 지도를 제스쳐로 이동하였을때 현재 위치로 다시 고정되는데 필요한 시간
     */
    public static final long MAP_UPDATE_SUSPEND_IN_MILLISECOND = 4000;

    /**
     * location 객체를 UTMK 좌표 객체로 변환하여 반환
     *
     * @param location location 객체
     * @return UTMK로 변환된 객체
     */
    public static UTMK convertLocationToUtmk(Location location) {
        return UTMK.valueOf(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    /**
     * 대상 resolution이 모두 표현될수 있는 지도 zoomlevel 계산
     *
     * @param targetResolution 대상 resolution
     * @return 계산된 zoomlevel
     */
    public static int calcMapZoomlevel(double targetResolution) {
        //계산된 zoomlevel
        int zoomlevel = 0;
        //0레벨일때 resolution 값
        double resolution = 4096.0;

        //최대 12레벨에서 0레벨까지 축소
        for (int i = 12; i >= 0; i--) {
            if ((resolution / Math.pow(2, i)) > targetResolution) {
                zoomlevel = i;
                break;
            }
        }

        return zoomlevel;
    }
}