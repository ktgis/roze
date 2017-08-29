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

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * 이미지 회전 Animation Class
 */
public class CirclePathAnimation extends Animation {

    private View view;
    private float cx, cy;           // center x,y position of circular path
    private float prevX, prevY;     // previous x,y position of image during animation
    private float r;                // radius of circle
    private float prevDx, prevDy;
    private float transImageDegree;
    private boolean isLeftRotate;


    /**
     * @param view             회전 대상 View(아마도 ImageView)
     * @param r                이미지 회전 반경
     * @param transImageDegree 회전하는 이미지의 초기 각도
     * @param isLeftRotate     좌/우 회전 결정. true 일 때 왼쪽으로 회전.
     */
    public CirclePathAnimation(View view, float r, float transImageDegree, boolean isLeftRotate) {
        this.view = view;
        this.r = r;
        this.transImageDegree = transImageDegree;
        this.isLeftRotate = isLeftRotate;
    }

    @Override
    public boolean willChangeBounds() {
        return true;
    }

    @Override
    public void initialize(int width, int height, int parentWidth, int parentHeight) {
        // calculate position of image center
        int cxImage = width / 2;
        int cyImage = height / 2;
        cx = view.getLeft() + cxImage;
        cy = view.getTop() + cyImage;
        // set previous position to center
        prevX = cx;
        prevY = cy;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        if (interpolatedTime == 0) {
            t.getMatrix().setTranslate(prevDx, prevDy);
            return;
        }
        view.setVisibility(View.VISIBLE);
        float angleDegDefault = (interpolatedTime * 360f + 90) % 360;
        float angleDeg = isLeftRotate ? 180 - angleDegDefault : angleDegDefault;
        float angleRad = (float) Math.toRadians(angleDeg);
        float x = (float) (cx + r * Math.cos(angleRad));
        float y = (float) (cy + r * Math.sin(angleRad));


        float dx = prevX - x;
        float dy = prevY - y;

        prevX = x;
        prevY = y;

        prevDx = dx;
        prevDy = dy;

        t.getMatrix().setTranslate(dx, dy);
        view.setRotation(transImageDegree + angleDeg);
    }
}
