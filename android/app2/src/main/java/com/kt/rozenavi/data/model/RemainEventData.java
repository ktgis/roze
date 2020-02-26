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

package com.kt.rozenavi.data.model;

public class RemainEventData {
    public final int timeInSecond;
    public final int distanceInMeter;
    //since 1.7.0 현재 위치의 link index 정보 추가
    public final int currentLinkIndex;

    public RemainEventData(int timeInSecond, int distanceInMeter, int currentLinkIndex) {
        this.currentLinkIndex = currentLinkIndex;
        this.timeInSecond = timeInSecond;
        this.distanceInMeter = distanceInMeter;
    }
}