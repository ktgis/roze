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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.geom.model.UTMK;
import com.kt.geom.model.UTMKBounds;
import com.kt.maps.GMap;
import com.kt.maps.model.Viewpoint;
import com.kt.roze.data.model.Route;
import com.kt.roze.routing.RouteSummary;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.MainActivity;
import com.kt.rozenavi.ui.main.MainViewInterface;
import com.kt.rozenavi.ui.main.MapController;
import com.kt.rozenavi.ui.main.UIController;
import com.kt.rozenavi.ui.main.route.data.LocationItem;
import com.kt.rozenavi.ui.search.SearchActivity;
import com.kt.rozenavi.utils.MapUtils;
import com.kt.rozenavi.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 경로검색 화면 클래스
 * 목적지 검색 화면에서 검색된 정보를 이용하여 경로검색을 하고 결과를 보여주는 클래스
 * 경로 타입별 routepath정보와 경로의 전체거리, 예상시간 등의 정보를 보여주고
 * 경로의 전체 tbt를 리스트로 표출하여 원하는 tbt의 위치로 이동하고
 * 경로편집을 통해 출발지/도착지/경유지를 편집할 수 있는 기능을 제공
 */
public class RouteView extends RelativeLayout implements MainViewInterface {
    /**
     * 도착지 명칭 textview
     */
    @BindView(R.id.destination_textview)
    protected TextView destinationTextView;
    /**
     * 경로편집 화면 클래스 객체
     */
    @BindView(R.id.route_edit_view)
    protected RouteEditView routeEditView;
    /**
     * 경로타입 뷰 클래스 객체
     */
    @BindView(R.id.route_type_view)
    protected RouteTypeView routeTypeView;
    /**
     * 경로타입 뷰 모드 토글 textview
     * 모드가 바뀔때 text를 변경
     */
    @BindView(R.id.route_detail_textview)
    protected TextView routeDetailTextView;
    /**
     * 경로요약정보
     * 검색된 경로정보와 요청한 경로타입/경유지 정보등이 포함
     */
    private RouteSummary routeSummary;
    /**
     * 지도 viewpoint 객체
     */
    private Viewpoint lastViewPoint;

    public RouteView(Context context) {
        super(context);
        initView(context);
    }

    public RouteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RouteView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * 경로검색 화면 초기화
     * 레이아웃 설정 및 객체 생성
     *
     * @param context context 객체
     */
    private void initView(Context context) {
        View.inflate(context, R.layout.view_route_plan, this);
        ButterKnife.bind(this);

        setVisibility(GONE);
    }

    @Override
    public void destroy() {
        stop();
        RouteController.getInstance().destroy();
    }

    @Override
    public void start() {
        setVisibility(VISIBLE);
    }

    @Override
    public void stop() {
        closeRouteEditView();
        setVisibility(INVISIBLE);
    }

    /**
     * 경유지 및 도착지 검색을 했을때 처리를 위해 onActivityResult에 대한
     * 내용을 처리 할 수 있는 메소드
     *
     * @param requestCode request code
     * @param resultCode  result code
     * @param data        intent data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == MainActivity.SEARCH_REQ_CODE) {
            //검색결과에서 목적지 좌표, 목적지 명칭 반환
            int x = data.getIntExtra(SearchActivity.RESULT_EXTRA_COORD_X, 0);
            int y = data.getIntExtra(SearchActivity.RESULT_EXTRA_COORD_Y, 0);
            String name = data.getStringExtra(SearchActivity.RESULT_EXTRA_DESTINATION_NAME);

            if (getVisibility() != VISIBLE) {   //다른 화면에서 경로검색 처음 시작시
                RouteController.getInstance().requestRoute(getContext(), x, y, name);
            } else {    //경로검색화면에서 목적지 추가/변경시
                routeEditView.setLocationData(x, y, name);
            }
        }
    }

    /**
     * 경로 검색 취소
     * 경로 검색화면을 종료하고 이전 안전운행모드로 돌아감
     */
    @OnClick(R.id.cancel_icon_imageView)
    protected void clickCancel() {
        //안전운행 모드로 설정
        UIController.getInstance().setMode(UIController.MODE_DRIVE);
    }

    /**
     * 경로 편집
     * 출발지/목적지/경유지를 편집할 수 있는 화면을 실행
     */
    @OnClick(R.id.reroute_icon_imageView)
    protected void clickRouteEdit() {
        //경로편집모드 표시
        showRouteEditView(RouteController.getInstance().getTempLocationList());
    }

    /**
     * 선택된 경로로 안내 시작
     */
    @OnClick(R.id.route_start_textview)
    protected void clickStartNavigation() {
        //경로타입뷰에서 현재 선택된 경로 index반환
        int routeIndex = routeTypeView.getSelectedRouteIndex();
        if (routeIndex < 0) {
            return;
        }
        //경로 index로 경로안내 시작 요청
        startNavigation(routeIndex);
    }

    /**
     * 상세경로 보기 / 경로타입 선택 토글
     * 상세 TBT정보를 보여주는 list와 경로타입을 선택하는 UI를 토글
     */
    @OnClick(R.id.route_detail_textview)
    protected void clickRouteDetail() {
        if (routeTypeView.getMode() == RouteTypeView.MODE_ROUTE_TYPE) { //tbt보기 모드로 변경
            routeDetailTextView.setText(R.string.route_type_view_toggle_select_route);
            routeTypeView.setMode(RouteTypeView.MODE_TBT_TYPE);
            //현재 viewpoint 저장
            lastViewPoint = MapController.getInstance().getCurrentViewPoint();
        } else {    //경로타입 모드로 변경
            routeDetailTextView.setText(R.string.route_type_view_toggle_show_tbt);
            routeTypeView.setMode(RouteTypeView.MODE_ROUTE_TYPE);
            //저장했던 viewpoint로 이동
            MapController.getInstance().changeViewpoint(lastViewPoint);
        }
    }


    /**
     * 선택된 경로를 이용하여 길안내 시작
     *
     * @param routeIndex 경로 인덱스
     */
    public void startNavigation(int routeIndex) {
        UIController.getInstance().getNavigationView().requestStartNavigation(routeSummary,
                routeIndex);
        UIUtils.showProgressDialog(getContext());
    }

    /**
     * 경로 요약정보 설정
     * 경로타입별 정보, 경로 path정보, 출발지/도착지/경유지 정보를 표시
     * 경로path, 출발지/도착지/경유지는 지도상에 표시
     *
     * @param routeSummary route summary 객체
     */
    public void setRouteSummary(RouteSummary routeSummary) {
        this.routeSummary = routeSummary;
        routeTypeView.resetView();
        routeTypeView.setRouteTypeInfo(routeSummary.routes, routeSummary.routePlan.routeTypes);

        //경로가 포함되는 영역 계산
        List<UTMK> pathNodeList = new ArrayList<>();
        for (Route route : routeSummary.routes) {
            pathNodeList.addAll(route.routePath());
        }
        UTMKBounds pathBounds = UTMKBounds.fromCoords(pathNodeList);
        //영역이 포함되는 지도줌레벨 변경
        MapController.getInstance()
                .changeViewpoint(pathBounds,
                        MapUtils.MAP_ANIMATION_DURATION_IN_MILLISECOND_ROUTE_VIEW,
                        GMap.AnimationTiming.LINEAR);
        //경로 path 표시
        MapController.getInstance().drawSummaryRoutes(routeSummary.routes);

        //출발지 마커 설정
        UTMK coord = routeSummary.getActiveRoute().routePath().get(0);
        MapController.getInstance().setStartMarkerPosition(coord);
        //도착지 마커 설정
        coord = routeSummary.getActiveRoute()
                .routePath()
                .get(routeSummary.getActiveRoute().routePath().size() - 1);
        MapController.getInstance().setFinishMarkerPosition(coord);
        //경유지 마커 설정
        MapController.getInstance().setWaypointMarkerPosition(routeSummary.routePlan.waypoints);
    }

    /**
     * 경로편집 화면 표출
     *
     * @param locationList 경로편집용 목적지 리스트
     */
    public void showRouteEditView(List<LocationItem> locationList) {
        //경로편집 화면 표출
        routeEditView.setVisibility(View.VISIBLE);
        routeEditView.initLocationData(locationList);
        //경로타입 뷰 숨김
        routeTypeView.setVisibility(View.INVISIBLE);
        //지도 routepath 숨김
        MapController.getInstance().hideRouteList();
    }

    /**
     * 경로편집 화면 종료
     */
    public void closeRouteEditView() {
        //경로편집 화면 숨김
        routeEditView.setVisibility(View.INVISIBLE);
        //경로타입 뷰 표출
        routeTypeView.setVisibility(View.VISIBLE);
        //지도 routepath 표출
        MapController.getInstance().showRouteList();
    }

    /**
     * 출발지/목적지 명칭 설정
     *
     * @param startLocation  출발지 정보
     * @param finishLocation 도착지 정보
     */
    public void setRouteLocationData(LocationItem startLocation, LocationItem finishLocation) {
        //경로편집 화면 출발지/도착지 명칭 설정
        routeEditView.setStartLocation(startLocation.getName());
        routeEditView.setFinishLocation(finishLocation.getName());
        //도착지 명칭 textview 설정
        destinationTextView.setText(finishLocation.getName());
    }
}
