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
package com.kt.rozenavi.ui.main.route;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Message;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kt.geom.model.LatLng;
import com.kt.geom.model.UTMK;
import com.kt.geom.model.UTMKBounds;
import com.kt.maps.GMap;
import com.kt.maps.ViewpointChange;
import com.kt.maps.model.Point;
import com.kt.maps.model.ResourceDescriptorFactory;
import com.kt.maps.overlay.Marker;
import com.kt.maps.overlay.MarkerOptions;
import com.kt.maps.overlay.TrafficRoutePath;
import com.kt.naviextension.data.Failure;
import com.kt.naviextension.data.ResultListener;
import com.kt.naviextension.routepath.RoutePathManager;
import com.kt.naviextension.routepath.data.RoutePathOption;
import com.kt.roze.NavigationManager;
import com.kt.roze.RozeError;
import com.kt.roze.RozeOptions;
import com.kt.roze.data.model.Route;
import com.kt.roze.routing.RouteManager;
import com.kt.roze.routing.RoutePlan;
import com.kt.roze.routing.RouteSummary;
import com.kt.rozenavi.R;
import com.kt.rozenavi.data.model.ViewpointChangeEventData;
import com.kt.rozenavi.provider.LocationProvider;
import com.kt.rozenavi.provider.MapProvider;
import com.kt.rozenavi.ui.component.core.BaseFragment;
import com.kt.rozenavi.ui.main.MainActivity;
import com.kt.rozenavi.ui.main.navigation.NavigationFragment;
import com.kt.rozenavi.ui.main.route.data.LocationItem;
import com.kt.rozenavi.ui.main.route.view.RouteDestinationView;
import com.kt.rozenavi.ui.main.route.view.RouteTbtView;
import com.kt.rozenavi.ui.main.route.view.RouteTypeRecyclerViewAdapter;
import com.kt.rozenavi.ui.main.route.view.RouteTypeView;
import com.kt.rozenavi.ui.search.SearchActivity;
import com.kt.rozenavi.ui.setting.SettingRouteActivity;
import com.kt.rozenavi.utils.AnimationListenerAdapter;
import com.kt.rozenavi.utils.CommonUtils;
import com.kt.rozenavi.utils.MapUtils;
import com.kt.rozenavi.utils.PreferenceUtils;
import com.kt.rozenavi.utils.RouteListenerAdpater;
import com.kt.rozenavi.utils.RoutePathUtils;
import com.kt.rozenavi.utils.UIUtils;
import com.kt.rozenavi.utils.WeakReferenceHandler;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RouteFragment extends BaseFragment implements RouteManager.RouteManagerListener,
        RouteTypeRecyclerViewAdapter.OnRouteTypeItemEventListener,
        WeakReferenceHandler.OnMessageHandler,
        RouteTbtView.OnTbtItemEventListener {
    @BindView(R.id.route_destination_view)
    protected RouteDestinationView routeDestinationView;

    @BindView(R.id.route_type_view)
    protected RouteTypeView routeTypeView;
    @BindView(R.id.route_tbt_layout)
    protected LinearLayout routeTbtLayout;
    @BindView(R.id.route_tbt_view)
    protected RouteTbtView routeTbtView;
    @BindView(R.id.route_back_button)
    protected View routeBackButton;

    private LoadingAnimationHelper loadingAnimationHelper;
    private WeakReferenceHandler handler = new WeakReferenceHandler(this);

    private GMap gMap;

    private List<LocationItem> destinationList;
    private RouteSummary routeSummary;

    //기본 RoutePath 이용 시
    //private List<RoutePath> routePathList = new ArrayList<>();
    //since 1.7.0 교통정보 RoutePath
    private List<TrafficRoutePath> routePathList = new ArrayList<>();
    private List<Marker> markerList = new ArrayList<>();
    private int selectedRouteIndex = 0;

    private Fragment navigationFragment;
    private Point routePivot;

    //since 1.7.0 교통정보 RoutePath
    /**
     * onMapReady 시점에 초기화됨.
     * RoutePath 객체를 위한 옵션
     */
    private RoutePathOption defaultRoutePathOptions;

    public static Fragment getInstance(RouteSummary routeSummary, LocationItem destination) {
        RouteFragment fragment = new RouteFragment();
        fragment.setParameter(routeSummary, destination);
        return fragment;
    }

    private void setParameter(RouteSummary routeSummary, LocationItem destination) {
        this.routeSummary = routeSummary;
        this.destinationList = getDestinationList(destination);
    }

    @Override
    protected View createView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        //layout을 지정하면 자동으로 Butterknife bind
        return inflater.inflate(R.layout.fragment_route, container, false);
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

            routeDestinationView.setLocationData(x, y, name);
        }
    }

    @Override
    public void onDestroy() {
        clearOverlay();
        loadingAnimationHelper.stopAnimation();
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
        routeDestinationView.initLocationData(destinationList,
                new RouteDestinationView.OnDestinationItemEventListener() {
                    @Override
                    public void onDestinationChanged(List<LocationItem> destinationList) {
                        requestRouteSummary(destinationList);
                    }

                    @Override
                    public void onDestinationClick() {
                        Intent intent = new Intent(getActivity(), SearchActivity.class);
                        startActivityForResult(intent, MainActivity.SEARCH_REQ_CODE);
                        //검색화면 시작시 애니메이션 삭제
                        getActivity().overridePendingTransition(0, 0);
                    }
                });
        routeTypeView.setRouteInfo(routeSummary.routes, routeSummary.routePlan.routeTypes, this);
        routeTbtView.setRouteInfo(routeSummary.routes, this);

        loadingAnimationHelper = new LoadingAnimationHelper(getView());
    }

    private void setZoomtoFitRoute() {
        List<UTMK> pathNodeList = new ArrayList<>();
        for (Route route : routeSummary.routes) {
            pathNodeList.addAll(route.routePath());
        }
        UTMKBounds pathBounds = UTMKBounds.fromCoords(pathNodeList);
        DisplayMetrics metrics = gMap.getView().getResources().getDisplayMetrics();

        Rect rect = new Rect();
        routeDestinationView.getGlobalVisibleRect(rect);

        //가로/세로 해상도를 구해서 큰값으로 zoomlevel 계산
        double widthResolution =
                (pathBounds.getWidth() / (gMap.getView().getWidth() / metrics.density));
        double heightResolution =
                (pathBounds.getHeight() / (rect.top / metrics.density));
        double targetResolution =
                widthResolution > heightResolution ? widthResolution : heightResolution;
        double zoomlevel = MapUtils.calcMapZoomlevel(targetResolution);

        routePivot = new Point(gMap.getView().getWidth() / 2, rect.top / 2);
        gMap.animate(
                ViewpointChange.builder()
                        .zoomTo(zoomlevel)
                        .rotateTo(0)
                        .tiltTo(0)
                        .panTo(pathBounds.getCenter())
                        .pivot(routePivot)
                        .build()
                , 500
                , GMap.AnimationTiming.LINEAR);
    }

    //getArguments() 및 초기 데이터 쿼리 관련 초기화
    private void initData() {
        //그외 데이터 쿼리 로직
        MapProvider mapProvider = MapProvider.getInstance();
        mapProvider.viewpointEventData.observe(this,
                new Observer<ViewpointChangeEventData>() {
                    @Override
                    public void onChanged(@Nullable ViewpointChangeEventData viewpointChangeEventData) {
                        if (viewpointChangeEventData == null || CommonUtils.isEmpty(routePathList)) {
                            return;
                        }

                        //기본 RoutePath 이용 시
                        //for (RoutePath routePath : routePathList) {
                        //since 1.7.0 교통정보 RoutePath
                        for (TrafficRoutePath routePath : routePathList) {
                            routePath.setBufferWidth(gMap.getResolution() * 4);
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
    }

    private List<LocationItem> getDestinationList(LocationItem destination) {
        List<LocationItem> destinationList = new ArrayList<>();

        Location currentLocation = NavigationManager.getInstance().getLastGpsLocation();
        UTMK utmk = UTMK.valueOf(
                new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        destinationList.add(new LocationItem(utmk.x, utmk.y, "현재위치"));
        destinationList.add(destination);
        return destinationList;
    }

    private void clearOverlay() {
        if (!CommonUtils.isEmpty(routePathList)) {
            //기본 RoutePath 이용 시
            //for (RoutePath routePath : routePathList) {
            //since 1.7.0 교통정보 RoutePath
            for (TrafficRoutePath routePath : routePathList) {
                gMap.removeOverlay(routePath);
            }
            routePathList.clear();
        }

        if (!CommonUtils.isEmpty(markerList)) {
            for (Marker marker : markerList) {
                gMap.removeOverlay(marker);
            }
            markerList.clear();
        }
    }

    private void setRoutePath() {
        clearOverlay();

        createRoutePath();
        //addmarker
        //출발지 마커
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.anchor(new Point(0.5, 1.0))
                .icon(ResourceDescriptorFactory.fromResource(R.drawable.route_marker_start))
                .iconSize(new Point(30, 49))
                .position(routeSummary.routePlan.start).visible(true);
        Marker marker = new Marker(markerOptions);
        markerList.add(marker);
        gMap.addOverlay(marker);

        //도착지 마커
        markerOptions = new MarkerOptions();
        markerOptions.anchor(new Point(0.5, 1.0))
                .icon(ResourceDescriptorFactory.fromResource(R.drawable.route_marker_end))
                .iconSize(new Point(30, 49))
                .position(routeSummary.routePlan.dests.get(0)).visible(true);
        marker = new Marker(markerOptions);
        markerList.add(marker);
        gMap.addOverlay(marker);

        //경유지 마커
        //Since 1.3.0 WayPoint : 경유지를 좌표에서 멀티입구점으로 처리하기 위해 UTMK 에서 -> List<UTMK> 로 변경했습니다.
        for (List<UTMK> waypoint : routeSummary.routePlan.waypoints) {
            markerOptions = new MarkerOptions();
            markerOptions.anchor(new Point(0.5, 1.0))
                    .icon(ResourceDescriptorFactory.fromResource(R.drawable.route_marker_waypoint))
                    .iconSize(new Point(30, 49))
                    .position(waypoint.get(0)).visible(true);
            marker = new Marker(markerOptions);
            markerList.add(marker);
            gMap.addOverlay(marker);
        }

        selectRoute(0);
    }
//기본 RoutePath 이용 시
//    private RoutePath createRoutePath(List<UTMK> pathPointList) {
//        return new RoutePath(new RoutePathOptions().addPoints(pathPointList)
//                .bufferWidth(gMap.getResolution() * 4)
//                .strokeWidth(1)
//                .strokeColor(Color.DKGRAY)
//                .passedFillColor(getResources().getColor(R.color.elephant_grey))
//                .fillColor(getResources().getColor(R.color.elephant_grey)));
//    }

    //since 1.7.0 교통정보 RoutePath
    private void createRoutePath() {
        Context context = getContext();
        if (gMap == null || routeSummary == null || CommonUtils.isEmpty(routeSummary.routes) || context == null) {
            return;
        }

        routePathList.clear();
        //since 01.17.00 navi Extension 에서 제공하는 RoutePathManager 이용
        for (Route route : routeSummary.routes) {
            //지도 및 리스트에 routepath 추가
            RoutePathManager.createTrafficRoutePath(context, route, defaultRoutePathOptions, new ResultListener<TrafficRoutePath>() {
                @Override
                public void success(TrafficRoutePath routePath) {
                    if (MapUtils.addOverlay(gMap, routePath)) {
                        routePathList.add(routePath);
                    }
                }

                @Override
                public void fail(@NotNull Failure failure) {
                    Log.e("RouteFragment", "Error in RoutePath " + failure.getCode());
                }
            });
        }
    }

    //기본 RoutePath 이용 시
//    public void selectRoute(int index) {
//        if (selectedRouteIndex > -1) {
//            routePathList.get(selectedRouteIndex).setFillColor(
//                    getResources().getColor(R.color.elephant_grey));
//        }
//        selectedRouteIndex = index;
//
//        gMap.removeOverlay(routePathList.get(selectedRouteIndex));
//        gMap.addOverlay(routePathList.get(selectedRouteIndex));
//        routePathList.get(selectedRouteIndex).setFillColor(
//                getResources().getColor(R.color.cool_red));
//    }
    //since 1.7.0 교통정보 RoutePath
    /**
     * 경로 선택
     * 선택된 경로정보를 활성화 하고 선택되지 않은 경로정보는 비활성화 상태로 변경
     *
     * @param index index
     */
    private void selectRoute(int index) {
        if (CommonUtils.isEmpty(routePathList)) {
            return;
        }

        boolean isSuccess = RoutePathUtils.changeSelectedTrafficRoutePath(index, routePathList);
        if (!isSuccess) {
            return;
        }
        selectedRouteIndex = index;
    }


    @OnClick(R.id.cancel_button)
    protected void onCancelClick() {
        getActivity().onBackPressed();
    }


    @OnClick(R.id.route_back_button)
    protected void onBackClick() {
        routeTbtLayout.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down));
        routeTbtLayout.setVisibility(View.GONE);
        TransitionManager.beginDelayedTransition((ViewGroup) routeBackButton.getParent(),
                new ChangeBounds());
        routeBackButton.setVisibility(View.GONE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setZoomtoFitRoute();
            }
        }, 500);
    }

    @OnClick(R.id.route_start_button)
    protected void onStartClick() {
        loadingAnimationHelper.startAniamtion();

        final NavigationFragment fragment = (NavigationFragment) NavigationFragment.getInstance();
        fragment.startNavigation(routeSummary, selectedRouteIndex, new RouteListenerAdpater() {
            @Override
            public void onRouteStarted() {
                navigationFragment = fragment;
            }

            @Override
            public void onRouteStartFail(RozeError error) {
                Log.e("Roze", "onRouteStartFail");
                // ~ 1.1.1 버전
                //UIUtils.showToast(getActivity(), rozeError.message);
                //1.1.2 ~ 버전
                //서버 api 오류 코드 추가 : RozeError.rozeErrorCode
                //서버 api 오류 코드는 가이드 문서 참고
                UIUtils.showToast(getActivity(),
                        TextUtils.isEmpty(error.rozeErrorCode) ? error.message : error.rozeErrorCode);
            }
        });
    }

    /**
     * 지도 비동기 입력시 처리 메소드
     *
     * @param gMap 지도 객체
     */
    private void onMapReady(GMap gMap) {
        this.gMap = gMap;
        //since 1.7.0 교통정보 RoutePath 주행선 생성을 위한 기본 옵션 설정
        defaultRoutePathOptions = RoutePathUtils.createNormalRoutePathOption(
                gMap,
                4,
                1,
                Color.DKGRAY,
                ContextCompat.getColor(getContext(), R.color.elephant_grey),
                ContextCompat.getColor(getContext(), R.color.cool_red),
                ContextCompat.getColor(getContext(), R.color.elephant_grey)
        );
        setRoutePath();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setZoomtoFitRoute();
            }
        }, 500);
    }

    /**
     * 경로 요청
     */
    private void requestRouteSummary(List<LocationItem> destinationList) {
        this.destinationList = destinationList;
        UIUtils.showProgressDialog(getActivity());

        LocationItem item = destinationList.get(0);
        UTMK startCoord = new UTMK(item.getX(), item.getY());
        item = destinationList.get(destinationList.size() - 1);
        UTMK destCoord = new UTMK(item.getX(), item.getY());
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

        for (int i = 1, size = destinationList.size() - 1; i < size; i++) {
            item = destinationList.get(i);
            if (!TextUtils.isEmpty(item.getName()) && item.getX() > 0 && item.getY() > 0) {
                builder.waypoint(new UTMK(item.getX(), item.getY()));
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

        this.routeSummary = routeSummary;
        routeTypeView.setRouteInfo(routeSummary.routes, routeSummary.routePlan.routeTypes, this);
        routeTbtView.setRouteInfo(routeSummary.routes, this);
        setRoutePath();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                setZoomtoFitRoute();
            }
        }, 500);
    }

    @Override
    public void onRouteCalculateFailed(RozeError rozeError) {
        UIUtils.dismissProgressDialog();
        loadingAnimationHelper.stopAnimation();
        // ~ 1.1.1 버전
        //UIUtils.showToast(getActivity(), rozeError.message);
        //1.1.2 ~ 버전
        //서버 api 오류 코드 추가 : RozeError.rozeErrorCode
        //서버 api 오류 코드는 가이드 문서 참고
        UIUtils.showToast(getActivity(),
                TextUtils.isEmpty(rozeError.rozeErrorCode) ? rozeError.message : rozeError.rozeErrorCode);
    }

    @Override
    public void onRouteTypeSelected(int index) {
        selectRoute(index);
        routeTbtView.setRouteIndex(index);
    }

    @Override
    public void onRouteDetailClick(int index) {
        routeTbtLayout.setAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up));
        routeTbtLayout.setVisibility(View.VISIBLE);
        TransitionManager.beginDelayedTransition((ViewGroup) routeBackButton.getParent(),
                new ChangeBounds());
        routeBackButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTbtSelected(int index, UTMK utmk) {
        gMap.animate(ViewpointChange.builder()
                .pivot(routePivot)
                .panTo(utmk)
                .zoomTo(13).build()
        );
    }

    @Override
    public void handleMessage(Message msg) {
    }

    class LoadingAnimationHelper {
        @BindView(R.id.loading_layout)
        protected FrameLayout loadingLayout;
        @BindView(R.id.loading_car_imageview)
        protected ImageView loadingCarImageView;
        @BindView(R.id.click_disable_view)
        protected View clickDisableView;

        LoadingAnimationHelper(View view) {
            ButterKnife.bind(this, view);
        }

        void startAniamtion() {
            clickDisableView.setClickable(true);
            loadingLayout.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(getActivity(),
                    R.anim.route_loading_car_animation);
            animation.setAnimationListener(new AnimationListenerAdapter() {

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (navigationFragment == null) {
                        startAniamtion();
                        return;
                    }

                    Activity activity = getActivity();
                    if (activity instanceof MainActivity) {
                        stopAnimation();
                        ((MainActivity) activity).replaceFragment(navigationFragment);
                    }
                }
            });
            loadingCarImageView.setAnimation(animation);
            loadingCarImageView.setVisibility(View.VISIBLE);
            AnimationDrawable drawable = (AnimationDrawable) loadingCarImageView.getDrawable();
            if (drawable.isRunning()) {
                drawable.stop();
            }
            drawable.start();
        }

        void stopAnimation() {
            clickDisableView.setClickable(false);
            loadingLayout.setVisibility(View.GONE);
            loadingCarImageView.setVisibility(View.GONE);
            loadingCarImageView.clearAnimation();
            AnimationDrawable drawable = (AnimationDrawable) loadingCarImageView.getDrawable();
            if (drawable.isRunning()) {
                drawable.stop();
            }
        }
    }

}
