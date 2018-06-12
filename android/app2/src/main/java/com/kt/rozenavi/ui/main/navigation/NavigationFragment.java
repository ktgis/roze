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

import android.arch.lifecycle.Observer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.kt.geom.model.Coord;
import com.kt.geom.model.LatLng;
import com.kt.geom.model.UTMK;
import com.kt.maps.GMap;
import com.kt.maps.ViewpointChange;
import com.kt.maps.model.Point;
import com.kt.maps.model.ResourceDescriptorFactory;
import com.kt.maps.overlay.Marker;
import com.kt.maps.overlay.MarkerOptions;
import com.kt.maps.overlay.RoutePath;
import com.kt.maps.overlay.RoutePathOptions;
import com.kt.roze.NavigationManager;
import com.kt.roze.RozeError;
import com.kt.roze.RozeOptions;
import com.kt.roze.SoundManager;
import com.kt.roze.data.model.Lane;
import com.kt.roze.data.model.Route;
import com.kt.roze.data.model.SafetySummary;
import com.kt.roze.data.model.WayPoint;
import com.kt.roze.guidance.model.HighwayGuidance;
import com.kt.roze.guidance.model.IntervalSpeedSpotGuidance;
import com.kt.roze.guidance.model.OilPriceGuidance;
import com.kt.roze.guidance.model.SafetySpotGuidance;
import com.kt.roze.guidance.model.SafetySpotInterface;
import com.kt.roze.guidance.model.Sound;
import com.kt.roze.guidance.model.TurnGuidance;
import com.kt.roze.location.model.GeoLocation;
import com.kt.roze.location.model.RouteLocation;
import com.kt.roze.resource.SoundResourceManager;
import com.kt.roze.routing.RouteSummary;
import com.kt.rozenavi.R;
import com.kt.rozenavi.data.NavigationData;
import com.kt.rozenavi.data.model.LowestGasEventData;
import com.kt.rozenavi.data.model.RemainEventData;
import com.kt.rozenavi.data.model.SafetyEventData;
import com.kt.rozenavi.data.model.ViewpointChangeEventData;
import com.kt.rozenavi.provider.LocationProvider;
import com.kt.rozenavi.provider.MapProvider;
import com.kt.rozenavi.ui.component.SpeedMeterView;
import com.kt.rozenavi.ui.component.core.BaseFragment;
import com.kt.rozenavi.ui.main.alarm.NightAlarmBroadcastReceiver;
import com.kt.rozenavi.ui.main.navigation.view.NavigationHighWayView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationHipassView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationLaneView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationLowestGasView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationMenuView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationRemainView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationRoadView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationSpotView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationTbtView;
import com.kt.rozenavi.utils.CommonUtils;
import com.kt.rozenavi.utils.MapUtils;
import com.kt.rozenavi.utils.NaviUtils;
import com.kt.rozenavi.utils.RouteListenerAdpater;
import com.kt.rozenavi.utils.UIUtils;
import com.kt.rozenavi.utils.WeakReferenceHandler;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class NavigationFragment extends BaseFragment implements NavigationManager.RouteListener,
        WeakReferenceHandler.OnMessageHandler, NavigationManager.RerouteListener,
        NavigationManager.SoundListener {
    @BindView(R.id.speed_value_layout)
    protected SpeedMeterView speedMeterView;
    @BindView(R.id.compass_button)
    protected ImageView compassButton;

    /**
     * TBT 표시 View
     */
    @BindView(R.id.tbt_guidance_view)
    protected NavigationTbtView tbtGuidanceView;
    /**
     * Remain View
     */
    @BindView(R.id.remain_guidance_view)
    protected NavigationRemainView remainGuidanceView;
    /**
     * Drive Menu(경로저장 , 재탐색 , 경로취소) View
     */
    @BindView(R.id.drive_menu_view)
    protected NavigationMenuView driveMenuView;
    /**
     * RoadView Image View
     */
    @BindView(R.id.roadview_guidance_view)
    protected NavigationRoadView roadViewGuidanceView;
    /**
     * 차선 정보 표시 View
     */
    @BindView(R.id.lane_guidance_view)
    protected NavigationLaneView laneGuidanceView;
    /**
     * 하이패스 정보 표시 View
     */
    @BindView(R.id.hipass_guidance_view)
    protected NavigationHipassView hipassGuidanceView;
    /**
     * 고속도로 정보 표시 View
     */
    @BindView(R.id.highway_guidance_view)
    protected NavigationHighWayView highwayGuidanceView;
    /**
     * 안전운행 정보 표시 View
     */
    @BindView(R.id.spot_guidance_view)
    protected NavigationSpotView spotGuidanceView;
    /**
     * 최저가 주유소 정보 표시 View
     */
    @BindView(R.id.lowest_gas_guidance_view)
    protected NavigationLowestGasView lowestGasGuidanceView;

    private ZoomChanger zoomChanger = new ZoomChanger();

    private RouteListenerAdpater routeListenerAdpater;
    private RouteSummary routeSummary;
    private int routeIndex;

    private GMap gMap;
    private WeakReferenceHandler handler = new WeakReferenceHandler(this);
    private int currentMarkerIcon = R.drawable.my_location_navigation_on;
    private Marker currentLocationMarker;
    private RoutePath currentRoutePath;
    private List<Marker> routePointList;
    private Point currentPivot;

    private boolean isFixedCurrentLocation = true;
    private boolean isHeading = true;

    private boolean isArrived = false;
    private boolean isRerouting = false;

    //getInstance는 상황에 따라서 선택 사용
    //파라매터 없는 경우
    public static Fragment getInstance() {
        return new NavigationFragment();
    }

    public NavigationFragment() {
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.setRerouteListener(this);
    }

    @Override
    protected View createView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        //layout을 지정하면 자동으로 Butterknife bind
        return inflater.inflate(R.layout.fragment_navigation, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (routeListenerAdpater != null) {
            routeListenerAdpater = null;
        }

        if (NavigationManager.getInstance().getMode() == NavigationManager.Mode.TRACKING && isArrived) {
            getActivity().onBackPressed();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Activity 에서 onNewIntent 동작시 해당 fragment까지 전달받을 경우 처리
    }

    @Override
    public void onDestroy() {
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.setRerouteListener(null);
        NavigationManager.getInstance().setSoundListener(null);

        tbtGuidanceView.releaseMap();
        spotGuidanceView.releaseMap();
        lowestGasGuidanceView.releaseMap();
        zoomChanger.releaseMap();

        if (gMap != null) {
            if (currentLocationMarker != null) {
                gMap.removeOverlay(currentLocationMarker);
            }
            gMap.setOnAnimationEndListener(null);
        }

        clearOverlay();

        if (navigationManager.getMode() == NavigationManager.Mode.NAVIGATING) {
            navigationManager.stopNavigation(NavigationManager.RouteFinishMode.USER_FINISH);
        }
        unregisterReceiver();
        super.onDestroy();
    }

    @Override
    protected void init() {
        //data 초기화
        initData();
        //view 초기화
        initView();

        registerReceiver();
    }

    //view에 대한 기본 parameter 초기화
    //data 생성 및 설정부분 제외
    private void initView() {
        //view에 대한 초기값 설정 및 상태값 변경
        NavigationManager navigationManager = NavigationManager.getInstance();

        driveMenuView.setOnMenuClickListener(new NavigationMenuView.OnNavigationStopListener() {
            @Override
            public void onStopClick() {
                getActivity().onBackPressed();
            }
        });

        Route route = routeSummary.getActiveRoute();
        tbtGuidanceView.setRoute(route);
        zoomChanger.setRoute(route);

        updateRemain(route.time, route.distance);
    }

    private void playStartSound(SafetySummary summary) {
        Sound sound;
        if (summary != null && summary.hasSafetySection) {
            sound = SoundResourceManager.getSafetySummarySound(summary);
        } else {
            sound = SoundResourceManager.getRouteSound(SoundResourceManager.RouteState.ROUTE_START);
        }

        if (sound != null) {
            NavigationManager.getInstance().playNavigationSound(sound);
        }
    }

    @Override
    public boolean canGoBack() {
        if (NavigationManager.getInstance().getMode() == NavigationManager.Mode.NAVIGATING) {
            showNavigationModeExitAlertDialog();
            return false;
        }
        return true;
    }

    private void showNavigationModeExitAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("경로 안내 종료");
        builder.setMessage("경로 안내를 종료합니다.");
        builder.setPositiveButton("경로안내 종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(
                    DialogInterface dialog, int id) {
                try {
                    NavigationManager.getInstance().stopNavigation(NavigationManager.RouteFinishMode.USER_FINISH);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                getActivity().onBackPressed();
            }
        });
        builder.setNegativeButton("앱 종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(
                    DialogInterface dialog, int id) {
                getActivity().finish();
            }
        });
        builder.show();
    }

    public void drawRoute(Route route) {
        if (gMap == null) {
            return;
        }
        //기존 routepath가 있는경우 삭제
        clearOverlay();
        //routepath객체 생성
        currentRoutePath = createRoutePath(route.routePath());

        //지도 및 리스트에 routepath 추가
        gMap.addOverlay(currentRoutePath);
        routePointList = new ArrayList<>();

        //waypoint
        if (!CommonUtils.isEmpty(routeSummary.routePlan.waypoints)) {
            for (UTMK coord : routeSummary.routePlan.waypoints) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.anchor(new Point(0.5, 1.0))
                        .icon(ResourceDescriptorFactory.fromResource(R.drawable.route_marker_waypoint))
                        .iconSize(new Point(30, 49))
                        .position(coord).visible(true);
                Marker marker = new Marker(markerOptions);
                routePointList.add(marker);
                gMap.addOverlay(marker);
            }
        }

        //end point
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.anchor(new Point(0.5, 1.0))
                .icon(ResourceDescriptorFactory.fromResource(R.drawable.route_marker_end))
                .iconSize(new Point(30, 49))
                .position(route.routePath().get(route.routePath().size() - 1)).visible(true);
        Marker marker = new Marker(markerOptions);
        routePointList.add(marker);
        gMap.addOverlay(marker);
    }

    private RoutePath createRoutePath(List<UTMK> pathPointList) {
        return new RoutePath(new RoutePathOptions().addPoints(pathPointList)
                .bufferWidth(gMap.getResolution() * 6)
                .strokeWidth(1)
                .strokeColor(Color.DKGRAY)
                .passedFillColor(getResources().getColor(R.color.elephant_grey))
                .fillColor(getResources().getColor(R.color.cool_red)));
    }

    private void clearOverlay() {
        if (currentRoutePath != null) {
            gMap.removeOverlay(currentRoutePath);
        }
        if (routePointList != null && !routePointList.isEmpty()) {
            for (Marker marker : routePointList) {
                gMap.removeOverlay(marker);
            }
        }
    }

    private void resetGuidanceView() {
        tbtGuidanceView.clearOverlay();
        highwayGuidanceView.setVisibility(View.INVISIBLE);
        hipassGuidanceView.setVisibility(View.INVISIBLE);
        roadViewGuidanceView.setVisibility(View.INVISIBLE);
        laneGuidanceView.setVisibility(View.INVISIBLE);
        spotGuidanceView.setVisibility(View.INVISIBLE);
        spotGuidanceView.clearOverlay();
        lowestGasGuidanceView.setVisibility(View.INVISIBLE);
        lowestGasGuidanceView.clearOverlay();
    }

    //getArguments() 및 초기 데이터 쿼리 관련 초기화
    private void initData() {
        //그외 데이터 쿼리 로직
        LocationProvider locationProvider = LocationProvider.getInstance();
        MapProvider mapProvider = MapProvider.getInstance();
        locationProvider.location.observe(this, new Observer<GeoLocation>() {
            @Override
            public void onChanged(@Nullable GeoLocation geoLocation) {
                if (geoLocation == null || gMap == null) {
                    return;
                }
                if (!geoLocation.hasRoutedLocation() && geoLocation.location == null) {
                    return;
                }

                int currentSpeed = geoLocation.location == null ? 0 : NaviUtils.calculateSpeed(geoLocation.location);
                float angle;
                UTMK coord;
                if (geoLocation.hasRoutedLocation()) {
                    angle = geoLocation.routeLocation.angle;
                    coord = geoLocation.routeLocation.location;
                } else {
                    if (geoLocation.location == null) {
                        return;
                    }

                    if (currentSpeed > 0) {
                        angle = geoLocation.location.hasBearing() ? geoLocation.location.getBearing() : 0;
                    } else {
                        angle = 0;
                    }

                    coord = MapUtils.convertLocationToUtmk(geoLocation.location);
                }

                speedMeterView.setSpeed(currentSpeed);
                spotGuidanceView.setSpeed(currentSpeed);
                if (zoomChanger != null) {
                    zoomChanger.setSpeedAndLocation(currentSpeed, geoLocation.routeLocation);
                }

                if (isFixedCurrentLocation && !isFreezeMap) {
                    gMap.animate(
                            ViewpointChange.builder()
                                    .rotateTo(isHeading ? angle : 0)
                                    .tiltTo(isHeading ? 45 : 0)
                                    .panTo(coord)
                                    .pivot(currentPivot).build()
                            , 1000
                            , GMap.AnimationTiming.LINEAR);
                } else {
                    setCurrentLocationMarker(coord);
                }
            }
        });

        locationProvider.isGpsOn.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                setCurrentMarkerIcon(
                        aBoolean ? R.drawable.my_location_navigation_on : R.drawable.my_location_navigation_off);
            }
        });

        mapProvider.viewpointEventData.observe(this,
                new Observer<ViewpointChangeEventData>() {
                    @Override
                    public void onChanged(
                            @Nullable ViewpointChangeEventData
                                    viewpointChangeEventData) {
                        if (viewpointChangeEventData == null) {
                            return;
                        }

                        if (currentRoutePath != null) {
                            currentRoutePath.setBufferWidth(gMap.getResolution() * 6);
                        }

                        tbtGuidanceView.updateTbtPath();

                        if (viewpointChangeEventData.b) {
                            handler.removeMessages(0);
                            handler.sendEmptyMessageDelayed(0, 2000);
                            isFixedCurrentLocation = false;
                            zoomChanger.setEnable(false);
                        }

                        if (isFixedCurrentLocation) {
                            if (currentPivot != null) {
                                Coord markerLocation = gMap.getCoordFromViewportPoint(currentPivot);
                                setCurrentLocationMarker(UTMK.valueOf(markerLocation));
                            }
                        } else {
                            if (currentLocationMarker != null) {
                                currentLocationMarker.setRotation(NavigationManager.getInstance().getLastBearing());
                            }
                        }
                    }
                });


        mapProvider.gMap.observe(this, new Observer<GMap>() {
            @Override
            public void onChanged(@Nullable GMap gMap) {
                if (gMap == null) {
                    return;
                }
                onMapReady(gMap);
            }
        });

        NavigationData navigationData = NavigationData.getInstance();

        navigationData.laneEvent.observe(this, new Observer<Lane>() {
            @Override
            public void onChanged(@Nullable Lane lane) {
                updateLanePannel(lane);
            }
        });

        navigationData.laneDistance.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer distance) {
                if (distance == null) {
                    return;
                }
                updateLaneDistance(distance);
            }
        });

        navigationData.roadView.observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String path) {
                updateRoadView(path);
            }
        });

        navigationData.highwayEvent.observe(this, new Observer<List<HighwayGuidance>>() {
            @Override
            public void onChanged(@Nullable List<HighwayGuidance> highwayGuidances) {
                updateHighwayView(highwayGuidances);
            }
        });
        navigationData.highwayDistance.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer distance) {
                if (distance == null) {
                    return;
                }
                updateHighwayDistance(distance);
            }
        });
        navigationData.turnEvent.observe(this, new Observer<List<TurnGuidance>>() {
            @Override
            public void onChanged(@Nullable List<TurnGuidance> turnGuidances) {
                if (CommonUtils.isEmpty(turnGuidances)) {
                    return;
                }
                updateTBTViews(turnGuidances);
                hipassGuidanceView.setTurnGuidance(turnGuidances);
            }
        });
        navigationData.turnDistance.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer distance) {
                if (distance == null) {
                    return;
                }
                updateTBTDistance(distance);
                hipassGuidanceView.updateHipassView(distance);
            }
        });
        navigationData.remainEvent.observe(this,
                new Observer<RemainEventData>() {
                    @Override
                    public void onChanged(
                            @Nullable RemainEventData remainEventData) {
                        if (remainEventData == null) {
                            return;
                        }
                        updateRemain(remainEventData.timeInSecond, remainEventData.distanceInMeter);
                    }
                });

        navigationData.safetySpotEvent.observe(this,
                new Observer<SafetyEventData>() {
                    @Override
                    public void onChanged(
                            @Nullable SafetyEventData safetyEventData) {
                        if (safetyEventData == null) {
                            return;
                        }
                        updateSafetSpotView(safetyEventData.isShow, safetyEventData.safetyGuidanceList);
                    }
                });

        navigationData.intervalEvent.observe(this, new Observer<IntervalSpeedSpotGuidance>() {
            @Override
            public void onChanged(@Nullable IntervalSpeedSpotGuidance intervalSpeedSpotGuidance) {
                updateIntervalSafetySpotView(intervalSpeedSpotGuidance);
            }
        });

        navigationData.nearWaypointEvent.observe(this, new Observer<List<WayPoint>>() {
            @Override
            public void onChanged(@Nullable List<WayPoint> wayPoints) {
                if (CommonUtils.isEmpty(wayPoints)) {
                    return;
                }
                showWaypointDialog();
            }
        });
        navigationData.lowestGasEvent.observe(this,
                new Observer<LowestGasEventData>() {
                    @Override
                    public void onChanged(
                            @Nullable LowestGasEventData lowestGasEventData) {
                        if (lowestGasEventData == null) {
                            return;
                        }
                        updateLowestGasStation(lowestGasEventData.isShow,
                                lowestGasEventData.oilPriceList);
                    }
                });
        navigationData.lowestGasDistance.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer distance) {
                if (distance == null) {
                    return;
                }

                updateLowestGasStationDistance(distance);
            }
        });
    }

    boolean isFreezeMap = false;

    /**
     * 지도 비동기 입력시 처리 메소드
     *
     * @param gMap 지도 객체
     */
    private void onMapReady(GMap gMap) {
        this.gMap = gMap;
        gMap.setOnAnimationEndListener(new GMap.OnAnimationEndListener() {
            @Override
            public void onAnimationComplete() {
                isFreezeMap = false;
            }

            @Override
            public void onAnimationCancel() {
                isFreezeMap = false;
            }
        });
        applyDriveStyle(RozeOptions.getInstance().isNight());

        Route route = routeSummary.getActiveRoute();
        drawRoute(route);

        setHeadingPivot();
        tbtGuidanceView.initMap(gMap);
        spotGuidanceView.initMap(gMap);
        lowestGasGuidanceView.initMap(gMap);

        NavigationManager navigationManager = NavigationManager.getInstance();
        lowestGasGuidanceView.setLowestGasStationList(navigationManager.getOilPricePOIList());
        spotGuidanceView.setAccidentList(navigationManager.getAccidentPOIList());

        zoomChanger.initMap(gMap);
        isFreezeMap = true;
        gMap.animate(
                ViewpointChange.builder()
                        .zoomTo(12)
                        .pivot(currentPivot).build(),
                500,
                GMap.AnimationTiming.LINEAR);
    }

    private void setNorthPivot() {
        currentPivot = getGMapPivot(0.5, 0.5);
        zoomChanger.setCurrentPivot(currentPivot);
        setCurrentLocation();
    }

    private void setHeadingPivot() {
        currentPivot = getGMapPivot(0.5, 0.8);
        zoomChanger.setCurrentPivot(currentPivot);
        setCurrentLocation();
    }

    private Point getGMapPivot(double x, double y) {
        return new Point(gMap.getView().getWidth() * x, gMap.getView().getHeight() * y);
    }

    private void setCurrentMarkerIcon(int markerIconRes) {
        currentMarkerIcon = markerIconRes;
        if (currentLocationMarker == null) {
            return;
        }
        currentLocationMarker.setIcon(ResourceDescriptorFactory.fromResource(currentMarkerIcon));
    }

    protected void setCurrentLocation() {
        handler.removeMessages(0);
        isFixedCurrentLocation = true;
        zoomChanger.setEnable(true);

        UTMK coord = null;
        float angle = 0;
        NavigationManager navigationManager = NavigationManager.getInstance();
        RouteLocation routeLocation = navigationManager.getLastRouteLocation();
        if (routeLocation == null) {
            Location location = navigationManager.getLastGpsLocation();
            if (location != null) {
                coord = MapUtils.convertLocationToUtmk(location);
                angle = location.getBearing();
            }
        } else {
            coord = routeLocation.location;
            angle = routeLocation.angle;
        }

        if (coord == null || gMap == null) {
            return;
        }
        gMap.change(ViewpointChange.builder()
                .rotateTo(isHeading ? angle : 0)
                .tiltTo(isHeading ? 45 : 0)
                .panTo(coord)
                .pivot(currentPivot).build()
        );
        setCurrentLocationMarker(coord);
    }

    public void setCurrentLocationMarker(UTMK coord) {
        if (currentLocationMarker == null) {
            currentLocationMarker = new Marker(new MarkerOptions()
                    .flat(true)
                    .anchor(new Point(0.5, 0.5))
                    .icon(ResourceDescriptorFactory.fromResource(currentMarkerIcon))
                    .iconSize(new Point(54, 54)).position(coord).visible(true));
            gMap.addOverlay(currentLocationMarker);
        } else {
            currentLocationMarker.setPosition(coord);
        }

        currentLocationMarker.setRotation((isHeading && isFixedCurrentLocation) ?
                gMap.getViewpoint().rotation : NavigationManager.getInstance().getLastBearing());
        currentLocationMarker.bringToFront();

        if (currentRoutePath != null) {
            currentRoutePath.setSplitCoord(coord);
        }
    }

    /**
     * 지도 스타일 적용
     * 도로 강조 스타일
     */
    private void applyDriveStyle(boolean isNight) {
        this.isNight = isNight;
        gMap.setStyle(ResourceDescriptorFactory.fromResource(isNight ? R.raw.night_drive : R.raw.day_drive));
        gMap.setSyetemImage(ResourceDescriptorFactory.fromResource(R.drawable.com_kt_maps_totalimage),
                ResourceDescriptorFactory.fromResource(R.raw.com_kt_maps_totalimage));
    }

    private boolean isNight = false;

    @OnClick(R.id.compass_button)
    protected void onClickCompassButton() {
        this.isHeading = !isHeading;
        if (isHeading) {
            compassButton.setImageResource(R.drawable.btn_compass_heading);
            setHeadingPivot();
        } else {
            compassButton.setImageResource(R.drawable.btn_compass_north);
            setNorthPivot();
        }
    }

    @OnClick(R.id.navigation_menu)
    protected void onClickMenu() {
        driveMenuView.toggleMenu();
    }

    @Override
    public void handleMessage(Message msg) {
        setCurrentLocation();
    }

    public void startNavigation(RouteSummary routeSummary, int routeIndex, RouteListenerAdpater routeListenerAdpater) {
        this.routeListenerAdpater = routeListenerAdpater;
        this.routeSummary = routeSummary;
        this.routeIndex = routeIndex;

        routeSummary.setActiveRoute(routeIndex);
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.setSoundListener(this);
        navigationManager.startRouting(routeSummary, this);
    }

    private void requestReRoute(Location location, NavigationManager.RouteMode mode) {
        if (location == null) {
            return;
        }
        UTMK utmk = UTMK.valueOf(new LatLng(location.getLatitude(), location.getLongitude()));
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.reroute(utmk, navigationManager.getLastBearing(), mode);
    }


    @Override
    public void onRouteStarted() {
        if (routeListenerAdpater != null) {
            routeListenerAdpater.onRouteStarted();
            Route route = routeSummary.getActiveRoute();
            playStartSound(route.safetySummary);
        }
    }

    @Override
    public void onRouteStartFail(RozeError error) {
        if (routeListenerAdpater != null) {
            routeListenerAdpater.onRouteStartFail(error);
        }
    }

    @Override
    public void onTrafficUpdate() {

    }

    @Override
    public void onArrived(short arrivedIndex) {
        if (arrivedIndex == -1) { // 목적지 도착
            NavigationManager navigationManager = NavigationManager.getInstance();

            navigationManager.playRouteStateSound(SoundResourceManager.RouteState.ROUTE_END);
            UIUtils.showToast(getActivity(), R.string.toast_message_navigation_arrived_destination);

            try {
                navigationManager.stopNavigation(NavigationManager.RouteFinishMode.ARRIVED_DESTINATION);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (isResumed()) {
                getActivity().onBackPressed();
            } else {
                isArrived = true;
            }
        } else {
            // 경유지 도착
            UIUtils.showToast(getActivity(), R.string.toast_message_navigation_arrived_waypoint);
        }
    }

    @Override
    public void onRouteDeviated(Location location) {
        UIUtils.showToast(getActivity(), R.string.toast_message_location_deviated);
        requestReRoute(location, NavigationManager.RouteMode.DEVIATED_REROUTE);
    }

    @Override
    public void onRouteDidNotEnter(Location location) {
        UIUtils.showToast(getActivity(), R.string.toast_message_location_did_not_enter);
        requestReRoute(location, NavigationManager.RouteMode.DID_NOT_ENTER_REROUTE);
    }

    @Override
    public void onRerouteBegin(NavigationManager.RouteMode mode) {
        isRerouting = true;
        if (mode == NavigationManager.RouteMode.AUTO_REROUTE) {
            UIUtils.showToast(getActivity(), R.string.toast_message_reroute_auto);
        }
    }

    @Override
    public void onRerouteEnd(NavigationManager.RouteMode mode, RouteSummary routeSummary) {
        this.routeSummary = routeSummary;
        if (!isAdded()) {
            return;
        }
        routeSummary.setActiveRoute(routeIndex);
        Route route = routeSummary.getActiveRoute();

        resetGuidanceView();

        NavigationManager navigationManager = NavigationManager.getInstance();

        lowestGasGuidanceView.setLowestGasStationList(navigationManager.getOilPricePOIList());
        spotGuidanceView.setAccidentList(navigationManager.getAccidentPOIList());

        drawRoute(route);

        tbtGuidanceView.setRoute(route);
        zoomChanger.setRoute(route);
        updateRemain(route.time, route.distance);
        isRerouting = false;
    }

    @Override
    public void onRerouteFailed(NavigationManager.RouteMode mode, RozeError error) {
        // ~ 1.1.1 버전
        //UIUtils.showToast(getActivity(), R.string.toast_message_route_fail);
        //1.1.2 ~ 버전
        //서버 api 오류 코드 추가 : RozeError.rozeErrorCode
        //서버 api 오류 코드는 가이드 문서 참고
        UIUtils.showToast(getActivity(),
                TextUtils.isEmpty(error.rozeErrorCode) ? error.message : error.rozeErrorCode);
        isRerouting = false;
        if (mode.isReusePreviousRoute()) {
            return;
        }

        getActivity().onBackPressed();
    }


    private void showWaypointDialog() {
        WaypointProgressDialog waypointProgressDialog = new WaypointProgressDialog(getActivity());
        waypointProgressDialog.show();
    }

    /**
     * TBT 정보 표시
     */
    public void updateTBTViews(@NonNull List<TurnGuidance> guidances) {
        if (isRerouting) {
            return;
        }
        tbtGuidanceView.updateTBTViews(guidances);
        zoomChanger.updateTbt(guidances);
    }

    /**
     * 현재 위치에서 첫번째 TBT 까지의 거리
     *
     * @param distance 거리(m)
     */
    public void updateTBTDistance(int distance) {
        if (isRerouting) {
            return;
        }
        tbtGuidanceView.updateTBTDistance(distance);
        zoomChanger.checkZoomlevel(distance);
    }

    /**
     * 경로 주행 중 남은 시간 / 거리 표시
     *
     * @param timeInSecond    남은 시간(s)
     * @param distanceInMeter 남은 거리(m)
     */
    public void updateRemain(int timeInSecond, int distanceInMeter) {
        remainGuidanceView.updateRemain(timeInSecond, distanceInMeter);
    }

    /**
     * Road View 표시
     *
     * @param imagePath Iamge File path
     */
    public void updateRoadView(String imagePath) {
        roadViewGuidanceView.updateRoadView(imagePath);
    }

    /**
     * 차선정보 표시
     *
     * @param lane {@link Lane}
     */
    public void updateLanePannel(Lane lane) {
        laneGuidanceView.updateLanePannel(lane);
    }

    /**
     * 현재 위치에서 표출하고 있는 차선 정보 해제거리까지의 거리
     *
     * @param distance 거리(m)
     */
    public void updateLaneDistance(int distance) {
        laneGuidanceView.updateLaneDistance(distance);
    }

    /**
     * 고속도로 정보 표시
     * 현재 위치로 부터 옵션에 설정된 갯수 까지 표시 가능
     * {@link RozeOptions#getMaxHighwayGuideCount()}
     *
     * @param guidances 고속도로 안내점 정보
     */
    public void updateHighwayView(List<HighwayGuidance> guidances) {
        highwayGuidanceView.setHighwayGuidances(guidances);
    }

    /**
     * 현재 위치에서 고속도로 정보의 첫번째 안내점까지의 거리
     *
     * @param distance 거리(m)
     */
    public void updateHighwayDistance(int distance) {
        highwayGuidanceView.updateDistance(distance);
    }

    /**
     * 안전운행 정보 표시.
     *
     * @param list 표시 거리 이하로 들어온 모든 안전운행 안내점 정보
     */
    //-- 1.2.0 data type 변경
    public void updateSafetSpotView(boolean isShow, List<SafetySpotInterface> list) {
        spotGuidanceView.updateSafetySpotView(isShow, list);
    }

    /**
     * 구간단속 카메라 표시
     *
     * @param intervalGuidance 구간단속 시작/종료 객체
     */
    public void updateIntervalSafetySpotView(IntervalSpeedSpotGuidance intervalGuidance) {
        spotGuidanceView.updateIntervalSafetySpotView(intervalGuidance);
    }

    public void updateLowestGasStation(boolean isShow, List<OilPriceGuidance> list) {
        lowestGasGuidanceView.updateLowestGasStation(isShow, list);
    }

    public void updateLowestGasStationDistance(int distance) {
        lowestGasGuidanceView.updateLowestGasStationDistance(distance);
    }

    @Override
    public void onSoundStart(SoundManager soundManager, Sound sound) {
        soundManager.play(sound);
    }

    @Override
    public void onExceedSoundEvent(SoundManager soundManager, Sound sound) {
        if (sound == null) {
            soundManager.stopExceedSound();
        } else {
            soundManager.playExceedSound(sound);
        }
    }

    @Override
    public void onSoundEnd() {
    }

    // 1.2.0 버전
    //--사운드 삭제 리스너 추가
    @Override
    public void onSoundDeleteEvent(SoundManager soundManager, List<Long> ids) {
        for (Long id : ids) {
            soundManager.deleteSoundsById(id);
        }
    }
    //--사운드 삭제 리스너 추가

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NightAlarmBroadcastReceiver.ACTION);
        getActivity().registerReceiver(receiver, intentFilter);
    }

    private void unregisterReceiver() {
        getActivity().unregisterReceiver(receiver);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (NightAlarmBroadcastReceiver.ACTION.equals(intent.getAction())) {
                applyDriveStyle(intent.getBooleanExtra(NightAlarmBroadcastReceiver.EXTRA_IS_NIGHT, false));
            }
        }
    };
}
