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
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.core.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.kt.geom.model.Coord;
import com.kt.maps.GMap;
import com.kt.maps.GMapFragment;
import com.kt.maps.GMapResultCode;
import com.kt.maps.OnMapReadyListener;
import com.kt.maps.model.Viewpoint;
import com.kt.roze.NavigationManager;
import com.kt.roze.RozeResultCode;
import com.kt.rozenavi.R;
import com.kt.rozenavi.RozeNaviApplication;
import com.kt.rozenavi.ui.search.SearchActivity;
import com.kt.rozenavi.utils.PreferenceUtils;
import com.kt.rozenavi.utils.UIUtils;
import com.squareup.leakcanary.RefWatcher;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 메인 액티비티
 * 지도 및 안전운행 뷰, 경로검색 뷰, 경로안내 뷰가 포함
 * 지도에 대한 이벤트리스너를 처리하는 클래스
 */
public class MainActivity extends AppCompatActivity
        implements OnMapReadyListener, GMap.OnViewpointChangeListener, GMap.OnMapLongpressListener {
    /**
     * 목적지 검색 요청 코드 상수
     */
    public static final int SEARCH_REQ_CODE = 101;
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
    private ActionBarDrawerToggle drawerToggle;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        //목적지 검색 결과는 routeview로 전달
        if (requestCode == SEARCH_REQ_CODE) {
            UIController.getInstance().getRouteView().onActivityResult(requestCode, resultCode,
                    data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();
    }

    /**
     * 초기화 기능 수행
     * UI초기화 및 navigationmanager 초기화를 수행
     */
    private void init() {
        initToolbar();

        //UIController에 UI component 연결
        UIController.getInstance().bindMainActivity(this);

        //구글플레이서비스를 활용하지 못하는 상황일때 정상적인 내비기능을 활용하지 못하여
        //구글플레이서비스를 업데이트 하거나 사용가능하도록 변경하도록 안내 팝업을 표시 후 앱을 종료시킴

        RozeResultCode initCode = NavigationManager.getInstance()
                .initApplicationContext(getApplicationContext());
        if (initCode != RozeResultCode.SUCCESS) {
            showNaviInitFailDialog(initCode);
            return;
        }
        //gps 수신을 위해 내비게이션 시작
        NavigationManager.getInstance().startTracking();

        //내비 동작중에 화면이 꺼지지 않도록 wake lock 설정
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //지도 초기화
        initGMap();
    }

    /**
     * 툴바 초기화
     * ActionBarDrawerToggle 설정
     */
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name,
                R.string.app_name);
        drawerLayout.addDrawerListener(drawerToggle);

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
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return drawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //앱 종료전 wake lock 설정한거 해제
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //지도 및 UI 제어 싱글턴 클래스 destroy
        UIController.getInstance().destroy();
        MapController.getInstance().destroy();

        //최종 좌표 저장
        Location lastLocation = NavigationManager.getInstance().getLastGpsLocation();
        if (lastLocation != null) {
            PreferenceUtils.putString(this, PreferenceUtils.KEY_LAST_LOCATION_LAT,
                    String.valueOf(lastLocation.getLatitude()));
            PreferenceUtils.putString(this, PreferenceUtils.KEY_LAST_LOCATION_LON,
                    String.valueOf(lastLocation.getLongitude()));
        }

        //네비게이션엔진 종료
        NavigationManager.getInstance().stop();

        //지도 객체 리스너 해제
        if (map != null) {
            map.setOnViewpointChangeListener(null);
            map.setOnMapLongpressListener(null);
            map = null;
        }

        //메모리 릭 확인을 leakcanary code
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

        //Route mode, Navigation mode일때 처리
        if (UIController.getInstance().onBackPressed()) {
            return;
        }

        //Drive mode일때 처리
        //뒤로가기 버튼 종료 interval처리
        long currentTime = System.currentTimeMillis();
        long intervalTime = currentTime - backPressedTime;

        if (intervalTime < UIUtils.APP_FINISH_INTERVAL_IN_MILLISECOND) {
            super.onBackPressed();
        } else {
            backPressedTime = currentTime;
            UIUtils.showToast(this, R.string.toast_message_main_back_button);
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
//        options.viewpoint(new Viewpoint(MapUtils.DEFAULT_COORD, MapUtils.DEFAULT_ZOOM, MapUtils.DEFAULT_TILT,
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
        //지도 long touch 이벤트 리스너 설정
        gMap.setOnMapLongpressListener(this);

//        //지도화면을 임의로 회전시키는것 방지
//        gMap.setRotateGesturesEnabled(false);
//        //지도화면 임의로 tilt를 변경시키는것 방지
//        gMap.setTiltGesturesEnabled(false);
        //초기화전 지도화면을 숨겨두었다가 완료후 표출
        mapView.setVisibility(View.VISIBLE);

        MapController.getInstance().setMap(gMap);
        if (!MapController.getInstance().showCurrentLocation()) {
            String lat =
                    PreferenceUtils.getString(this, PreferenceUtils.KEY_LAST_LOCATION_LAT, null);
            String lon =
                    PreferenceUtils.getString(this, PreferenceUtils.KEY_LAST_LOCATION_LON, null);
            if (!(TextUtils.isEmpty(lat) || TextUtils.isEmpty(lon))) {
                MapController.getInstance()
                        .setInitLocation(Double.parseDouble(lat), Double.parseDouble(lon));
            }
        }
        //2017.07.19 앱 재시작시 오류
        //안전운행모드로 설정
        //명시적으로 지도 객체 설정이후 시점으로 변경
        UIController.getInstance().setMode(UIController.MODE_DRIVE);
    }

    /**
     * 목적지 검색 요청
     * 목적지 검색 화면으로 전환
     */
    @OnClick(R.id.btn_search)
    public void requestSearch() {
        Intent intent = new Intent(MainActivity.this, SearchActivity.class);
        startActivityForResult(intent, SEARCH_REQ_CODE);
        //검색화면 시작시 애니메이션 삭제
        overridePendingTransition(0, 0);
    }

    @Override
    public void onViewpointChange(GMap map, Viewpoint viewpoint, boolean gesture) {
        //지도 viewpoint 변경 이벤트 발생시 driveview에 이벤트 전달
        UIController.getInstance().getDriveView().onViewpointChange(map, viewpoint, gesture);
    }

    @Override
    public void onMapLongpress(GMap map, Coord location) {
    }

    @Override
    public void onFailReadyMap(GMapResultCode code) {
        //since : sdk 0.9.5
        //지도 초기화 실패시 오류코드 반환
        /**
         * Add Custom Code
         * Code Type
         * Success  : success
         * EMPTY    : api key not found
         * INVALID  : invalid key length
         * FAIL     : incorrect api key
         */
        int errorMessageRes;
        if (GMapResultCode.EMPTY == code) {
            errorMessageRes = R.string.toast_message_map_init_fail_result_empty;
        } else if (GMapResultCode.INVALID == code) {
            errorMessageRes = R.string.toast_message_map_init_fail_result_invalid;
        } else if (GMapResultCode.FAIL == code) {
            errorMessageRes = R.string.toast_message_map_init_fail_result_fail;
        } else {
            return;
        }
        Toast.makeText(this, errorMessageRes, Toast.LENGTH_SHORT).show();

    }
}
