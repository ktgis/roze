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
package com.kt.rozenavi.ui.main;

import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.kt.maps.GMap;
import com.kt.maps.GMapFragment;
import com.kt.maps.OnMapReadyListener;
import com.kt.maps.model.Viewpoint;
import com.kt.maps.util.GMapKeyManager;
import com.kt.roze.NavigationManager;
import com.kt.roze.RozeResultCode;
import com.kt.roze.location.model.GeoLocation;
import com.kt.rozenavi.R;
import com.kt.rozenavi.RozeNaviApplication;
import com.kt.rozenavi.ui.component.core.BaseActivity;
import com.kt.rozenavi.ui.component.core.BaseFragment;
import com.kt.rozenavi.ui.main.alarm.NightAlarmManager;
import com.kt.rozenavi.ui.main.drive.DriveFragment;
import com.kt.rozenavi.ui.main.service.TbtGuidancePopupService;
import com.kt.rozenavi.ui.setting.AppOptions;
import com.kt.rozenavi.utils.PreferenceUtils;
import com.kt.rozenavi.utils.UIUtils;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;

/**
 * 메인 액티비티
 * 지도 및 안전운행 뷰, 경로검색 뷰, 경로안내 뷰가 포함
 * 지도에 대한 이벤트리스너를 처리하는 클래스
 */
public class MainActivity extends BaseActivity
        implements OnMapReadyListener,
        NavigationManager.LocationListener, GMap.OnViewpointChangeListener,
        NavigationManager.GpsSignalListener {
    /**
     * 목적지 검색 요청 코드 상수
     */
    public static final int SEARCH_REQ_CODE = 101;

    public static TbtGuidancePopupService tbtGuidancePopupService;

    /**
     * 앱 뒤로가기 버튼 입력 시간
     * 한번 더 눌렀을때 재확인을 위해 저장
     */
    private long backPressedTime = 0;
    /**
     * gmap 지도 객체
     */
    private GMap map;
    /**
     * GMapFragment 생성할 content layout
     */
    @BindView(R.id.gmap)
    protected FrameLayout mapView;
    /**
     * navigation drawer
     */
    @BindView(R.id.drawer_layout)
    protected DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main, true);
        init();
    }

    /**
     * 초기화 기능 수행
     * UI초기화 및 navigationmanager 초기화를 수행
     */
    @Override
    protected void init() {
        //data 초기화
        initData();
        //view 초기화
        initView();

        // service bind
        setBindService(this);

        NavigationManager navigationManager = NavigationManager.getInstance();
        //구글플레이서비스를 활용하지 못하는 상황일때 정상적인 내비기능을 활용하지 못하여
        //구글플레이서비스를 업데이트 하거나 사용가능하도록 변경하도록 안내 팝업을 표시 후 앱을 종료시킴
        RozeResultCode initCode = navigationManager.initApplicationContext(getApplicationContext());
        if (initCode != RozeResultCode.SUCCESS) {
            showNaviInitFailDialog(initCode);
            return;
        }

        navigationManager.setLocationListener(this);
        navigationManager.setGpsSignalListener(this);

        //gps 수신을 위해 내비게이션 시작
        navigationManager.startTracking();

        //내비 동작중에 화면이 꺼지지 않도록 wake lock 설정
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //지도 초기화
        initGMap();

        // 앱 그리기 권한 설정
        if (!isDrawOverlayEnabled(this)) {
            showRequestAllowDrawOverlayDialog();
        }
    }

    private MainActivityViewModel viewModel;

    private void initData() {
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
    }

    private void initView() {
        replaceFragment(DriveFragment.getInstance());
    }

    /**
     * navigationmanager 초기화 실패시 안내 팝업 표시
     * 구글플레이 서비스 오류로 초기화에 실패
     * 업데이트 안내팝업
     */
    private void showNaviInitFailDialog(RozeResultCode errorCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_navigation_init_fail_title);
        switch (errorCode) {
            case API_KEY_ERROR:
                builder.setMessage(R.string.dialog_navigation_init_fail_api_key_message);
                break;
            default:
                builder.setMessage(R.string.dialog_navigation_init_fail_message);
                break;
        }
        builder.setNegativeButton(R.string.dialog_navigation_init_fail_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            DialogInterface dialog, int id) {
                        finish();
                    }
                });
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //앱 종료전 wake lock 설정한거 해제
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.setLocationListener(null);
        navigationManager.setGpsSignalListener(null);
        //최종 좌표 저장
        Location lastLocation = navigationManager.getLastGpsLocation();
        if (lastLocation != null) {
            PreferenceUtils.putString(this, PreferenceUtils.KEY_LAST_LOCATION_LAT,
                    String.valueOf(lastLocation.getLatitude()));
            PreferenceUtils.putString(this, PreferenceUtils.KEY_LAST_LOCATION_LON,
                    String.valueOf(lastLocation.getLongitude()));
        }

        // service Unbind
        if (tbtGuidancePopupService != null) {
            tbtGuidancePopupService.removeNotification();
            tbtGuidancePopupService.removeTbtPopupView();
            unbindService(conn);
        }

        //네비게이션엔진 종료
        navigationManager.stop();

        //지도 객체 리스너 해제
        if (map != null) {
            map.setOnViewpointChangeListener(null);
            map.setOnMapLongpressListener(null);
            map = null;
        }

        AppOptions.getInstance().close();
        //알람매니저 종료
        NightAlarmManager.cancelAlarmManager(this);

        RefWatcher refWatcher = RozeNaviApplication.getRefWatcher(this);
        refWatcher.watch(this);
    }

    @Override
    public void onBackPressed() {
        //drawer가 열려있는경우는 닫기
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.base_fragment_container);
        if (fragment instanceof DriveFragment) {
            //뒤로가기 버튼 종료 interval처리
            long currentTime = System.currentTimeMillis();
            long intervalTime = currentTime - backPressedTime;

            if (intervalTime < UIUtils.APP_FINISH_INTERVAL_IN_MILLISECOND) {
                super.onBackPressed();
            } else {
                backPressedTime = currentTime;
                UIUtils.showToast(this, R.string.toast_message_main_back_button);
            }
        } else {
            if (fragment instanceof BaseFragment) {
                if (!((BaseFragment) fragment).canGoBack()) {
                    return;
                }
            }
            replaceFragment(DriveFragment.getInstance());
        }
    }

    /**
     * 지도 초기화
     * fragmentManager를 이용한 코드방식 생성
     */
    private void initGMap() {
        //code 방식
//        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
//        //지도옵션을 통해 초기위치 지정
//        MapOptions options = new MapOptions();
//        options.viewpoint(new Viewpoint(MapUtils.DEFAULT_COORD, MapUtils.DEFAULT_ZOOM, MapUtils
// .DEFAULT_TILT,
//                MapUtils.DEFAULT_ROTATION));
//        options.maxZoom(MapUtils.DEFAULT_MAX_ZOOM);
//        options.minZoom(MapUtils.DEFAULT_MIN_ZOOM);
//        //gmapfragment에 지도옵션 설정하여 생성
//        GMapFragment fragment = new GMapFragment(options);
//        fragmentTransaction.add(R.id.gmap, fragment);
//        fragmentTransaction.commit();

        //layout xml 방식
        GMapFragment fragment =
                (GMapFragment) getFragmentManager().findFragmentById(R.id.gmapfragment);
        //지도 OnMapReadyListener 설정 onMapReady()를 통해 완료이벤트 발생
        fragment.setOnMapReadyListener(this);
    }

    @Override
    public void onMapReady(GMap gMap) {
        map = gMap;
        //지도 viewpoint 변경 이벤트 리스너 설정
        gMap.setOnViewpointChangeListener(this);

        viewModel.gMap.setValue(gMap);

        //초기화전 지도화면을 숨겨두었다가 완료후 표출
        mapView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onViewpointChange(GMap map, Viewpoint viewpoint, boolean gesture) {
        viewModel.viewpointEventData.setValue(new MainActivityViewModel.ViewpointChangeEventData(map, viewpoint, gesture));
    }

    @Override
    public void onGpsLost() {
        viewModel.isGpsOn.setValue(false);
    }

    @Override
    public void onGpsAvailable() {
        viewModel.isGpsOn.setValue(true);
    }

    @Override
    public void onLocationUpdated(GeoLocation geoLocation) {
        viewModel.geoLocation.setValue(geoLocation);
    }

    @Override
    public void onFailReadyMap(GMapKeyManager.ResultCode code) {
        /**
         * Add Custom Code
         * Code Type
         * Success  : success
         * EMPTY    : api key not found
         * INVALID  : invalid key length
         * FAIL     : incorrect api key
         */
        int messageId = R.string.toast_message_map_init_fail_result_success;
        switch (code) {
            case EMPTY:
                messageId = R.string.toast_message_map_init_fail_result_empty;
                break;
            case INVALID:
                messageId = R.string.toast_message_map_init_fail_result_invalid;
                break;
            case FAIL:
                messageId = R.string.toast_message_map_init_fail_result_fail;
                break;
        }
        Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
    }

    /**
     * 앱 포커싱 해제 체크 이벤트
     * 홈버튼, 목록버튼, 기타 다른 앱 전환 시 아래 메서드 호출
     */
    @Override
    protected void onUserLeaveHint() {
        // Bind to LocalService
        if (NavigationManager.getInstance().getMode() == NavigationManager.Mode.NAVIGATING) {
            // 앱 그리기 권한 설정
            if (isDrawOverlayEnabled(this)) {
                tbtGuidancePopupService.addTbtPopupView();
            } else {
                UIUtils.showToast(this, R.string.permission_draw_overlay_introduce_message);
            }
        }
        super.onUserLeaveHint();
    }

    /**
     * 서비스 바인딩 시 콜백 메서드 구현
     */
    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            TbtGuidancePopupService.TbtGuidancePopupBinder tbtGuidancePopupBinder =
                    (TbtGuidancePopupService.TbtGuidancePopupBinder) service;
            // 서비스가 제공하는 메소드 호출하여 서비스쪽 객체를 전달받을수 있슴
            tbtGuidancePopupService = tbtGuidancePopupBinder.getService();
        }

        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
        }
    };

    /**
     * 서비스 바인딩 호출
     */
    private void setBindService(Context context) {
        // Bind to LocalService
        Intent intent = new Intent(context, TbtGuidancePopupService.class);
        context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        // Popup View 삭제
        if (conn != null && tbtGuidancePopupService != null) {
            tbtGuidancePopupService.removeTbtPopupView();
        }
        super.onResume();
    }

    /**
     * 다른 앱 위에 그리기 권한 체크
     */
    private static boolean isDrawOverlayEnabled(Context context) {
        return Build.VERSION.SDK_INT < 23 || Settings.canDrawOverlays(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private static Intent getDrawOverlayPermissionIntent(Context context) {
        return new Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + context.getPackageName()));
    }

    private void showRequestAllowDrawOverlayDialog() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.permission_draw_overlay_dialog_message)
                .setNegativeButton(R.string.common_cancel, null)
                .setPositiveButton(R.string.common_setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            startActivity(getDrawOverlayPermissionIntent(MainActivity.this));
                        }
                    }
                }).show();
    }

    public void openDrawer() {
        drawerLayout.openDrawer(Gravity.START);
    }
}
