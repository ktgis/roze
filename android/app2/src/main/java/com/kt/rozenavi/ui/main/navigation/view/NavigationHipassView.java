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

package com.kt.rozenavi.ui.main.navigation.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.roze.guidance.RGType;
import com.kt.roze.guidance.model.TurnGuidance;
import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.CommonUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 하이패스 정보 표시 View
 */
public class NavigationHipassView extends RelativeLayout {
    /**
     * 하이패스 차로 정보 View
     */
    @BindView(R.id.hipass_lane_layout)
    protected LinearLayout hipassView;

    private static final int SHOW_DISTANCE = 600;
    private static final String LANE_SEPARATOR = "...";
    private static final int INIT_LANE = -1;
    /**
     * Hipass 차선 정보 제공
     */
    private byte[] hipassLanes;

    private LinearLayout.LayoutParams params =
            new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

    public NavigationHipassView(Context context) {
        super(context);
        initView(context);
    }

    public NavigationHipassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NavigationHipassView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_navigation_hipass, this);
        ButterKnife.bind(this);

        int laneMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());

        params.leftMargin = laneMargin;
        params.rightMargin = laneMargin;

    }

    /**
     * Hipass 정보를 전달한다. 현재 Guidance 가 TG가 아닐 경우 view 를 hide 처리 한다.
     *
     * @param guidance 고속도로 안내점 정보
     */
    public void setTurnGuidance(List<TurnGuidance> guidance) {
        hideHipassView();

        if (CommonUtils.isEmpty(guidance)) {
            return;
        }

        TurnGuidance firstGuidance = guidance.get(0);
        if (firstGuidance.turnCode == RGType.TG) {
            this.hipassLanes = firstGuidance.getHipass();
            updateHipassView(firstGuidance.nextDistance);
        }
    }

    /**
     * 요금소에 <code>ShOW_DISTANCE</code> 만큼 근접하면 하이패스 정보를 표출한다
     *
     * @param distance 요금소와의 거리(m)
     */
    public void updateHipassView(int distance) {
        if (checkHipassView(distance)) {
            updateHipassLanes();
        }
    }

    private boolean checkHipassView(int distance) {
        //표출된 하이패스 정보가 있는 경우 중복으로 표출하지 않는다
        if (hipassView.isShown()) {
            return false;
        }

        //첫 고속도로 안내점이 요금소 && 표출 거리 이내일 경우에 하이패스 정보를 표출한다
        if (CommonUtils.isEmpty(hipassLanes) || distance >= SHOW_DISTANCE) {
            return false;
        }
        setVisibility(View.VISIBLE);
        return true;
    }

    /**
     * Hipass View 숨김
     */
    private void hideHipassView() {
        this.hipassLanes = null;
        setVisibility(View.GONE);
    }

    /**
     * 각 차선 번호를 TextView 에 Setting 하여 반환한다
     * <UI Logic>
     */
    @SuppressWarnings("deprecation")
    private TextView getHipassItemView(Context context, int number) {
        TextView laneText = new TextView(context);
        if (number >= 0) {
            laneText.setText(String.format("%d", number));
        } else {
            laneText.setText(LANE_SEPARATOR);
        }
        laneText.setTextColor(getResources().getColor(R.color.sea_blue));
        laneText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        return laneText;
    }

    /**
     * Hipass 차로 정보를 View 에 Add 한다.
     * <UI Logic>
     */
    private void updateHipassLanes() {
        byte[] lanes = this.hipassLanes;
        if (CommonUtils.isEmpty(lanes)) {
            hideHipassView();
            return;
        }

        hipassView.removeAllViews();
        Context ctx = getContext();
        int prevLane = INIT_LANE;
        for (int laneNumber : lanes) {
            if (prevLane != INIT_LANE && Math.abs(prevLane - laneNumber) > 1) {
                hipassView.addView(getHipassItemView(ctx, INIT_LANE), params);
            }
            hipassView.addView(getHipassItemView(ctx, laneNumber + 1), params);
            prevLane = laneNumber;
        }
    }
}
