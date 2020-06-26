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

package com.kt.rozenavi.provider;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.kt.maps.GMap;
import com.kt.maps.model.Viewpoint;
import com.kt.rozenavi.data.LiveDataAdv;
import com.kt.rozenavi.data.model.ViewpointChangeEventData;


public class MapProvider implements LifecycleObserver, GMap.OnViewpointChangeListener {
    private static MapProvider instance = null;
    public LiveDataAdv<ViewpointChangeEventData> viewpointEventData = new LiveDataAdv<>();
    public LiveDataAdv<GMap> gMap = new LiveDataAdv<>();

    public static MapProvider getInstance() {
        if (instance == null) {
            instance = new MapProvider();
        }
        return instance;
    }

    @Override
    public void onViewpointChange(GMap gMap, Viewpoint viewpoint, boolean gesture) {
        viewpointEventData.setValue(new ViewpointChangeEventData(gMap, viewpoint, gesture));
    }

    public void bindMap(GMap gMap) {
        this.gMap.setValue(gMap);
        gMap.setOnViewpointChangeListener(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy() {
        GMap gMapValue = gMap.getValue();
        if (gMapValue != null) {
            gMapValue.setOnViewpointChangeListener(null);
            gMap.setValue(null);
        }
        instance = null;
    }

}
