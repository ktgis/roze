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

package com.kt.rozenavi.ui.component.core;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.OnLifecycleEvent;

public class MutableLiveDataExt<T> extends MutableLiveData<T> {

    public void observeSpecific(LifecycleOwner owner, Observer<T> observer, Lifecycle.Event[] states) {
        observe(new SpecificLifecycleOwner(owner, states), observer);
    }

    class SpecificLifecycleOwner implements LifecycleOwner, LifecycleObserver {
        private LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);
        private int eventFlag = 0;

        SpecificLifecycleOwner(LifecycleOwner owner, Lifecycle.Event[] states) {
            owner.getLifecycle().addObserver(this);
            eventFlag = makeEventFlag(states);
        }

        @Override
        public Lifecycle getLifecycle() {
            return lifecycleRegistry;
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
        void onStateEvent(LifecycleOwner owner, Lifecycle.Event event) {
            if (!hasEvent(event)) {
                return;
            }
            
            lifecycleRegistry.handleLifecycleEvent(event);
        }

        private int makeEventFlag(Lifecycle.Event[] states) {
            int flag = 0;
            for (Lifecycle.Event event : states) {
                if (event == Lifecycle.Event.ON_ANY) {
                    flag = (int) Math.pow(2, event.ordinal()) - 1;
                    return flag;
                }
                flag += (int) Math.pow(2, event.ordinal());
            }
            return flag;
        }

        private boolean hasEvent(Lifecycle.Event event) {
            return (eventFlag & (int) Math.pow(2, event.ordinal())) > 0;
        }
    }
}
