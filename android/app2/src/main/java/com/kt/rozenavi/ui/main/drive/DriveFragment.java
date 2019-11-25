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
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

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
import com.kt.roze.SoundManager;
import com.kt.roze.data.model.Link;
import com.kt.roze.data.model.Route;
import com.kt.roze.guidance.model.SafetySpotInterface;
import com.kt.roze.guidance.model.Sound;
import com.kt.roze.guidance.model.TrackingGuidance;
import com.kt.roze.location.model.GeoLocation;
import com.kt.roze.routing.RouteManager;
import com.kt.roze.routing.RoutePlan;
import com.kt.roze.routing.RouteSummary;
import com.kt.roze.util.SimpleLog;
import com.kt.rozenavi.R;
import com.kt.rozenavi.data.NavigationData;
import com.kt.rozenavi.data.SingleMessage;
import com.kt.rozenavi.data.model.TrackingEventData;
import com.kt.rozenavi.data.model.ViewpointChangeEventData;
import com.kt.rozenavi.provider.LocationProvider;
import com.kt.rozenavi.provider.MapProvider;
import com.kt.rozenavi.ui.component.SpeedMeterView;
import com.kt.rozenavi.ui.component.core.BaseFragment;
import com.kt.rozenavi.ui.main.MainActivity;
import com.kt.rozenavi.ui.main.drive.view.NavigationTrackingView;
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

//-- 1.2.0 안전운행 발성을 위한 SoundListener 추가
public class DriveFragment extends BaseFragment implements WeakReferenceHandler.OnMessageHandler,
        RouteManager.RouteManagerListener, NavigationManager.SoundListener {
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
    //-- 1.2.0 안전운행 안내를 위한 safety view 추가
    @BindView(R.id.spot_guidance_view)
    protected NavigationTrackingView trackingView;


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
            double x = data.getDoubleExtra(SearchActivity.RESULT_EXTRA_COORD_X, 0);
            double y = data.getDoubleExtra(SearchActivity.RESULT_EXTRA_COORD_Y, 0);
            String name = data.getStringExtra(SearchActivity.RESULT_EXTRA_DESTINATION_NAME);

            requestRouteSummary(x, y, name);
        }
    }

    @Override
    public void onDestroy() {
        // 1.2.0 안전운행 지도 해제
        trackingView.releaseMap();
        // 1.2.0 안전운행 지도 해제

        if (gMap != null) {
            if (currentLocationMarker != null) {
                gMap.removeOverlay(currentLocationMarker);
            }
            gMap.setOnAnimationEndListener(null);
            gMap = null;
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
        // 1.2.0 안전운행 안내 초기화
        trackingView.hideAllView();
        // 1.2.0 안전운행 안내 초기화
    }

    //getArguments() 및 초기 데이터 쿼리 관련 초기화
    private void initData() {
        //그외 데이터 쿼리 로직
        LocationProvider locationProvider = LocationProvider.getInstance();
        MapProvider mapProvider = MapProvider.getInstance();
        locationProvider.location.observe(this, new Observer<GeoLocation>() {
            @Override
            public void onChanged(@Nullable GeoLocation geoLocation) {
                if (geoLocation == null || !isResumed()) {
                    return;
                }

                //-- 1.5.0 안전운행 가상 주행관련 예외처리
                // 가상주행시 Location없이 RouteLocation만 전달
                if ((geoLocation.location == null && geoLocation.routeLocation == null) || gMap == null) {
                    return;
                }

                UTMK coord = getCoord(geoLocation);
                float angle = getAngle(geoLocation);
                //좌표나 각도가 유효하지 않은경우는 위치정보를 반영하지 않는다.
                if (coord == null || angle < 0) {
                    return;
                }

                // 1.2.0 안전운행 속도정보 업데이트
                int currentSpeed = NaviUtils.calculateSpeed(geoLocation.location);

                speedMeterView.setSpeed(currentSpeed);
                trackingView.setSpeed(currentSpeed);
                // 1.2.0 안전운행 속도정보 업데이트

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
        locationProvider.isGpsOn.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean aBoolean) {
                if (aBoolean == null) {
                    return;
                }
                setCurrentMarkerIcon(aBoolean ? R.drawable.my_location_on : R.drawable.my_location_off);
            }
        });

        mapProvider.viewpointEventData.observe(this,
                new Observer<ViewpointChangeEventData>() {
                    @Override
                    public void onChanged(
                            @Nullable ViewpointChangeEventData
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

        mapProvider.gMap.observe(this, new Observer<GMap>() {
            @Override
            public void onChanged(@Nullable GMap gMap) {
                if (gMap == null) {
                    return;
                }
                onMapReady(gMap);
            }
        });

        // 1.2.0 버전
        //--안전운행모드 기능 추가
        NavigationData.getInstance().trackingEvent.observe(this, new Observer<TrackingEventData>() {
            @Override
            public void onChanged(@Nullable TrackingEventData trackingEventData) {
                if (trackingEventData == null) {
                    return;
                }
                updateSafetSpotView(trackingEventData.isShow, trackingEventData.guidances);
            }
        });

        NavigationData.getInstance().trackingInitializedEvent.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean isInitialized) {
                SimpleLog.i("TEST", "trackingInitializedEvent "+isInitialized);
                if (isInitialized != null && isInitialized) {
                    NavigationManager.getInstance().startTracking();
                }
            }
        });

        //-- 1.4.0 안전운행 이벤트 추가
        NavigationData.getInstance().trackingDeviatedEvent.observe(this,
                (SingleMessage.SingleMessageObserver) message -> hideSafetyView());

        NavigationManager.getInstance().setSoundListener(this);
        //--안전운행모드 사운드 기능 추가
    }

    /**
     * 좌표 선택
     * 안전운행용 맵매칭 좌표와 일반 좌표에서 가능한 좌표 반환
     * 오류 상황일 경우 null 반환
     */
    private UTMK getCoord(GeoLocation geoLocation) {
        if(geoLocation == null) {
            return null;
        }

        if(geoLocation.hasRoutedLocation()) {
            return geoLocation.routeLocation.location;
        } else {
            try {
                Location location = geoLocation.location;
                return UTMK.valueOf(new LatLng(location.getLatitude(), location.getLongitude()));
            } catch (Exception e) {
                return null;
            }
        }
    }

    /**
     * 방향값 선택
     * 안전운행용 맵매칭 좌표와 일반 좌표에서 유효한 방향값 선택
     * 오류 상황일 경우 null 반환
     */
    private float getAngle(GeoLocation geoLocation) {
        if(geoLocation == null) {
            return -1;
        }

        if(geoLocation.hasRoutedLocation()) {
            return geoLocation.routeLocation.angle;
        } else {
            return geoLocation.location == null ? -1 : geoLocation.location.getBearing();
        }
    }

    boolean isFreezeMap = false;

    /**
     * 지도 비동기 입력시 처리 메소드
     *
     * @param gMap 지도 객체
     */
    private void onMapReady(final GMap gMap) {
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
        // 1.2.0 안전운행 지도 등록
        trackingView.initMap(gMap);
        // 1.2.0 안전운행 지도 등록

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (gMap == null || currentPivot == null) {
                    return;
                }
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
        gMap.setSyetemImage(ResourceDescriptorFactory.fromResource(R.drawable.com_kt_maps_totalimage),
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
                .panTo(UTMK.valueOf(new LatLng(location.getLatitude(), location.getLongitude())))
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
            currentLocationMarker.setIcon(ResourceDescriptorFactory.fromResource(currentMarkerIcon));
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
        setCurrentLocationMarker(UTMK.valueOf(new LatLng(location.getLatitude(), location.getLongitude())));
    }

    /**
     * 경로 요청
     */
    private void requestRouteSummary(double x, double y, String name) {
        //현재 gps가 사용가능한 상태인지 마지막 수신 정보 확인
        Location lastLocation = NavigationManager.getInstance().getLastGpsLocation();
        if (lastLocation == null) {
            UIUtils.showToast(getActivity(), R.string.toast_message_gps_signal_not_found);
            return;
        }

        UIUtils.showProgressDialog(getActivity());
        UTMK startCoord = UTMK.valueOf(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()));
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

        Boolean isGpsOn = LocationProvider.getInstance().isGpsOn.getValue();
        if (isGpsOn != null && isGpsOn) {
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

    //-- 1.2.0 안전운행 업데이트 기능 추가

    /**
     * 안전운행 정보 표시.
     *
     * @param list 표시 거리 이하로 들어온 모든 안전운행 안내점 정보
     */
    public void updateSafetSpotView(boolean isShow, List<TrackingGuidance> list) {
        List<SafetySpotInterface> guidances = new ArrayList<>();
        if (!com.kt.roze.util.CommonUtils.isEmpty(list)) {
            guidances.addAll(list);
        }
        trackingView.updateSafetySpotView(isShow, guidances);
    }

    //-- 1.4.0 안전운행 이벤트 추가
    private void hideSafetyView() {
        trackingView.hideAllView();
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

    /**
     * 경로안내를 할 수 없는 경로인지 체크
     * since 1.5.0 경로탐색 실패 예외처리 추가
     */
    public static boolean isUnavailableRoute(Route route) {
        if (route == null) {
            return true;
        }

        if (TextUtils.equals(route.errorCode, "C0002")) {
            return true;
        }

        return route.links == null || route.links.size() == 0;
    }

    @Override
    public void onRouteCalculateFinished(RouteSummary routeSummary) {
        UIUtils.dismissProgressDialog();
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity == null || routeSummary == null || routeSummary.routes == null || routeSummary.routes.size() == 0) {
            return;
        }
        List<Route> route = routeSummary.routes;
        for (Route r : route) {
            //-- 1.5.0 경로탐색 오류 예외처리 추가
            if (isUnavailableRoute(r)) {
                UIUtils.showToast(getContext(), "경로 검색에 실패했습니다." + r.errorCode);
                return;
            }
        }
        mainActivity.replaceFragment(RouteFragment.getInstance(routeSummary, destination));
    }

    @Override
    public void onRouteCalculateFailed(RozeError rozeError) {
        UIUtils.dismissProgressDialog();
        // ~ 1.1.1 버전
        //UIUtils.showToast(getActivity(), rozeError.message);
        //1.1.2 ~ 버전
        //서버 api 오류 코드 추가 : RozeError.rozeErrorCode
        //서버 api 오류 코드는 가이드 문서 참고
        UIUtils.showToast(getActivity(),
                TextUtils.isEmpty(rozeError.rozeErrorCode) ? rozeError.message : rozeError.rozeErrorCode);
    }

    // 1.2.0 버전
    //--안전운행모드시 사운드 발성추가
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
        //Sound Focus 설정 시 이용
    }

    @Override
    public void onSoundDeleteEvent(SoundManager soundManager, List<Long> ids) {
        for (Long id : ids) {
            soundManager.deleteSoundsById(id);
        }
    }
    //--안전운행모드시 사운드 발성추가
}
