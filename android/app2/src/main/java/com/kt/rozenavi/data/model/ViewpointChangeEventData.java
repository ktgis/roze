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

import com.kt.maps.GMap;
import com.kt.maps.model.Viewpoint;

public class ViewpointChangeEventData {
    public GMap gMap;
    public Viewpoint viewpoint;
    public boolean b;

    public ViewpointChangeEventData(GMap gMap, Viewpoint viewpoint, boolean b) {
        this.gMap = gMap;
        this.viewpoint = viewpoint;
        this.b = b;
    }
}
