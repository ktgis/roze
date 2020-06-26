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

package com.kt.rozenavi.utils;

import androidx.databinding.BindingAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomBindingAdapter {

    @BindingAdapter({"imageResource"})
    public static void setImageResource(ImageView imageView, int resourceId) {
        imageView.setImageResource(resourceId);
    }

    @BindingAdapter({"spannableDistance"})
    public static void setSpannableDistance(TextView textView, int distance) {
        NaviUtils.setSizeSpanDistance(textView, distance);
    }
}
