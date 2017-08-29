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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kt.roze.data.model.Route;
import com.kt.roze.routing.RoutePlan;
import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 경로 타입 표시 Adapter
 */
public class RouteTypeRecyclerViewAdapter extends
        RecyclerView.Adapter<RouteTypeRecyclerViewAdapter.ViewHolder> {

    private List<Route> routeList = null;
    private List<RoutePlan.RouteType> routeTypeList = null;
    private OnRouteTypeItemEventListener routeTypeItemListener;
    private int selectedIndex = 0;

    RouteTypeRecyclerViewAdapter(List<Route> routeList, List<RoutePlan.RouteType> routeTypeList,
            OnRouteTypeItemEventListener routeTypeItemListener) {
        this.routeList = routeList;
        this.routeTypeList = routeTypeList;
        this.routeTypeItemListener = routeTypeItemListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(
                        parent.getContext()).inflate(
                        R.layout.view_route_type_row, parent, false));
    }

    /**
     * RecyclerView 용 View 홀더 내 데이터 설정
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Route route = routeList.get(position);
        RoutePlan.RouteType routeType = routeTypeList.get(position);

        holder.remainTimeTextView.setText(NaviUtils.convertRemainTimeByFormat(route.time));
        holder.remainDistanceTextView.setText(NaviUtils.convertDistanceUnit(route.distance));
        holder.tollTextView.setText(NaviUtils.convertPrice(route.totalToll));
        holder.arriveTimeTextView.setText(NaviUtils.convertArrivedTime(route.time));
        holder.typeNameTextView.setText(NaviUtils.getRouteTypeStringRes(routeType));

        holder.setSelected(selectedIndex == position);
        holder.toggleDivideView(getItemCount() - 1 == position);
    }

    @Override
    public int getItemCount() {
        return routeList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        @BindView(R.id.route_type_remain_time_textview)
        TextView remainTimeTextView;
        @BindView(R.id.route_type_arrive_time_textview)
        TextView arriveTimeTextView;
        @BindView(R.id.route_type_remain_distance_textview)
        TextView remainDistanceTextView;
        @BindView(R.id.route_type_toll_textview)
        TextView tollTextView;
        @BindView(R.id.route_type_name_textview)
        TextView typeNameTextView;
        @BindView(R.id.row_divide_view)
        ImageView divideView;

        ViewHolder(View itemView) {
            super(itemView);
            this.rootView = itemView;
            ButterKnife.bind(this, itemView);
        }

        void setSelected(boolean isSelect) {
            rootView.setSelected(isSelect);
        }

        void toggleDivideView(boolean isLast) {
            if (isLast) {
                divideView.setVisibility(View.INVISIBLE);
            } else {
                divideView.setVisibility(View.VISIBLE);
            }
        }

        @OnClick(R.id.route_type_row)
        protected void onClickRow() {
            int lastSelectedIndex = selectedIndex;
            selectedIndex = getAdapterPosition();

            notifyItemChanged(lastSelectedIndex);
            notifyItemChanged(selectedIndex);

            if (routeTypeItemListener != null) {
                routeTypeItemListener.onRouteTypeSelected(selectedIndex);
            }
        }

        @OnClick(R.id.route_type_detail_imageview)
        protected void onClickDetail() {
            if (selectedIndex != getAdapterPosition()) {
                onClickRow();
                return;
            }

            if (routeTypeItemListener != null) {
                routeTypeItemListener.onRouteDetailClick(getAdapterPosition());
            }
        }
    }

    /**
     * 경로 타입 이벤트 리스너
     */
    public interface OnRouteTypeItemEventListener {
        /**
         * 경로 타입 항목 선택 이벤트
         */
        void onRouteTypeSelected(int index);

        /**
         * 경로 상세보기 이벤트
         */
        void onRouteDetailClick(int index);
    }
}
