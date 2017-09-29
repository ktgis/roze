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

package com.kt.rozenavi.ui.main.navigation.data.model;

public class RemainEventData {
    public final int timeInSecond;
    public final int distanceInMeter;

    public RemainEventData(int timeInSecond, int distanceInMeter) {
        this.timeInSecond = timeInSecond;
        this.distanceInMeter = distanceInMeter;
    }
}
