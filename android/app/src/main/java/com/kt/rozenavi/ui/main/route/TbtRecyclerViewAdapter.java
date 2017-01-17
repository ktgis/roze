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

package com.kt.rozenavi.ui.main.route;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.kt.geom.model.UTMK;
import com.kt.maps.GMap;
import com.kt.roze.resource.TurnResourceManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.MapController;
import com.kt.rozenavi.ui.main.route.data.TbtItem;
import com.kt.rozenavi.utils.MapUtils;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * tbt 정보 recyclerviewadapter 클래스
 * 경로별 전체 tbt정보를 보여줄때 사용되는 adapter 클래스
 * tbt명칭, 다음 tbt까지 거리, tbt타입에 해당하는 icon 표시
 */
class TbtRecyclerViewAdapter
        extends RecyclerView.Adapter<TbtRecyclerViewAdapter.LocationViewHolder> {

    /**
     * tbt 데이터 리스트
     */
    private List<TbtItem> tbtItemList = null;

    TbtRecyclerViewAdapter(List<TbtItem> tbtItemList) {
        this.tbtItemList = tbtItemList;
    }

    @Override
    public LocationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_tbt_row, parent, false);
        return new LocationViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(LocationViewHolder holder, int position) {
        TbtItem tbtItem = tbtItemList.get(position);
        //클릭 이벤트 발생시 해당 tbt에 대한 이벤트를 처리하기 위해 index를 저장ㄱ
        holder.setTurnIndex(position);
        //tbt명칭을 설정
        int resId = NaviUtils.getRgTypeString(tbtItem.getType());
        if (resId > 0) {
            holder.turnName.setText(resId);
        } else {
            holder.turnName.setText(tbtItem.getName());
        }

        //다음 tbt까지 남은거리 설정
        holder.remainDistance.setText(NaviUtils.convertDistanceUnit(tbtItem.getNextDistance()));

        //tbt타입에 해당하는 icon 설정
        resId = TurnResourceManager.getResourceId((short) tbtItem.getType());
        if (resId > 0) {
            holder.turnImage.setImageResource(resId);
        } else {
            holder.turnImage.setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        return tbtItemList.size();
    }


    class LocationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.turn_name_textview)
        TextView turnName;
        @BindView(R.id.remain_distance_textview)
        TextView remainDistance;
        @BindView(R.id.turn_icon_imageview)
        ImageView turnImage;

        private int turnIndex;

        LocationViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        void setTurnIndex(int turnIndex) {
            this.turnIndex = turnIndex;
        }

        @Override
        public void onClick(View v) {
            TbtItem tbtItem = tbtItemList.get(turnIndex);
            //해당 tbt 위치로 지도 이동
            MapController.getInstance()
                    .changeViewpoint(new UTMK(tbtItem.getX(), tbtItem.getY()), 0, 0,
                            NaviUtils.DRIVE_MODE_DEFAULT_ZOOM,
                            MapController.getInstance().calcPivot(0.5, 0.4),
                            MapUtils.MAP_ANIMATION_DURATION_IN_MILLISECOND_LOCATION_UPDATE,
                            GMap.AnimationTiming.LINEAR);
        }
    }
}
