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
import android.widget.Space;
import android.widget.TextView;

import com.kt.roze.guidance.model.HighwayGuidance;
import com.kt.roze.guidance.model.TGGuidance;
import com.kt.rozenavi.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 하이패스 정보 표시 View
 */
public class NavigationHipassView extends RelativeLayout {
    /**
     * 하이패스 차로 정보
     */
    @BindView(R.id.hipass_lane_layout)
    protected LinearLayout hipass_lane_inform;

    private int laneMargin;
    private List<HighwayGuidance> guidances;

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

        laneMargin = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 3, getResources().getDisplayMetrics());
    }

    /**
     * 고속도로 안내점 정보를 개별 View에 전달한다.
     *
     * @param guidances 고속도로 안내점 정보
     */
    public void setHighwayGuidances(List<HighwayGuidance> guidances) {
        this.guidances = guidances;

        if (guidances == null) {
            setVisibility(View.INVISIBLE);
            return;
        }
    }

    private TextView getHipassItemView(Context context, int number) {
        TextView laneText = new TextView(context);
        if (number >= 0) {
            laneText.setText(String.format("%d", number));
        } else {
            laneText.setText("...");
        }
        laneText.setTextColor(getResources().getColor(R.color.sea_blue));
        laneText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        return laneText;
    }

    /**
     * HIPASS 차로 정보를 View에 set한다.
     *
     * @param tg 요금소 정보
     */
    private void updateHipassLanes(TGGuidance tg) {
        if (tg == null) {
            setVisibility(View.INVISIBLE);
            return;
        }

        int laneCount = tg.getLaneCount();
        byte[] lanes = tg.getHighpassLanes();

        if (lanes.length == 0) {
            setVisibility(View.INVISIBLE);
            return;
        }

        boolean ret = true;
        boolean lastRet = true;
        int size = lanes.length;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.leftMargin = laneMargin;
        params.rightMargin = laneMargin;

        hipass_lane_inform.removeAllViews();

        for (int i = lanes[0]; i < laneCount; i++) {
            for (byte b : lanes) {
                if (b == i) {
                    ret = true;
                    break;
                }
                ret = false;
            }

            if (ret && lastRet) {
                hipass_lane_inform.addView(getHipassItemView(getContext(), i + 1), params);
            } else if (ret && !lastRet) {
                hipass_lane_inform.addView(getHipassItemView(getContext(), -1), params);
                hipass_lane_inform.addView(getHipassItemView(getContext(), i + 1), params);
            }

            if (lanes[size - 1] == i) {
                break;
            }
            lastRet = ret;
        }
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
        if (guidances == null || guidances.isEmpty()) {
            return;
        }

        if (guidances != null && guidances.size() > 0 && distance < 600) {
            HighwayGuidance firstGuidance = guidances.get(0);
            if (firstGuidance.getType() == HighwayGuidance.Type.TG) {
                TGGuidance tg = (TGGuidance) firstGuidance;
                updateHipassLanes(tg);
            } else {
                updateHipassLanes(null);
            }
        } else {
            updateHipassLanes(null);
        }
    }
}
