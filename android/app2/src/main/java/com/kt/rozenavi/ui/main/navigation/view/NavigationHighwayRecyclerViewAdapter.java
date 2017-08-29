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

package com.kt.rozenavi.ui.main.navigation.view;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.kt.roze.data.model.EnergyPrice;
import com.kt.roze.guidance.model.HighwayGuidance;
import com.kt.roze.guidance.model.SAGasStation;
import com.kt.roze.guidance.model.SAGuidance;
import com.kt.roze.guidance.model.TGGuidance;
import com.kt.roze.resource.GasStationResourceManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * 고속도로 정보 표시 Adapter
 */
class NavigationHighwayRecyclerViewAdapter extends
        RecyclerView.Adapter<NavigationHighwayRecyclerViewAdapter.ViewHolder> {

    public static final int MAX_VISIBLE_COUNT = 3;
    public int extraIconSize;
    public int extraIconMargin;

    private List<HighwayGuidance> highwayGuidanceList = null;
    private int remainDistance = 0;

    NavigationHighwayRecyclerViewAdapter(Context context,
            List<HighwayGuidance> highwayGuidanceList) {
        this.highwayGuidanceList = highwayGuidanceList;
        extraIconSize =
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15,
                        context.getResources().getDisplayMetrics());
        extraIconMargin =
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5,
                        context.getResources().getDisplayMetrics());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(
                        parent.getContext()).inflate(
                        R.layout.view_navigation_highway_row, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HighwayGuidance highway = highwayGuidanceList.get(position);
        Resources res = holder.rootView.getResources();
        //item background set
        switch (position) {
            case 0:
                holder.rootView.setBackgroundResource(R.drawable.highway_row_background_alpha_80);
                break;
            case 1:
                holder.rootView.setBackgroundResource(R.drawable.highway_row_background_alpha_60);
                break;
            default:
                holder.rootView.setBackgroundResource(R.drawable.highway_row_background_alpha_40);
                break;
        }

        //item name set
        switch (highway.getType()) {
            case HighwayGuidance.Type.TG:
                StringBuilder builder = new StringBuilder();
                builder.append(highway.getNodeName());
                TGGuidance TG = (TGGuidance) highway;
                if (TG.getToll() != 0) {
                    builder.append("\n요금 : ").append(TG.getToll());
                }
                holder.nameTextView.setText(builder.toString());
                break;
            case HighwayGuidance.Type.RA:
                holder.nameTextView.setText("졸음쉼터");
                break;
            default:
                holder.nameTextView.setText(highway.getNodeName());
                break;
        }
        holder.nameTextView.setTextColor(position == 0 ?
                res.getColor(R.color.dawm_grey) : res.getColor(R.color.dawm_grey_alpha_80));

        //item distance set
        holder.distanceTextView.setText(
                NaviUtils.convertDistanceUnit(highway.getDistance() + remainDistance));
        holder.distanceTextView.setTextColor(position == 0 ?
                res.getColor(R.color.dawm_grey) : res.getColor(R.color.dawm_grey_alpha_80));
        //item extra grid set
        holder.extraGridLayout.removeAllViews();
        if (highway.getType() == HighwayGuidance.Type.SA) {
            SAGuidance sa = (SAGuidance) highway;

            List<SAGasStation> gasStations = sa.getGasInforms();
            if (gasStations != null && !gasStations.isEmpty()) {
                int resId;
                ImageView item;
                GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                        new ViewGroup.LayoutParams(extraIconSize, extraIconSize));
                params.rightMargin = extraIconMargin;

                for (SAGasStation g : gasStations) {
                    resId = GasStationResourceManager.getSAGasResourceID(g.getBrand()
                            , EnergyPrice.EnergyType.valueOf(g.getEnergySrc()));
                    if (resId != GasStationResourceManager.RESOURCE_NOT_FOUND) {
                        item = new ImageView(holder.extraGridLayout.getContext());
                        item.setImageResource(resId);
                        holder.extraGridLayout.addView(item, params);
                    }
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        int realSize = highwayGuidanceList.size();
        return realSize > MAX_VISIBLE_COUNT ? MAX_VISIBLE_COUNT : realSize;
    }

    public void setRemainDistance(int distance) {
        this.remainDistance = distance;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        @BindView(R.id.highway_name_textview)
        TextView nameTextView;
        @BindView(R.id.highway_distance_textview)
        TextView distanceTextView;
        @BindView(R.id.hightway_extra_gridlayout)
        GridLayout extraGridLayout;

        ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            this.rootView = itemView;
        }
    }
}
