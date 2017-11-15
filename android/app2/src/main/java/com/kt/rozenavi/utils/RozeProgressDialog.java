/*
 *  Copyright (c) 2016 kt corp. All rights reserved.
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
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.kt.rozenavi.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * progress dialog
 * 기능 동작중 progress중임을 표시하는 dialog
 */
class RozeProgressDialog extends Dialog {
    @BindView(R.id.progress_icon_imageview)
    ImageView iconImageView;

    RozeProgressDialog(Context context) {
        super(context, R.style.ProgressDialog);
        init();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //가운데 이미지 뷰를 애니메이션 리소스를 이용해 회전동작을 시킴
        iconImageView.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.progress_animation));
    }

    private void init() {
        setContentView(R.layout.dialog_progress);
        ButterKnife.bind(this);
    }
}
