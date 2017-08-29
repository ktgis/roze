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
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.geom.model.UTMK;
import com.kt.maps.GMap;
import com.kt.maps.model.Point;
import com.kt.maps.model.ResourceDescriptorFactory;
import com.kt.maps.overlay.Marker;
import com.kt.maps.overlay.MarkerOptions;
import com.kt.roze.data.model.Accident;
import com.kt.roze.guidance.RGType;
import com.kt.roze.guidance.model.IntervalSpeedSpotGuidance;
import com.kt.roze.guidance.model.SafetySpotGuidance;
import com.kt.roze.resource.AccidentResourceManager;
import com.kt.roze.resource.RoadSignResourceManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.component.SpeedMeterView;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

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
    private Subscription cameraSubscription;
    private int iconIndex = 0;
    private int speed = 0;
    private Marker warningCamera;

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

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void initMap(GMap gMap) {
        this.gMap = gMap;
    }

    public void setAccidentList(List<Accident> accidentList) {
        if (accidentList == null || accidentList.isEmpty()) {
            return;
        }
        Marker accidentMarker;
        for (Accident accident : accidentList) {
            accidentMarker = new Marker();
            accidentMarker.setPosition(accident.coord);
            int resId = AccidentResourceManager.getAccidentResourceID((short) accident.typeCode);
            if (resId > 0) {
                accidentMarker.setIcon(ResourceDescriptorFactory.fromResource(resId));
                accidentMarker.setIconSize(new Point(30, 30));
            }
            accidentMarkerList.add(accidentMarker);
            gMap.addOverlay(accidentMarker);
        }
    }

    /**
     * 안전운행 정보를 표시한다.
     *
     * @param isShow Show Event 일 때 {@code true} Hide Event 일 때 {@code false}
     * @param list   {@link SafetySpotGuidance} list
     */
    public void updateSafetySpotView(boolean isShow, List<SafetySpotGuidance> list) {
        /* isShow == true 주기적으로 계속 들어온다
        *  이전에 작업한 내용과 새로 들어온 내용이 맞는지 확인 후 새로 작업을 할지 비교
        *  ex) marker
        *
        *  isShow == false 그때그때 이벤트 방식으로 들어온다
        *  이전에 작업한 내용과 들어온 내용을 비교해서 숨길지 말지를 결정하면 될듯
        *  ex) marker
        *
        *  item.safetySpot.type == 방지턱
        *  item.safetySpot.coord == 유니크하게 구분할수 있는 구분값으로 활용
        * */

        if (isShow) {
            SafetySpotGuidance item = null;
            for (SafetySpotGuidance spot : list) {
                if (RGType.isSpeedCamera(spot.safetySpot.type)) {
                    item = spot;
                    break;
                }
            }

            if (gMap != null) {
                addSpotMarker(list);
            }

            if (item != null) {
                setSafetyImage(item);
                if (item.safetySpot.getLimitSpeed() < speed) {
                    startCameraWarningAnimation(item.safetySpot.getCoord());
                } else {
                    stopCameraWarningAnimation();
                }
            } else {
                for (SafetySpotGuidance safety : list) {
                    if (safety.safetySpot.type != RGType.CAM_CCTV) {
                        setSafetyImage(safety);
                    }
                }
                stopCameraWarningAnimation();
            }


        } else {
            if (gMap != null) {
                removeSpotMarker(list);
            }
            stopCameraWarningAnimation();
            setSafetyImage(null);
        }
    }

    private void addSpotMarker(List<SafetySpotGuidance> spotList) {
        Marker marker;
        UTMK coord;
        List<Marker> addMarkerList = new ArrayList<>();
        for (SafetySpotGuidance spot : spotList) {
            marker = null;
            coord = spot.safetySpot.getCoord();
            for (Marker item : safetyMarkerList) {
                if (coord.compareTo(item.getPosition()) == 0) {
                    marker = item;
                }
            }
            if (marker == null) {
                createSpotMarker(spot, addMarkerList);
            }
        }
        safetyMarkerList.addAll(addMarkerList);
    }

    private void createSpotMarker(SafetySpotGuidance spot, List<Marker> addMarkerList) {
        int resourceId = NaviUtils.getRgTypeImage(spot.safetySpot.type);
        if (resourceId > 0) {
            MarkerOptions option = new MarkerOptions();
            option.position(spot.safetySpot.getCoord());
            option.icon(ResourceDescriptorFactory.fromResource(resourceId));
            option.anchor(new Point(0.5, 0.5));
            int iconSize = NaviUtils.getRgTypeIconSize(spot.safetySpot.type);
            option.iconSize(new Point(iconSize, iconSize));
            Marker marker = new Marker(option);
            addMarkerList.add(marker);
            gMap.addOverlay(marker);
        }
    }

    private void removeSpotMarker(List<SafetySpotGuidance> spotList) {
        if (spotList == null || spotList.size() == 0) {
            for (Marker marker : safetyMarkerList) {
                gMap.removeOverlay(marker);
            }
            safetyMarkerList.clear();
        } else {
            //현재 마커리스트를 복제한다
            List<Marker> lastMarkerList = new ArrayList<>();
            lastMarkerList.addAll(safetyMarkerList);

            UTMK coord;
            for (SafetySpotGuidance spot : spotList) {
                coord = spot.safetySpot.getCoord();
                for (Marker marker : safetyMarkerList) {
                    if (coord.compareTo(marker.getPosition()) == 0) {
                        gMap.removeOverlay(marker);
                        //삭제되어야 하는 마커를 복제된 리스트에서 제거
                        lastMarkerList.remove(marker);
                    }
                }
            }
            //기존 리스트를 정리한뒤 새로운 리스트로 대치
            safetyMarkerList.clear();
            safetyMarkerList = lastMarkerList;
        }
    }

    /**
     * 안전운행 안내점 Image를 View에 표시한다.
     *
     * @param spot 표시 대상 SafetySpot Guidance
     */
    private void setSafetyImage(SafetySpotGuidance spot) {
        if (spot == null) {
            if (!intervalInfoLayout.isShown()) {
                setVisibility(View.INVISIBLE);
            }
            return;
        }

        int distance = spot.getRemainDistance();
        int resId = RoadSignResourceManager.getResourceId(spot.safetySpot.type);
        if (resId != RoadSignResourceManager.RESOURCE_NOT_FOUND) {
            setVisibility(View.VISIBLE);
            spotImageView.setImageResource(resId);
            remainTextView.setText(NaviUtils.convertDistanceUnit(distance));
            if (RGType.isSpeedCamera(spot.safetySpot.type)) {
                speedMeterView.setVisibility(View.VISIBLE);
                speedMeterView.setSpeed(spot.safetySpot.getLimitSpeed());
            } else {
                speedMeterView.setVisibility(View.INVISIBLE);
            }
        } else {
            //방범 카메라 등 목적지 안내에 필요없는 데이터는 보여주지 않는다.
            setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 구간단속 정보를 화면에 표시한다.
     *
     * @param intervalGuidance {@link IntervalSpeedSpotGuidance}구간단속 정보
     */
    public void updateIntervalSafetySpotView(IntervalSpeedSpotGuidance intervalGuidance) {
        if (intervalGuidance == null) {
            speedMeterView.setVisibility(View.GONE);
            intervalInfoLayout.setVisibility(View.GONE);
            averageTextView.setText("");
            setVisibility(View.INVISIBLE);
            return;
        }

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
        } else {
            //방범 카메라 등 목적지 안내에 필요없는 데이터는 보여주지 않는다.
            intervalInfoLayout.setVisibility(View.INVISIBLE);
            setVisibility(View.INVISIBLE);
        }
    }

    private void startCameraWarningAnimation(UTMK coord) {
        if (cameraSubscription != null) {
            return;
        }

        for (Marker item : safetyMarkerList) {
            if (coord.compareTo(item.getPosition()) == 0) {
                warningCamera = item;
                break;
            }
        }

        iconIndex = 0;
        cameraSubscription = rx.Observable.interval(100, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (iconIndex == cameraResourceArray.length) {
                            iconIndex = 0;
                        }
                        warningCamera.setIcon(ResourceDescriptorFactory.fromResource
                                (cameraResourceArray[iconIndex]));
                        iconIndex += 1;
                    }
                });
    }

    private void stopCameraWarningAnimation() {
        if (cameraSubscription == null || warningCamera == null) {
            return;
        }
        cameraSubscription.unsubscribe();
        cameraSubscription = null;
        warningCamera.setIcon(ResourceDescriptorFactory.fromResource(cameraResourceArray[0]));
        warningCamera = null;
    }


    public void releaseMap() {
        clearOverlay();
        gMap = null;
    }

    public void clearOverlay() {
        if (gMap == null) {
            return;
        }
        for (Marker marker : safetyMarkerList) {
            gMap.removeOverlay(marker);
        }
        safetyMarkerList.clear();

        for (Marker marker : accidentMarkerList) {
            gMap.removeOverlay(marker);
        }
        accidentMarkerList.clear();
    }



}
