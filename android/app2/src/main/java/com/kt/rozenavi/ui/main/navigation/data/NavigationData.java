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


package com.kt.rozenavi.ui.main.navigation.data;

import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.MutableLiveData;

import com.kt.roze.data.model.Lane;
import com.kt.roze.data.model.WayPoint;
import com.kt.roze.guidance.model.HighwayGuidance;
import com.kt.roze.guidance.model.IntervalSpeedSpotGuidance;
import com.kt.roze.guidance.model.TurnGuidance;
import com.kt.rozenavi.ui.main.navigation.data.model.LowestGasEventData;
import com.kt.rozenavi.ui.main.navigation.data.model.RemainEventData;
import com.kt.rozenavi.ui.main.navigation.data.model.SafetyEventData;

import java.util.List;

/**
 * 경로안내정보 유실을 막기 위한 Data 클래스
 */
public class NavigationData implements LifecycleObserver {
    public MutableLiveData<Lane> laneEvent = new MutableLiveData<>();
    public MutableLiveData<Integer> laneDistance = new MutableLiveData<>();
    public MutableLiveData<String> roadViewPath = new MutableLiveData<>();
    public MutableLiveData<List<HighwayGuidance>> highwayEvent = new MutableLiveData<>();
    public MutableLiveData<Integer> highwayDistance = new MutableLiveData<>();
    public MutableLiveData<List<TurnGuidance>> turnEvent = new MutableLiveData<>();
    public MutableLiveData<Integer> turnDistance = new MutableLiveData<>();
    public MutableLiveData<RemainEventData> remainEvent = new MutableLiveData<>();
    public MutableLiveData<SafetyEventData> safetySpotEvent = new MutableLiveData<>();
    public MutableLiveData<IntervalSpeedSpotGuidance> intervalEvent = new MutableLiveData<>();
    public MutableLiveData<List<WayPoint>> nearWaypointEvent = new MutableLiveData<>();
    public MutableLiveData<LowestGasEventData> lowestGasEvent = new MutableLiveData<>();
    public MutableLiveData<Integer> lowestGasDistance = new MutableLiveData<>();
}
