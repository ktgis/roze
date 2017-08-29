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
package com.kt.rozenavi.ui.main;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.kt.maps.GMap;
import com.kt.maps.model.Viewpoint;
import com.kt.roze.location.model.GeoLocation;

// TODO: 2017-08-24
/*
* viewmodel로 작업해서 mainactivity와 lifecycle을 동기화 했는데 다른 화면에서 attach가 되지 않은시점
* 에서는 viewmodel을 받아올수가 없어서 viewmodel로 자동화 하지 않고 lifecycleobserver를 달아서
* 별도로 동기화 해야 할지도 모르겠음
*
 */
public class MainActivityViewModel extends ViewModel {

    public MutableLiveData<ViewpointChangeEventData> viewpointEventData = new MutableLiveData<>();
    public MutableLiveData<GeoLocation> geoLocation = new MutableLiveData<>();
    public MutableLiveData<Boolean> isGpsOn = new MutableLiveData<>();
    public MutableLiveData<GMap> gMap = new MutableLiveData<>();

    public MainActivityViewModel() {
        isGpsOn.setValue(false);
    }

    public static class ViewpointChangeEventData {
        public GMap gMap;
        public Viewpoint viewpoint;
        public boolean b;

        public ViewpointChangeEventData(GMap gMap, Viewpoint viewpoint, boolean b) {
            this.gMap = gMap;
            this.viewpoint = viewpoint;
            this.b = b;
        }
    }
}