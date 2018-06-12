/*
 *  Copyright (c) 2018 kt corp. All rights reserved.
 *
 *  This is a proprietary software of kt corp, and you may not use this file
 *  except in compliance with license agreement with kt corp. Any redistribution
 *  or use of this software, with or without modification shall be strictly
 *  prohibited without prior written approval of kt corp, and the copyright
 *   notice above does not evidence any actual or intended publication of such
 *  software.
 *
 */

package com.kt.rozenavi.ui.main.drive.view;

import android.content.Context;
import android.util.AttributeSet;

import com.kt.roze.guidance.model.SafetySpotInterface;
import com.kt.rozenavi.ui.main.navigation.view.NavigationSpotView;

import java.util.List;

/**
 * 1.2.0
 * 안전운행 관련 Route Guidance 표시 View
 */
public class NavigationTrackingView extends NavigationSpotView {
    /**
     * 안전운행 정보가 표시 될지 여부
     */
    private boolean isSpotViewEnabled = true;

    public NavigationTrackingView(Context context) {
        super(context);
    }

    public NavigationTrackingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NavigationTrackingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void updateSafetySpotView(boolean isShow, List<SafetySpotInterface> list) {
        if (isShow && isSpotViewEnabled) {
            showSafetySpot(list);
        } else {
            hideSafetySpot(list);
        }
    }
    /**
     * 안전운행 정보 표시 기능 활성화 여부 설정
     *
     * @param isEnabled 활성화 여부
     */
    public void setSpotViewEnabled(boolean isEnabled) {
        isSpotViewEnabled = isEnabled;
        if (!isSpotViewEnabled) {
            hideSafetySpot(null);
        }
    }
}
