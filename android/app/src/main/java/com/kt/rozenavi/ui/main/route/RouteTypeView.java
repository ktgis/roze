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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.geom.model.UTMK;
import com.kt.roze.data.model.Route;
import com.kt.roze.data.model.Turn;
import com.kt.roze.routing.RoutePlan;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.MapController;
import com.kt.rozenavi.ui.main.route.data.TbtItem;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 경로타입 뷰 클래스
 * 경로검색을 통해 전달받은 경로정보에서 경로거리, 예상시간, 예상 톨게이트 비용등을 표시하고
 * 경로에대한 전체 tbt정보를 리스트로 표시하는 기능을 제공
 */
public class RouteTypeView extends RelativeLayout {
    /**
     * 경로타입 표출 모드
     * 경로에 대한 거리, 예상 시간, 예상 비용을 표시하는 상태
     */
    public static final int MODE_ROUTE_TYPE = 0;
    /**
     * tbt정보 표출 모드
     * 경로의 전체 tbt정보를 리스트 형태로 표시하는 상태
     */
    public static final int MODE_TBT_TYPE = 1;

    /**
     * 경로타입 정보 레이아웃
     */
    @BindView(R.id.route_list_layout)
    protected LinearLayout routeListLayout;
    /**
     * 경로 tbt recyclerview
     */
    @BindView(R.id.route_tbt_recyclerview)
    protected RecyclerView routeTbtRecyclerView;

    /**
     * 경로정보 리스트
     */
    private List<Route> routeList;

    /**
     * 현재 선택된 경로 아이템 뷰
     */
    private View selectedRoute;
    /**
     * 현재 표출 모드
     */
    private int currentMode = MODE_ROUTE_TYPE;

    public RouteTypeView(Context context) {
        super(context);
        initView(context);
    }

    public RouteTypeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RouteTypeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * 경로타입 뷰 초기화
     * 레이아웃 설정 및 객체 생성
     *
     * @param context context 객체
     */
    private void initView(Context context) {
        View.inflate(context, R.layout.view_route_type, this);
        ButterKnife.bind(this);

        //recyclerview에 기본적인 linearlayoutmanager를 설정
        //listview와 동일한 레이아웃으로 표출
        routeTbtRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * 현재 경로타입 뷰 모드 반환
     * 경로타입 정보를 보여주는 모드인지 전체 tbt 리스트를 보여주는 모드인지 반환
     *
     * @return 현재 설정된 모드 반환
     * @see #MODE_ROUTE_TYPE
     * @see #MODE_TBT_TYPE
     */
    public int getMode() {
        return currentMode;
    }

    /**
     * 경로타입 뷰 모드 설정
     * 경로타입 정보를 보여주는 상태, 전체 tbt 리스트를 보여주는 상태로 설정
     *
     * @param mode 모드 상수
     * @see #MODE_ROUTE_TYPE
     * @see #MODE_TBT_TYPE
     */
    public void setMode(int mode) {
        this.currentMode = mode;
        if (currentMode == MODE_ROUTE_TYPE) {   //경로타입 표출 모드
            //tbt recyclerview를 animation을 이용하여 화면에서 숨김
            routeTbtRecyclerView.setAnimation(
                    AnimationUtils.loadAnimation(getContext(), R.anim.slide_down));
            routeTbtRecyclerView.setVisibility(View.GONE);
        } else {    //tbt표출 모드
            //tbt recyclerview를 animation을 이용하여 화면에 표시
            routeTbtRecyclerView.setAnimation(
                    AnimationUtils.loadAnimation(getContext(), R.anim.slide_up));
            routeTbtRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 경로검색을 통해 전달받은 route정보 설정
     * 경로정보의 전체 거리, 예상 시간, 예상 톨게이트 비용등을 표시
     * 경로가 2개이상일경우 리스트처럼 여러개의 항목으로 표시
     *
     * @param list       경로 리스트
     * @param routeTypes 요청 경로타입 리스트
     */
    public void setRouteTypeInfo(List<Route> list, List<RoutePlan.RouteType> routeTypes) {
        //경로정보 리스트
        routeList = list;
        RouteRowLayout routeRow;
        //경로정보 아이템 뷰를 생성하여 데이터 설정 후 리스트 레이아웃에 추가
        for (int i = 0; i < list.size(); i++) {
            routeRow = new RouteRowLayout(getContext());
            routeRow.setRouteIndex(i);
            routeRow.setDistance(NaviUtils.convertDistanceUnit(list.get(i).distance));
            routeRow.setTotalToll(NaviUtils.convertPrice(list.get(i).totalToll));
            routeRow.setArrivedTime(NaviUtils.convertArrivedTime(list.get(i).time));
            routeRow.setRouteType(routeTypes.get(i).name());
            routeListLayout.addView(routeRow);

        }
        //기본으로 첫번째 경로를 선택
        selectRouteView(routeListLayout.getChildAt(0));
    }

    /**
     * 경로 아이템뷰 선택
     * 사용자가 선택한 경로를 활성화
     * 선택된 경로 아이템뷰를 selected상태로 변경
     *
     * @param view 선택된 경로 아이템 뷰 객체
     */
    private void selectRouteView(View view) {
        //기존에 선택된 것과 같으면 종료
        if (selectedRoute == view) {
            return;
        }
        //기존에 선택된 경로는 비활성화
        if (selectedRoute != null) {
            selectedRoute.setSelected(false);
        }

        selectedRoute = view;
        selectedRoute.setSelected(true);
        //선택된 경로의 tbt 리스트 설정
        setRouteTbtList(((RouteRowLayout) selectedRoute).getRouteIndex());
    }

    /**
     * 경로의 전체 tbt 리스트 설정
     * 선택된 경로의 전체 tbt리스트를 tbt recyclerview에 설정
     *
     * @param routeIndex 선택된 경로 index
     */
    private void setRouteTbtList(int routeIndex) {
        //선택한 index에 해당하는 경로정보 반환
        Route currentRoute = routeList.get(routeIndex);

        List<TbtItem> tbtItemList = new ArrayList<>();
        TbtItem item;
        UTMK lastNode;
        //경로정보에서 turn정보와 link정보를 이용해 tbtitem 객체 생성
        for (Turn turn : currentRoute.turns) {
            item = new TbtItem();
            item.setName(turn.nodeName);
            item.setNextDistance(turn.nextDistance);
            item.setType(turn.type);
            lastNode = currentRoute.links.get(turn.linkIndex).getLastNode();
            item.setX(lastNode.x);
            item.setY(lastNode.y);
            tbtItemList.add(item);
        }
        //tbt데이터를 adapter를 통해 recyclerview에 설정
        routeTbtRecyclerView.setAdapter(new TbtRecyclerViewAdapter(tbtItemList));
    }

    /**
     * 현재 선택된 경로의 index 반환
     *
     * @return 경로 index
     */
    public int getSelectedRouteIndex() {
        return ((RouteRowLayout) selectedRoute).getRouteIndex();
    }

    /**
     * 경로타입 뷰 초기화
     */
    public void resetView() {
        currentMode = MODE_ROUTE_TYPE;
        routeListLayout.removeAllViews();
        routeTbtRecyclerView.setVisibility(View.GONE);
    }

    /**
     * 경로 아이템 뷰 클래스
     * 경로정보를 표시하는 뷰객체 클래스로 경로 타입, 경로 전체 거리, 예상 도착시간, 예상 톨게이트 비용등을 표시
     */
    public class RouteRowLayout extends RelativeLayout {
        /**
         * 경로 타입 textview
         */
        @BindView(R.id.route_type_textview)
        TextView routeTypeTextView;
        /**
         * 예상도착시간 textview
         */
        @BindView(R.id.route_arrived_time_textview)
        TextView routeArrivedTimeTextView;
        /**
         * 경로 전체거리 textview
         */
        @BindView(R.id.route_dis_textview_textview)
        TextView routeDistanceTextView;
        /**
         * 예상 톨게이트 비용 textview
         */
        @BindView(R.id.route_totaltoll_textview_textview)
        TextView routeTotalTollTextView;

        /**
         * 경로 index
         */
        private int routeIndex;

        public RouteRowLayout(Context context) {
            super(context);
            initView(context);
        }

        public RouteRowLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            initView(context);
        }

        public RouteRowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
            initView(context);
        }

        /**
         * 경로 아이템 뷰 초기화
         * 레이아웃 설정 및 객체 생성
         *
         * @param context context 객체
         */
        private void initView(Context context) {
            View.inflate(context, R.layout.layout_route_type_row, this);
            ButterKnife.bind(this);
        }

        /**
         * 경로 타입 설정
         * {@see com.kt.roze.routing.RoutePlan.RouteType} name()을 이용하여
         * string 값으로 설정
         *
         * @param type 경로타입
         */
        public void setRouteType(String type) {
            routeTypeTextView.setText(type);
        }

        /**
         * 경로 index 설정
         *
         * @param index 경로 index
         */
        public void setRouteIndex(int index) {
            routeIndex = index;
        }

        /**
         * 경로 index 반환
         *
         * @return 경로 index
         */
        public int getRouteIndex() {
            return routeIndex;
        }

        /**
         * 예상 도착시간 설정
         *
         * @param time 예상 도착 시간
         */
        public void setArrivedTime(String time) {
            routeArrivedTimeTextView.setText(time);
        }

        /**
         * 경로 전체 거리 설정
         *
         * @param distance 경로 전체 거리
         */
        public void setDistance(String distance) {
            routeDistanceTextView.setText(distance);
        }

        /**
         * 예상 톨게이트 비용 설정
         *
         * @param toll 예상 톨게이트 비용
         */
        public void setTotalToll(String toll) {
            routeTotalTollTextView.setText(toll);
        }

        @OnClick({R.id.route_type_textview, R.id.search_row})
        protected void onClick(View view) {
            int id = view.getId();
            if (id == R.id.search_row) {
                //현재 아이템뷰를 선택
                selectRouteView(this);
                //지도에서 routepath를 선택 처리
                MapController.getInstance().selectRoute(routeIndex);
            }
        }
    }
}
