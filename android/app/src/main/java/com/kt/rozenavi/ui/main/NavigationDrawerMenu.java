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

package com.kt.rozenavi.ui.main;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.setting.SettingCarActivity;
import com.kt.rozenavi.ui.setting.SettingRouteActivity;
import com.kt.rozenavi.ui.setting.SettingSoundActivity;
import com.kt.rozenavi.utils.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Navigation Drawer 메뉴 클래스
 * 설정메뉴이동 담당
 */
public class NavigationDrawerMenu extends RelativeLayout {
    /**
     * 버전정보 표시 텍스트뷰
     */
    @BindView(R.id.sdk_version_textview)
    protected TextView versionTextView;

    public NavigationDrawerMenu(Context context) {
        super(context);
        initView(context);
    }

    public NavigationDrawerMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NavigationDrawerMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.layout_left_drawer, this);
        ButterKnife.bind(this);

        versionTextView.setText(UIUtils.getVersionString());
    }

    /**
     * 차량 타입 설정화면으로 이동
     */
    @OnClick(R.id.setting_car_textview)
    protected void startSettingCarActivity() {
        getContext().startActivity(SettingCarActivity.newIntent(getContext()));
    }

    /**
     * 경로 타입 설정화면으로 이동
     */
    @OnClick(R.id.setting_route_textview)
    protected void startSettingRouteActivity() {
        getContext().startActivity(SettingRouteActivity.newIntent(getContext()));
    }

    /**
     * 음성안내 설정화면으로 이동
     */
    @OnClick(R.id.setting_sound_textview)
    protected void startSettingSoundActivity() {
        getContext().startActivity(SettingSoundActivity.newIntent(getContext()));
    }
}
