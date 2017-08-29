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
package com.kt.rozenavi.ui.main.drive;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.kt.geom.model.Coord;
import com.kt.geom.model.LatLng;
import com.kt.geom.model.UTMK;
import com.kt.maps.GMap;
import com.kt.maps.ViewpointChange;
import com.kt.maps.model.Point;
import com.kt.maps.model.ResourceDescriptorFactory;
import com.kt.maps.overlay.Marker;
import com.kt.roze.NavigationManager;
import com.kt.roze.RozeError;
import com.kt.roze.RozeOptions;
import com.kt.roze.location.model.GeoLocation;
import com.kt.roze.routing.RouteManager;
import com.kt.roze.routing.RoutePlan;
import com.kt.roze.routing.RouteSummary;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.component.SpeedMeterView;
import com.kt.rozenavi.ui.component.core.BaseFragment;
import com.kt.rozenavi.ui.main.MainActivity;
import com.kt.rozenavi.ui.main.MainActivityViewModel;
import com.kt.rozenavi.ui.main.route.RouteFragment;
import com.kt.rozenavi.ui.main.route.data.LocationItem;
import com.kt.rozenavi.ui.search.SearchActivity;
import com.kt.rozenavi.ui.setting.SettingRouteActivity;
import com.kt.rozenavi.utils.NaviUtils;
import com.kt.rozenavi.utils.PreferenceUtils;
import com.kt.rozenavi.utils.UIUtils;
import com.kt.rozenavi.utils.WeakReferenceHandler;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class DriveFragment extends BaseFragment implements WeakReferenceHandler.OnMessageHandler,
        RouteManager.RouteManagerListener {
    @BindView(R.id.speed_value_layout)
    protected SpeedMeterView speedMeterView;
    @BindView(R.id.location_button)
    protected ImageView locationButton;
    @BindView(R.id.compass_button)
    protected ImageView compassButton;
    @BindView(R.id.edit_search_keyword)
    protected EditText searchText;
    @BindView(R.id.search_button)
    protected View searchBarLayout;

    private MainActivityViewModel viewModel;

    private GMap gMap;
    private WeakReferenceHandler handler = new WeakReferenceHandler(this);
    private int currentMarkerIcon = R.drawable.my_location_on;
    private Marker currentLocationMarker;
    private Point currentPivot;

    private LocationItem destination;

    private boolean isFixedCurrentLocation = true;
    private boolean isHeading = true;

    //getInstance는 상황에 따라서 선택 사용
    //파라매터 없는 경우
    public static Fragment getInstance() {
        return new DriveFragment();
    }

    @Override
    protected View createView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        //layout을 지정하면 자동으로 Butterknife bind
        return inflater.inflate(R.layout.fragment_drive, container, false);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //Activity 에서 onNewIntent 동작시 해당 fragment까지 전달받을 경우 처리
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == MainActivity.SEARCH_REQ_CODE) {
            //검색결과에서 목적지 좌표, 목적지 명칭 반환
            int x = data.getIntExtra(SearchActivity.RESULT_EXTRA_COORD_X, 0);
            int y = data.getIntExtra(SearchActivity.RESULT_EXTRA_COORD_Y, 0);
            String name = data.getStringExtra(SearchActivity.RESULT_EXTRA_DESTINATION_NAME);

            requestRouteSummary(x, y, name);
        }
    }

    @Override
    public void onDestroy() {
        if (currentLocationMarker != null) {
            gMap.removeOverlay(currentLocationMarker);
        }
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
        searchText.setFocusable(false);
        searchText.setClickable(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            searchBarLayout.setElevation(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10,
                    getResources().getDisplayMetrics()));
        }
    }

    //getArguments() 및 초기 데이터 쿼리 관련 초기화
    private void initData() {
        //그외 데이터 쿼리 로직
        viewModel = ViewModelProviders.of(getActivity()).get(MainActivityViewModel.class);
        viewModel.geoLocation.observe(this, new Observer<GeoLocation>() {
            @Override
            public void onChanged(@Nullable GeoLocation geoLocation) {
                if (geoLocation == null || !isResumed()) {
                    return;
                }
                Location location = geoLocation.location;
                if (location == null || gMap == null) {
                    return;
                }
                speedMeterView.setSpeed(NaviUtils.calculateSpeed(geoLocation.location));
                if (isFixedCurrentLocation && !isFreezeMap) {
                    gMap.animate(
                            ViewpointChange.builder()
                                    .rotateTo(isHeading ? location.getBearing() : 0)
                                    .panTo(UTMK.valueOf(
                                            new LatLng(location.getLatitude(),
                                                    location.getLongitude())))
                                    .pivot(currentPivot).build()
                            , 1000
                            , GMap.AnimationTiming.LINEAR);
                } else {
                    setCurrentLocationMarker(location);
                }
            }
        });

        viewModel.viewpointEventData.observe(this,
                new Observer<MainActivityViewModel.ViewpointChangeEventData>() {
                    @Override
                    public void onChanged(
                            @Nullable MainActivityViewModel.ViewpointChangeEventData
                                    viewpointChangeEventData) {
                        if (viewpointChangeEventData == null || !isResumed()) {
                            return;
                        }

                        if (viewpointChangeEventData.b) {
                            handler.removeMessages(0);
                            handler.sendEmptyMessageDelayed(0, 4000);
                            isFixedCurrentLocation = false;
                            locationButton.setImageResource(R.drawable.btn_current_location_off);
                        }

                        if (isFixedCurrentLocation && currentPivot != null) {
                            Coord markerLocation = gMap.getCoordFromViewportPoint(currentPivot);
                            setCurrentLocationMarker(UTMK.valueOf(markerLocation));
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
                        aBoolean ? R.drawable.my_location_on : R.drawable.my_location_off);
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
        applyDefaultStyle();

        setHeadingPivot();
        isFreezeMap = true;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DriveFragment.this.gMap.animate(
                        ViewpointChange.builder()
                                .zoomTo(11)
                                .pivot(currentPivot).build(),
                        500,
                        GMap.AnimationTiming.LINEAR);

            }
        }, 500);
    }

    /**
     * 정북방향 설정
     * pivot 지도의 중앙
     */
    private void setNorthPivot() {
        currentPivot = getGMapPivot(0.5, 0.5);
        setCurrentLocation();
    }

    /**
     * 진행방향 설정
     * pivot 지도의 하단중앙
     */
    private void setHeadingPivot() {
        currentPivot = getGMapPivot(0.5, 0.8);
        setCurrentLocation();
    }

    private Point getGMapPivot(double x, double y) {
        return new Point(gMap.getView().getWidth() * x, gMap.getView().getHeight() * y);
    }

    /**
     * 지도 스타일 적용
     * 일반 지도 스타일
     */
    private void applyDefaultStyle() {
        gMap.setStyle(ResourceDescriptorFactory.fromResource(R.raw.com_kt_maps_style));
        gMap.setSyetemImage(ResourceDescriptorFactory
                        .fromResource(R.drawable.com_kt_maps_totalimage),
                ResourceDescriptorFactory.fromResource(R.raw.com_kt_maps_totalimage));
    }

    /**
     * 현재 내위치 마커 이미지 변경
     */
    private void setCurrentMarkerIcon(int markerIconRes) {
        currentMarkerIcon = markerIconRes;
        if (currentLocationMarker == null) {
            return;
        }
        currentLocationMarker.setIcon(ResourceDescriptorFactory.fromResource(currentMarkerIcon));
    }


    /**
     * 현재 내위치로 지도 이동
     * pivot은 미리 설정한 정보를 이용
     */
    protected void setCurrentLocation() {
        handler.removeMessages(0);
        isFixedCurrentLocation = true;

        Location location = NavigationManager.getInstance().getLastGpsLocation();
        if (location == null || gMap == null) {
            return;
        }

        gMap.change(ViewpointChange.builder()
                .rotateTo(isHeading ? NavigationManager.getInstance().getLastBearing() : 0)
                .panTo(UTMK.valueOf(
                        new LatLng(location.getLatitude(), location.getLongitude())))
                .pivot(currentPivot).build());

        setCurrentLocationMarker(location);
    }

    /**
     * 내위치 마커 설정
     * 기존에 설정된 마커가 있는경우 위치만 변경
     */
    public void setCurrentLocationMarker(UTMK coord) {
        if (currentLocationMarker == null) {
            currentLocationMarker = new Marker();
            currentLocationMarker.setAnchor(new Point(0.5, 0.5));
            currentLocationMarker.setIcon(
                    ResourceDescriptorFactory.fromResource(currentMarkerIcon));
            currentLocationMarker.setIconSize(new Point(30, 30));
            currentLocationMarker.setVisible(true);
            currentLocationMarker.setPosition(coord);
            gMap.addOverlay(currentLocationMarker);
        } else {
            currentLocationMarker.setPosition(coord);
        }
    }

    /**
     * 내위치 마커 설정
     */
    public void setCurrentLocationMarker(Location location) {
        setCurrentLocationMarker(
                UTMK.valueOf(new LatLng(location.getLatitude(), location.getLongitude())));
    }

    /**
     * 경로 요청
     */
    private void requestRouteSummary(int x, int y, String name) {
        //현재 gps가 사용가능한 상태인지 마지막 수신 정보 확인
        Location lastLocation = NavigationManager.getInstance().getLastGpsLocation();
        if (lastLocation == null) {
            UIUtils.showToast(getActivity(), R.string.toast_message_gps_signal_not_found);
            return;
        }

        UIUtils.showProgressDialog(getActivity());
        UTMK startCoord =
                UTMK.valueOf(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
        //경로검색
        //출발지 좌표
        //도착지 좌표
        UTMK destCoord = new UTMK(x, y);
        //경유지 좌표 리스트
        NavigationManager navigationManager = NavigationManager.getInstance();
        RozeOptions rozeOptions = RozeOptions.getInstance();
        //경로타입, 차량정보 환경설정 정보 받아오기 없는 경우 기본값
        List<RoutePlan.RouteType> routeTypes = getRouteTypeList();
        RozeOptions.CarType carType = rozeOptions.getCarType();
        boolean isHipass = rozeOptions.isHipass();

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

        if (viewModel.isGpsOn.getValue() != null && viewModel.isGpsOn.getValue()) {
            //회전값
            builder.bearing(navigationManager.getLastBearing());
        }

        Location lastGpsLocation = navigationManager.getLastGpsLocation();
        //정확도(accuracy)에 대한 측정값이 있으면 추가
        if (lastGpsLocation != null) {
            if (lastGpsLocation.hasAccuracy()) {
                builder.accuracy(lastGpsLocation.getAccuracy());
            }
            if (lastGpsLocation.hasAltitude()) {
                builder.altitude(lastGpsLocation.getAltitude());
            }
            builder.createTime(lastGpsLocation.getTime());

        }

        RoutePlan routePlan = builder.build();

        RouteManager routeManager = new RouteManager();
        if (!routeManager.isBusy()) {
            destination = new LocationItem(x, y, name);
            routeManager.calculateRoute(routePlan, this);
        }
    }

    /**
     * 경로타입 반환
     * 경로설정 화면({@link SettingRouteActivity})에서 설정한 정보를 반환
     * sharedpreferences에 저장된 값을 가져오며 설정값이 없는 경우 기본 값으로 반환
     *
     * @return 경로타입 정보 리스트
     */
    public List<RoutePlan.RouteType> getRouteTypeList() {
        int routeType1Index = PreferenceUtils.getInt(getActivity(),
                PreferenceUtils.KEY_ROUTE_TYPE_1,
                SettingRouteActivity.DEFAULT_ROUTE_TYPE_1);
        int routeType2Index = PreferenceUtils.getInt(getActivity(),
                PreferenceUtils.KEY_ROUTE_TYPE_2,
                SettingRouteActivity.DEFAULT_ROUTE_TYPE_2);
        List<RoutePlan.RouteType> routeTypes = new ArrayList<>();
        routeTypes.add(RoutePlan.RouteType.values()[routeType1Index]);
        routeTypes.add(RoutePlan.RouteType.values()[routeType2Index]);
        return routeTypes;
    }

    @OnClick(R.id.location_button)
    protected void onClickLocationButton() {
        locationButton.setImageResource(R.drawable.btn_current_location_on);
        setCurrentLocation();
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

    @OnClick({R.id.search_button, R.id.edit_search_keyword})
    protected void onClickSearchButton() {
        Intent intent = new Intent(getActivity(), SearchActivity.class);
        startActivityForResult(intent, MainActivity.SEARCH_REQ_CODE);
        //검색화면 시작시 애니메이션 삭제
        getActivity().overridePendingTransition(0, 0);
    }

    @OnClick(R.id.toggle_container)
    protected void onClickDrawerButton() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.openDrawer();
    }

    @Override
    public void handleMessage(Message msg) {
        onClickLocationButton();
    }

    @Override
    public void onRouteCalculateFinished(RouteSummary routeSummary) {
        UIUtils.dismissProgressDialog();
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.replaceFragment(RouteFragment.getInstance(routeSummary, destination));
    }

    @Override
    public void onRouteCalculateFailed(RozeError rozeError) {
        UIUtils.dismissProgressDialog();
        Toast.makeText(getActivity(), rozeError.message, Toast.LENGTH_SHORT).show();
    }
}
