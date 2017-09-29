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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.kt.geom.model.UTMK;
import com.kt.roze.data.model.Route;
import com.kt.rozenavi.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Tbt 정보 표시 View
 */
public class RouteTbtView extends FrameLayout {
    @BindView(R.id.route_tbt_recyclerview)
    protected RecyclerView recyclerView;

    private List<Route> routeList;
    private OnTbtItemEventListener onRouteTypeItemListener;

    public RouteTbtView(Context context) {
        super(context);
        init();
    }

    public RouteTbtView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RouteTbtView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.view_route_tbt, this);
        ButterKnife.bind(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public void setRouteInfo(List<Route> routeList, OnTbtItemEventListener onRouteTypeItemListener) {
        //경로정보 리스트
        this.routeList = routeList;
        this.onRouteTypeItemListener = onRouteTypeItemListener;
        recyclerView.setAdapter(new RouteTbtRecyclerViewAdapter(routeList.get(0), onRouteTypeItemListener));
    }

    public void setRouteIndex(int routeIndex) {
        recyclerView.setAdapter(new RouteTbtRecyclerViewAdapter(routeList.get(routeIndex), onRouteTypeItemListener));
    }

    /**
     * Tbt 정보 이벤트 리스너
     */
    public interface OnTbtItemEventListener {
        void onTbtSelected(int index, UTMK utmk);
    }
}
