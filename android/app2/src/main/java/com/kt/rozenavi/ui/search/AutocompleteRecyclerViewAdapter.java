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
import com.kt.rozenavi.utils.CommonUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 자동완성 RecyclerViewAdapter
 * 자동완성 항목을 표출 할 수 있도록 처리하며 클릭시 이벤트를 반환하는 리스너를
 * 설정할 수 있는 기능을 제공
 */
public class AutocompleteRecyclerViewAdapter
        extends RecyclerView.Adapter<AutocompleteRecyclerViewAdapter.LocationViewHolder>
        implements RecyclerAdapterDataModel<String> {

    /**
     * 검색 결과 장소 리스트
     */
    private List<String> searchPlaceDataList = null;
    /**
     * 클릭 리스너
     */
    private View.OnClickListener onClickListener = null;

    public AutocompleteRecyclerViewAdapter(
            List<String> searchPlaceDataList, View.OnClickListener listener) {
        this.searchPlaceDataList = searchPlaceDataList;
        this.onClickListener = listener;
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_autocomplete_row, parent, false);
        itemView.setOnClickListener(onClickListener);
        return new LocationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LocationViewHolder holder, int position) {
        String placeData = searchPlaceDataList.get(position);
        holder.name.setText(placeData);
    }

    @Override
    public int getItemCount() {
        return searchPlaceDataList == null ? 0 : searchPlaceDataList.size();
    }

    @Override
    public void add(String poi) {
        if (poi == null) {
            return;
        }

        searchPlaceDataList.add(poi);
        notifyDataSetChanged();
    }

    @Override
    public String getItem(int index) {
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
        @BindView(R.id.autocomplete_name)
        TextView name;

        LocationViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
