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

package com.kt.rozenavi.ui.main.navigation;

import com.kt.geom.model.UTMK;
import com.kt.maps.GMap;
import com.kt.maps.model.Viewpoint;
import com.kt.roze.NavigationManager;
import com.kt.roze.data.model.Link;
import com.kt.roze.data.model.Route;
import com.kt.roze.guidance.RGType;
import com.kt.roze.guidance.model.TurnGuidance;
import com.kt.roze.location.model.RouteLocation;
import com.kt.rozenavi.ui.main.MapController;
import com.kt.rozenavi.ui.main.UIController;
import com.kt.rozenavi.utils.MapUtils;

import java.util.List;

class ZoomChanger {
    /**
     * TBT 이후 줌레벨 유지 시간
     */
    private static final long MODE_KEEP_DURATION = 5000L;
    /**
     * 줌레벨 변경시 애니메이션 타임
     */
    private static final int ZOOM_CHANGE_DURATION =
            MapUtils.MAP_ANIMATION_DURATION_IN_MILLISECOND_LOCATION_UPDATE;
    /**
     * 속도타입 Speed.Low기준 최대 속도
     * 넘을시 Speed.Middle
     */
    private static final int LOW_SPEED_LIMIT_VALUE = 31;
    /**
     * 속도타입 Speed.Middle기준 최대 속도
     * 넘을시 Speed.High
     */
    private static final int MIDDLE_SPEED_LIMIT_VALUE = 111;
    /**
     * 고속도로일때 TBT기반 줌레벨 변경 시작거리
     */
    private static final int HIGHWAY_TURN_ZOOM_DISTANCE = 350;
    /**
     * 일반도로일때 TBT기반 줌레벨 변경 시작거리
     */
    private static final int ROAD_TURN_ZOOM_DISTANCE = 250;

    /**
     * 속도 타입
     */
    private enum Speed {
        None, Low, Middle, High
    }

    private Speed currentSpeed = Speed.None;
    private short currentTurn;

    private int linkIndex = -1;
    private long lastReceiveTime;
    private long modeKeepTime;
    private Route route;

    ZoomChanger(Route route) {
        this.route = route;
    }

    /**
     * 재탐색 등으로 Route정보가 변경될때 업데이트
     *
     * @param route Route 정보
     */
    public void setRoute(Route route) {
        this.route = route;
        linkIndex = -1;
        currentTurn = RGType.NONE;
    }

    /**
     * TBT정보가 업데이트 될때 현재 TBT 정보와 링크정보를 업데이트
     *
     * @param guidances TBT 정보 리스트
     */
    void updateTbt(List<TurnGuidance> guidances) {
        if (guidances == null || guidances.size() == 0) {
            return;
        }
        TurnGuidance turnGuidance = guidances.get(0);
        linkIndex = turnGuidance.linkIndex;
        currentTurn = turnGuidance.turnCode;
    }

    /**
     * 속도기반 줌레벨 체크
     * TBT 줌레벨 변경시 일정시간동안 줌레벨을 유지하고
     * 해당 유지시간이 경과되었을때는 속도기반으로 줌레벨을 변경
     */
    private void checkZoomlevelBySpeed() {
        long receiveTime = System.currentTimeMillis();
        if (receiveTime < modeKeepTime) {
            return;
        }

        RouteLocation routeLocation = NavigationManager.getInstance().getLastRouteLocation();
        if (routeLocation == null) {
            return;
        }

        int speed = UIController.getInstance().getDriveView().currentSpeed;
        Viewpoint viewPoint = MapController.getInstance().getCurrentViewPoint();
        if (speed < LOW_SPEED_LIMIT_VALUE) {
            setZoomlevelBySpeed(receiveTime, Speed.Low, routeLocation.location,
                    viewPoint.zoom, 13);
        } else if (speed < MIDDLE_SPEED_LIMIT_VALUE) {
            setZoomlevelBySpeed(receiveTime, Speed.Middle, routeLocation.location,
                    viewPoint.zoom, 12);
        } else {
            setZoomlevelBySpeed(receiveTime, Speed.High, routeLocation.location,
                    viewPoint.zoom, 11);
        }
    }

    /**
     * 속도기반 줌레벨 변경
     * 현재 속도가 유지되는지 확인하기 위해 속도 타입을 설정 및 확인하는 작업을 처리하고
     * 일정 시간 현재 속도가 유지되는 경우 속도에 맞는 줌레벨로 변경
     *
     * @param receiveTime 수신된 시간
     * @param speed       속도 타입
     * @param location    좌표
     * @param currentZoom 현재 줌레벨
     * @param targetZoom  변경 줌레벨
     */
    private void setZoomlevelBySpeed(long receiveTime, Speed speed, UTMK location,
            float currentZoom, float targetZoom) {
        if (currentSpeed != speed) {
            lastReceiveTime = receiveTime;
            currentSpeed = speed;
        } else {
            if (receiveTime - lastReceiveTime > MODE_KEEP_DURATION && currentZoom != targetZoom) {
                setZoomLevel(location, targetZoom);
            }
        }
    }

    /**
     * TBT 거리가 업데이트될때 줌레벨 변경여부를 체크
     *
     * @param distance TBT까지 남은 거리
     */
    void checkZoomlevel(int distance) {
        if (route.links.size() == 0) {
            return;
        }

        RouteLocation routeLocation = NavigationManager.getInstance().getLastRouteLocation();
        if (routeLocation == null) {
            return;
        }

        if (linkIndex < 0) {
            return;
        }
        Link currentLink = route.links.get(linkIndex);
        if (isNearTurn(currentLink, distance)) {
            setZoomlevelByTurn(routeLocation.location);
        } else {
            checkZoomlevelBySpeed();
        }
    }

    /**
     * TBT 기반 줌레벨 변경
     *
     * @param location 좌표
     */
    private void setZoomlevelByTurn(UTMK location) {
        Viewpoint viewPoint = MapController.getInstance().getCurrentViewPoint();
        if (viewPoint.zoom != 13) {
            setZoomLevel(location, 13);
        }
        lastReceiveTime = System.currentTimeMillis();
        modeKeepTime = lastReceiveTime + MODE_KEEP_DURATION;
    }

    /**
     * TBT안내가 근처에 있는지 체크
     * 고속도로일때와 일반도로일때 거리 정보가 다르게 체크
     * TBT가 직진 정보일때는 알리지 않음
     *
     * @param currentLink 현재 링크
     * @param distance    TBT까지 남은 거리
     */
    private boolean isNearTurn(Link currentLink, int distance) {
        boolean isHighway = currentLink.isHighwayRoadType();
        return (isHighway && distance <= HIGHWAY_TURN_ZOOM_DISTANCE ||
                !isHighway && distance <= ROAD_TURN_ZOOM_DISTANCE)
                && currentTurn != RGType.GO_STRAIGHT;
    }

    /**
     * 지도 줌레벨 변경
     *
     * @param location  좌표
     * @param zoomLevel 줌레벨
     */
    private void setZoomLevel(UTMK location, float zoomLevel) {
        MapController.getInstance().changeViewpoint(location, -1, -1, zoomLevel, null,
                ZOOM_CHANGE_DURATION, GMap.AnimationTiming.LINEAR);
    }
}
