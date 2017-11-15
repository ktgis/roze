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

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;


/**
 * ButterKnife 기본 적용 createView에서 생성된 View를 자동 bind
 */
public abstract class BaseFragment extends Fragment {
    public void onNewIntent(Intent intent) {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = createView(inflater, container, savedInstanceState);
        ButterKnife.bind(this, view);
        return view;
    }

    /**
     * fragment 에서는 backpressed 이벤트를 받을 수 없기 때문에
     * 처리가 필요할 경우 override하여 사용
     */
    public boolean canGoBack() {
        return true;
    }

    public Object createEnterTransition(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        return TransitionInflater
                .from(context)
                .inflateTransition(android.R.transition.explode);
    }

    public Object createExitTransition(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }
        return TransitionInflater
                .from(context)
                .inflateTransition(android.R.transition.explode);
    }

    /**
     * 초기화는 항상 onActivityCreated()에서 실행
     */
    abstract protected void init();

    /**
     * layout으로 사용할 View 생성 onCreateView()에서 실행
     */
    abstract protected View createView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState);

}
