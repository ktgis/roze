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

import com.kt.roze.guidance.model.OilPriceGuidance;

import java.util.List;

public class LowestGasEventData {
    public final boolean isShow;
    public final List<OilPriceGuidance> oilPriceList;

    public LowestGasEventData(boolean isShow, List<OilPriceGuidance> oilPriceList) {
        this.isShow = isShow;
        this.oilPriceList = oilPriceList;
    }
}
