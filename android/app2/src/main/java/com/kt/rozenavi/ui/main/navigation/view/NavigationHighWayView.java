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
import android.graphics.Rect;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;

import com.kt.roze.guidance.model.HighwayGuidance;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.navigation.view.adapter.NavigationHighwayRecyclerViewAdapter;
import com.kt.rozenavi.utils.CommonUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 고속도로 정보 표시 View
 */
public class NavigationHighWayView extends RelativeLayout {
    private List<HighwayGuidance> guidances;

    @BindView(R.id.highway_recyclerview)
    protected RecyclerView recyclerView;

    public NavigationHighWayView(Context context) {
        super(context);
        initView(context);
    }

    public NavigationHighWayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NavigationHighWayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_navigation_highway, this);
        ButterKnife.bind(this);

        int itemMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
        recyclerView.addItemDecoration(new ItemMargin(itemMargin));
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    /**
     * 고속도로 안내점 정보를 개별 View에 전달한다.
     *
     * @param guidances 고속도로 안내점 정보
     */
    public void setHighwayGuidances(List<HighwayGuidance> guidances) {
        this.guidances = guidances;

        if (CommonUtils.isEmpty(guidances)) {
            setVisibility(View.INVISIBLE);
            return;
        }

        recyclerView.setAdapter(new NavigationHighwayRecyclerViewAdapter(getContext(), guidances));
        setVisibility(View.VISIBLE);
    }

    /**
     * 현재 위치에서 각 고속도로 안내점까지의 거리를 표시</br>
     * 라이브러리에서는 현재 위치에서 첫번째 아이템까지의 거리만 전달한다.</br>
     * 첫번째 이후 안내점은 전달된 거리를 이용하여 변화량을 계산하여 App에서 계산하도록 한다.
     *
     * @param distance 거리(m)
     */
    public void updateDistance(int distance) {
        if (CommonUtils.isEmpty(guidances)) {
            return;
        }
        int remainDistance = distance - guidances.get(0).getDistance();
        NavigationHighwayRecyclerViewAdapter adapter = (NavigationHighwayRecyclerViewAdapter) recyclerView.getAdapter();
        adapter.setRemainDistance(remainDistance);
        adapter.notifyDataSetChanged();
    }

    private class ItemMargin extends RecyclerView.ItemDecoration {
        int margin = 0;

        ItemMargin(int marginByPx) {
            this.margin = marginByPx;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.set(0, 0, 0, margin);
        }
    }
}
