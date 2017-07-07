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

package com.kt.rozenavi.ui.main;

import android.graphics.Color;
import android.location.Location;
import android.util.DisplayMetrics;

import com.kt.geom.model.Coord;
import com.kt.geom.model.LatLng;
import com.kt.geom.model.UTMK;
import com.kt.geom.model.UTMKBounds;
import com.kt.maps.GMap;
import com.kt.maps.ViewpointChange;
import com.kt.maps.model.Point;
import com.kt.maps.model.ResourceDescriptor;
import com.kt.maps.model.ResourceDescriptorFactory;
import com.kt.maps.model.Viewpoint;
import com.kt.maps.overlay.Marker;
import com.kt.maps.overlay.MarkerOptions;
import com.kt.maps.overlay.RoutePath;
import com.kt.maps.overlay.RoutePathOptions;
import com.kt.roze.NavigationManager;
import com.kt.roze.data.model.Route;
import com.kt.roze.location.model.RouteLocation;
import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.MapUtils;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 지도 제어 클래스
 * 싱글턴 방식으로 구성되어 있고 지도 헤딩, 지도 이동, 지도 마커 등록 등의
 * 지도관련 기능을 모두 처리
 * 안전운행, 경로검색, 경로안내에 대한 모드를 제공하여 쉽게 각각의 뷰에 대한
 * 지도 상태를 변경가능
 */
public class MapController {
    /**
     * 일반 지도 스타일
     */
    private static final int MAP_STYLE_DEFAULT = 0;
    /**
     * 경로안내 지도 스타일
     */
    private static final int MAP_STYLE_DAY_DRIVE = 1;

    /**
     * 헤딩 타입 : 나침반
     * 지도가 각도에 맞추어 회전
     */
    public static int HEADING_BEARING = 0;
    /**
     * 헤딩 타입 : 정북
     * 내차 마크가 각도에 맞추어 회전
     */
    public static int HEADING_NORTH = 1;

    private static MapController instance;

    /**
     * 현재 지도 스타일
     */
    private int mapStyle = MAP_STYLE_DEFAULT;
    /**
     * 지도 제어를 위한 지도 객체
     */
    private GMap map;
    /**
     * HEADING_BEARING 타입일때 사용하는 pivot정보 객체
     */
    private Point headingPivot;
    /**
     * HEADING_NORTH 타입일때 사용하는 pivot정보 객체
     */
    private Point northPivot;
    /**
     * 경로검색화면에서 사용하는 pivot정보 객체
     */
    private Point summaryPivot;

    /**
     * 현재 지도 pivot정보 객체
     */
    private Point currentPivot;
    /**
     * 현재 지도 모드 정보
     */
    private int currentMode = UIController.MODE_DRIVE;
    /**
     * 현재 지도 헤딩 정보
     */
    private int currentHeading = HEADING_BEARING;
    /**
     * 현재 지도 줌레벨 정보
     */
    private double currentZoom = NaviUtils.DRIVE_MODE_DEFAULT_ZOOM;
    /**
     * 현재 지도 기울기 정보
     */
    private double currentTilt = NaviUtils.MAP_TILT_DEFAULT;
    /**
     * 현재 지도 회전 정보
     */
    private double currentRotation = 0.0;
    /**
     * 지도 초기화 완료전 수신 좌표
     * 지도 설정시 해당 위치를 초기 위치로 설정
     */
    private UTMK firstFixLocation;
    /**
     * 경로 정보를 보여주는 routepath 리스트 객체
     */
    private List<RoutePath> pathList;

    /**
     * 내차 마커 객체
     */
    private Marker carMarker;
    /**
     * 출발지 마커 객체
     */
    private Marker startMarker;
    /**
     * 도착지 마커 객체
     */
    private Marker finishMarker;
    /**
     * 경유지 마커 객체 리스트
     */
    private List<Marker> waypointMarkerList;
    /**
     * 내차 마커 이미지 객체
     */
    private ResourceDescriptor carIndicatorImage;
    /**
     * 현재 선택된 routepath index값
     * 경로검색화면에서 2개의 route를 표시할때 각각의 route를 선택하여
     * 색상 및 z-index 변경
     */
    private int selectedRouteIndex = -1;

    private MapController() {
        if (carIndicatorImage == null) {
            carIndicatorImage = ResourceDescriptorFactory.fromResource(R.drawable.tracking_off);
        }
        pathList = new ArrayList<>();
    }

    public static MapController getInstance() {
        if (instance == null) {
            instance = new MapController();
        }

        return instance;
    }

    /**
     * MapController 객체 종료
     */
    void destroy() {
        instance = null;
    }

    /**
     * 지도 제어를 위해 gmap 객체 설정 및 pivot 정보 생성
     *
     * @param map gmap 객체
     */
    void setMap(GMap map) {
        this.map = map;

        //가로 중앙, 세로 하단으로 헤딩 pivot 생성
        headingPivot = calcPivot(0.5, 0.8);
        //가로 중앙, 세로 중하단으로 정북 pivot 생성
        northPivot = calcPivot(0.5, 0.6);
        //가로 중앙, 세로 중상단으로 경로검색화면 pivot 생성
        summaryPivot = calcPivot(0.5, 0.4);

        //기본 피봇은 headingPivot
        currentPivot = headingPivot;
    }

    /**
     * gmap 객체 설정 여부 반환
     *
     * @return gmap 설정 여부
     */
    public boolean isSetMap() {
        return (map != null);
    }

    /**
     * 지도 pivot 계산
     * 가로 비율은 좌측이 0.0, 우측이 1.0
     * 세로 비율은 상단이 0.0, 하단이 1.0
     * <p>
     * 원하는 pivot 위치의 비율을 입력하면 지도화면의 크기로 계산하여
     * pivot 중심점의 좌표를 반환
     *
     * @param x 가로 비율
     * @param y 세로 비율
     * @return pivot 정보 객체
     */
    public Point calcPivot(double x, double y) {
        return new Point(map.getView().getWidth() * x, map.getView().getHeight() * y);
    }

    /**
     * 현재 지도화면의 viewpoint 정보를 반환
     * 중심좌표, 기울기값, 지도 회전값, 줌레벨 등의 정보를 확인 가능한
     * viewpoint 객체를 반환
     *
     * @return viewpoint 객체
     */
    public Viewpoint getCurrentViewPoint() {
        return map.getViewpoint();
    }

    /**
     * 지도 헤딩 타입 설정 및 pivot 정보 변경
     * MapController.HEADING_NORTH
     * - 지도 고정, 내차마크 회전
     * MapController.HEADING_BEARING
     * - 지도 회전, 내차마크 고정
     *
     * @param heading 헤딩 모드 상수
     */
    public void setHeadingMode(int heading) {
        //입력된 헤딩타입 대치
        this.currentHeading = heading;

        //현재 중심점 확인 pivot기준
        Coord currentCoord = getCurrentLocation();
        if (currentCoord == null) {
            currentCoord = map.getCoordFromViewportPoint(currentPivot);
        }

        //마커와 지도의 중심점을 일단 동일하게 마지막 bearing값으로 셋팅
        float markerBearing = NavigationManager.getInstance().getLastBearing();
        float mapBearing = NavigationManager.getInstance().getLastBearing();

        if(NavigationManager.getInstance().getLastRouteLocation() != null) {
            markerBearing = (float) NavigationManager.getInstance().getLastRouteLocation().angle;
            mapBearing = (float) NavigationManager.getInstance().getLastRouteLocation().angle;
        }

        ViewpointChange viewpointChange;
        if (currentHeading == HEADING_NORTH) {  //정북방향
            currentPivot = northPivot;
            //정북방향이므로 지도 bearing값을 0으로 초기화
            mapBearing = 0;
            viewpointChange = ViewpointChange.builder()
                    .panTo(currentCoord)
                    .pivot(currentPivot)
                    .rotateTo(mapBearing)
                    .build();

        } else if (currentHeading == HEADING_BEARING) { //회전방향
            currentPivot = headingPivot;
            //나침반 모드이므로 marker bearing값을 0으로 초기화
            viewpointChange = ViewpointChange.builder()
                    .panTo(currentCoord)
                    .pivot(currentPivot)
                    .rotateTo(mapBearing)
                    .build();
        } else {
            return;
        }

        //지도 및 내차마크 회전값 적용
        map.change(viewpointChange);
        carMarker.setRotation(markerBearing);
    }

    /**
     * 경로안내화면 routepath객체 생성 및 화면표시
     * 경로안내화면에서 사용할 routepath 생성 하여 Color.BLUE로 기본색상 설정
     *
     * @param route route정보 객체
     */
    public void drawRoute(Route route) {
        //기존 routepath가 있는경우 삭제
        clearRoutePath();

        //routepath객체 생성
        RoutePath path = createRoutePath(route.routePath());
        //routepath객체 색상 설정
        path.setFillColor(Color.BLUE);

        //지도 및 리스트에 routepath 추가
        map.addOverlay(path);
        pathList.add(path);
    }

    /**
     * 경로검색화면 routepath객체 생성 및 화면표시
     * 경로검색화면에서 사용할 routepath 생성 및 화면표시
     * 기본으로 생성후 첫번째 경로를 선택한 상태로 표시
     *
     * @param routeList route정보 리스트 객체
     */
    public void drawSummaryRoutes(List<Route> routeList) {
        //기존 routepath가 있는경우 삭제
        clearRoutePath();

        //routepath객체 생성
        RoutePath path;
        for (Route route : routeList) {
            path = createSummaryRoutePath(route.routePath());

            //지도 및 리스트에 routepath 추가
            map.addOverlay(path);
            pathList.add(path);
        }

        //기본으로 첫번째 route를 선택
        selectRoute(0);
    }

    /**
     * 전체 routepath 삭제
     */
    private void clearRoutePath() {
        if (pathList != null && pathList.size() > 0) {
            for (RoutePath routePath : pathList) {
                map.removeOverlay(routePath);
            }
            pathList.clear();
        }
    }

    /**
     * routepath객체 생성
     * 입력받은 path의 좌표정보를 이용하여 routepath를 생성
     * 기본색상은 Color.GRAY로 설정
     *
     * @param pathPointList path좌표정보 리스트
     * @return routepath 객체
     */
    private RoutePath createRoutePath(List<UTMK> pathPointList) {
        return new RoutePath(new RoutePathOptions().addPoints(pathPointList)
                .bufferWidth(map.getResolution() *
                        MapUtils.ROUTE_PATH_WIDTH_IN_DP)
                .strokeWidth(1)
                .strokeColor(Color.DKGRAY)
                //2017.07.07 add path pattern
                .period(100)
                .hasPeriodicImage(true)
                .periodicImage(ResourceDescriptorFactory.fromAsset("slash.png"))
                //2017.07.07 add path pattern
                .passedFillColor(Color.GRAY)
                .fillColor(Color.GRAY));
    }

    /**
     * 요약경로 용 routepath객체 생성
     * 입력받은 path의 좌표정보를 이용하여 routepath를 생성
     * 기본색상은 Color.GRAY로 설정
     *
     * @param pathPointList path좌표정보 리스트
     * @return routepath 객체
     */
    private RoutePath createSummaryRoutePath(List<UTMK> pathPointList) {
        return new RoutePath(new RoutePathOptions().addPoints(pathPointList)
                .bufferWidth(map.getResolution() *
                        MapUtils.ROUTE_PATH_WIDTH_IN_DP)
                .strokeWidth(1)
                .strokeColor(Color.DKGRAY)
                .passedFillColor(Color.GRAY)
                .fillColor(Color.GRAY));
    }

    /**
     * routepath 활성/비활성 기능
     * 경로검색화면에서 route 선택시 기존선택된 route와 새로선택된 route에
     * 해당하는 routepath의 색상과 z-index를 변경
     *
     * @param index route index 정보
     */
    public void selectRoute(int index) {
        if (selectedRouteIndex > -1) {
            pathList.get(selectedRouteIndex).setFillColor(Color.GRAY);
        }
        selectedRouteIndex = index;

        map.removeOverlay(pathList.get(selectedRouteIndex));
        map.addOverlay(pathList.get(selectedRouteIndex));
        pathList.get(selectedRouteIndex).setFillColor(Color.BLUE);
    }

    /**
     * routepath 전체 숨김
     * 경로검색화면에서 경로편집시 화면에 표시된 모든 경로를 숨김
     */
    public void hideRouteList() {
        if (pathList != null && pathList.size() > 0) {
            for (RoutePath routePath : pathList) {
                routePath.setVisible(false);
            }
        }
    }

    /**
     * routepath 전체 표시
     * 경로검색화면에서 경로편집 종료시 화면에 다시 모든 경로를 표시
     */
    public void showRouteList() {
        if (pathList != null && pathList.size() > 0) {
            for (RoutePath routePath : pathList) {
                routePath.setVisible(true);
            }
        }
    }

    /**
     * 내차 마커 이동
     * 현재 설정된 pivot 위치에 내차 마커를 이동
     * 현재 맵매칭 중인지 여부를 이용하여 routepath 현재 통과한 정보를 표시
     *
     * @param isEnterInRoutePath 경로에 맵매칭 여부
     */
    public void moveCarMarker(boolean isEnterInRoutePath) {
        //현재 설정된 pivot정보에 해당하는 좌표를 반환
        UTMK coord = (UTMK) map.getCoordFromViewportPoint(currentPivot);
        //좌표를 이용한 내차마커 이동
        moveCarMarker(coord, isEnterInRoutePath);
    }

    /**
     * 내차 마커 이동
     * 입력된 좌표에 내차 마커를 이동
     * 현재 맵매칭 중인지 여부를 이용하여 routepath 현재 통과한 정보를 표시
     *
     * @param coord              좌표 객체
     * @param isEnterInRoutePath 경로에 맵매칭 여부
     */
    public void moveCarMarker(Coord coord, boolean isEnterInRoutePath) {
        //현재 경로검색화면 일경우 기능 종료
        if (currentMode == UIController.MODE_ROUTE) {
            return;
        }
        if (carMarker == null) {    //내차 마커가 생성되지 않은 경우
            //마커 옵션 설정
            MarkerOptions markerOptions = new MarkerOptions()
                    //좌표 설정
                    .position(coord)
                    //마커 아이콘 설정
                    .icon(carIndicatorImage)
                    //마커 사이즈 설정
                    .iconSize(new Point(NaviUtils.CAR_MARKER_ICON_SIZE,
                            NaviUtils.CAR_MARKER_ICON_SIZE))
                    //설정된 좌표가 마커의 어느위치에 기준점이 될지 설정
                    //기본은 0.5, 1.0으로 하단 중앙에 위치
                    .anchor(new Point(0.5, 0.5))
                    .flat(true)
                    //화면에 표시 여부
                    .visible(true);
            //마커 옵션을 이용하여 마커 생성
            carMarker = new Marker(markerOptions);
            //지도에 마커 추가
            map.addOverlay(carMarker);
        } else {    //내차 마커가 있는 경우
            carMarker.setPosition(coord);
            carMarker.bringToFront();
        }
        //routepath가 있고 맵매칭이 되고 있는상황일때
        if (pathList.size() > 0 && isEnterInRoutePath) {
            //좌표에 해당하는 routepath까지 통과정보로 설정
            pathList.get(0).setSplitCoord(coord);
        }
    }

    /**
     * 내차 마커 아이콘 변경
     * 입력된 resource id에 해당하는 이미지로 내차 마커를 변경
     *
     * @param resId 이미지 resource id
     */
    public void setCarMarkerIcon(int resId) {
        carIndicatorImage = ResourceDescriptorFactory.fromResource(resId);
        if (carMarker != null) {
            carMarker.setIcon(carIndicatorImage);
        }
    }

    /**
     * 지도 모드 설정
     * uicontroller에서 제공하는 mode상수를 공통으로 사용하며 uicontroller의 mode변경과
     * 함께 동기화 해서 변경한다.
     * UIController.MODE_DRIVE, UIController.MODE_NAVIGATION 시작시는 지도 viewchange가 일어난다
     *
     * @param mode 지도 모드 정보
     */
    void setMode(int mode) {
        //지도 모드 대치
        currentMode = mode;
        //viewchange가 일어나는 경우 사용할 객체
        ViewpointChange change = null;
        if (currentMode == UIController.MODE_DRIVE) {
            //안전운행 모드 처리
            change = changeDriveMode();
            applyDefaultStyle();
        } else if (currentMode == UIController.MODE_ROUTE) {
            //경로검색 모드 처리
            changeRouteMode();
            applyDefaultStyle();
        } else if (currentMode == UIController.MODE_NAVIGATION) {
            //경로안내 모드 처리
            change = changeNavigationMode();
            applyDriveStyle();
        }

        //viewchange가 발생할때 처리
        if (change != null) {
            map.change(change);
        }
    }

    /**
     * 지도 스타일 적용
     * 일반 지도 스타일
     * 안전운행모드, 경로검색모드 적용
     */
    private void applyDefaultStyle() {
        if (mapStyle == MAP_STYLE_DEFAULT) {
            return;
        }
        map.setStyle(ResourceDescriptorFactory.fromResource(R.raw.com_kt_maps_style));
        map.setSyetemImage(ResourceDescriptorFactory
                        .fromResource(R.drawable.com_kt_maps_totalimage),
                ResourceDescriptorFactory.fromResource(R.raw.com_kt_maps_totalimage));
        mapStyle = MAP_STYLE_DEFAULT;
    }

    /**
     * 지도 스타일 적용
     * 도로 강조 스타일
     * 경로안내모드 적용
     */
    private void applyDriveStyle() {
        if (mapStyle == MAP_STYLE_DAY_DRIVE) {
            return;
        }
        map.setStyle(ResourceDescriptorFactory.fromResource(R.raw.day_navigation));
        map.setSyetemImage(ResourceDescriptorFactory
                        .fromResource(R.drawable.com_kt_maps_totalimage),
                ResourceDescriptorFactory.fromResource(R.raw.com_kt_maps_totalimage));
        mapStyle = MAP_STYLE_DAY_DRIVE;
    }

    /**
     * 안전운행 모드 변경
     * 내차 마커를 제외한 출발지, 도착지, 경유지 마커를 삭제
     * 지도에 표시되어있는 routepath를 삭제
     * 현재위치로 줌레벨을 변경하는 viewchange 반환
     *
     * @return viewchange객체 반환
     */
    private ViewpointChange changeDriveMode() {
        //routepath 삭제
        clearRoutePath();
        //내차 마커 표시 설정
        if (carMarker != null) {
            carMarker.setVisible(true);
        }
        //출발지 마커 제거
        if (startMarker != null) {
            map.removeOverlay(startMarker);
            startMarker = null;
        }
        //도착지 마커 제거
        if (finishMarker != null) {
            map.removeOverlay(finishMarker);
            finishMarker = null;
        }
        //경유지 마커 제거
        clearWaypoint();

        //수신된 gps좌표 반환
        Location lastLocation = NavigationManager.getInstance().getLastGpsLocation();
        ViewpointChange change = null;
        if (lastLocation != null) {
            change = ViewpointChange.builder()
                    //수신된 좌표로 위치 설정
                    .panTo(UTMK.valueOf(new LatLng(lastLocation.getLatitude(),
                            lastLocation.getLongitude())))
                    .pivot(currentPivot)
                    .tiltTo(NaviUtils.MAP_TILT_DEFAULT)
                    //안전운행 모드 기본 줌레벨 설정
                    .zoomTo(NaviUtils.DRIVE_MODE_DEFAULT_ZOOM)
                    .build();
        }
        return change;
    }

    /**
     * 경로검색 모드 변경
     * 내차 마커 숨김처리
     */
    private void changeRouteMode() {
        //내차 마커 숨김 설정
        if (carMarker != null) {
            carMarker.setVisible(false);
        }
    }

    /**
     * 경로안내 모드 변경
     * 내차 마커 표출, 출발지 마커는 삭제
     * 현재위치로 줌레벨을 변경하는 viewchange 반환
     *
     * @return viewchange객체 반환
     */
    private ViewpointChange changeNavigationMode() {
        //내차 마커 표시 설정
        if (carMarker != null) {
            carMarker.setVisible(true);
        }
        //출발지 마커 삭제
        if (startMarker != null) {
            map.removeOverlay(startMarker);
            startMarker = null;
        }

        //수신된 gps좌표 반환
        Location lastLocation = NavigationManager.getInstance().getLastGpsLocation();
        ViewpointChange change = null;
        if (lastLocation != null) {
            change = ViewpointChange.builder()
                    //수신된 좌표로 위치 설정
                    .panTo(UTMK.valueOf(new LatLng(lastLocation.getLatitude(),
                            lastLocation.getLongitude())))
                    .pivot(currentPivot)
                    .tiltTo(NaviUtils.MAP_TILT_DEFAULT)
                    //경로안내 모드 기본 줌레벨 설정
                    .zoomTo(NaviUtils.NAVIGATION_MODE_DEFAULT_ZOOM)
                    .build();
        }
        return change;
    }


    /**
     * 지도의 viewpoint가 변경이 일어났을때 발생하는 리스너 정보 처리
     * GMap.OnViewpointChangeListener.onViewpointChange() 이벤트 발생시
     * routepath의 bufferwidth를 지도의 줌레벨과 상수를 이용하여 재설정
     * 지도의 줌레벨이 변경되어도 routepath가 일정한 width를 가질수 있도록 처리
     *
     * @param map       지도 객체
     * @param viewpoint 변경된 viewpoint 객체
     * @param gesture   gesture를 통한 동작 여부
     */
    public void onViewpointChange(GMap map, Viewpoint viewpoint, boolean gesture) {
        if (pathList != null && pathList.size() > 0) {
            for (RoutePath routePath : pathList) {
                routePath.setBufferWidth(map.getResolution() * MapUtils.ROUTE_PATH_WIDTH_IN_DP);
                //2017.07.07 add path pattern
                if(routePath.getHasPeriodicImage()){
                    routePath.setPeriod(getPeriodicDistance(viewpoint.zoom));
                }
                //2017.07.07 add path pattern
            }
        }
        currentZoom = viewpoint.zoom;
        currentTilt = viewpoint.tilt;
        currentRotation = viewpoint.rotation;
    }

    /**
     * 마커 객체 생성
     * 좌표와 마커 이미지 resource id를 이용하여 객체를 생성
     *
     * @param coord      좌표 정보 객체
     * @param resourceId 마커 이미지 resource id
     * @return 생성된 마커 객체
     */
    private Marker createMarker(UTMK coord, int resourceId) {
        MarkerOptions markerOptions = new MarkerOptions().position(coord)
                .icon(ResourceDescriptorFactory.fromResource(
                        resourceId))
                .visible(true);
        return new Marker(markerOptions);
    }

    /**
     * 지도 viewpoint 변경
     * viewpoint 정보를 이용하여 지도를 이동
     *
     * @param viewpoint viewpoint 객체
     */
    public void changeViewpoint(Viewpoint viewpoint) {
        map.moveTo(viewpoint);
    }

    /**
     * 지도 viewpoint 변경
     * 경로검색화면에서 사용하는 기능
     * 지도상에 routepath의 정보가 한번에 표시되도록 routepath의 bound정보를 이용하여
     * 줌레벨을 계산하여 viewpoint 변경
     * pivot정보는 현재 설정되어있는 정보를 이용
     *
     * @param bounds          지도상에 표시되어야 하는 영역 bound객체
     * @param duration        화면 변경 애니메이션 시간
     * @param animationTiming 화면 변경 애니메이션 타입
     */
    public void changeViewpoint(
            UTMKBounds bounds, int duration, GMap.AnimationTiming animationTiming) {
        DisplayMetrics metrics = map.getView().getResources().getDisplayMetrics();

        //가로/세로 해상도를 구해서 큰값으로 zoomlevel 계산
        double widthResolution = (bounds.getWidth() / (map.getView().getWidth() / metrics.density));
        double heightResolution =
                (bounds.getHeight() / (map.getView().getWidth() / metrics.density));
        double targetResolution =
                widthResolution > heightResolution ? widthResolution : heightResolution;

        int zoomlevel = MapUtils.calcMapZoomlevel(targetResolution);

        changeViewpoint(bounds.getCenter(), 0, 0, zoomlevel, summaryPivot, duration,
                animationTiming);
    }

    /**
     * 지도 viewpoint 변경
     * 좌표, 회전 값, 애니메이션 시간, 애니메이션 정보를 이용하여 지도화면을 이동
     * pivot정보, 기울기 값, 줌레벨은 현재 설정되어있는 정보를 이용
     *
     * @param coord           좌표 정보 객체
     * @param rotation        회전 값
     * @param duration        화면 변경 애니메이션 시간
     * @param animationTiming 화면 변경 애니메이션 타입
     */
    public void changeViewpoint(
            UTMK coord, double rotation, int duration,
            GMap.AnimationTiming animationTiming) {
        changeViewpoint(coord, currentTilt, rotation, currentZoom, currentPivot, duration,
                animationTiming);
    }

    /**
     * 지도 viewpoint 변경
     * 좌표, 기울기 값, 회전 값, 애니메이션 시간, 애니메이션 정보를 이용하여 지도화면을 이동
     * pivot정보, 줌레벨은 현재 설정되어있는 정보를 이용
     *
     * @param coord           좌표 정보 객체
     * @param tilt            기울기 값
     * @param rotation        회전 값
     * @param duration        화면 변경 애니메이션 시간
     * @param animationTiming 화면 변경 애니메이션 타입
     */
    public void changeViewpoint(
            UTMK coord, double tilt, double rotation, int duration,
            GMap.AnimationTiming animationTiming) {
        changeViewpoint(coord, tilt, rotation, currentZoom, currentPivot, duration,
                animationTiming);
    }

    /**
     * 지도 viewpoint 변경
     * 좌표, 기울기 값, 회전 값, 줌레벨 값, 화면 pivot정보, 애니메이션 시간, 애니메이션 정보를
     * 이용하여 지도화면을 이동
     * 기울기값, 회전값, 줌레벨값은 설정하지 않는경우 -1로 입력
     * pivot정보는 변경하지 않는경우 null 입력
     * 애니메이션을 사용하지 않는경우는 애니메이션시간은 -1, 애니메이션 타입은 null로 입력
     *
     * @param coord           좌표 정보 객체
     * @param tilt            기울기 값
     * @param rotation        회전 값
     * @param zoom            줌레벨 값
     * @param pivot           화면 pivot 정보
     * @param duration        화면 변경 애니메이션 시간
     * @param animationTiming 화면 변경 애니메이션 타입
     */
    public void changeViewpoint(
            UTMK coord, double tilt, double rotation, double zoom, Point pivot, int duration,
            GMap.AnimationTiming animationTiming) {

        //지도 객체가 없는 경우 기능 종료
        //지도 객체가 초기화 될경우 설정할 좌표를 백업
        if (map == null) {
            firstFixLocation = coord;
            return;
        }
        ViewpointChange.Builder builder = ViewpointChange.builder();
        //좌표 설정
        builder.panTo(coord);
        //기울기 값이 있는경우 설정
        //설정하지 않는경우 -1로 입력받음
        if (tilt >= 0) {
            builder.tiltTo(tilt);
        }
        //회전값이 있는경우 설정
        //설정하지 않는경우 -1로 입력받음
        if (rotation >= 0) {
            //현재 헤딩 타입에 따라서 지도회전 / 내차 마커 회전을 설정
            builder.rotateTo(currentHeading == HEADING_BEARING ? rotation : 0);
            if(carMarker != null) {
                carMarker.setRotation((float) rotation);
            }
        }
        //줌레벨값 있는경우 설정
        //설정하지 않는경우 -1로 입력받음
        if (zoom >= 0) {
            builder.zoomTo(zoom);
        }

        //pivot정보가 있는경우 설정
        if (pivot != null) {
            builder.pivot(pivot);
        } else {
            builder.pivot(currentPivot);
        }

        //애니메이션 시간과 애니메이션 타입에 따라 화면이동
        if (duration > 0 && animationTiming != null) {
            map.animate(builder.build(), duration, animationTiming);
        } else {
            map.change(builder.build());
        }
    }

    /**
     * 내위치로 이동
     *
     * @return 내위치 이동 동작여부
     */
    public boolean showCurrentLocation() {
        UTMK coord = getCurrentLocation();
        if (coord == null) {
            return false;
        }
        //내위치 이동
        changeViewpoint(coord, currentTilt, -1, currentZoom, currentPivot, 0, null);
        return true;
    }

    /**
     * 현재 위치 반환
     * 마지막 gps 위치정보를 이용하며 네비게이션 모드일경우
     * 맵매칭 좌표를 확인하여 해당 좌표로 이동
     *
     * @return 현재 위치 좌표
     */
    private UTMK getCurrentLocation() {
        //마지막 gps 수신위치를 가져옴
        Location lastGpsLocation = NavigationManager.getInstance().getLastGpsLocation();
        //수신된 gps 가 없는경우는 종료
        if (lastGpsLocation == null) {
            return null;
        }

        //wgs84좌표를 지도에서 사용할 수 있는 utmk좌표로 변환
        //변환된 좌표는 내위치로 활용
        UTMK coord = MapUtils.convertLocationToUtmk(lastGpsLocation);

        //navigation mode일때
        if (NavigationManager.getInstance().getMode() == NavigationManager.Mode.NAVIGATING) {
            //마지막으로 맵 매칭된 좌표를 가져옴
            RouteLocation routeLocation = NavigationManager.getInstance().getLastRouteLocation();
            //맵매칭된 좌표가 있는경우 내위치 좌표를 맵매칭된 좌표로 대치
            if (routeLocation != null) {
                coord = routeLocation.location;
            }
        }
        return coord;
    }

    void setInitLocation(double lat, double lon) {
        UTMK coord = UTMK.valueOf(new LatLng(lat, lon));

        if (firstFixLocation != null) {
            coord = firstFixLocation;
            firstFixLocation = null;
        }

        changeViewpoint(coord, currentTilt, currentRotation, currentZoom, currentPivot, 0, null);
    }

    /**
     * 출발지 마커 위치 설정
     * 출발지 마커의 위치정보를 설정
     *
     * @param coord 위치정보 객체
     */
    public void setStartMarkerPosition(UTMK coord) {
        if (startMarker == null) {
            //출발지 마커가 생성되지 않은경우 새로 생성
            startMarker = createMarker(coord, R.drawable.ico_route_map_start);
            //마커의 아이콘 사이즈를 설정 dp단위로 입력
            startMarker.setIconSize(new Point(NaviUtils.ROUTE_LOCATION_MARKER_ICON_WIDTH,
                    NaviUtils.ROUTE_LOCATION_MARKER_ICON_HEIGHT));
            map.addOverlay(startMarker);
        } else {
            startMarker.setPosition(coord);
        }
    }

    /**
     * 도착지 마커 위치 설정
     * 도착지 마커의 위치정보를 설정
     *
     * @param coord 위치정보 객체
     */
    public void setFinishMarkerPosition(UTMK coord) {
        if (finishMarker == null) {
            //도착지 마커가 생성되지 않은경우 새로 생성
            finishMarker = createMarker(coord, R.drawable.ico_route_map_arrive);
            //마커의 아이콘 사이즈를 설정 dp단위로 입력
            finishMarker.setIconSize(new Point(NaviUtils.ROUTE_LOCATION_MARKER_ICON_WIDTH,
                    NaviUtils.ROUTE_LOCATION_MARKER_ICON_HEIGHT));
            map.addOverlay(finishMarker);
        } else {
            finishMarker.setPosition(coord);
        }
    }

    /**
     * 경유지 마커 위치 설정
     * 경유지 마커의 위치정보를 설정
     *
     * @param coordList 위치정보 객체 리스트
     */
    public void setWaypointMarkerPosition(List<UTMK> coordList) {
        //경유지 마커 삭제
        clearWaypoint();

        Marker waypoint;
        for (UTMK utmkCoord : coordList) {
            waypoint = createMarker(utmkCoord, R.drawable.ico_route_map_via);
            waypoint.setIconSize(new Point(NaviUtils.ROUTE_LOCATION_MARKER_ICON_WIDTH,
                    NaviUtils.ROUTE_LOCATION_MARKER_ICON_HEIGHT));
            map.addOverlay(waypoint);
            waypointMarkerList.add(waypoint);
        }
    }

    /**
     * 전체 경유지 마커 삭제
     */
    private void clearWaypoint() {
        if (waypointMarkerList != null) {
            for (Marker marker : waypointMarkerList) {
                map.removeOverlay(marker);
            }
            waypointMarkerList.clear();
        } else {
            waypointMarkerList = new ArrayList<>();
        }
    }

    /**
     * 지도에 마커 추가
     * 전달된 마커객체를 지도에 추가
     *
     * @param marker 마커 객체
     */
    public void addMarker(Marker marker) {
        map.addOverlay(marker);
    }

    /**
     * 지도에서 마커 삭제
     * 전달된 마커객체를 지도에서 삭제
     *
     * @param marker 마커 객체
     */
    public void removeMarker(Marker marker) {
        map.removeOverlay(marker);
    }

    //2017.07.07 add path pattern
    /**
     * periodic number
     * 레벨에 따른 거리 periodic distance(M) return
     */
    private final static int MAX_ZOOM = 14;
    private final static int DEFAULT_PERIOD_DISTANCE = 25;
    private int getPeriodicDistance(float zoomlevel){
        int zoom = MAX_ZOOM - Math.round(zoomlevel);
        int fixedDistance = (int)Math.pow(2,zoom);
        return fixedDistance * DEFAULT_PERIOD_DISTANCE;
    }
    //2017.07.07 add path pattern
}
