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

import static com.kt.rozenavi.utils.NaviUtils.convertArrivedTime;
import static com.kt.rozenavi.utils.NaviUtils.convertRemainTime;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.NaviUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 길찾기 시 남은 거리 및 남은 시간 표시
 * 클릭 시 남은 시간, 현재 시간이 번갈아 표시 된다.
 */
public class NavigationRemainView extends RelativeLayout {
    @BindView(R.id.remain_time_textview)
    protected TextView remainTimeTextView;

    @BindView(R.id.remain_distance_textview)
    protected TextView remainDistanceTextView;

    private boolean isShowRemainTime = false;
    private int remainTime;

    public NavigationRemainView(Context context) {
        super(context);
        initView(context);
    }

    public NavigationRemainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NavigationRemainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_navigation_remain, this);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.remain_data_layout)
    protected void onRemainTimeToggle() {
        isShowRemainTime = !isShowRemainTime;
        remainTimeTextView.setText(isShowRemainTime ? convertRemainTime(remainTime) : convertArrivedTime(remainTime));
    }

    /**
     * 남은 시간과 거리를 표시한다.
     *
     * @param timeInSecond    목적지까지 도착 예정 시간이 초 단위로 전달.
     * @param distanceInMeter 목적지 까지 남은 거리가 m 단위로 전달된다.
     * @see com.kt.roze.guidance.RouteGuidanceListener#onRemainChangedEvent(int, int)
     */
    public void updateRemain(int timeInSecond, int distanceInMeter) {
        remainTime = timeInSecond;
        remainTimeTextView.setText(
                isShowRemainTime ? convertRemainTime(timeInSecond) : convertArrivedTime(timeInSecond));
        remainDistanceTextView.setText(NaviUtils.convertDistanceUnit(distanceInMeter));
    }
}
