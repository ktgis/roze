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

import com.kt.roze.guidance.model.SafetySpotGuidance;

import java.util.List;

/**
 * 안전운행 데이터 Wrapper
 */
public class SafetyEventData {
    public final boolean isShow;
    public final List<SafetySpotGuidance> safetyGuidanceList;

    public SafetyEventData(boolean isShow, List<SafetySpotGuidance> safetyGuidanceList) {
        this.isShow = isShow;
        this.safetyGuidanceList = safetyGuidanceList;
    }
}
