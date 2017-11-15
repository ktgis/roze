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
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
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

    /**
     * 이미지 뷰 모서리 radius
     */
    private int radius;
    /**
     * glide 이미지 다운로드 futuretarget
     */
    private FutureTarget futureTarget = null;

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
        closeView();
    }

    public void closeView() {
        setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out));
        setVisibility(View.INVISIBLE);
    }

    public void showView() {
        setAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
        setVisibility(View.VISIBLE);
    }

    /**
     * image path 정보에 따라 로드뷰 표시
     */
    public void updateRoadView(String imagePath) {
        if (TextUtils.isEmpty(imagePath)) {
            if (futureTarget != null) {
                futureTarget.cancel(true);
                futureTarget = null;
            }
            roadview.setImageBitmap(null);
            closeView();
        } else {
            futureTarget = Glide.with(getContext())
                    .load(imagePath)
                    .apply(RequestOptions.bitmapTransform(
                            new RoundedCornersTransformation(radius, 0, RoundedCornersTransformation.CornerType.ALL)))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target,
                                boolean isFirstResource) {
                            futureTarget = null;
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                DataSource dataSource, boolean isFirstResource) {
                            roadview.setImageDrawable(resource);
                            futureTarget = null;
                            showView();
                            return false;
                        }
                    }).submit();
        }
    }
}
