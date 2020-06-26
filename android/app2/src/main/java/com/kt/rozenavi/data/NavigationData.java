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


package com.kt.rozenavi.data;

import com.kt.roze.NavigationManager;
import com.kt.roze.RozeError;
import com.kt.roze.data.model.Lane;
import com.kt.roze.data.model.RGCurrentInform;
import com.kt.roze.data.model.WayPoint;
import com.kt.roze.guidance.RouteGuidanceListener;
import com.kt.roze.guidance.model.HighwayGuidance;
import com.kt.roze.guidance.model.IntervalSpeedSpotGuidance;
import com.kt.roze.guidance.model.OilPriceGuidance;
import com.kt.roze.guidance.model.SafetySpotGuidance;
import com.kt.roze.guidance.model.TrackingGuidance;
import com.kt.roze.guidance.model.TurnGuidance;
import com.kt.rozenavi.data.model.LowestGasEventData;
import com.kt.rozenavi.data.model.RemainEventData;
import com.kt.rozenavi.data.model.SafetyEventData;
import com.kt.rozenavi.data.model.TrackingEventData;

import java.util.List;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

/**
 * 경로안내정보 유실을 막기 위한 Data 클래스
 */
public class NavigationData implements LifecycleObserver {
    private NavigationManager navigationManager = null;
    private static NavigationData instance = null;

    public LiveDataAdv<Lane> laneEvent = new LiveDataAdv<>();
    public LiveDataAdv<Integer> laneDistance = new LiveDataAdv<>();
    public LiveDataAdv<String> roadView = new LiveDataAdv<>();
    public LiveDataAdv<List<HighwayGuidance>> highwayEvent = new LiveDataAdv<>();
    public LiveDataAdv<Integer> highwayDistance = new LiveDataAdv<>();
    public LiveDataAdv<List<TurnGuidance>> turnEvent = new LiveDataAdv<>();
    public LiveDataAdv<Integer> turnDistance = new LiveDataAdv<>();
    public LiveDataAdv<RemainEventData> remainEvent = new LiveDataAdv<>();
    public LiveDataAdv<SafetyEventData> safetySpotEvent = new LiveDataAdv<>();
    public LiveDataAdv<IntervalSpeedSpotGuidance> intervalEvent = new LiveDataAdv<>();
    public LiveDataAdv<List<WayPoint>> nearWaypointEvent = new LiveDataAdv<>();
    public LiveDataAdv<LowestGasEventData> lowestGasEvent = new LiveDataAdv<>();
    public LiveDataAdv<Integer> lowestGasDistance = new LiveDataAdv<>();
    public LiveDataAdv<TrackingEventData> trackingEvent = new LiveDataAdv<>();
    public LiveDataAdv<Boolean> trackingInitializedEvent = new LiveDataAdv<>();
    /**
     * 안전운행 중 가상경로 이탈 시 발행
     */
	//-- 1.4.0 안전운행 이벤트 추가
    public SingleMessage<Boolean> trackingDeviatedEvent = new SingleMessage<>();

    public static NavigationData getInstance() {
        if (instance == null) {
            instance = new NavigationData();
        }
        return instance;
    }

    public void bindNavigationManager(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
        navigationManager.setRouteGuidanceEventListener(internalGuidanceListener);
	//-- 1.4.0 안전운행 이벤트 추가		
        navigationManager.setTrackingEventListener(trackingListener);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    void onDestroy() {
        if(navigationManager != null){
            navigationManager.setRouteGuidanceEventListener(null);
			//-- 1.4.0 안전운행 이벤트 추가
            navigationManager.setTrackingEventListener(null);
            navigationManager = null;
        }
        instance = null;
    }
	//-- 1.4.0 안전운행 이벤트 추가
    private NavigationManager.TrackingEventListener trackingListener = new NavigationManager.TrackingEventListener() {
        @Override
        public void onTrackingDataReadyEvent() {
            trackingInitializedEvent.setValue(true);
        }

        @Override
        public void onTrackingDataFailed(RozeError error) {
            trackingInitializedEvent.setValue(false);
        }

        @Override
        public void onTrackingRouteDeviated(NavigationManager.RerouteExtraInfo extraReason) {
            trackingDeviatedEvent.setValue(true);
        }
    };

    private RouteGuidanceListener internalGuidanceListener = new RouteGuidanceListener() {
        @Override
        public void onTrackingSpotChangedEvent(boolean isShow, List<TrackingGuidance> list) {
            trackingEvent.setValue(new TrackingEventData(isShow, list));
        }

        @Override
        public void onLaneChangedEvent(Lane lane) {
            super.onLaneChangedEvent(lane);
            laneEvent.setValue(lane);
        }

        @Override
        public void onLaneDistanceChangedEvent(int distance) {
            super.onLaneDistanceChangedEvent(distance);
            laneDistance.setValue(distance);
        }

        @Override
        public void onTurnChangedEvent(List<TurnGuidance> turnGuidances) {
            super.onTurnChangedEvent(turnGuidances);
            turnEvent.setValue(turnGuidances);
        }

        @Override
        public void onTurnDistanceChangedEvent(int distance) {
            super.onTurnDistanceChangedEvent(distance);
            turnDistance.setValue(distance);
        }

        @Override
        public void onRoadViewChangedEvent(String path) {
            super.onRoadViewChangedEvent(path);
            roadView.setValue(path);
        }

        @Override
        public void onHighwayChangedEvent(List<HighwayGuidance> highwayGuidances) {
            super.onHighwayChangedEvent(highwayGuidances);
            highwayEvent.setValue(highwayGuidances);
        }

        @Override
        public void onHighwayDistanceEvent(int distance) {
            super.onHighwayDistanceEvent(distance);
            highwayDistance.setValue(distance);
        }

        @Override
        public void onCurrentInformEvent(RGCurrentInform inform) {
            super.onCurrentInformEvent(inform);
            //since 1.7.0 현재 위치의 link index 를 받기 위해 observing event 변경
            remainEvent.setValue(new RemainEventData(inform.getTimeInSecond(),
                    inform.getDistanceInMeter(),
                    inform.getCurrentLinkIndex()));
        }

        @Override
        public void onSafetySpotChangedEvent(boolean isShow, List<SafetySpotGuidance> safetySpotGuidance) {
            super.onSafetySpotChangedEvent(isShow, safetySpotGuidance);
            safetySpotEvent.setValue(new SafetyEventData(isShow, safetySpotGuidance));
        }

        @Override
        public void onIntervalSafetySpotChangedEvent(IntervalSpeedSpotGuidance intervalGuidance) {
            super.onIntervalSafetySpotChangedEvent(intervalGuidance);
            intervalEvent.setValue(intervalGuidance);
        }

        @Override
        public void onLowestGasStationChangedEvent(boolean isShow, List<OilPriceGuidance> list) {
            super.onLowestGasStationChangedEvent(isShow, list);
            lowestGasEvent.setValue(new LowestGasEventData(isShow, list));
        }

        @Override
        public void onLowestGasStationDistanceChangedEvent(int distance) {
            super.onLowestGasStationDistanceChangedEvent(distance);
            lowestGasDistance.setValue(distance);
        }

        @Override
        public void onNearWayPointEvent(List<WayPoint> wayPoints) {
            super.onNearWayPointEvent(wayPoints);
            nearWaypointEvent.setValue(wayPoints);
        }
    };

    public void init() {
        laneEvent.setValue(null);
        laneDistance.setValue(null);
        roadView.setValue(null);
        highwayEvent.setValue(null);
        highwayDistance.setValue(null);
        turnEvent.setValue(null);
        turnDistance.setValue(null);
        remainEvent.setValue(null);
        safetySpotEvent.setValue(null);
        intervalEvent.setValue(null);
        nearWaypointEvent.setValue(null);
        lowestGasEvent.setValue(null);
        lowestGasDistance.setValue(null);
        trackingEvent.setValue(null);
        trackingDeviatedEvent.setValue(null);
    }
}
