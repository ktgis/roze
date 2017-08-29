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
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kt.roze.NavigationManager;
import com.kt.rozenavi.R;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 길 안내 모드 시 활성화 되는 View로 아래와 같은 기능을 갖는다.
 *  - 경로 저장 - 주행 중 GPS 저장 기능 제공.
 *  - 재탐색 - 수동 재탐색 기능 제공.
 *  - 경로 취소 - 주행 중인 경로를 취소 하고 Tracking Mode로 전환.
 */
public class NavigationMenuView extends RelativeLayout {

    private OnNavigationStopListener onNavigationStopListener;

    public NavigationMenuView(Context context) {
        super(context);
        initView(context);
    }

    public NavigationMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_navigation_menu, this);
        ButterKnife.bind(this);
    }

    public void setOnMenuClickListener(OnNavigationStopListener onNavigationStopListener) {
        this.onNavigationStopListener = onNavigationStopListener;
    }

    /**
     * 사용자 재탐색 기능을 실행한다.
     */
    @OnClick(R.id.reroute)
    protected void onRerouting() {
        NavigationManager navigationManager = NavigationManager.getInstance();
        if (navigationManager.getMode() != NavigationManager.Mode.REROUTING) {
            navigationManager.reroute(NavigationManager.RouteMode.USER_REROUTE);
            toggleMenu();
            Toast.makeText(getContext(), "경로를 재탐색 합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 주행중인 경로를 취소하고 App Mode 를 Tracking Mode로 전환한다.
     */
    @OnClick(R.id.cancel)
    protected void onStopRouting() {
        Toast.makeText(getContext(), "경로 안내를 종료합니다.", Toast.LENGTH_SHORT).show();

        try {
            NavigationManager.getInstance().stopNavigation(
                    NavigationManager.RouteFinishMode.USER_FINISH);
        } catch (Exception e) {
            e.printStackTrace();
        }
        toggleMenu();
        if (onNavigationStopListener != null) {
            onNavigationStopListener.onStopClick();
        }
    }

    /**
     * 메뉴 외 터치 이벤트 처리
     * 기능 버튼외 터치 이벤트 발생시 종료 처리
     */
    @OnClick(R.id.drive_menu_back)
    protected void onTouchBg() {
        toggleMenu();
    }

    /**
     * DriveMenuView 종료
     */
    public void toggleMenu() {
        if (isShown()) {
            setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_down));
            setVisibility(View.INVISIBLE);
        } else {
            setAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_up));
            setVisibility(View.VISIBLE);
        }
    }

    public interface OnNavigationStopListener {
        void onStopClick();
    }

}
