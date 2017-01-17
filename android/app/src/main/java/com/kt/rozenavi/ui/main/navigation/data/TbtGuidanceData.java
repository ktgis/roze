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

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.android.databinding.library.baseAdapters.BR;

/**
 * TBT 정보 데이터 클래스
 * Data Binding Observable 구현을 통해 데이터 변경시 자동으로 UI객체에 입력
 */
public class TbtGuidanceData extends BaseObservable {
    /**
     * first tbt 이미지 리소스 id
     */
    private int firstTurnResourceId = com.kt.roze.R.drawable.tbt_straight;
    /**
     * second tbt 이미지 리소스 id
     */
    private int secondTurnResourceId = com.kt.roze.R.drawable.tbt_straight;
    /**
     * first tbt 남은 거리(m)
     */
    private int firstTurnDistance = 0;
    /**
     * second tbt 남은 거리(m)
     */
    private int secondTurnDistance = 0;
    /**
     * 방면정보 텍스트
     */
    private String directionText;

    @Bindable
    public int getFirstTurnResourceId() {
        return firstTurnResourceId;
    }

    public void setFirstTurnResourceId(int firstTurnResourceId) {
        this.firstTurnResourceId = firstTurnResourceId;
        notifyPropertyChanged(BR.firstTurnResourceId);
    }

    @Bindable
    public int getSecondTurnResourceId() {
        return secondTurnResourceId;
    }

    public void setSecondTurnResourceId(int secondTurnResourceId) {
        this.secondTurnResourceId = secondTurnResourceId;
        notifyPropertyChanged(BR.secondTurnResourceId);
    }

    @Bindable
    public int getFirstTurnDistance() {
        return firstTurnDistance;
    }

    public void setFirstTurnDistance(int firstTurnDistance) {
        this.firstTurnDistance = firstTurnDistance;
        notifyPropertyChanged(BR.firstTurnDistance);
    }

    @Bindable
    public int getSecondTurnDistance() {
        return secondTurnDistance;
    }

    public void setSecondTurnDistance(int secondTurnDistance) {
        this.secondTurnDistance = secondTurnDistance;
        notifyPropertyChanged(BR.secondTurnDistance);
    }

    @Bindable
    public String getDirectionText() {
        return directionText;
    }

    public void setDirectionText(String directionText) {
        this.directionText = directionText;
        notifyPropertyChanged(BR.directionText);
    }
}
