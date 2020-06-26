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
package com.kt.rozenavi.ui.main.route.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.kt.roze.data.model.Route;
import com.kt.roze.routing.RoutePlan;
import com.kt.rozenavi.R;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 경로 타입 표시 View
 */
public class RouteTypeView extends FrameLayout {

    @BindView(R.id.route_type_recyclerview)
    protected RecyclerView recyclerView;

    public RouteTypeView(Context context) {
        super(context);
        init();
    }

    public RouteTypeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RouteTypeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.view_route_type, this);
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * 경로검색을 통해 전달받은 route정보 설정
     * 경로정보의 전체 거리, 예상 시간, 예상 톨게이트 비용등을 표시
     * 경로가 2개이상일경우 리스트처럼 여러개의 항목으로 표시
     *
     * @param routeList  경로 리스트
     * @param routeTypes 요청 경로타입 리스트
     */
    public void setRouteInfo(List<Route> routeList, List<RoutePlan.RouteType> routeTypes,
            RouteTypeRecyclerViewAdapter.OnRouteTypeItemEventListener onRouteTypeItemEventListener) {
        recyclerView.setAdapter(new RouteTypeRecyclerViewAdapter(routeList, routeTypes, onRouteTypeItemEventListener));
    }
}
