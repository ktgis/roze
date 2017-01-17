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
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.roze.data.model.Lane;
import com.kt.roze.resource.LaneResourceManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 차선 정보 표시 View
 */
public class LaneGuidanceView extends RelativeLayout {
    @BindView(R.id.lane_image_linearlayout)
    protected LinearLayout laneLinearLayout;
    @BindView(R.id.lane_remain_textview)
    protected TextView laneRemainTextView;

    /**
     * 차선 이미지 가로 크기정보
     */
    private int laneImageWidth;

    public LaneGuidanceView(Context context) {
        super(context);
        initView(context);
    }

    public LaneGuidanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LaneGuidanceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_guidance_lane, this);
        ButterKnife.bind(this);
        setVisibility(GONE);

        laneImageWidth = getResources().getDimensionPixelSize(R.dimen.lane_guidance_image_width);
    }

    /**
     * 차선 정보 표시
     * Lane으로 부터 방향 타입(좌회전, 우회전 등)과 확장타입(고가, 버스전용, 지하차로 진,출입 등)을 받아</br>
     * 각각의 차선 이미지 Resource Id를 {@link LaneResourceManager}에서 가져와 이미지 View에 추가한다.
     *
     * @param lane {@link Lane}
     */
    public boolean updateLanePannel(Lane lane) {
        byte[] turnType = lane.turnType;
        byte[] exType = lane.exType;
        List<Integer> resList = new ArrayList<>();

        int resId;
        if (turnType.length != exType.length) {
            return false;
        }
        int size = turnType.length;
        for (int i = 0; i < size; i++) {
            resId = getLaneResource(exType[i], turnType[i]);
            if (resId != LaneResourceManager.RESOURCE_NOT_FOUND) {
                resList.add(resId);
            }
        }
        if (resList.size() > 0) {
            addLaneImage(resList);
        }
        return true;
    }

    /**
     * 입력된 값에 따라 차선 이미지 ResId를 반환한다
     *
     * @param exType   확장타입(버스,고가 등)
     * @param turnType TBT Type(좌,우회전등)
     * @return Lane Image ResourceId
     */
    private int getLaneResource(byte exType, byte turnType) {
        int resId;
        boolean isHighlight = ((exType & Lane.LaneType.highlight) == Lane.LaneType.highlight);
        resId = LaneResourceManager.getExtraResourceId(exType, isHighlight);
        if (resId != LaneResourceManager.RESOURCE_NOT_FOUND) {
            return resId;
        } else {
            return LaneResourceManager.getLaneResourceId(turnType, isHighlight);
        }
    }

    /**
     * 차로에 속한 모든 차선 이미지를 View에 추가한다.
     *
     * @param resList 차선 이미지 리스트
     */
    private void addLaneImage(List<Integer> resList) {
        laneLinearLayout.removeAllViews();

        ImageView laneImage;
        for (int resId : resList) {
            laneLinearLayout.addView(getLaneSeparator());
            laneImage = new ImageView(laneLinearLayout.getContext());
            laneImage.setImageResource(resId);
            laneLinearLayout.addView(laneImage, laneImageWidth,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        }
        laneLinearLayout.addView(getLaneSeparator());
    }

    private ImageView getLaneSeparator() {
        ImageView laneSeparatorImage = new ImageView(laneLinearLayout.getContext());
        laneSeparatorImage.setImageResource(LaneResourceManager.getSeparatorId());
        return laneSeparatorImage;
    }

    /**
     * 현재위치로 부터 차선정보 해제지점까지의 거리를 표시
     *
     * @param distance 거리(m)
     */
    public void updateLaneDistance(int distance) {
        laneRemainTextView.setText(NaviUtils.convertDistanceUnit(distance));
    }
}
