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

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.kt.roze.NavigationManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.drive.DriveView;
import com.kt.rozenavi.ui.main.navigation.NavigationView;
import com.kt.rozenavi.ui.main.route.RouteView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * UI 제어 클래스
 * 싱글턴 방식으로 구성되어 있고 메인 액티비티에 포함된 안전운행 뷰, 경로검색 뷰
 * 경로안내 뷰를 제어할 수 있도록 인스턴스 접근 기능을 제어하며
 * 각각의 모드를 설정할 수 있도록 하여 쉽게 뷰를 전환할 수 있는 기능을 제공
 */
public class UIController {
    /**
     * 안전 운행 모드
     */
    public static int MODE_DRIVE = 0;
    /**
     * 경로 검색 모드
     */
    public static int MODE_ROUTE = 1;
    /**
     * 경로 안내 모드
     */
    public static int MODE_NAVIGATION = 2;

    @BindView(R.id.content_frame)
    FrameLayout contentFrameLayout;

    /**
     * 안전 운행 모드 뷰 객체
     */
    @BindView(R.id.drive_view)
    DriveView driveView;
    /**
     * 경로 검색 모드 뷰 객체
     */
//    @BindView(R.id.route_view)
    private RouteView routeView;
    /**
     * 경로 안내 모드 뷰 객체
     */
//    @BindView(R.id.navigation_view)
    private NavigationView navigationView;
    /**
     * 검색 툴바 객체
     */
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;

    private static UIController instance;
    public MainActivity mainActivity;

    /**
     * 현재 모드 정보
     * 기본 시작은 안전운행 모드로 시작
     */
    private int currentMode = MODE_DRIVE;

    private UIController() {
    }

    public static UIController getInstance() {
        if (instance == null) {
            instance = new UIController();
        }

        return instance;
    }

    /**
     * UI 객체 할당
     *
     * @param activity activity
     */
    void bindMainActivity(MainActivity activity) {
        this.mainActivity = activity;
        ButterKnife.bind(this, activity);
    }

    public DriveView getDriveView() {
        return driveView;
    }

    public RouteView getRouteView() {
        if (routeView == null) {
            routeView = new RouteView(contentFrameLayout.getContext());
            contentFrameLayout.addView(routeView);
        }
        return routeView;
    }

    public NavigationView getNavigationView() {
        if (navigationView == null) {
            navigationView = new NavigationView(contentFrameLayout.getContext());
            contentFrameLayout.addView(navigationView);
        }
        return navigationView;
    }

    /**
     * UIController 객체 종료
     * 각 객체 초기화
     */
    void destroy() {
        instance = null;
        toolbar = null;
        mainActivity = null;

        //안전운행 뷰 종료
        if (driveView != null) {
            driveView.destroy();
            driveView = null;
        }
        //경로검색 뷰 종료
        if (routeView != null) {
            routeView.destroy();
            routeView = null;
        }
        //경로안내 뷰 종료
        if (navigationView != null) {
            navigationView.destroy();
            navigationView = null;
        }
    }

    /**
     * UI Mode에 따른 뒤로가기버튼 기능 처리
     * 경로 검색 화면일때는 안전운행 모드로 전환
     * 경로 안내 화면일때는 종료확인 팝업 실행
     * 안전운행 모드일때는 기본 뒤로가기 기능 실행
     * 반환값이 true일때 내부 동작 false일때 내부 동작 없음
     *
     * @return 기능 동작 여부
     */
    boolean onBackPressed() {
        //경로 요약화면이나 경로안내중에는 해당 종료 기능을 먼저
        if (currentMode == MODE_ROUTE) {
            setMode(MODE_DRIVE);
            return true;
        } else if (currentMode == MODE_NAVIGATION) {
            showNavigationModeExitAlertDialog();
            return true;
        }
        return false;
    }

    /**
     * 경로안내 종료 팝업
     * 경로안내 종료, 앱종료 선택
     */
    private void showNavigationModeExitAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        builder.setTitle("경로 안내 종료");
        builder.setMessage("경로 안내를 종료합니다.");
        builder.setPositiveButton("경로안내 종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(
                    DialogInterface dialog, int id) {
                try {
                    NavigationManager.getInstance().startTracking();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                UIController.getInstance().driveView.carMarkerChange();
                setMode(MODE_DRIVE);
            }
        });
        builder.setNegativeButton("앱 종료", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(
                    DialogInterface dialog, int id) {
                mainActivity.finish();
            }
        });
        builder.show();
    }


    /**
     * UI 모드 설정
     * 안전운행모드, 경로검색모드, 경로안내모드를 상호 변경
     * 각 모드에 해당하는 뷰와 기능의 시작/중지를 제어
     *
     * @param mode 모드 상수
     * @see #MODE_DRIVE
     * @see #MODE_ROUTE
     * @see #MODE_NAVIGATION
     */
    public void setMode(int mode) {
        currentMode = mode;
        if (MODE_DRIVE == mode) { //안전 운행 모드
            //검색 툴바 표출
            toolbar.setVisibility(View.VISIBLE);
            //안전운행 모드 뷰 시작
            driveView.start();
            //경로 검색 모드 뷰 중지
            if (routeView != null) {
                routeView.destroy();
                contentFrameLayout.removeView(routeView);
                routeView = null;
            }
            //경로 안내 모드 뷰 중지
            if (navigationView != null) {
                navigationView.destroy();
                contentFrameLayout.removeView(navigationView);
                navigationView = null;
            }
            //지도 객체 안전운행 모드로 변경
            MapController.getInstance().setMode(MODE_DRIVE);
        } else if (MODE_ROUTE == mode) { //경로 검색 모드
            //검색 툴바 숨김
            toolbar.setVisibility(View.INVISIBLE);
            //안전운행 모드 뷰 중지
            driveView.stop();
            //경로 검색 모드 뷰 시작
            getRouteView().start();
            //경로 안내 모드 뷰 중지
            if (navigationView != null) {
                navigationView.stop();
            }
            //지도 객체 경로 검색 모드로 변경
            MapController.getInstance().setMode(MODE_ROUTE);
        } else if (MODE_NAVIGATION == mode) { //경로 안내 모드
            //검색 툴바 숨김
            toolbar.setVisibility(View.INVISIBLE);
            //안전 운행 모드 뷰 시작
            driveView.start();
            //경로 검색 모드 뷰 중지
            if (routeView != null) {
                routeView.destroy();
                contentFrameLayout.removeView(routeView);
                routeView = null;
            }
            //경로 안내 모드 뷰 시작
            getNavigationView().start();
            //지도 객체 경로 안내 모드로 변경
            MapController.getInstance().setMode(MODE_NAVIGATION);
        }
    }
}
