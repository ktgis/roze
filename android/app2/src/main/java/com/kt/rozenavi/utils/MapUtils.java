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

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.kt.geom.model.LatLng;
import com.kt.geom.model.UTMK;
import com.kt.maps.GMap;
import com.kt.maps.overlay.Overlay;
import com.kt.naviextension.traffic.adapter.TrafficAdapter;

/**
 * 지도 기능을 구현하는데 필요한 공통 기능을 모아놓은 유틸리티 클래스
 * animation 시간, 지도 pivot, 좌표변환 등의 기능을 제공한다.
 */
public class MapUtils {
    /**
     * TBT 표시 path 객체 width 보정용 상수
     */
    public static final float TBT_PATH_WIDTH_IN_DP = 7;

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
    public static double calcMapZoomlevel(double targetResolution) {
        //계산된 zoomlevel
        double zoomlevel = 0;
        //0레벨일때 resolution 값
        double resolution = 4096.0;

        double curResolution = 0.0;
        //최대 12레벨에서 0레벨까지 축소
        for (int i = 12; i >= 0; i--) {
            curResolution = (resolution / Math.pow(2, i));
            if (curResolution > targetResolution) {
                zoomlevel = i + (1 - (targetResolution / curResolution));
                break;
            }
        }
        //화면에 잘리지 않도록 좀 더 작게 축소
        return zoomlevel < 0.2 ? 0 : zoomlevel - 0.2;
    }

    /**
     * 지도에 overlay 등록
     *
     * @param gMap    지도 객체
     * @param overlay 오버레이 객체
     * @return 등록 성공/실패
     */
    public static boolean addOverlay(GMap gMap, Overlay overlay) {
        if (gMap == null || overlay == null) {
            return false;
        }
        gMap.addOverlay(overlay);
        return true;
    }

    /**
     * 교통정보 layer adapter 설정
     */
    public static void setTrafficLayerAdapter(GMap gMap, Context applicationContext, TrafficAdapter adapter) {
        if (gMap == null || applicationContext == null) {
            return;
        }
        gMap.setGTrafficLayerAdaptor(adapter, applicationContext);
    }
}