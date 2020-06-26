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

import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.route.data.LocationItem;
import com.kt.rozenavi.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 경로 출발/도착/경유지 표시 View
 */
public class RouteDestinationView extends FrameLayout {

    @BindView(R.id.route_destination_recyclerview)
    protected RecyclerView recyclerView;

    private OnDestinationItemEventListener onDestinationChangeListener;

    private List<LocationItem> destinationList = new ArrayList<>();
    /**
     * 검색 요청 인덱스
     */
    public static int routeSelectLocationIndex = -1;

    /**
     * 목적지 탐색 수량 제한 수
     */
    private static final int MAX_DESTINATION_COUNT = 4;

    public RouteDestinationView(Context context) {
        super(context);
        init();
    }

    public RouteDestinationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public RouteDestinationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initView();
    }

    private void initView() {
        View.inflate(getContext(), R.layout.view_route_destination, this);
        ButterKnife.bind(this);
    }

    /**
     * 경유지 정보 추가
     * 경유지를 추가할때는 아무런 정보가 없는 상태에서 추가됨
     */
    private void addWaypointView() {
        if (destinationList.size() >= MAX_DESTINATION_COUNT) {
            return;
        }
        destinationList.add(destinationList.size() - 1, new LocationItem(-1, -1, ""));
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * 경유지 추가
     */
    @OnClick(R.id.route_destination_add_layout)
    protected void onAddDestinationClick() {
        addWaypointView();
    }

    @OnClick(R.id.route_destination_change_layout)
    protected void onChangeDestinationClick() {
        LocationItem firstLocationItem = destinationList.remove(0);
        LocationItem lastLocationItem = destinationList.remove(destinationList.size() - 1);
        destinationList.add(0, lastLocationItem);
        destinationList.add(firstLocationItem);

        recyclerView.getAdapter().notifyDataSetChanged();
        if (onDestinationChangeListener != null) {
            onDestinationChangeListener.onDestinationChanged(destinationList);
        }
    }


    /**
     * 검색 정보 설정
     * 검색 요청 인덱스, 검색 요청 타입 정보를 이용하여 출발지/목적지/경유지 정보를 설정
     *
     * @param x    x 좌표
     * @param y    y 좌표
     * @param name 명칭
     */
    public void setLocationData(double x, double y, String name) {
        LocationItem newLocationItem = new LocationItem(x, y, name);

        // 중복값 체크
        for (LocationItem item : destinationList) {
            if (newLocationItem.equals(item)) {
                UIUtils.showToast(getContext(), R.string.route_edit_view_waypoint_location_overlay);
                return;
            }
        }

        destinationList.set(routeSelectLocationIndex, new LocationItem(x, y, name));
        recyclerView.getAdapter().notifyDataSetChanged();

        //요청 값 초기화
        routeSelectLocationIndex = -1;

        if (onDestinationChangeListener != null) {
            onDestinationChangeListener.onDestinationChanged(destinationList);
        }
    }

    /**
     * 출발지/목적지/경유지 정보 초기 설정
     *
     * @param locationList 장소정보 리스트
     */
    public void initLocationData(List<LocationItem> locationList,
            OnDestinationItemEventListener destinationChangeListener) {
        this.onDestinationChangeListener = destinationChangeListener;
        destinationList.clear();
        destinationList.addAll(locationList);
        RouteDestinationRecyclerViewAdapter adapter =
                new RouteDestinationRecyclerViewAdapter(destinationList,
                        new RouteDestinationRecyclerViewAdapter.OnAdapterClickListener() {
                            @Override
                            public void onItemClick(int index) {
                                routeSelectLocationIndex = index;
                                if (onDestinationChangeListener != null) {
                                    onDestinationChangeListener.onDestinationClick();
                                }
                            }

                            @Override
                            public void onDeleteClick(int index) {
                                if (onDestinationChangeListener != null) {
                                    onDestinationChangeListener.onDestinationChanged(destinationList);
                                }
                            }
                        },
                        onDestinationChangeListener);

        // long press drag 용 ItemTouchHelper 등록
        ItemTouchHelper.Callback callback = new RouteDestinationTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(recyclerView);

        // RecyclerView 어뎁터 등록
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * Destination 이벤트 리스너
     */
    public interface OnDestinationItemEventListener {
        /**
         * Destination 리스트 변경 이벤트
         */
        void onDestinationChanged(List<LocationItem> destinationList);

        /**
         * Destination 항목 클릭 이벤트
         */
        void onDestinationClick();
    }
}
