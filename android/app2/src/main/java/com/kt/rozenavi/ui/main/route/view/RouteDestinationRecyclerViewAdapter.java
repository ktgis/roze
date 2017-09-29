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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.route.data.LocationItem;

import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * 경로 출발/도착/경유지 표시 Adapter
 */
public class RouteDestinationRecyclerViewAdapter extends
        RecyclerView.Adapter<RouteDestinationRecyclerViewAdapter.ViewHolder> {

    private List<LocationItem> destinationList = null;
    private OnAdapterClickListener onAdapterClickListener = null;
    private RouteDestinationView.OnDestinationItemEventListener onDestinationChangeListener = null;

    RouteDestinationRecyclerViewAdapter(List<LocationItem> destinationList,
                                        OnAdapterClickListener onAdapterClickListener,
                                        RouteDestinationView.OnDestinationItemEventListener onDestinationChangeListener) {
        this.destinationList = destinationList;
        this.onAdapterClickListener = onAdapterClickListener;
        this.onDestinationChangeListener = onDestinationChangeListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(
                        parent.getContext()).inflate(
                        R.layout.view_route_destination_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        LocationItem waypointItem = destinationList.get(position);

        holder.setType(position);
        holder.locationTextView.setText(waypointItem.getName());
    }

    @Override
    public int getItemCount() {
        return destinationList.size();
    }

    boolean onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(destinationList, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(destinationList, i, i - 1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
        notifyDataSetChanged();

        if (onDestinationChangeListener != null) {
            onDestinationChangeListener.onDestinationChanged(destinationList);
        }
        return true;
    }
    class ViewHolder extends RecyclerView.ViewHolder {

        /**
         * 출발지 타입
         */
        static final int TYPE_START = 0;
        /**
         * 도착지 타입
         */
        static final int TYPE_FINISH = 1;
        /**
         * 경유지 타입
         */
        static final int TYPE_WAYPOINT = 2;

        @BindView(R.id.route_destination_marker_icon)
        ImageView iconImageView;
        @BindView(R.id.route_destination_title_textview)
        TextView locationTextView;
        @BindView(R.id.route_destination_delete_icon)
        ImageView deleteImageView;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        /**
         * 목적지 타입 반환
         *
         * @param position 타겟 목적지 인덱스
         */
        private int getRouteType(int position) {
            int resultType = TYPE_WAYPOINT;

            if (position == 0) {
                resultType = TYPE_START;
            } else if (position == getItemCount() - 1) {
                resultType = TYPE_FINISH;
            }

            return resultType;
        }

        /**
         * 목적지 타입 별 icon 설정
         *
         * @param index 목적지 index
         */
        public void setType(int index) {
            int drawableId;
            switch (getRouteType(index)) {
                case TYPE_START:
                    drawableId = R.drawable.route_destination_start_icon;
                    deleteImageView.setVisibility(View.INVISIBLE);
                    break;
                case TYPE_FINISH:
                    drawableId = R.drawable.route_destination_end_icon;
                    deleteImageView.setVisibility(View.INVISIBLE);
                    break;
                default:
                    drawableId = R.drawable.route_destination_waypoint_icon;
                    deleteImageView.setVisibility(View.VISIBLE);
                    break;
            }

            iconImageView.setImageResource(drawableId);
        }

        @OnClick(R.id.route_destination_delete_icon)
        protected void onClickDelete() {
            int index = getAdapterPosition();
            LocationItem removeItem = destinationList.remove(index);
            notifyDataSetChanged();

            if (TextUtils.isEmpty(removeItem.getName())) {
                return;
            }

            if (onAdapterClickListener != null) {
                onAdapterClickListener.onDeleteClick(getAdapterPosition());
            }
        }

        @OnClick(R.id.route_destination_row)
        protected void onClickRow() {
            if (onAdapterClickListener != null) {
                onAdapterClickListener.onItemClick(getAdapterPosition());
            }
        }
    }

    interface OnAdapterClickListener {
        void onItemClick(int index);
        void onDeleteClick(int index);
    }
}
