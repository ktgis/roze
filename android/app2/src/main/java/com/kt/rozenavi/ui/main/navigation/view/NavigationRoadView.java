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

package com.kt.rozenavi.ui.main.navigation.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.kt.rozenavi.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * 경로 로드뷰(정션뷰) 표시 View
 */
public class NavigationRoadView extends RelativeLayout {
    @BindView(R.id.roadview_imageview)
    protected ImageView roadview;

    int radius;

    public NavigationRoadView(Context context) {
        super(context);
        initView(context);
    }

    public NavigationRoadView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_navigation_roadview, this);
        ButterKnife.bind(this);

        radius = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 5, getContext().getResources().getDisplayMetrics());
    }

    @OnClick(R.id.close_roadview)
    protected void onClickClose() {
        toggleView();
    }

    /**
     * DriveMenuView 종료
     */
    public void toggleView() {
        if (isShown()) {
            setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
            setVisibility(View.INVISIBLE);
        } else {
            setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
            setVisibility(View.VISIBLE);
        }
    }

    public void updateRoadView(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            roadview.setImageBitmap(null);
        } else {
            Glide.with(getContext())
                    .load(imagePath)
                    .bitmapTransform(new RoundedCornersTransformation(getContext(), radius, 0,
                            RoundedCornersTransformation.CornerType.ALL)).into(roadview);
        }
        toggleView();
    }
}
