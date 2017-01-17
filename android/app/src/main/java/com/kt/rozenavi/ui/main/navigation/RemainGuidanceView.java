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

package com.kt.rozenavi.ui.main.navigation;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.databinding.library.baseAdapters.BR;

import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.navigation.data.RemainGuidanceData;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 길찾기 시 남은 거리 및 남은 시간 표시
 * 클릭 시 남은 시간, 현재 시간이 번갈아 표시 된다.
 */
public class RemainGuidanceView extends RelativeLayout {
    /**
     * Remain 정보 observable 데이터 객체
     */
    private RemainGuidanceData remainGuidanceData = new RemainGuidanceData();
    public RemainGuidanceView(Context context) {
        super(context);
        initView(context);
    }

    public RemainGuidanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RemainGuidanceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_guidance_remain, this);
        ButterKnife.bind(this);
        DataBindingUtil.bind(getChildAt(0)).setVariable(BR.remainGuidanceData, remainGuidanceData);
    }

    @OnClick(R.id.remain_data_layout)
    protected void onRemainTimeToggle() {
        remainGuidanceData.setDisplayRemainTime(!remainGuidanceData.isDisplayRemainTime());
    }

    /**
     * 남은 시간과 거리를 표시한다.
     *
     * @param timeInSecond    목적지까지 도착 예정 시간이 초 단위로 전달.
     * @param distanceInMeter 목적지 까지 남은 거리가 m 단위로 전달된다.
     * @see com.kt.roze.guidance.RouteGuidanceListener#onRemainChangedEvent(int, int)
     */
    public void updateRemain(int timeInSecond, int distanceInMeter) {
        remainGuidanceData.setLastRemainTime(timeInSecond);
        remainGuidanceData.setLastRemainDistance(distanceInMeter);
    }
}
