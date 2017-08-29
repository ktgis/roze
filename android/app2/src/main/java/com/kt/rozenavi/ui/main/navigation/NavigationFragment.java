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
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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
import com.kt.roze.data.model.WayPoint;
import com.kt.roze.guidance.RouteGuidanceListener;
import com.kt.roze.guidance.model.HighwayGuidance;
import com.kt.roze.guidance.model.IntervalSpeedSpotGuidance;
import com.kt.roze.guidance.model.OilPriceGuidance;
import com.kt.roze.guidance.model.SafetySpotGuidance;
import com.kt.roze.guidance.model.Sound;
import com.kt.roze.guidance.model.TurnGuidance;
import com.kt.roze.location.model.GeoLocation;
import com.kt.roze.location.model.RouteLocation;
import com.kt.roze.resource.SoundResourceManager;
import com.kt.roze.routing.RouteSummary;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.component.SpeedMeterView;
import com.kt.rozenavi.ui.component.core.BaseFragment;
import com.kt.rozenavi.ui.main.MainActivityViewModel;
import com.kt.rozenavi.ui.main.navigation.view.NavigationHighWayView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationHipassView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationLaneView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationLowestGasView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationMenuView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationRemainView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationRoadView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationSpotView;
import com.kt.rozenavi.ui.main.navigation.view.NavigationTbtView;
import com.kt.rozenavi.ui.main.service.TbtGuidancePopupService;
import com.kt.rozenavi.utils.MapUtils;
import com.kt.rozenavi.utils.NaviUtils;
import com.kt.rozenavi.utils.RouteListenerAdpater;
import com.kt.rozenavi.utils.UIUtils;
import com.kt.rozenavi.utils.WeakReferenceHandler;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

// TODO: 2017-08-21
/*
* 1. 종료시 기능을 현재는 getActivity().onBackpressed로 처리하는데 별도의 메소드로 빼야 할듯
* 2. reroute시 guidance표시 하는 view를 경로 시작 전으로 초기화 해야 하는데 현재는 별도의 초기화 메소드
* 없이 setvisibility와 resetmarker 등으로 처리하는데 별도의 처리 메소드 필요할듯
* 3. guidance 관련 뷰를 현재 인터페이스 없이 별도로 생성을 하는데 guidance view에서 gmap 컨트롤도 필요하고 해서
* initmap, releasemap, resetmarker 등과 같은 메소드도 필요하고 해서 인터페이스를 추가하고 처리 해야 할듯
* 관련 기능 자동으로 처리되게
*
 */
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

    public static TbtGuidancePopupService tbtGuidancePopupService;
    private ZoomChanger zoomChanger = new ZoomChanger();

    private MainActivityViewModel viewModel;
    private NavigationData navigationData;

    private GMap gMap;
    private WeakReferenceHandler handler = new WeakReferenceHandler(this);
    private int currentMarkerIcon = R.drawable.my_location_navigation_on;
    private Marker currentLocationMarker;
    private RoutePath currentRoutePath;
    private List<Marker> routePointList;
    private Point currentPivot;

    private boolean isFixedCurrentLocation = true;
    private boolean isHeading = true;

    //getInstance는 상황에 따라서 선택 사용
    //파라매터 없는 경우
    public static Fragment getInstance() {
        return new NavigationFragment();
    }

    public NavigationFragment() {
        navigationData = new NavigationData(this);
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.setRouteGuidanceEventListener(routeGuidanceListener);
        navigationManager.setRerouteListener(this);
    }

    @Override
    protected View createView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        //layout을 지정하면 자동으로 Butterknife bind
        return inflater.inflate(R.layout.fragment_navigation, container, false);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Activity 에서 onNewIntent 동작시 해당 fragment까지 전달받을 경우 처리
    }

    @Override
    public void onDestroy() {
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.setRouteGuidanceEventListener(null);
        navigationManager.setRerouteListener(null);
        NavigationManager.getInstance().setSoundListener(null);

        if (getActivity() != null && tbtGuidancePopupService != null) {
            tbtGuidancePopupService.removeNotification();
            tbtGuidancePopupService.removeTbtPopupView();
            getActivity().unbindService(conn);
        }

        tbtGuidanceView.releaseMap();
        spotGuidanceView.releaseMap();
        lowestGasGuidanceView.releaseMap();
        zoomChanger.releaseMap();

        if (currentLocationMarker != null) {
            gMap.removeOverlay(currentLocationMarker);
        }
        clearOverlay();

        NavigationManager.getInstance().stopNavigation(
                NavigationManager.RouteFinishMode.USER_FINISH);
        super.onDestroy();
    }

    @Override
    protected void init() {
        //data 초기화
        initData();
        //view 초기화
        initView();
    }

    //view에 대한 기본 parameter 초기화
    //data 생성 및 설정부분 제외
    private void initView() {
        //view에 대한 초기값 설정 및 상태값 변경
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.playRouteStateSound(SoundResourceManager.RouteState.ROUTE_START);

        setBindService(getActivity());

        driveMenuView.setOnMenuClickListener(new NavigationMenuView.OnNavigationStopListener() {
            @Override
            public void onStopClick() {
                getActivity().onBackPressed();
            }
        });

        lowestGasGuidanceView.setLowestGasStationList(navigationManager.getOilPricePOIList());
        spotGuidanceView.setAccidentList(navigationManager.getAccidentPOIList());

        Route route = routeSummary.getActiveRoute();
        drawRoute(route);
        tbtGuidanceView.setRoute(route);
        zoomChanger.setRoute(route);

        updateRemain(route.time, route.distance);
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
                    NavigationManager.getInstance()
                            .stopNavigation(NavigationManager.RouteFinishMode.USER_FINISH);
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
        if (routeSummary.routePlan.waypoints != null
                && !routeSummary.routePlan.waypoints.isEmpty()) {
            for (UTMK coord : routeSummary.routePlan.waypoints) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.anchor(new Point(0.5, 1.0))
                        .icon(ResourceDescriptorFactory.fromResource(
                                R.drawable.route_marker_waypoint))
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
                .icon(ResourceDescriptorFactory.fromResource(
                        R.drawable.route_marker_end))
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

    private void initGuidanceView() {
        tbtGuidanceView.clearOverlay();
        highwayGuidanceView.setVisibility(View.INVISIBLE);
        hipassGuidanceView.setVisibility(View.INVISIBLE);
        roadViewGuidanceView.setVisibility(View.INVISIBLE);
        laneGuidanceView.setVisibility(View.INVISIBLE);
        spotGuidanceView.setVisibility(View.INVISIBLE);
        spotGuidanceView.clearOverlay();
        lowestGasGuidanceView.setVisibility(View.INVISIBLE);
        lowestGasGuidanceView.clearOverlay();
        lowestGasGuidanceView.setVisibility(View.INVISIBLE);
    }

    //getArguments() 및 초기 데이터 쿼리 관련 초기화
    private void initData() {
        //그외 데이터 쿼리 로직
        viewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
        viewModel.geoLocation.observe(this, new Observer<GeoLocation>() {
            @Override
            public void onChanged(@Nullable GeoLocation geoLocation) {
                if (geoLocation == null || gMap == null) {
                    return;
                }
                if (!geoLocation.hasRoutedLocation() && geoLocation.location == null) {
                    return;
                }

                int currentSpeed = geoLocation.location == null ?
                        0 : NaviUtils.calculateSpeed(geoLocation.location);
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
                        angle = geoLocation.location.hasBearing() ?
                                geoLocation.location.getBearing() : 0;
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
                                    .panTo(coord)
                                    .pivot(currentPivot).build()
                            , 1000
                            , GMap.AnimationTiming.LINEAR);
                } else {
                    setCurrentLocationMarker(coord);
                }
            }
        });

        viewModel.viewpointEventData.observe(this,
                new Observer<MainActivityViewModel.ViewpointChangeEventData>() {
                    @Override
                    public void onChanged(
                            @Nullable MainActivityViewModel.ViewpointChangeEventData
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
                            currentLocationMarker.setRotation(
                                    NavigationManager.getInstance().getLastBearing()
                                            - gMap.getViewpoint().rotation);
                        }
                    }
                });
        viewModel.isGpsOn.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                setCurrentMarkerIcon(
                        aBoolean ? R.drawable.my_location_navigation_on
                                : R.drawable.my_location_navigation_off);
            }
        });

        if (viewModel.gMap.getValue() != null) {
            onMapReady(viewModel.gMap.getValue());
        } else {
            viewModel.gMap.observe(this, new Observer<GMap>() {
                @Override
                public void onChanged(@Nullable GMap gMap) {
                    if (gMap == null) {
                        return;
                    }
                    onMapReady(gMap);
                }
            });
        }

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

        navigationData.roadViewPath.observe(this, new Observer<String>() {
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
                updateTBTViews(turnGuidances);
            }
        });
        navigationData.turnDistance.observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer distance) {
                if (distance == null) {
                    return;
                }
                updateTBTDistance(distance);
            }
        });
        navigationData.remainEvent.observe(this,
                new Observer<NavigationData.RemainEventData>() {
                    @Override
                    public void onChanged(
                            @Nullable NavigationData.RemainEventData remainEventData) {
                        if (remainEventData == null) {
                            return;
                        }
                        updateRemain(remainEventData.timeInSecond, remainEventData.distanceInMeter);
                    }
                });

        navigationData.safetySpotEvent.observe(this,
                new Observer<NavigationData.SafetyEventData>() {
                    @Override
                    public void onChanged(
                            @Nullable NavigationData.SafetyEventData safetyEventData) {
                        if (safetyEventData == null) {
                            return;
                        }
                        updateSafetSpotView(safetyEventData.isShow,
                                safetyEventData.safetyGuidanceList);
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
                if (wayPoints == null || wayPoints.isEmpty()) {
                    return;
                }
                showWaypointDialog();
            }
        });
        navigationData.lowestGasEvent.observe(this,
                new Observer<NavigationData.LowestGasEventData>() {
                    @Override
                    public void onChanged(
                            @Nullable NavigationData.LowestGasEventData lowestGasEventData) {
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
        applyDriveStyle();

        setHeadingPivot();
        tbtGuidanceView.initMap(gMap);
        spotGuidanceView.initMap(gMap);
        lowestGasGuidanceView.initMap(gMap);
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
                .panTo(coord)
                .pivot(currentPivot).build()
        );
        setCurrentLocationMarker(coord);
    }

    public void setCurrentLocationMarker(UTMK coord) {
        if (currentLocationMarker == null) {
            currentLocationMarker = new Marker(new MarkerOptions()
                    .anchor(new Point(0.5, 0.5))
                    .icon(ResourceDescriptorFactory.fromResource(currentMarkerIcon))
                    .iconSize(new Point(34, 47)).position(coord).visible(true));
            gMap.addOverlay(currentLocationMarker);
        } else {
            currentLocationMarker.setPosition(coord);
        }

        currentLocationMarker.setRotation(
                (isHeading && isFixedCurrentLocation) ? 0
                        : NavigationManager.getInstance().getLastBearing()
                                - gMap.getViewpoint().rotation);
        currentLocationMarker.bringToFront();

        if (currentRoutePath != null) {
            currentRoutePath.setSplitCoord(coord);
        }
    }

    /**
     * 지도 스타일 적용
     * 도로 강조 스타일
     */
    private void applyDriveStyle() {
        gMap.setStyle(ResourceDescriptorFactory.fromResource(R.raw.day_drive));
        gMap.setSyetemImage(ResourceDescriptorFactory
                        .fromResource(R.drawable.com_kt_maps_totalimage),
                ResourceDescriptorFactory.fromResource(R.raw.com_kt_maps_totalimage));
    }

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

    private RouteListenerAdpater routeListenerAdpater;
    private RouteSummary routeSummary;
    private int routeIndex;

    public void startNavigation(RouteSummary routeSummary, int routeIndex,
            RouteListenerAdpater routeListenerAdpater) {
        this.routeListenerAdpater = routeListenerAdpater;
        this.routeSummary = routeSummary;
        this.routeIndex = routeIndex;

        routeSummary.setActiveRoute(routeIndex);
        NavigationManager.getInstance().setSoundListener(this);
        NavigationManager.getInstance().startRouting(routeSummary, this);
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
                navigationManager.stopNavigation(
                        NavigationManager.RouteFinishMode.ARRIVED_DESTINATION);
            } catch (Exception e) {
                e.printStackTrace();
            }

            getActivity().onBackPressed();
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
        if (mode == NavigationManager.RouteMode.AUTO_REROUTE) {
            UIUtils.showToast(getActivity(), R.string.toast_message_reroute_auto);
        }
    }

    @Override
    public void onRerouteEnd(NavigationManager.RouteMode mode, RouteSummary routeSummary) {
        routeSummary.setActiveRoute(routeIndex);
        Route route = routeSummary.getActiveRoute();

        initGuidanceView();

        NavigationManager navigationManager = NavigationManager.getInstance();
        lowestGasGuidanceView.setLowestGasStationList(navigationManager.getOilPricePOIList());
        spotGuidanceView.setAccidentList(navigationManager.getAccidentPOIList());

        drawRoute(route);

        tbtGuidanceView.setRoute(route);
        zoomChanger.setRoute(route);
        updateRemain(route.time, route.distance);
    }

    @Override
    public void onRerouteFailed(NavigationManager.RouteMode mode, RozeError error) {
        UIUtils.showToast(getActivity(), R.string.toast_message_route_fail);
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
    public void updateTBTViews(List<TurnGuidance> guidances) {
        tbtGuidanceView.updateTBTViews(guidances);
        zoomChanger.updateTbt(guidances);
    }

    /**
     * 현재 위치에서 첫번째 TBT 까지의 거리
     *
     * @param distance 거리(m)
     */
    public void updateTBTDistance(int distance) {
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
        hipassGuidanceView.setHighwayGuidances(guidances);
    }

    /**
     * 현재 위치에서 고속도로 정보의 첫번째 안내점까지의 거리
     *
     * @param distance 거리(m)
     */
    public void updateHighwayDistance(int distance) {
        highwayGuidanceView.updateDistance(distance);
        hipassGuidanceView.updateDistance(distance);
    }

    /**
     * 안전운행 정보 표시.
     *
     * @param list 표시 거리 이하로 들어온 모든 안전운행 안내점 정보
     */
    public void updateSafetSpotView(boolean isShow, List<SafetySpotGuidance> list) {
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

    /**
     * 경로 안내 UI 표시 용 정보를 수신하기 위한 Listener
     * {@link RouteGuidanceListener}
     */
    private RouteGuidanceListener routeGuidanceListener = new RouteGuidanceListener() {
        @Override
        public void onLaneChangedEvent(Lane lane) {
            super.onLaneChangedEvent(lane);
            navigationData.laneEvent.setValue(lane);
        }

        @Override
        public void onLaneDistanceChangedEvent(int distance) {
            super.onLaneDistanceChangedEvent(distance);
            navigationData.laneDistance.setValue(distance);
        }

        @Override
        public void onRoadViewChangedEvent(String path) {
            super.onRoadViewChangedEvent(path);
            navigationData.roadViewPath.setValue(path);
        }

        @Override
        public void onHighwayChangedEvent(List<HighwayGuidance> highwayGuidances) {
            super.onHighwayChangedEvent(highwayGuidances);
            navigationData.highwayEvent.setValue(highwayGuidances);
        }

        @Override
        public void onHighwayDistanceEvent(int distance) {
            super.onHighwayDistanceEvent(distance);
            navigationData.highwayDistance.setValue(distance);
        }

        @Override
        public void onTurnChangedEvent(List<TurnGuidance> turnGuidances) {
            super.onTurnChangedEvent(turnGuidances);
            navigationData.turnEvent.setValue(turnGuidances);
            if (tbtGuidancePopupService != null) {
                tbtGuidancePopupService.updateTBTViews(turnGuidances);
            }
        }

        @Override
        public void onTurnDistanceChangedEvent(int distance) {
            super.onTurnDistanceChangedEvent(distance);
            navigationData.turnDistance.setValue(distance);
            if (tbtGuidancePopupService != null) {
                tbtGuidancePopupService.updateTBTDistance(distance);
            }
        }

        @Override
        public void onRemainChangedEvent(int timeInSecond, int distanceInMeter) {
            super.onRemainChangedEvent(timeInSecond, distanceInMeter);
            navigationData.remainEvent.setValue(
                    new NavigationData.RemainEventData(timeInSecond, distanceInMeter));
        }

        @Override
        public void onSafetySpotChangedEvent(
                boolean isShow, List<SafetySpotGuidance> safetySpotGuidance) {
            super.onSafetySpotChangedEvent(isShow, safetySpotGuidance);
            navigationData.safetySpotEvent.setValue(
                    new NavigationData.SafetyEventData(isShow, safetySpotGuidance));
        }

        @Override
        public void onIntervalSafetySpotChangedEvent(IntervalSpeedSpotGuidance intervalGuidance) {
            super.onIntervalSafetySpotChangedEvent(intervalGuidance);
            navigationData.intervalEvent.setValue(intervalGuidance);
        }

        @Override
        public void onNearWayPointEvent(List<WayPoint> wayPoints) {
            navigationData.nearWaypointEvent.setValue(wayPoints);
        }

        @Override
        public void onLowestGasStationChangedEvent(boolean isShow, List<OilPriceGuidance> list) {
            super.onLowestGasStationChangedEvent(isShow, list);
            navigationData.lowestGasEvent.setValue(
                    new NavigationData.LowestGasEventData(isShow, list));
        }

        @Override
        public void onLowestGasStationDistanceChangedEvent(int distance) {
            super.onLowestGasStationDistanceChangedEvent(distance);
            navigationData.lowestGasDistance.setValue(distance);
        }
    };

    /**
     * 서비스 바인딩 시 콜백 메서드 구현
     */
    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            TbtGuidancePopupService.TbtGuidancePopupBinder tbtGuidancePopupBinder =
                    (TbtGuidancePopupService.TbtGuidancePopupBinder) service;
            tbtGuidancePopupService = tbtGuidancePopupBinder.getService();
            tbtGuidancePopupService.showNotification();
        }

        public void onServiceDisconnected(ComponentName name) {
        }
    };

    /**
     * 서비스 바인딩 호출
     */
    private void setBindService(Context context) {
        // Bind to LocalService
        Intent intent = new Intent(context, TbtGuidancePopupService.class);
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
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
}
