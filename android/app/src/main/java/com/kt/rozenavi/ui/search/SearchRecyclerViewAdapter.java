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

package com.kt.rozenavi.ui.search;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kt.roze.search.model.SearchPlaceData;
import com.kt.rozenavi.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 목적지 검색 결과 RecyclerViewAdapter
 * 목적지 항목을 표출 할 수 있도록 처리하며 클릭시 이벤트를 반환하는 리스너를
 * 설정할 수 있는 기능을 제공
 */
class SearchRecyclerViewAdapter
        extends RecyclerView.Adapter<SearchRecyclerViewAdapter.LocationViewHolder>
        implements RecyclerAdapterDataModel<SearchPlaceData> {

    /**
     * 검색 결과 장소 리스트
     */
    private List<SearchPlaceData> searchPlaceDataList = null;
    /**
     * 클릭 리스너
     */
    private View.OnClickListener onClickListener = null;

    SearchRecyclerViewAdapter(
            List<SearchPlaceData> searchPlaceDataList, View.OnClickListener listener) {
        this.searchPlaceDataList = searchPlaceDataList;
        this.onClickListener = listener;
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_search_row, parent, false);
        itemView.setOnClickListener(onClickListener);
        return new LocationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LocationViewHolder holder, int position) {
        SearchPlaceData placeData = searchPlaceDataList.get(position);
        holder.name.setText(placeData.name);
        holder.address.setText(placeData.address);
    }

    @Override
    public int getItemCount() {
        return searchPlaceDataList.size();
    }

    @Override
    public void add(SearchPlaceData item) {
        if (item == null) {
            return;
        }

        searchPlaceDataList.add(item);
        notifyDataSetChanged();
    }

    @Override
    public SearchPlaceData getItem(int index) {
        return searchPlaceDataList.get(index);
    }

    @Override
    public void clear() {
        searchPlaceDataList.clear();
    }

    class LocationViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text_location_name)
        TextView name;
        @BindView(R.id.text_location_address)
        TextView address;

        LocationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
