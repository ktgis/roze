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

package com.kt.rozenavi.ui.main.navigation.data;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

/**
 * 길안내시 남은거리, 남은시간/도착시간 정보 데이터 클래스
 * Data Binding Observable 구현을 통해 데이터 변경시 자동으로 UI객체에 입력
 */
public class RemainGuidanceData extends BaseObservable {
    /**
     * 마지막으로 업데이트된 남은거리
     */
    private int lastRemainDistance;
    /**
     * 마지막으로 업데이트 된 남은 시간
     */
    private int lastRemainTime;
    /**
     * 도착시간/남은시간 변환 flag
     */
    private boolean isDisplayRemainTime = false;

    @Bindable
    public boolean isDisplayRemainTime() {
        return isDisplayRemainTime;
    }

    public void setDisplayRemainTime(boolean displayRemainTime) {
        isDisplayRemainTime = displayRemainTime;
        notifyPropertyChanged(BR.displayRemainTime);
    }

    @Bindable
    public int getLastRemainDistance() {
        return lastRemainDistance;
    }

    public void setLastRemainDistance(int lastRemainDistance) {
        this.lastRemainDistance = lastRemainDistance;
        notifyPropertyChanged(BR.lastRemainDistance);
    }

    @Bindable
    public int getLastRemainTime() {
        return lastRemainTime;
    }

    public void setLastRemainTime(int lastRemainTime) {
        this.lastRemainTime = lastRemainTime;
        notifyPropertyChanged(BR.lastRemainTime);
    }
}
