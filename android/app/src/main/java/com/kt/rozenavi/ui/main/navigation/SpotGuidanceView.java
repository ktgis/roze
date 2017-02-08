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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.geom.model.UTMK;
import com.kt.maps.model.Point;
import com.kt.maps.model.ResourceDescriptorFactory;
import com.kt.maps.overlay.Marker;
import com.kt.maps.overlay.MarkerOptions;
import com.kt.roze.guidance.RGType;
import com.kt.roze.guidance.model.IntervalSpeedSpotGuidance;
import com.kt.roze.guidance.model.SafetySpotGuidance;
import com.kt.roze.resource.RoadSignResourceManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.MapController;
import com.kt.rozenavi.ui.main.UIController;
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
public class SpotGuidanceView extends RelativeLayout {

    /**
     * Speed Cam 및 Warning Sign(낙석, 안개 등) View
     */
    @BindView(R.id.spped_camera_imageview)
    protected ImageView speedCameraImageView;
    @BindView(R.id.speed_carmera_layout)
    protected LinearLayout speedCameraLayout;
    @BindView(R.id.speed_camera_textview)
    protected TextView speedCameraTextView;
    @BindView(R.id.speed_limit_layout)
    protected LinearLayout speedlimitlayout;

    /**
     * 구간단속 카메라 View
     */
    @BindView(R.id.interval_sign_layout)
    protected LinearLayout intervalSignLayout;
    @BindView(R.id.interval_camera_imageview)
    protected ImageView intervalCameraImageView;
    @BindView(R.id.interval_limit_layout)
    protected LinearLayout intervallimitlayout;
    @BindView(R.id.interval_average_textview)
    protected TextView intervalAverageTextView;
    @BindView(R.id.interval_timer_textview)
    protected TextView intervalTimerTextView;
    @BindView(R.id.interval_remainDistance_textview)
    protected TextView intervalRemainDistanceTextView;

    private List<Marker> markerList = new ArrayList<>();

    private Subscription subscription;
    private static int remainIntervalTime = -1;

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
    private Marker warningCamera;

    public SpotGuidanceView(Context context) {
        super(context);
        initView(context);
    }

    public SpotGuidanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SpotGuidanceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_guidance_spot, this);
        ButterKnife.bind(this);
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

            addSpotMarker(list);

            if (item != null) {
                setSafetyImage(item);
                if (item.safetySpot.getLimitSpeed() <
                        UIController.getInstance().getDriveView().currentSpeed) {
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
            removeSpotMarker(list);
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
            for (Marker item : markerList) {
                if (coord.compareTo(item.getPosition()) == 0) {
                    marker = item;
                }
            }
            if (marker == null) {
                createSpotMarker(spot, addMarkerList);
            }
        }
        markerList.addAll(addMarkerList);
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
            MapController.getInstance().addMarker(marker);
        }
    }

    private void removeSpotMarker(List<SafetySpotGuidance> spotList) {
        if (spotList == null || spotList.size() == 0) {
            for (Marker marker : markerList) {
                MapController.getInstance().removeMarker(marker);
            }
            markerList.clear();
        } else {
            //현재 마커리스트를 복제한다
            List<Marker> lastMarkerList = new ArrayList<>();
            lastMarkerList.addAll(markerList);

            UTMK coord;
            for (SafetySpotGuidance spot : spotList) {
                coord = spot.safetySpot.getCoord();
                for (Marker item : markerList) {
                    if (coord.compareTo(item.getPosition()) == 0) {
                        MapController.getInstance().removeMarker(item);
                        //삭제되어야 하는 마커를 복제된 리스트에서 제거
                        lastMarkerList.remove(item);
                    }
                }
            }
            //기존 리스트를 정리한뒤 새로운 리스트로 대치
            markerList.clear();
            markerList = lastMarkerList;
        }
    }

    /**
     * 안전운행 안내점 Image를 View에 표시한다.
     *
     * @param spot 표시 대상 SafetySpot Guidance
     */
    private void setSafetyImage(SafetySpotGuidance spot) {
        if (spot == null) {
            speedCameraLayout.setVisibility(View.GONE);
            return;
        }

        int distance = spot.getRemainDistance();
        int resId = RoadSignResourceManager.getResourceId(spot.safetySpot.type);
        if (resId != RoadSignResourceManager.RESOURCE_NOT_FOUND) {
            speedCameraImageView.setImageResource(resId);
            speedCameraTextView.setText(NaviUtils.convertDistanceUnit(distance));
            speedCameraLayout.setVisibility(View.VISIBLE);
            if (RGType.isSpeedCamera(spot.safetySpot.type)) {
                setSpeed(spot.safetySpot.getLimitSpeed(), speedlimitlayout);
            } else {
                speedlimitlayout.setVisibility(View.GONE);
            }
        } else {
            //방범 카메라 등 목적지 안내에 필요없는 데이터는 보여주지 않는다.
            speedCameraLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 제한 속도를 View에 추가한다.
     *
     * @param speed  도로의 제한속도(km/h)
     * @param layout 제한속도가 표시될 View
     */
    public void setSpeed(int speed, LinearLayout layout) {
        int resId;
        int number;
        ImageView numberImage;
        //1자리는 항상 표시되어야 하므로 속도가 0일때도 무조건 처리
        //10으로 나눈 나머지에 해당하는 이미지 입력
        resId = NaviUtils.getNumberResourceId(speed % 10);
        numberImage = (ImageView) layout.getChildAt(layout.getChildCount() - 1);
        numberImage.setImageResource(resId);

        //10의 자리 이후는 값이 없는경우는 빈칸처리
        for (int i = 1; i < layout.getChildCount(); i++) {
            //10의 n승으로 자리수를 체크
            number = (int) (speed / Math.pow(10, i));
            //가장 큰 자리수가 0번째이므로 뒤에서 부터 순차적으로 입력
            numberImage = (ImageView) layout.getChildAt((layout.getChildCount() - 1) - i);
            //자리수의 값이 0이면 빈칸처리
            if (number == 0) {
                numberImage.setImageDrawable(null);
            } else {
                //자리수의 값이 0이 아니면 10으로 나눈 나머지에 해당하는 이미지 입력
                numberImage.setImageResource(NaviUtils.getNumberResourceId(number % 10));
            }
            layout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 구간단속 정보를 화면에 표시한다.
     *
     * @param intervalGuidance {@link IntervalSpeedSpotGuidance}구간단속 정보
     */
    public void updateIntervalSafetySpotView(IntervalSpeedSpotGuidance intervalGuidance) {
        if (intervalGuidance == null) {
            if (subscription != null) {
                subscription.unsubscribe();
                subscription = null;
            }
            intervalSignLayout.setVisibility(View.GONE);
            return;
        }

        int resId = RoadSignResourceManager.getResourceId(RGType.CAM_SPEED);
        if (resId != RoadSignResourceManager.RESOURCE_NOT_FOUND) {
            intervalCameraImageView.setImageResource(resId);
            int speed = (int) intervalGuidance.getAverageSpeed();
            NaviUtils.setSizeSpanDistance(intervalRemainDistanceTextView,
                    intervalGuidance.getLastRemainDistance());
            intervalAverageTextView.setText("AV : " + speed + " Km/h");
            if (subscription == null) {
                updateIntervalTimer(intervalGuidance.getMinimumDriveTime());
            }
            intervalSignLayout.setVisibility(View.VISIBLE);
            setSpeed(intervalGuidance.getLimitSpeed(), intervallimitlayout);
        } else {
            //방범 카메라 등 목적지 안내에 필요없는 데이터는 보여주지 않는다.
            intervalCameraImageView.setVisibility(View.GONE);
        }
    }

    /**
     * 구간단속 카메라 시작 시 제한속도로 단속 구간을 달렸을 때의 시간이 전달된다.</br>
     * 전달된 시간값에서 1초당 1씩 감소 시키며 화면에 보여준다.
     *
     * @param remainTime 최소 주행 시간
     */
    private void updateIntervalTimer(int remainTime) {
        remainIntervalTime = remainTime;
        setIntervalRemainTimeText(remainTime);
        subscription = rx.Observable.interval(1000, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        remainIntervalTime -= 1;
                        setIntervalRemainTimeText(remainIntervalTime);
                        if (remainIntervalTime <= 0) {
                            if (subscription != null) {
                                subscription.unsubscribe();
                                subscription = null;
                            }
                        }

                    }
                });

    }

    private void startCameraWarningAnimation(UTMK coord) {
        if (cameraSubscription != null) {
            return;
        }

        for (Marker item : markerList) {
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


    private void setIntervalRemainTimeText(int remainTime) {
        if (remainTime < 0) {
            intervalTimerTextView.setText("00:00");
            return;
        }

        intervalTimerTextView.setText(NaviUtils.convertRemainTimeWithSec(remainTime));
    }

    /**
     * 안전운행 안내점 정보와 경로선 상의 안전운행 관련 Icon을 해제한다.
     */
    public void resetView() {
        speedCameraLayout.setVisibility(View.GONE);
        removeSpotMarker(null);
        stopCameraWarningAnimation();
        updateIntervalSafetySpotView(null);
    }

}
