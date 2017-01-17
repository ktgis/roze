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
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.UIController;
import com.kt.rozenavi.ui.main.route.data.LocationItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 경로편집 화면 클래스
 * 임시 출발지/목적지/경유지 정보를 이용하여 경로를 편집하고
 * 사용자가 탐색을 요청할수 있도록 기능 제공
 */
public class RouteEditView extends RelativeLayout {
    /**
     * 목적지 정보 표시 layout
     */
    @BindView(R.id.route_destination_layout)
    protected LinearLayout destinationLayout;
    /**
     * 검색 요청 인덱스
     */
    private int routeSelectLocationIndex = -1;
    /**
     * 검색 요청 타입
     */
    private int routeSelectLocationType = -1;

    public RouteEditView(Context context) {
        super(context);
        initView(context);
    }

    public RouteEditView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RouteEditView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * 경로편집 화면 초기화
     * 레이아웃 설정 및 객체 생성
     *
     * @param context context 객체
     */
    private void initView(Context context) {
        View.inflate(context, R.layout.view_route_select, this);
        ButterKnife.bind(this);

        //목적지 정보 레이아웃에 출발지/목적지 항목 추가
        destinationLayout.addView(
                new LocationItemView(context).setType(LocationItemView.TYPE_START));

        destinationLayout.addView(
                new LocationItemView(context).setType(LocationItemView.TYPE_FINISH));
        setVisibility(GONE);
    }

    /**
     * 출발지 명칭 설정
     *
     * @param name 명칭
     */
    public void setStartLocation(String name) {
        //출발지 정보는 목적지 레이아웃에서 첫번째 항목
        ((LocationItemView) destinationLayout.getChildAt(0)).setLocation(name);
    }

    /**
     * 도착지 명칭 설정
     *
     * @param name 명칭
     */
    public void setFinishLocation(String name) {
        //도착지 정보는 목적지 레이아웃에서 마지막 항목
        ((LocationItemView) destinationLayout.getChildAt(
                destinationLayout.getChildCount() - 1)).setLocation(name);
    }

    /**
     * 경유지 정보 추가
     * 경유지를 추가할때는 아무런 정보가 없는 상태에서 추가됨
     */
    private void addWaypointView() {
        //출발지/목적지 포함 최대 4개의 좌표를 지원
        //경유지는 최대 2개까지 추가 가능하도록 제한
        if (destinationLayout.getChildCount() >= 4) {
            return;
        }

        //경유지 정보는 목적지 레이아웃에서 항상 마지막의 이전 항목을 신규 추가가
        destinationLayout.addView(
                new LocationItemView(getContext()).setType(LocationItemView.TYPE_WAYPOINT),
                destinationLayout.getChildCount() - 1);
    }

    /**
     * 출발지/목적지 상호 교환
     */
    @OnClick(R.id.route_destination_change_imageview)
    protected void changeDestination() {
        RouteController.getInstance().changeDestination();
    }

    /**
     * 경유지 추가
     */
    @OnClick(R.id.route_waypoint_add_layout)
    protected void addWaypoint() {
        addWaypointView();
    }

    /**
     * 경로 편집 취소
     */
    @OnClick(R.id.route_select_cancel_textview)
    protected void cancelRouteSelect() {
        resetLocationData();
        //경로검색 화면에서 경로편집 화면 종료
        UIController.getInstance().getRouteView().closeRouteEditView();
    }

    /**
     * 경로 탐색
     * 현재 경로 편집된 정보로 신규 경로를 요청
     */
    @OnClick(R.id.route_select_search_textview)
    protected void searchRoute() {
        resetLocationData();
        //경로검색 화면에서 경로편집 화면 종료
        UIController.getInstance().getRouteView().closeRouteEditView();
        //경로검색 요청
        RouteController.getInstance().requestRoute(getContext());

    }

    /**
     * 검색 정보 설정
     * 검색 요청 인덱스, 검색 요청 타입 정보를 이용하여 출발지/목적지/경유지 정보를 설정
     *
     * @param x    x 좌표
     * @param y    y 좌표
     * @param name 명칭
     */
    public void setLocationData(int x, int y, String name) {
        //검색 요청 인덱스를 이용해 검색된 명칭 설정
        setLocationName(routeSelectLocationIndex, name);
        //경로검색 제어 클래스에 요청 인덱스와 타입에 해당하는 정보를 설정
        RouteController.getInstance()
                .setSearchLocation(routeSelectLocationIndex, routeSelectLocationType, x, y,
                        name);
        //요청 값 초기화
        routeSelectLocationIndex = -1;
        routeSelectLocationType = -1;
    }

    /**
     * 출발지/목적지/경유지 명칭 설정
     * index에 해당하는 위치의 명칭을 변경
     *
     * @param index 목적지 표시 layout내부 인덱스
     * @param name  명칭
     */
    public void setLocationName(int index, String name) {
        if (index < 0 || destinationLayout.getChildCount() <= index) {
            return;
        }
        ((LocationItemView) destinationLayout.getChildAt(index)).setLocation(name);
    }

    /**
     * 출발지/목적지/경유지 정보 초기 설정
     *
     * @param locationList 장소정보 리스트
     */
    public void initLocationData(List<LocationItem> locationList) {
        setStartLocation(locationList.get(0).getName());
        setFinishLocation(locationList.get(locationList.size() - 1).getName());

        for (int i = 1; i < locationList.size() - 1; i++) {
            addWaypointView();
            setLocationName(i, locationList.get(i).getName());
        }
    }

    /**
     * 목적지 정보 초기화
     */
    private void resetLocationData() {
        setStartLocation("");
        setFinishLocation("");

        //출발지/목적지를 제외한 경유지 정보는 초기화 시점에서 제거
        destinationLayout.removeViews(1, destinationLayout.getChildCount() - 2);
    }

    /**
     * 목적지 정보 레이아웃 클래스
     */
    class LocationItemView extends RelativeLayout {
        /**
         * 출발지 타입
         */
        public static final int TYPE_START = 0;
        /**
         * 도착지 타입
         */
        public static final int TYPE_FINISH = 1;
        /**
         * 경유지 타입
         */
        public static final int TYPE_WAYPOINT = 2;

        @BindView(R.id.route_select_row_icon_imageview)
        ImageView iconImageView;
        @BindView(R.id.route_select_row_textview)
        TextView locationTextView;
        @BindView(R.id.route_select_row_delete_imageview)
        ImageView deleteImageView;

        private int type;

        public LocationItemView(Context context) {
            super(context);
            initView(context);
        }

        public LocationItemView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initView(context);
        }

        public LocationItemView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            initView(context);
        }

        /**
         * 목적지 정보 레이아웃 초기화
         * 레이아웃 설정 및 객체 생성
         *
         * @param context context 객체
         */
        private void initView(Context context) {
            View.inflate(context, R.layout.layout_route_edit_row, this);
            ButterKnife.bind(this);
        }

        /**
         * 목적지 타입 설정
         *
         * @param type 타입정보
         * @return 현재 객체
         */
        public LocationItemView setType(int type) {
            this.type = type;
            //타입에 따른 아이콘 설정
            if (type == TYPE_START) {
                iconImageView.setImageResource(R.drawable.ico_route_map_start);
            } else if (type == TYPE_FINISH) {
                iconImageView.setImageResource(R.drawable.ico_route_map_arrive);
            } else if (type == TYPE_WAYPOINT) {
                iconImageView.setImageResource(R.drawable.ico_route_map_via);
                //경유지일 경우 삭제가 가능하도록 삭제버튼 표출
                deleteImageView.setVisibility(View.VISIBLE);
            }
            return this;
        }

        /**
         * 목적지 명칭 설정
         *
         * @param name 명칭
         * @return 현재 객체
         */
        public LocationItemView setLocation(String name) {
            locationTextView.setText(name);
            return this;
        }

        @OnClick({R.id.route_select_row, R.id.route_select_row_delete_imageview})
        protected void onClick(View view) {
            int id = view.getId();
            if (id == R.id.route_select_row_delete_imageview) { //삭제버튼 클릭
                //경유지 정보 삭제
                RouteController.getInstance().removeWaypoint(destinationLayout.indexOfChild(this));
                destinationLayout.removeView(this);
            } else { //목적지 레이아웃 클릭
                //요청타입 설정
                routeSelectLocationIndex = destinationLayout.indexOfChild(this);
                routeSelectLocationType = type;
                //목적지 검색 요청
                UIController.getInstance().mainActivity.requestSearch();
            }
        }
    }
}
