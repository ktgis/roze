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
package com.kt.rozenavi.ui.search;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.search.place.model.poi.Poi;
import com.kt.rozenavi.utils.CommonUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 목적지 검색 결과 RecyclerViewAdapter
 * 목적지 항목을 표출 할 수 있도록 처리하며 클릭시 이벤트를 반환하는 리스너를
 * 설정할 수 있는 기능을 제공
 */
public class SearchRecyclerViewAdapter extends RecyclerView.Adapter<SearchRecyclerViewAdapter.LocationViewHolder>
        implements RecyclerAdapterDataModel<Poi> {

    /**
     * 검색 결과 장소 리스트
     */
    private List<Poi> searchPlaceDataList = null;
    /**
     * 클릭 리스너
     */
    private View.OnClickListener onClickListener = null;

    public SearchRecyclerViewAdapter(List<Poi> searchPlaceDataList, View.OnClickListener listener) {
        this.searchPlaceDataList = searchPlaceDataList;
        this.onClickListener = listener;
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_search_row, parent, false);
        itemView.setOnClickListener(onClickListener);
        return new LocationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LocationViewHolder holder, int position) {
        Poi placeData = searchPlaceDataList.get(position);
        holder.name.setText(placeData.getPoiName());
        holder.address.setText(placeData.getPoiAddress());
    }

    @Override
    public int getItemCount() {
        return searchPlaceDataList.size();
    }

    @Override
    public void add(Poi poi) {
        if (poi == null) {
            return;
        }

        searchPlaceDataList.add(poi);
        notifyDataSetChanged();
    }

    @Override
    public Poi getItem(int index) {
        return searchPlaceDataList.get(index);
    }

    @Override
    public void clear() {
        searchPlaceDataList.clear();
    }

    /**
     * 스크롤 이용한 추가 검색 중 신규 명칭으로 검색을 눌렀을 때, 기존 검색 결과 데이터 초기화
     */
    public void clearData() {
        if (!CommonUtils.isEmpty(searchPlaceDataList)) {
            searchPlaceDataList.clear();
            notifyDataSetChanged();
        }
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
