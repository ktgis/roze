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

import com.kt.roze.data.model.Link;
import com.kt.roze.data.model.Route;
import com.kt.roze.data.model.Turn;
import com.kt.roze.resource.TurnResourceManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Tbt 정보 표시 Adapter
 */
public class RouteTbtRecyclerViewAdapter extends RecyclerView.Adapter<RouteTbtRecyclerViewAdapter.ViewHolder> {
    private List<Turn> turnList = null;
    private List<Link> linkList = null;
    private RouteTbtView.OnTbtItemEventListener tbtItemListener;

    RouteTbtRecyclerViewAdapter(Route route, RouteTbtView.OnTbtItemEventListener tbtItemListener) {
        this.turnList = route.turns;
        this.linkList = route.links;
        this.tbtItemListener = tbtItemListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(
                        parent.getContext()).inflate(
                        R.layout.view_route_tbt_row, parent, false));
    }

    /**
     * RecyclerView 용 View 홀더 내 데이터 설정
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Turn turn = turnList.get(position);
        switch (turn.type) {
            case 998:
                holder.tbtIconImageView.setImageResource(R.drawable.route_marker_start);
                break;
            case 999:
                holder.tbtIconImageView.setImageResource(R.drawable.route_marker_end);
                break;
            case 1000:
                holder.tbtIconImageView.setImageResource(R.drawable.route_marker_waypoint);
                break;
            default:
                int resId = TurnResourceManager.getResourceId(turn.type);
                if (resId > 0) {
                    holder.tbtIconImageView.setImageResource(resId);
                } else {
                    holder.tbtIconImageView.setImageDrawable(null);
                }
                break;
        }

        int resId = NaviUtils.getRgTypeString(turn.type);
        if (resId > 0) {
            holder.tbtTitleTextView.setText(resId);
        } else {
            holder.tbtTitleTextView.setText(turn.nodeName);
        }

        if (turn.nextDistance == 0) {
            holder.tbtDistanceTextView.setText("");
        } else {
            holder.tbtDistanceTextView.setText(NaviUtils.convertDistanceUnit(turn.nextDistance));
        }

        holder.toggleDivideView(getItemCount() - 1 == position);
    }

    @Override
    public int getItemCount() {
        return turnList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        @BindView(R.id.route_tbt_icon_imageview)
        protected ImageView tbtIconImageView;
        @BindView(R.id.route_tbt_title_textview)
        protected TextView tbtTitleTextView;
        @BindView(R.id.route_tbt_distance_textview)
        protected TextView tbtDistanceTextView;

        @BindView(R.id.row_divide_view)
        ImageView divideView;

        ViewHolder(View itemView) {
            super(itemView);
            this.rootView = itemView;
            ButterKnife.bind(this, itemView);
        }

        void toggleDivideView(boolean isLast) {
            if (isLast) {
                divideView.setVisibility(View.INVISIBLE);
            } else {
                divideView.setVisibility(View.VISIBLE);
            }
        }

        @OnClick(R.id.route_tbt_row)
        protected void onClickRow() {
            int clickIndex = getAdapterPosition();

            if (tbtItemListener != null) {
                int linkIndex = turnList.get(clickIndex).linkIndex;
                if (linkIndex < linkList.size()) {
                    tbtItemListener.onTbtSelected(clickIndex, linkList.get(linkIndex).getLastNode());
                }
            }
        }
    }

}
