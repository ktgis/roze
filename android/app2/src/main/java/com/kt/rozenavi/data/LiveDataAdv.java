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

package com.kt.rozenavi.data;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.Observer;

import com.kt.rozenavi.ui.component.core.MutableLiveDataExt;


public class LiveDataAdv<T> extends MutableLiveDataExt<T> {
    private final Lifecycle.Event[] OBSERVE_STATE = new Lifecycle.Event[]{
            Lifecycle.Event.ON_CREATE,
            Lifecycle.Event.ON_START,
            Lifecycle.Event.ON_RESUME,
            Lifecycle.Event.ON_DESTROY
    };

    public void observeAlways(LifecycleOwner owner, Observer<T> observer) {
        observeSpecific(owner, observer, OBSERVE_STATE);
    }
}
