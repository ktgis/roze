/*
 *  Copyright (c) 2017 kt corp. All rights reserved.
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
import android.support.annotation.Nullable;

import com.kt.roze.NavigationManager;
import com.kt.roze.RozeError;
import com.kt.roze.data.model.ControlLink;
import com.kt.roze.data.model.FerryPoint;

import java.util.List;

public class RouteListenerAdpater implements NavigationManager.RouteListener {
    @Override
    public void onRouteStarted() {
    }

    @Override
    public void onRouteStartFail(RozeError error) {
    }

    @Override
    public void onTrafficUpdate() {
    }

    //Since 1.5.0 교통정보 업데이트 완료 이벤트 메소드 추가
    @Override
    public void onTrafficUpdateComplete(@Nullable List<ControlLink> list) {

    }

    @Override
    public void onArrived(short arrivedIndex) {
    }

    //Since 1.3.0 Reroute : 재탐색에 대한 이유에 따라 UI 처리가 다른 경우가 있어, 재탐색에 대한 사유를 추가했습니다.
    @Override
    public void onRouteDeviated(Location location, NavigationManager.RerouteExtraInfo extraReason) {
    }

//    @Override
//    public void onRouteDeviated(Location location) {
//    }

    @Override
    public void onRouteDidNotEnter(Location location) {
    }

    //Since 1.4.3 페리경로 시작지점 도착 이벤트 메소드 추가
    @Override
    public void onArrivedFerryRoute(FerryPoint ferryPoint) {

    }
}
