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

import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LifecycleRegistryOwner;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.transition.TransitionInflater;

import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.MainActivity;

import butterknife.ButterKnife;

/**
 * 기본 Activity 의 용도, 모든 Activity 선언 시 사용할 예정
 * Activity 기능 확장에 필요한 부분을 제공
 */
public abstract class BaseActivity extends AppCompatActivity implements LifecycleRegistryOwner {
    LifecycleRegistry lifecycleRegistry = new LifecycleRegistry(this);

    @Override
    public LifecycleRegistry getLifecycle() {
        return lifecycleRegistry;
    }

    /**
     * setContentView
     *
     * @param layout_id      Layout ID
     * @param useViewMapping ButterKnife 사용 여부
     */
    public void setContentView(int layout_id, boolean useViewMapping) {
        if (useViewMapping) {
            setContentView(layout_id);
            ButterKnife.bind(this);
        } else {
            setContentView(layout_id);
        }
    }

    /**
     * replace fragment
     * layout에서 R.id.base_fragment_container로 지정되어있어야 함
     * ids.xml에 정의되어 있기 때문에 android:id="@id/base_fragment_container"로 지정만 하면 됨
     */
    public void replaceFragment(@NonNull Fragment screen) {
        Fragment current = getSupportFragmentManager().findFragmentById(
                        R.id.base_fragment_container);

        if (current != null && current instanceof BaseFragment) {
            Object transition = ((BaseFragment)current).createExitTransition(this);
            if (transition != null) {
                current.setExitTransition(transition);
            }
        }

        if (screen instanceof BaseFragment) {
            Object transition =  ((BaseFragment)screen).createEnterTransition(this);
            if (transition != null) {
                screen.setEnterTransition(transition);
            }
        }

        replaceFragment(R.id.base_fragment_container, screen);
    }

    /**
     * replace fragment
     *
     * @param resId  container id
     * @param screen fragment
     */
    public void replaceFragment(@IdRes int resId, @NonNull Fragment screen) {
        getSupportFragmentManager().beginTransaction().replace(resId, screen).commit();
    }

    /**
     * 초기화는 항상 동일한 위치에서 시작합니다.
     */
    abstract protected void init();
}
