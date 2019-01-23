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

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;

import com.kt.roze.NavigationManager;
import com.kt.roze.location.model.GeoLocation;
import com.kt.rozenavi.data.LiveDataAdv;


public class LocationProvider implements LifecycleObserver, NavigationManager.LocationListener,
        NavigationManager.GpsSignalListener {
    private static LocationProvider instance = null;
    private NavigationManager navigationManager = null;
    public LiveDataAdv<GeoLocation> location = new LiveDataAdv<>();
    public LiveDataAdv<Boolean> isGpsOn = new LiveDataAdv<>();

    public static LocationProvider getInstance() {
        if (instance == null) {
            instance = new LocationProvider();
        }
        return instance;
    }

    public void bindNavigationManager(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
        navigationManager.setLocationListener(this);
        navigationManager.setGpsSignalListener(this);
    }

    @Override
    public void onGpsLost() {
        isGpsOn.setValue(false);
    }

    @Override
    public void onGpsAvailable() {
        isGpsOn.setValue(true);
    }

    @Override
    public void onLocationUpdated(GeoLocation geoLocation) {
        location.setValue(geoLocation);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy() {
        if (navigationManager != null) {
            navigationManager.setLocationListener(null);
            navigationManager.setGpsSignalListener(null);
            navigationManager = null;
        }
        instance = null;
    }
}
