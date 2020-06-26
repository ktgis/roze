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


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.widget.ImageView;

import com.kt.rozenavi.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ProgressDialog_CircleCar extends Dialog {
    @BindView(R.id.animationView)
    ImageView animationView;

    ProgressDialog_CircleCar(Context context) {
        super(context, R.style.ProgressDialog);
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startAnimation(animationView);
    }

    private void init() {
        setContentView(R.layout.dialog_progress_circle_car);
        ButterKnife.bind(this);
    }

    private void startAnimation(@NonNull ImageView view) {
        AnimationDrawable drawable = (AnimationDrawable) view.getDrawable();
        if (drawable.isRunning()) {
            drawable.stop();
        }
        drawable.start();
    }
}
