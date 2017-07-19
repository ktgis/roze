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

package com.kt.rozenavi.ui.main.route;

import android.content.Context;
import android.location.Location;

import com.kt.geom.model.LatLng;
import com.kt.geom.model.UTMK;
import com.kt.roze.NavigationManager;
import com.kt.roze.RozeError;
import com.kt.roze.RozeOptions;
import com.kt.roze.routing.RouteManager;
import com.kt.roze.routing.RoutePlan;
import com.kt.roze.routing.RouteSummary;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.UIController;
import com.kt.rozenavi.ui.main.drive.DriveView;
import com.kt.rozenavi.ui.main.route.data.LocationItem;
import com.kt.rozenavi.ui.setting.SettingCarActivity;
import com.kt.rozenavi.ui.setting.SettingRouteActivity;
import com.kt.rozenavi.utils.PreferenceUtils;
import com.kt.rozenavi.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * 경로 검색 및 편집에서 사용할 기능에 대한 Controller 클래스
 */
class RouteController implements RouteManager.RouteManagerListener {
    private static RouteController instance;

    /**
     * 경유지 정보 목록
     */
    private List<LocationItem> waypointList;
    /**
     * 출발지 정보
     */
    private LocationItem startLocation;
    /**
     * 도착지 정보
     */
    private LocationItem finishLocation;

    /**
     * 경로 편집화면에서 사용할 임시 정보
     */
    private List<LocationItem> tempLocationList;

    private RouteController() {
        waypointList = new ArrayList<>();
    }

    public static RouteController getInstance() {
        if (instance == null) {
            instance = new RouteController();
        }
        return instance;
    }

    void destroy() {
        instance = null;
        startLocation = null;
        finishLocation = null;

        if (waypointList != null) {
            waypointList.clear();
            waypointList = null;
        }

        if (tempLocationList != null) {
            tempLocationList.clear();
            tempLocationList = null;
        }
    }

    /**
     * 목적지 검색을 통해 검색된 장소정보를 출발지/목적지/경유지에 맞추어 설정
     * type이 waypoint일때 index를 이용하여 대체/추가를 결정
     *
     * @param index 요청 index
     * @param type  요청 type
     * @param x     x 좌표
     * @param y     y 좌표
     * @param name  장소 명칭
     */
    void setSearchLocation(int index, int type, int x, int y, String name) {
        if (type == RouteEditView.LocationItemView.TYPE_WAYPOINT) {
            //선택된 index가 list의 마지막일경우 새로 추가
            //list의 마지막은 항상 목적지가 되어야 함
            if (index == tempLocationList.size() - 1) {
                tempLocationList.add(index, new LocationItem(x, y, name));
            } else {
                //선택된 index가 list의 마지막이 아닌경우 기존정보 대체
                tempLocationList.set(index, new LocationItem(x, y, name));
            }
        } else {
            //type에서 start / finish정보는 항상 대체
            tempLocationList.set(index, new LocationItem(x, y, name));
        }
    }

    /**
     * 경로탐색 요청
     * 목적지 정보를 이용하여 경로탐색을 요청
     *
     * @param context Context 객체
     * @param x       목적지 x 좌표
     * @param y       목적지 y 좌표
     * @param name    목적지 명칭
     */
    void requestRoute(Context context, int x, int y, String name) {
        //progress dialog 시작
        UIUtils.showProgressDialog(context);

        //경유지 정보 삭제
        waypointList.clear();
        //현재 gps가 사용가능한 상태인지 마지막 수신 정보 확인
        Location lastLocation = NavigationManager.getInstance().getLastGpsLocation();
        if (lastLocation == null) {
            UIUtils.showToast(context, R.string.toast_message_gps_signal_not_found);
            return;
        }

        UTMK utmk =
                UTMK.valueOf(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
        //출발지는 현재위치로 지정
        startLocation =
                new LocationItem(utmk.x, utmk.y, context.getString(R.string.current_location));
        finishLocation = new LocationItem(x, y, name);
        UIController.getInstance().getRouteView().setRouteLocationData(startLocation,
                finishLocation);
        //경로검색
        calculateRoute(context);
    }

    /**
     * 경로탐색 요청
     * 경로 편집화면에서 설정된 출발지/목적지/경유지 정보를 이용하여 새로운 경로정보를 요청
     *
     * @param context Context 객체
     */
    void requestRoute(Context context) {
        //progress dialog 시작
        UIUtils.showProgressDialog(context);
        //출발지는 경로편집 정보에서 첫번째 정보
        startLocation = tempLocationList.get(0);
        //경유지 정보는 기존정보는 삭제후 새로 추가 경로편집 정보에서 두번째부터 마지막 전까지
        waypointList.clear();
        for (int i = 1; i < tempLocationList.size() - 1; i++) {
            waypointList.add(tempLocationList.get(i));
        }
        //도착지 정보는 경로편집 정보에서 마지막 정보
        finishLocation = tempLocationList.get(tempLocationList.size() - 1);
        //경로탐색
        calculateRoute(context);
    }

    /**
     * 경로 계산
     * 현재 설정된 출발지/목적지/경유지 정보를 이용하여 경로정보를 계산
     *
     * @param context Context 객체
     */
    private void calculateRoute(Context context) {
        //출발지 좌표
        UTMK startCoord = new UTMK(startLocation.getX(), startLocation.getY());
        //도착지 좌표
        UTMK destCoord = new UTMK(finishLocation.getX(), finishLocation.getY());
        //경유지 좌표 리스트
        List<UTMK> viaList = null;
        if (waypointList != null && waypointList.size() > 0) {
            viaList = new ArrayList<>();
            for (LocationItem locationItem : waypointList) {
                viaList.add(new UTMK(locationItem.getX(), locationItem.getY()));
            }
        }
        //경로타입, 차량정보 환경설정 정보 받아오기 없는 경우 기본값
        List<RoutePlan.RouteType> routeTypes = getRouteTypeList(context);
        RozeOptions.CarType carType = getCarType(context);
        boolean isHipass = getCarHipass(context);

        //경로정보 생성
        RoutePlan.Builder builder = new RoutePlan.Builder()
                //출발지 좌표
                .start(startCoord)
                //도착지 좌표
                .dest(destCoord)
                //차량타입
                .carType(carType)
                //하이패스 유무 설정
                .hipass(isHipass)
                //경로타입
                .routeTypes(routeTypes);

        //2017.07.19 각도정보 제외
        //gps off상태일때는 bearing정보가 정확하지 않으므로 경로탐색에서 제외
        if (DriveView.isGpsOn) {
            //회전값
            builder.bearing(NavigationManager.getInstance().getLastBearing());
        }

        //정확도(accuracy)에 대한 측정값이 있으면 추가
        if (NavigationManager.getInstance().getLastGpsLocation().hasAccuracy()) {
            builder.accuracy(NavigationManager.getInstance().getLastGpsLocation().getAccuracy());
        }
        //경유지 정보가 있으면 추가
        if (viaList != null) {
            for (UTMK via : viaList) {
                builder.waypoint(via);
            }
        }

        RoutePlan routePlan = builder.build();

        RouteManager routeManager = new RouteManager();
        if (!routeManager.isBusy()) {
            routeManager.calculateRoute(routePlan, this);
        }
    }

    /**
     * 경로타입 반환
     * 경로설정 화면({@link SettingRouteActivity})에서 설정한 정보를 반환
     * sharedpreferences에 저장된 값을 가져오며 설정값이 없는 경우 기본 값으로 반환
     *
     * @param context context 객체
     * @return 경로타입 정보 리스트
     */
    private List<RoutePlan.RouteType> getRouteTypeList(Context context) {
        int routeType1Index = PreferenceUtils.getInt(context, PreferenceUtils.KEY_ROUTE_TYPE_1,
                SettingRouteActivity.DEFAULT_ROUTE_TYPE_1);
        int routeType2Index = PreferenceUtils.getInt(context, PreferenceUtils.KEY_ROUTE_TYPE_2,
                SettingRouteActivity.DEFAULT_ROUTE_TYPE_2);
        List<RoutePlan.RouteType> routeTypes = new ArrayList<>();
        routeTypes.add(RoutePlan.RouteType.values()[routeType1Index]);
        routeTypes.add(RoutePlan.RouteType.values()[routeType2Index]);
        return routeTypes;
    }

    /**
     * 차량타입 반환
     * 차량설정 화면({@link SettingCarActivity})에서 설정한 정보를 반환
     * sharedpreferences에 저장된 값을 가져오며 설정값이 없는 경우 기본 값으로 반환
     *
     * @param context context 객체
     * @return 차량타입 정보
     */
    private RozeOptions.CarType getCarType(Context context) {
        int carTypeIndex = PreferenceUtils.getInt(context, PreferenceUtils.KEY_CAR_TYPE,
                SettingCarActivity.DEFAULT_CAR_TYPE);
        return RozeOptions.CarType.values()[carTypeIndex];
    }

    /**
     * hipass 설정 반환
     * 차량설정 화면({@link SettingCarActivity})에서 설정한 정보를 반환
     * sharedpreferences에 저장된 값을 가져오며 설정값이 없는 경우 기본 값으로 반환
     *
     * @param context context 객체
     * @return hipass 사용유무 반환
     */
    private boolean getCarHipass(Context context) {
        return PreferenceUtils.getBoolean(context, PreferenceUtils.KEY_CAR_HIPASS,
                RozeOptions.getInstance().isHipass());
    }

    /**
     * 출발지, 목적지 상호 변경
     * 출발지와 목적지로 설정된 데이터가 교체
     * 경로편집화면에서 사용하는 임시데이터를 이용
     */
    void changeDestination() {
        LocationItem tempStart = tempLocationList.remove(tempLocationList.size() - 1);
        LocationItem tempFinish = tempLocationList.remove(0);
        tempLocationList.add(0, tempStart);
        tempLocationList.add(tempFinish);

        UIController.getInstance().getRouteView().setRouteLocationData(tempStart, tempFinish);
    }

    /**
     * 경유지 정보 삭제
     *
     * @param index 경유지 Index
     */
    void removeWaypoint(int index) {
        tempLocationList.remove(index);
    }

    /**
     * 경로편집화면에서 사용할 임시 출발지/목적지/경유지 정보
     * 경로탐색을 요청하기전까지만 사용하게됨
     *
     * @return 출발지/목적지/경유지 리스트
     */
    List<LocationItem> getTempLocationList() {
        tempLocationList = new ArrayList<>();
        tempLocationList.add(startLocation);
        tempLocationList.addAll(waypointList);
        tempLocationList.add(finishLocation);
        return tempLocationList;
    }


    //--RouteManager.RouteManagerListener
    @Override
    public void onRouteCalculateFinished(RouteSummary routeSummary) {
        //경로 화면으로 변경
        UIController.getInstance().setMode(UIController.MODE_ROUTE);
        //경로 요약정보 설정
        UIController.getInstance().getRouteView().setRouteSummary(routeSummary);
        //progress dialog 종료
        UIUtils.dismissProgressDialog();
    }

    @Override
    public void onRouteCalculateFailed(RozeError error) {
        UIUtils.dismissProgressDialog();
        UIUtils.showToast(UIController.getInstance().mainActivity,
                R.string.toast_message_route_fail);
    }
    //--RouteManager.RouteManagerListener
}
