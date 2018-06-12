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

package com.kt.rozenavi.ui.main.navigation.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.geom.model.UTMK;
import com.kt.maps.GMap;
import com.kt.maps.model.ResourceDescriptorFactory;
import com.kt.maps.overlay.Marker;
import com.kt.roze.data.model.Accident;
import com.kt.roze.guidance.RGType;
import com.kt.roze.guidance.model.IntervalSpeedSpotGuidance;
import com.kt.roze.guidance.model.SafetySpotGuidance;
import com.kt.roze.guidance.model.SafetySpotInterface;
import com.kt.roze.resource.RoadSignResourceManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.component.SpeedMeterView;
import com.kt.rozenavi.ui.main.navigation.util.MapHelper;
import com.kt.rozenavi.utils.CommonUtils;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;

/**
 * 안전운행 관련 Route Guidance 표시 View
 */
public class NavigationSpotView extends RelativeLayout {
    @BindView(R.id.spot_imageview)
    protected ImageView spotImageView;
    @BindView(R.id.spot_speed_meter_view)
    protected SpeedMeterView speedMeterView;
    @BindView(R.id.remain_distance_textview)
    protected TextView remainTextView;
    @BindView(R.id.interval_info_layout)
    protected View intervalInfoLayout;
    @BindView(R.id.interval_average_textview)
    protected TextView averageTextView;
    @BindView(R.id.restriction_value_textview)
    protected TextView restrictionTextView;

    private MapHelper mapHelper = MapHelper.getInstance();
    private GMap gMap;

    private List<Marker> safetyMarkerList = new ArrayList<>();
    private List<Marker> accidentMarkerList = new ArrayList<>();

    private int[] cameraResourceArray = new int[]{
            R.drawable.img_camera_warning01,
            R.drawable.img_camera_warning01_01,
            R.drawable.img_camera_warning01_02,
            R.drawable.img_camera_warning01_03,
            R.drawable.img_camera_warning02,
            R.drawable.img_camera_warning02_01,
            R.drawable.img_camera_warning02_02,
            R.drawable.img_camera_warning02_03
    };
    private Disposable cameraDisposable;
    private int speed = 0;
    private Marker warningCamera;
    //-- 1.2.0 data type 변경
    private SafetySpotInterface currentShowSpot;

    public NavigationSpotView(Context context) {
        super(context);
        initView(context);
    }

    public NavigationSpotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NavigationSpotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_navigation_spot, this);
        ButterKnife.bind(this);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView != this) {
            return;
        }

        //visibility변경시 interval 및 기타 layout visibility 초기화
        switch (visibility) {
            case INVISIBLE:
            case GONE:
                hideSubView();
                stopCameraWarningAnimation();
                break;
            case View.VISIBLE:
                break;
        }
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void initMap(GMap gMap) {
        this.gMap = gMap;
    }


    /**
     * 안전운행 정보를 표시한다.
     *
     * @param isShow Show Event 일 때 {@code true} Hide Event 일 때 {@code false}
     * @param list   {@link SafetySpotGuidance} list
     */
    //-- 1.2.0 data type 변경
    public void updateSafetySpotView(boolean isShow, List<SafetySpotInterface> list) {
        /*
        *  isShow == true 주기적으로 계속 들어온다
        *  화면에 표시되는 전체 safety spot 리스트가 전달
        *
        *  isShow == false 그때그때 이벤트 방식으로 들어온다
        *  화면에서 없어지는 safety spot 리스트가 전달
        * */

        //interval 정보 표시중에는 일반 safety를 이벤트를 처리하지 않는다
        if (isShowIntervalSafetySpot()) {
            return;
        }

        if (isShow) {
            showSafetySpot(list);
        } else {
            hideSafetySpot(list);
        }
    }

    /**
     * safety spot 정보 표시
     */
    //-- 1.2.0 data type 변경 및 접근자 변경 private -> protected
    protected void showSafetySpot(List<SafetySpotInterface> list) {
        if (CommonUtils.isEmpty(list)) {
            return;
        }
        SafetySpotInterface speedCamSpot = null;
        int safetyResId = RoadSignResourceManager.RESOURCE_NOT_FOUND;

        for (SafetySpotInterface spot : list) {
            //스피드캠 safety spot 체크
            boolean isSpeedCam = RGType.isSpeedCamera(spot.getType());
            if (isSpeedCam && (safetyResId = RoadSignResourceManager.getResourceId(spot.getType()))
                    != RoadSignResourceManager.RESOURCE_NOT_FOUND) {
                speedCamSpot = spot;
                break;
            }
        }
        //기존마커랑 비교하여 신규로 추가가 필요한 마커를 지도에 추가
        safetyMarkerList.addAll(mapHelper.addSpotMarker(gMap, getNewSafetySpots(list)));

        if (speedCamSpot != null) {
            showSafetyImage(speedCamSpot, safetyResId);
            //제한속도가 0보다 작거나 현재속도가 제한속도보다 작을경우 카메라 경고 중지
            if (speedCamSpot.getLimitSpeed() < 0 || speedCamSpot.getLimitSpeed() > speed) {
                stopCameraWarningAnimation();
            } else {
                startCameraWarningAnimation(speedCamSpot.getCoord());
            }
        } else {
            for (SafetySpotInterface spot : list) {
                //safety spot 리스트에서 화면에 표시 가능한 첫번째 항목 표시
                if ((safetyResId = RoadSignResourceManager.getResourceId(spot.getType()))
                        != RoadSignResourceManager.RESOURCE_NOT_FOUND) {
                    showSafetyImage(spot, safetyResId);
                    break;
                }
            }
            stopCameraWarningAnimation();
        }
    }

    /**
     * 기존에 추가된 마커를 이용하여 새로 추가되어야 하는 guidance 정보만 추출
     *
     * @param list guidance 정보 리스트
     * @return 신규 guidance 정보 리스트
     */
    //-- 1.2.0 data type 변경
    private List<SafetySpotInterface> getNewSafetySpots(List<SafetySpotInterface> list) {
        List<SafetySpotInterface> newGuidances = new ArrayList<>();
        boolean isContain;
        //TODO 3차에서 수정 예정
        for (SafetySpotInterface s : list) {
            isContain = false;
            for (Marker marker : safetyMarkerList) {
                if (s.getCoord().compareTo(marker.getPosition()) == 0) {
                    isContain = true;
                    break;
                }
            }
            if (!isContain) {
                newGuidances.add(s);
            }
        }
        return newGuidances;
    }

    /**
     * safety spot 정보 숨김
     */
    //-- 1.2.0 data type 변경 및 접근자 변경 private -> protected
    protected void hideSafetySpot(List<SafetySpotInterface> list) {
        removeSpotMarker(list);
        hideSafetyImage(list);
    }

    /**
     * safety spot 마커 삭제
     *
     * @param spotList 화면에서 삭제되는 safety spot 리스트
     */
    //-- 1.2.0 data type 변경
    private void removeSpotMarker(List<SafetySpotInterface> spotList) {
        if (CommonUtils.isEmpty(spotList)) {
            return;
        }

        List<Marker> deleteMarkerList = new ArrayList<>();
        for (SafetySpotInterface spot : spotList) {
            for (Marker marker : safetyMarkerList) {
                if (spot.getCoord().compareTo(marker.getPosition()) == 0) {
                    //삭제되어야 하는 마커를 현재 Marker List 에서 추출
                    deleteMarkerList.add(marker);
                }
            }
        }
        //추출된 Marker 를 Map 과 현재 Marker List 에서 삭제
        if (!CommonUtils.isEmpty(deleteMarkerList) && warningCamera != null
                && deleteMarkerList.contains(warningCamera)) {
            stopCameraWarningAnimation();
        }
        mapHelper.removeOverlays(gMap, deleteMarkerList);
        safetyMarkerList.removeAll(deleteMarkerList);
    }

    /**
     * 안전운행 안내점 Image를 View에 표시한다.
     *
     * @param spot 표시 대상 SafetySpot Guidance
     */
    //-- 1.2.0 data type 변경
    private void showSafetyImage(@NonNull SafetySpotInterface spot, int resId) {
        setVisibility(View.VISIBLE);
        currentShowSpot = spot;

        int distance = spot.getRemainDistance();
        spotImageView.setImageResource(resId);
        remainTextView.setText(NaviUtils.convertDistanceUnit(distance));
        boolean isSpeedCamSpot = RGType.isSpeedCamera(spot.getType());
        boolean isWeightSpot = RGType.CAUTION_WEIGHT == spot.getType();
        boolean isHeightSpot = RGType.CAUTION_HEIGHT == spot.getType();
        if (isSpeedCamSpot) {
            speedMeterView.setSpeed(spot.getLimitSpeed());
        } else if (isWeightSpot) {
            restrictionTextView.setText(String.valueOf(spot.getLimitWeight()));
        } else if (isHeightSpot) {
            restrictionTextView.setText(String.valueOf(spot.getLimitHeight()));
        }
        speedMeterView.setVisibility(isSpeedCamSpot ? View.VISIBLE : View.INVISIBLE);
        restrictionTextView.setVisibility((isWeightSpot || isHeightSpot) ? View.VISIBLE : View.INVISIBLE);

    }

    //-- 1.2.0 data type 변경
    private void hideSafetyImage(List<SafetySpotInterface> list) {
        if (CommonUtils.isEmpty(list) || currentShowSpot == null || !list.contains(currentShowSpot)) {
            return;
        }

        currentShowSpot = null;
        hideAllView();

    }

    private boolean isShowIntervalSafetySpot() {
        return intervalInfoLayout.isShown();
    }

    /**
     * 구간단속 정보를 화면에 표시한다.
     *
     * @param intervalGuidance {@link IntervalSpeedSpotGuidance}구간단속 정보
     */
    public void updateIntervalSafetySpotView(IntervalSpeedSpotGuidance intervalGuidance) {
        if (intervalGuidance == null) {
            hideAllView();
            return;
        }

        showIntervalSafetySpot(intervalGuidance);
    }

    private void showIntervalSafetySpot(IntervalSpeedSpotGuidance intervalGuidance) {
        int resId = RoadSignResourceManager.getResourceId(RGType.CAM_SPEED);
        if (resId != RoadSignResourceManager.RESOURCE_NOT_FOUND) {
            spotImageView.setImageResource(resId);
            int speed = (int) intervalGuidance.getAverageSpeed();
            remainTextView.setText(
                    NaviUtils.convertDistanceUnit(intervalGuidance.getLastRemainDistance()));
            averageTextView.setText(String.valueOf(speed));
            intervalInfoLayout.setVisibility(View.VISIBLE);
            speedMeterView.setVisibility(View.VISIBLE);
            speedMeterView.setSpeed(intervalGuidance.getLimitSpeed());
            setVisibility(View.VISIBLE);
        }
    }
    //-- 1.2.0 접근자 변경 private -> public
    public void hideAllView() {
        hideSubView();
        setVisibility(View.INVISIBLE);
    }

    private void hideSubView() {
        speedMeterView.setVisibility(View.INVISIBLE);
        intervalInfoLayout.setVisibility(View.INVISIBLE);
        averageTextView.setText("");
        restrictionTextView.setVisibility(View.INVISIBLE);
    }

    private void stopCameraWarningAnimation() {
        if (cameraDisposable == null) {
            return;
        }
        cameraDisposable.dispose();
        cameraDisposable = null;
        if (warningCamera != null) {
            warningCamera.setIcon(ResourceDescriptorFactory.fromResource(cameraResourceArray[0]));
        }
    }

    private void startCameraWarningAnimation(UTMK coord) {
        if (cameraDisposable != null || coord == null) {
            return;
        }

        for (Marker item : safetyMarkerList) {
            if (coord.compareTo(item.getPosition()) == 0) {
                warningCamera = item;
                break;
            }
        }
        if (warningCamera == null) {
            return;
        }
        cameraDisposable = mapHelper.startMarkerFrameAnimation(warningCamera, cameraResourceArray);
    }


    public void releaseMap() {
        clearOverlay();
        gMap = null;
    }

    /**
     * 안전운행 Marker 와 유고정보 marker 를 제거한다.
     */
    public void clearOverlay() {
        mapHelper.removeOverlays(gMap, safetyMarkerList);
        safetyMarkerList.clear();
        mapHelper.removeOverlays(gMap, accidentMarkerList);
        accidentMarkerList.clear();
    }

    /**
     * 유고 정보를 Map 에 Marker 로 표기 한다.
     *
     * @param accidentList 표시 대상 유고 정보 목록
     */
    public void setAccidentList(List<Accident> accidentList) {
        List markerList = mapHelper.setAccidentList(gMap, accidentList);
        if (CommonUtils.isEmpty(markerList)) {
            return;
        }
        accidentMarkerList.addAll(markerList);
    }
}
