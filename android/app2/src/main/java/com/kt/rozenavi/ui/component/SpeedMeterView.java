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
package com.kt.rozenavi.ui.component;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.kt.rozenavi.R;

public class SpeedMeterView extends FrameLayout {
    private int[] numberResourceArray = new int[]{
            R.drawable.img_hfigure0, R.drawable.img_hfigure1, R.drawable.img_hfigure2,
            R.drawable.img_hfigure3, R.drawable.img_hfigure4, R.drawable.img_hfigure5,
            R.drawable.img_hfigure6, R.drawable.img_hfigure7, R.drawable.img_hfigure8,
            R.drawable.img_hfigure9
    };
    protected LinearLayout speedValueLayout;

    public SpeedMeterView(@NonNull Context context) {
        super(context);
        init();
    }

    public SpeedMeterView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpeedMeterView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initView();
    }

    private void initView() {
        speedValueLayout = (LinearLayout) View.inflate(getContext(), R.layout.view_speed_meter, null);
        addView(speedValueLayout);

        setSpeed(0);
    }

    /**
     * 속도 값 설정
     * 화면에 속도정보를 표시하는 레이아웃 설정
     * 숫자로된 이미지 정보를 설정
     *
     * @param speed 속도값
     */
    public void setSpeed(int speed) {
        int resId;
        int number;
        ImageView numberImage;
        //1자리는 항상 표시되어야 하므로 속도가 0일때도 무조건 처리
        //10으로 나눈 나머지에 해당하는 이미지 입력
        resId = numberResourceArray[speed % 10];
        numberImage = (ImageView) speedValueLayout.getChildAt(speedValueLayout.getChildCount() - 1);
        numberImage.setImageResource(resId);

        //10의 자리 이후는 값이 없는경우는 빈칸처리
        for (int i = 1; i < speedValueLayout.getChildCount(); i++) {
            //10의 n승으로 자리수를 체크
            number = (int) (speed / Math.pow(10, i));
            //가장 큰 자리수가 0번째이므로 뒤에서 부터 순차적으로 입력
            numberImage = (ImageView) speedValueLayout.getChildAt((speedValueLayout.getChildCount() - 1) - i);
            //자리수의 값이 0이면 빈칸처리
            if (number == 0) {
                numberImage.setImageDrawable(null);
                numberImage.setVisibility(View.GONE);
            } else {
                //자리수의 값이 0이 아니면 10으로 나눈 나머지에 해당하는 이미지 입력
                numberImage.setVisibility(View.VISIBLE);
                numberImage.setImageResource(numberResourceArray[number % 10]);
            }
        }
    }
}
