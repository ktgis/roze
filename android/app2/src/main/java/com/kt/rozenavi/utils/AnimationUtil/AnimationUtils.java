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

package com.kt.rozenavi.utils.AnimationUtil;

import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.widget.ImageView;

/**
 * Animation 관련 중복 Code 제거를 위한 Util
 */
public class AnimationUtils {
    /**
     * Animation Drawable 시작
     * 1차. src에서 drawable 추출
     * 2차. drawable == null background에서 drawable 추출
     *
     * @param view 대상 ImageView
     */
    public static void startAnimationDrawable(@NonNull ImageView view) {
        AnimationDrawable drawable = (AnimationDrawable) view.getDrawable();
        if (drawable == null) {
            drawable = (AnimationDrawable) view.getBackground();
        }
        if (drawable != null && drawable.isRunning()) {
            drawable.stop();
        }
        drawable.start();
    }

    /**
     * Animation Drawable 중지
     * 1차. src에서 drawable 추출
     * 2차. drawable == null background에서 drawable 추출
     *
     * @param view 대상 ImageView
     */
    public static void stopAnimationDrawable(@NonNull ImageView view) {
        AnimationDrawable drawable = (AnimationDrawable) view.getDrawable();
        if (drawable == null) {
            drawable = (AnimationDrawable) view.getBackground();
        }

        if (drawable != null && drawable.isRunning()) {
            drawable.stop();
        }
    }
}
