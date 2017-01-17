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

package com.kt.rozenavi.ui.main.navigation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kt.roze.NavigationManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.UIController;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 길 안내 모드 시 활성화 되는 View로 아래와 같은 기능을 갖는다.
 * 1. 경로 저장 - 주행 중 GPS 저장 기능 제공.
 * 2. 재탐색 - 수동 재탐색 기능 제공.
 * 3. 경로 취소 - 주행 중인 경로를 취소 하고 Tracking Mode로 전환.
 */
public class DriveMenuView extends RelativeLayout {
    public DriveMenuView(Context context) {
        super(context);
        initView(context);
    }

    public DriveMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_drive_menu, this);
        ButterKnife.bind(this);
    }

    /**
     * GPS를 저장한다. GPS 저장 시 저장 시점까지 주행한 GPS는 local storage에 file로 남기고
     * 현재까지의 GPS정보를 버퍼에서 clear 한다.
     * <p>
     * local file 위치 : Environment.getExternalStorageDirectory()/telos/
     */
    @OnClick(R.id.btn_save_gps)
    protected void onSaveGps() {
        String msg = NavigationManager.getInstance().saveGpsInfo() ? "GPS 정보를 저장하였습니다." :
                     "GPS 정보 저장에 실패하였습니다.";
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        closeView();
    }

    /**
     * 사용자 재탐색 기능을 실행한다.
     */
    @OnClick(R.id.btn_rerouting)
    protected void onRerouting() {
        if (NavigationManager.getInstance().getMode() == NavigationManager.Mode.NAVIGATING) {
            NavigationManager.getInstance().reroute(NavigationManager.RouteMode.USER_REROUTE);
            closeView();
        }
    }

    /**
     * 주행중인 경로를 취소하고 App Mode 를 Tracking Mode로 전환한다.
     */
    @OnClick(R.id.btn_stop_routing)
    protected void onStopRouting() {
        Toast.makeText(getContext(), "경로 안내를 종료합니다.", Toast.LENGTH_SHORT).show();

        try {
            NavigationManager.getInstance().startTracking();
        } catch (Exception e) {
            e.printStackTrace();
        }
        closeView();
        UIController.getInstance().setMode(UIController.MODE_DRIVE);
    }

    /**
     * 메뉴 외 터치 이벤트 처리
     * 기능 버튼외 터치 이벤트 발생시 종료 처리
     */
    @OnClick(R.id.drive_menu_back)
    protected void onTouchBg() {
        closeView();
    }

    /**
     * DriveMenuView 종료
     */
    private void closeView() {
        UIController.getInstance().getNavigationView().toggleDriveMenu();
    }

}
