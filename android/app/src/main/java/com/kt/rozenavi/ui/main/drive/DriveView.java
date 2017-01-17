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

package com.kt.rozenavi.ui.main.drive;

import android.content.Context;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.kt.geom.model.UTMK;
import com.kt.maps.GMap;
import com.kt.maps.model.Viewpoint;
import com.kt.roze.NavigationManager;
import com.kt.roze.location.model.GeoLocation;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.MainViewInterface;
import com.kt.rozenavi.ui.main.MapController;
import com.kt.rozenavi.utils.MapUtils;
import com.kt.rozenavi.utils.NaviUtils;
import com.kt.rozenavi.utils.UIUtils;
import com.kt.rozenavi.utils.WeakReferenceHandler;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 안전운행 화면 뷰 클래스
 * 기본적인 지도화면에서의 정보를 표시해주고 속도, 현재위치 정보를 확인하고
 * 지도 헤딩모드를 변경하거나 내위치 이동 기능 및 지도에서 발생하는 이벤트를 처리
 */
public class DriveView extends RelativeLayout
        implements WeakReferenceHandler.OnMessageHandler, MainViewInterface, NavigationManager
        .Listener, NavigationManager.LocationListener, NavigationManager.GpsSignalListener {
    /**
     * 속도정보 표시 layout
     */
    @BindView(R.id.speed_value_layout)
    protected LinearLayout speedValueLayout;
    /**
     * 현재위치 이동 button
     */
    @BindView(R.id.btn_location)
    protected ImageView currentLocationBtn;
    /**
     * 헤딩상태 표시 view
     */
    @BindView(R.id.viewport_imageview)
    protected ImageView viewportImageView;
    /**
     * 차량 마커가 Route Path에 진입했는지 여부
     */
    private boolean isEnterInRoutePath = false;
    /**
     * Gps 정보가 실제 수신되었는지 여부
     */
    private boolean isGpsOn = false;
    /**
     * 정북방향 헤딩여부
     */
    private boolean isHeadingNorth = false;
    /**
     * 최초 위치좌표 수신 여부
     */
    private boolean isGpsFirstFix = false;
    /**
     * 현재 속도 정보
     */
    public int currentSpeed;
    /**
     * 타이머 대용 내부 핸들러
     * 지도 이동시 일정시간 뒤 내위치로 지도를 다시 복귀할 때 활용
     */
    private WeakReferenceHandler handler = new WeakReferenceHandler(this);

    public DriveView(Context context) {
        super(context);
        initView(context);
    }

    public DriveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public DriveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * 안전운행 화면 초기화
     * 레이아웃 설정 및 객체 생성
     *
     * @param context context 객체
     */
    private void initView(Context context) {
        View.inflate(context, R.layout.view_drive, this);
        ButterKnife.bind(this);

        //gps 활성/비활성 신호와 네비게이션 모드 변경은 항상 수신해야 하기 때문에 기본 설정
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.setGpsSignalListener(this);
        navigationManager.setListener(this);
        navigationManager.setLocationListener(this);
    }

    @Override
    public void destroy() {
        stop();
        //해제되지 않은 리스너 모두 해제
        NavigationManager navigationManager = NavigationManager.getInstance();
        navigationManager.setGpsSignalListener(null);
        navigationManager.setListener(null);
        navigationManager.setLocationListener(null);
    }

    @Override
    public void start() {
        //화면 표출
        setVisibility(VISIBLE);
    }

    @Override
    public void stop() {
        //화면 숨김
        setVisibility(INVISIBLE);
    }

    /**
     * 현재 내위치 이동
     * 지도 제어 클래스를 통해 마지막 수신된 좌표로 지도를 이동시키는 기능
     * 터치를 이용하여 지도를 이동했을시는 내위치 버튼을 숨김처리
     * 내위치로 이동시키는 핸들러 메시지는 삭제
     */
    @OnClick(R.id.btn_location)
    public void showCurrentLocation() {
        if (getVisibility() != View.VISIBLE) {
            return;
        }
        currentLocationBtn.setVisibility(View.INVISIBLE);
        handler.removeMessages(0);
        MapController.getInstance().showCurrentLocation();
    }

    /**
     * 지도 헤딩 토글 기능
     * 헤딩모드, 정북모드를 변경하는 기능
     */
    @OnClick(R.id.viewport_imageview)
    public void toggleHeading() {
        isHeadingNorth = !isHeadingNorth;
        if (isHeadingNorth) {
            viewportImageView.setImageResource(R.drawable.bt_north_pin);
            viewportImageView.setRotation(0);
            MapController.getInstance().setHeadingMode(MapController.HEADING_NORTH);
        } else {
            viewportImageView.setImageResource(R.drawable.bt_headup_pin);
            MapController.getInstance().setHeadingMode(MapController.HEADING_BEARING);
        }
    }

    /**
     * 내차 마커 아이콘 변경
     * gps 수신여부 및 navigationmanager mode에 따라서 맞는 이미지로 설정
     */
    public void carMarkerChange() {
        MapController.getInstance()
                .setCarMarkerIcon(NaviUtils.selectCarMarkerIconResourceId(isGpsOn));
    }

    /**
     * 지도 viewpoint 변경 이벤트 처리
     * mainactivity에서 전달받은 이벤트 정보를 처리
     * 사용자가 터치이벤트로 지도이동시 내위치로 복귀하는 타이머 로직을 동작하고
     * 아닌경우 지도화면에 맞추어 내차마커 이동
     *
     * @param map       지도 객체
     * @param viewpoint 변경된 Viewpoint
     * @param gesture   사용자 제스쳐 발생 여부
     */
    public void onViewpointChange(GMap map, Viewpoint viewpoint, boolean gesture) {
        //사용자 제스쳐이면서 경로검색 모드가 아닌경우 내위치 복귀 타이머 설정
        if (gesture && getVisibility() == View.VISIBLE) {
            if (handler.hasMessages(0)) {
                handler.removeMessages(0);
            }

            handler.sendEmptyMessageDelayed(0, MapUtils.MAP_UPDATE_SUSPEND_IN_MILLISECOND);
            //내위치 이동 버튼 표출
            currentLocationBtn.setVisibility(View.VISIBLE);
        } else {
            MapController.getInstance().moveCarMarker(isEnterInRoutePath);
        }

        //지도 제어클래스에서 viewpoint 변경관련 이벤트 처리
        MapController.getInstance().onViewpointChange(map, viewpoint, gesture);
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
        resId = NaviUtils.getNumberResourceId(speed % 10);
        numberImage = (ImageView) speedValueLayout.getChildAt(speedValueLayout.getChildCount() - 1);
        numberImage.setImageResource(resId);

        //10의 자리 이후는 값이 없는경우는 빈칸처리
        for (int i = 1; i < speedValueLayout.getChildCount(); i++) {
            //10의 n승으로 자리수를 체크
            number = (int) (speed / Math.pow(10, i));
            //가장 큰 자리수가 0번째이므로 뒤에서 부터 순차적으로 입력
            numberImage = (ImageView) speedValueLayout.getChildAt(
                    (speedValueLayout.getChildCount() - 1) - i);
            //자리수의 값이 0이면 빈칸처리
            if (number == 0) {
                numberImage.setImageDrawable(null);
            } else {
                //자리수의 값이 0이 아니면 10으로 나눈 나머지에 해당하는 이미지 입력
                numberImage.setImageResource(NaviUtils.getNumberResourceId(number % 10));
            }
        }

        currentSpeed = speed;
    }

    //--NavigationManager.LocationListener
    @Override
    public void onLocationUpdated(GeoLocation geoLocation) {
        //화면 숨김상태일때 위치정보 처리 안함
        if (getVisibility() != View.VISIBLE) {
            return;
        }
        //지도이동 좌표
        UTMK coord;
        //애니메이션 시간
        int animateDuration = MapUtils.MAP_ANIMATION_DURATION_IN_MILLISECOND_LOCATION_UPDATE;
        //회전값
        double rotate = 0;

        if (!isGpsFirstFix && MapController.getInstance().isSetMap()) {
            isGpsFirstFix = true;
            animateDuration = 0;
        }

        if (geoLocation.hasRoutedLocation()) { //맵매칭 된 좌표가 있는경우, 경로안내 모드
            //매칭된 좌표와 회전값 설정
            coord = geoLocation.routeLocation.location;
            rotate = geoLocation.routeLocation.angle;
            //route path에 매칭되는 상태로 설정
            isEnterInRoutePath = true;

            if (geoLocation.location == null) { //실제 좌표는 수신되지 않는 상태, 터널 prediction
                //속도값 설정
                setSpeed(0);
            } else {
                //속도값 설정
                setSpeed(NaviUtils.calculateSpeed(geoLocation.location));
            }
        } else {    //안전운행 모드
            //route path에 매칭되지 않는 상태로 설정
            isEnterInRoutePath = false;

            //gps정보를 utmk좌표로 변환하여 설정
            coord = MapUtils.convertLocationToUtmk(geoLocation.location);

            int speed = NaviUtils.calculateSpeed(geoLocation.location);
            //속도가 0보다 클때만 회전값 사용
            //정지중에 화면이 회전하는것 방지
            if (speed > 0) {
                if (geoLocation.location != null) {
                    rotate = geoLocation.location.getBearing();
                }
            }
            //속도값 설정
            setSpeed(speed);
        }

        if (handler.hasMessages(0)) { //내위치 복귀 타이머 설정된 상태
            MapController.getInstance().moveCarMarker(coord, isEnterInRoutePath);
        } else {
            MapController.getInstance()
                    .changeViewpoint(coord, rotate,
                            animateDuration, GMap.AnimationTiming.LINEAR);
        }
    }
    //--NavigationManager.LocationListener

    //--NavigationManager.Listener
    @Override
    public void onNavigationModeChanged(NavigationManager.Mode mode) {
        carMarkerChange();
    }
    //--NavigationManager.Listener

    //--NavigationManager.GpsSignalListener
    @Override
    public void onGpsLost() {
        isGpsOn = false;
        carMarkerChange();
    }

    @Override
    public void onGpsAvailable() {
        isGpsOn = true;
        carMarkerChange();
    }
    //--NavigationManager.GpsSignalListener

    //--WeakReferenceHandler.OnMessageHandler
    @Override
    public void handleMessage(Message msg) {
        showCurrentLocation();
    }
    //--WeakReferenceHandler.OnMessageHandler
}
