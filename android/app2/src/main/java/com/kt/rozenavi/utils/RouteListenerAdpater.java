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

import com.kt.roze.NavigationManager;
import com.kt.roze.RozeError;

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
}
