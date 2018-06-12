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

package com.kt.rozenavi.data.model;

import com.kt.roze.guidance.model.TrackingGuidance;

import java.util.List;

/**
 * 1.2.0
 * 안전운행 데이터 Wrapper
 */
public class TrackingEventData {
    public final boolean isShow;
    public final List<TrackingGuidance> guidances;

    public TrackingEventData(boolean isShow, List<TrackingGuidance> guidances) {
        this.isShow = isShow;
        this.guidances = guidances;
    }
}
