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
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kt.geom.model.LatLng;
import com.kt.geom.model.UTMK;
import com.kt.maps.model.Point;
import com.kt.maps.model.ResourceDescriptorFactory;
import com.kt.maps.overlay.Marker;
import com.kt.roze.NavigationManager;
import com.kt.roze.RozeError;
import com.kt.roze.RozeOptions;
import com.kt.roze.SoundManager;
import com.kt.roze.data.model.Accident;
import com.kt.roze.data.model.EnergyPrice;
import com.kt.roze.data.model.Lane;
import com.kt.roze.data.model.Route;
import com.kt.roze.guidance.RouteGuidanceListener;
import com.kt.roze.guidance.model.HighwayGuidance;
import com.kt.roze.guidance.model.IntervalSpeedSpotGuidance;
import com.kt.roze.guidance.model.OilPriceGuidance;
import com.kt.roze.guidance.model.SafetySpotGuidance;
import com.kt.roze.guidance.model.Sound;
import com.kt.roze.guidance.model.TurnGuidance;
import com.kt.roze.resource.AccidentResourceManager;
import com.kt.roze.resource.GasStationResourceManager;
import com.kt.roze.routing.RouteSummary;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.MainViewInterface;
import com.kt.rozenavi.ui.main.MapController;
import com.kt.rozenavi.ui.main.UIController;
import com.kt.rozenavi.utils.NaviUtils;
import com.kt.rozenavi.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NavigationView extends RelativeLayout
        implements MainViewInterface, NavigationManager.RouteListener,
        NavigationManager.RerouteListener, NavigationManager.SoundListener {

    /**
     * TBT 표시 View
     */
    @BindView(R.id.tbt_guidance_view)
    protected TbtGuidanceView tbtGuidanceView;
    /**
     * Remain View
     */
    @BindView(R.id.remain_guidance_view)
    protected RemainGuidanceView remainGuidanceView;
    /**
     * Drive Menu(경로저장 , 재탐색 , 경로취소) View
     */
    @BindView(R.id.drive_menu_view)
    protected DriveMenuView driveMenuView;
    /**
     * RoadView Image View
     */
    @BindView(R.id.roadview_imageview)
    protected ImageView roadViewImageView;
    /**
     * 차선 정보 표시 View
     */
    @BindView(R.id.lane_guidance_view)
    protected LaneGuidanceView laneGuidanceView;
    /**
     * 고속도로 정보 표시 View
     */
    @BindView(R.id.highway_guidance_view)
    protected HighWayView highWayView;
    /**
     * 안전운행 정보 표시 View
     */
    @BindView(R.id.spot_guidance_view)
    protected SpotGuidanceView spotGuidanceView;
    /**
     * 최저가 주유소 표시 textview
     */
    @BindView(R.id.oilprice_textview)
    protected TextView oilpriceTextView;


    private List<EnergyPrice> mEnergyPriceList;
    private List<Marker> oilPriceMarkerList = new ArrayList<>();
    private List<Marker> accidentMarkerList = new ArrayList<>();
    private Marker lowPriceOilMarker;

    private RouteSummary routeSummary;
    private int routeIndex;

    private ZoomChanger zoomChanger;
    public NavigationView(Context context) {
        super(context);
        initView(context);
    }

    public NavigationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_route_guidance, this);
        ButterKnife.bind(this);
        setVisibility(GONE);
    }

    @Override
    public void destroy() {
        stop();
    }

    @Override
    public void stop() {
        NavigationManager.getInstance().setRouteGuidanceEventListener(null);
        NavigationManager.getInstance().setRerouteListener(null);
        NavigationManager.getInstance().setSoundListener(null);
        spotGuidanceView.resetView();
        clearPoiMarker();


        setVisibility(INVISIBLE);
    }

    @Override
    public void start() {
        NavigationManager.getInstance().setRouteGuidanceEventListener(routeGuidanceListener);
        NavigationManager.getInstance().setRerouteListener(this);
        NavigationManager.getInstance().setSoundListener(this);
        setVisibility(VISIBLE);
    }

    /**
     * 경로안내중 부가 기능 메뉴 실행
     * 경로저장/재탐색/경로취소의 기능을 제공
     */
    @OnClick(R.id.btn_drive_menu)
    protected void onDriveMenu() {
        toggleDriveMenu();
    }

    /**
     * 부가 메뉴의 표시여부를 토글로 제어
     */
    public void toggleDriveMenu() {
        if (driveMenuView.isShown()) {
            driveMenuView.setVisibility(View.GONE);
        } else {
            driveMenuView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 경로 주행 중 남은 시간 / 거리 표시
     *
     * @param timeInSecond    남은 시간(s)
     * @param distanceInMeter 남은 거리(m)
     */
    public void updateRemain(int timeInSecond, int distanceInMeter) {
        remainGuidanceView.updateRemain(timeInSecond, distanceInMeter);
    }

    /**
     * Road View 표시
     *
     * @param imagePath Iamge File path
     */
    public void updateRoadView(String imagePath) {
        if (imagePath == null) {
            //resource 해지 및 view Gone 처리
            roadViewImageView.setImageBitmap(null);
            roadViewImageView.setVisibility(View.GONE);
            return;
        }
        roadViewImageView.setVisibility(View.VISIBLE);
        Glide.with(getContext()).load(imagePath).into(roadViewImageView);
    }

    /**
     * 안전운행 정보 표시.
     *
     * @param list 표시 거리 이하로 들어온 모든 안전운행 안내점 정보
     */
    public void updateSafetSpotView(boolean isShow, List<SafetySpotGuidance> list) {
        spotGuidanceView.updateSafetySpotView(isShow, list);
    }

    /**
     * Navigation 시작 시 RouteGuidance 관련 View를 초기화.
     * 각 Guider 추가 시 이곳에도 init 부분을 추가해준다.
     */
    public void initGuidanceView() {
        highWayView.setVisibility(View.GONE); //highway
        roadViewImageView.setVisibility(View.GONE); //roadview
        laneGuidanceView.setVisibility(View.GONE);
        spotGuidanceView.resetView();
        oilpriceTextView.setVisibility(View.GONE);
    }

    /**
     * 차선정보 표시
     *
     * @param lane {@link Lane}
     */
    public void updateLanePannel(Lane lane) {
        if (lane == null) {
            laneGuidanceView.setVisibility(View.INVISIBLE);
            return;
        }

        if (laneGuidanceView.updateLanePannel(lane)) {
            laneGuidanceView.setVisibility(View.VISIBLE);
        } else {
            laneGuidanceView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 현재 위치에서 표출하고 있는 차선 정보 해제거리까지의 거리
     *
     * @param distance 거리(m)
     */
    public void updateLaneDistance(int distance) {
        laneGuidanceView.updateLaneDistance(distance);
    }

    /**
     * 고속도로 정보 표시
     * 현재 위치로 부터 옵션에 설정된 갯수 까지 표시 가능
     * {@link RozeOptions#getMaxHighwayGuideCount()}
     *
     * @param guidances 고속도로 안내점 정보
     */
    public void updateHighwayView(List<HighwayGuidance> guidances) {
        highWayView.setHighwayGuidances(guidances);
    }

    /**
     * 현재 위치에서 고속도로 정보의 첫번째 안내점까지의 거리
     *
     * @param distance 거리(m)
     */
    public void updateHighwayDistance(int distance) {
        highWayView.updateDistance(distance);
    }

    /**
     * TBT 정보 표시
     */
    public void updateTBTViews(List<TurnGuidance> guidances) {
        tbtGuidanceView.updateTBTViews(guidances);
        zoomChanger.updateTbt(guidances);
    }

    /**
     * 현재 위치에서 첫번째 TBT 까지의 거리
     *
     * @param distance 거리(m)
     */
    public void updateTBTDistance(int distance) {
        tbtGuidanceView.updateTBTDistance(distance);
        zoomChanger.checkZoomlevel(distance);
    }

    /**
     * 구간단속 카메라 표시
     *
     * @param intervalGuidance 구간단속 시작/종료 객체
     */
    public void updateIntervalSafetySpotView(IntervalSpeedSpotGuidance intervalGuidance) {
        spotGuidanceView.updateIntervalSafetySpotView(intervalGuidance);
    }

    /**
     * 경로안내중 재탐색 요청
     * 경로이탈이나 경로에 진입실패시 요청
     *
     * @param location 현재 위치 좌표
     * @param mode     요청 모드
     */
    private void requestReRoute(Location location, NavigationManager.RouteMode mode) {
        if (location == null) {
            return;
        }
        UTMK utmk = UTMK.valueOf(new LatLng(location.getLatitude(), location.getLongitude()));
        NavigationManager.getInstance()
                .reroute(utmk, NavigationManager.getInstance().getLastBearing(), mode);
    }

    /**
     * 경로안내 요청
     * 경로검색을 통해 생성된 경로요약정보와 선택한 경로index를 이용하여 경로안내 요청
     * 리스너를 이용해 시작/실패 여부를 반환
     *
     * @param routeSummary 경로 요약정보
     * @param routeIndex   경로 index
     */
    public void requestStartNavigation(RouteSummary routeSummary, int routeIndex) {
        this.routeSummary = routeSummary;
        this.routeIndex = routeIndex;

        routeSummary.setActiveRoute(routeIndex);
        NavigationManager.getInstance().startRouting(routeSummary, this);
        zoomChanger = new ZoomChanger(routeSummary.getActiveRoute());
    }

    /**
     * 최저가 주유소 마커 생성
     *
     * @param energyPriceList 최저가 주유소 정보 리스트
     */
    private void createOilMarker(List<EnergyPrice> energyPriceList) {
        if (energyPriceList == null || energyPriceList.isEmpty()) {
            return;
        }
        this.mEnergyPriceList = energyPriceList;
        Marker oilMarker;
        for (EnergyPrice energyPrice : energyPriceList) {
            oilMarker = new Marker();
            oilMarker.setPosition(energyPrice.coord);
            oilMarker.setAnchor(new Point(1.0, 1.0));
            int resId = GasStationResourceManager.getPoiGasResourceID((short) energyPrice.brand);
            if (resId > 0) {
                oilMarker.setIcon(ResourceDescriptorFactory.fromResource(resId));
                oilMarker.setIconSize(new Point(30, 38));
            }
            oilMarker.setCaption(getEnergyPrice(energyPrice));
            oilPriceMarkerList.add(oilMarker);
            MapController.getInstance().addMarker(oilMarker);
        }
    }

    private String getEnergyPrice(EnergyPrice energyPrice) {
        int gasolinePrice = 0;
        int diselPrice = 0;
        int lpgPrice = 0;
        for (int i = 0, size = energyPrice.energyTypes.size(); i < size; i++) {
            EnergyPrice.EnergyType type = energyPrice.energyTypes.get(i);

            switch (type) {
                case GASOLINE:
                    gasolinePrice = energyPrice.energyPrices.get(i);
                    break;
                case DIESEL:
                    diselPrice = energyPrice.energyPrices.get(i);
                    break;
                case LPG:
                    lpgPrice = energyPrice.energyPrices.get(i);
                    break;
            }
        }
        return "G:" + gasolinePrice + ",D:" + diselPrice + ",L:" + lpgPrice;
    }

    /**
     * 유고 정보 마커 생성
     *
     * @param accidentList 유고 정보 리스트
     */
    private void createAccidentMarker(List<Accident> accidentList) {
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
            MapController.getInstance().addMarker(accidentMarker);
        }
    }

    /**
     * poi 정보 마커 삭제
     * 최저가 주유소, 유고 정보 등의 정보 마커를 삭제
     */
    private void clearPoiMarker() {
        //marker clear
        for (Marker marker : oilPriceMarkerList) {
            MapController.getInstance().removeMarker(marker);
        }
        oilPriceMarkerList.clear();

        if (lowPriceOilMarker != null) {
            MapController.getInstance().removeMarker(lowPriceOilMarker);
            lowPriceOilMarker = null;
        }

        for (Marker marker : accidentMarkerList) {
            MapController.getInstance().removeMarker(marker);
        }
        accidentMarkerList.clear();
    }

    @Override
    public void onRouteStarted() {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                Route route = routeSummary.routes.get(0);
                MapController.getInstance().drawRoute(route);

                initGuidanceView();
                updateRemain(route.time, route.distance);

                UIController.getInstance().getDriveView().carMarkerChange();
                UIController.getInstance().setMode(UIController.MODE_NAVIGATION);
                UIUtils.dismissProgressDialog();

                //경로상 주유소 및 사고상황 표시
                createOilMarker(NavigationManager.getInstance().getOilPricePOIList());
                createAccidentMarker(NavigationManager.getInstance().getAccidentPOIList());
            }
        });
    }

    @Override
    public void onRouteStartFail(RozeError error) {
        UIUtils.dismissProgressDialog();
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                UIUtils.showToast(getContext(), R.string.toast_message_navigation_start_fail);
                UIController.getInstance().setMode(UIController.MODE_DRIVE);
            }
        });
    }

    @Override
    public void onTrafficUpdate() {
        UIUtils.showToast(getContext(), R.string.toast_message_navigation_traffic_update);
    }

    /**
     * 목적지 및 경유지 도착 시의 Action 지정
     *
     * @param arrivedIndex {@code -1} 인경우 최종 목적지 {@code 0}이상인 경우 경유지의 인덱스
     */
    @Override
    public void onArrived(short arrivedIndex) {
        if (arrivedIndex == -1) { // 목적지 도착

            UIUtils.showToast(getContext(), R.string.toast_message_navigation_arrived_destination);

            try {
                NavigationManager.getInstance().startTracking();
            } catch (Exception e) {
                e.printStackTrace();
            }
            UIController.getInstance().setMode(UIController.MODE_DRIVE);

        } else {
            // 경유지 도착
            UIUtils.showToast(getContext(), R.string.toast_message_navigation_arrived_waypoint);
        }
    }

    @Override
    public void onRouteDeviated(Location location) {
        UIUtils.showToast(getContext(), R.string.toast_message_location_deviated);
        requestReRoute(location, NavigationManager.RouteMode.DEVIATED_REROUTE);
    }

    @Override
    public void onRouteDidNotEnter(Location location) {
        UIUtils.showToast(getContext(), R.string.toast_message_location_did_not_enter);
        requestReRoute(location, NavigationManager.RouteMode.DID_NOT_ENTER_REROUTE);
    }

    /**
     * 경로 안내 UI 표시 용 정보를 수신하기 위한 Listener
     * {@link RouteGuidanceListener}
     */
    private RouteGuidanceListener routeGuidanceListener = new RouteGuidanceListener() {
        @Override
        public void onLaneChangedEvent(Lane lane) {
            updateLanePannel(lane);
        }

        @Override
        public void onLaneDistanceChangedEvent(int distance) {
            super.onLaneDistanceChangedEvent(distance);
            updateLaneDistance(distance);
        }

        @Override
        public void onRoadViewChangedEvent(String path) {
            super.onRoadViewChangedEvent(path);
            updateRoadView(path);
        }

        @Override
        public void onHighwayChangedEvent(List<HighwayGuidance> highwayGuidances) {
            super.onHighwayChangedEvent(highwayGuidances);
            updateHighwayView(highwayGuidances);
        }

        @Override
        public void onHighwayDistanceEvent(int distance) {
            super.onHighwayDistanceEvent(distance);
            updateHighwayDistance(distance);
        }

        @Override
        public void onTurnChangedEvent(List<TurnGuidance> turnGuidances) {
            super.onTurnChangedEvent(turnGuidances);
            updateTBTViews(turnGuidances);
        }

        @Override
        public void onTurnDistanceChangedEvent(int distance) {
            super.onTurnDistanceChangedEvent(distance);
            updateTBTDistance(distance);
        }

        @Override
        public void onRemainChangedEvent(int timeInSecond, int distanceInMeter) {
            super.onRemainChangedEvent(timeInSecond, distanceInMeter);
            updateRemain(timeInSecond, distanceInMeter);
        }

        @Override
        public void onSafetySpotChangedEvent(
                boolean isShow, List<SafetySpotGuidance> safetySpotGuidance) {
            super.onSafetySpotChangedEvent(isShow, safetySpotGuidance);
            updateSafetSpotView(isShow, safetySpotGuidance);
        }

        @Override
        public void onIntervalSafetySpotChangedEvent(IntervalSpeedSpotGuidance intervalGuidance) {
            super.onIntervalSafetySpotChangedEvent(intervalGuidance);
            updateIntervalSafetySpotView(intervalGuidance);
        }

        @Override
        public void onLowestGasStationChangedEvent(boolean isShow, List<OilPriceGuidance> list) {
            super.onLowestGasStationChangedEvent(isShow, list);
            //최저가 주유소 표출 및 해제
            if (isShow) {
                EnergyPrice lowPrice = list.get(0).price;
                EnergyPrice.EnergyType energyType = RozeOptions.getInstance().getEnergyType();

                oilpriceTextView.setText(
                        energyType.name() + "최저가 주유소까지 " + NaviUtils.convertDistanceUnit(
                                list.get(0).getRemainDistance()) + "남았습니다.");
                for (int i = 0, size = mEnergyPriceList.size(); i < size; i++) {
                    if (mEnergyPriceList.get(i).id == lowPrice.id) {
                        lowPriceOilMarker = oilPriceMarkerList.get(i);
                        lowPriceOilMarker.setIconSize(new Point(44, 57));
                        lowPriceOilMarker.bringToFront();
                        break;
                    }
                }

                oilpriceTextView.setVisibility(View.VISIBLE);
            } else {
                if (lowPriceOilMarker != null) {
                    lowPriceOilMarker.setIconSize(new Point(30, 38));
                }
                oilpriceTextView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onLowestGasStationDistanceChangedEvent(int distance) {
            super.onLowestGasStationDistanceChangedEvent(distance);
            //최저가 주유소 거리 갱신
            EnergyPrice.EnergyType energyType = RozeOptions.getInstance().getEnergyType();
            oilpriceTextView.setText(
                    energyType.name() + "최저가 주유소까지 " + NaviUtils.convertDistanceUnit(distance) +
                            "남았습니다.");
        }
    };

    @Override
    public void onRerouteBegin(NavigationManager.RouteMode mode) {
        if (mode == NavigationManager.RouteMode.AUTO_REROUTE) {
            UIUtils.showToast(getContext(), R.string.toast_message_reroute_auto);
        }
    }

    @Override
    public void onRerouteEnd(NavigationManager.RouteMode mode, RouteSummary routeSummary) {
        this.routeSummary = routeSummary;
        routeSummary.setActiveRoute(routeIndex);
        Route route = routeSummary.routes.get(0);
        MapController.getInstance().drawRoute(route);
        zoomChanger.setRoute(route);

        clearPoiMarker();
        //경로상 주유소 및 사고상황 표시
        createOilMarker(NavigationManager.getInstance().getOilPricePOIList());
        createAccidentMarker(NavigationManager.getInstance().getAccidentPOIList());

        initGuidanceView();
        updateRemain(route.time, route.distance);

        UIController.getInstance().getDriveView().carMarkerChange();
    }

    @Override
    public void onRerouteFailed(NavigationManager.RouteMode mode, RozeError error) {
        UIUtils.showToast(getContext(), R.string.toast_message_route_fail);
		//since : sdk 0.9.3
		//사용자 재탐색 / 자동 재탐색(주기적) 일 경우 실패 시 기존경로를 이용하여 주행하도록 변경.
		if (mode.isReusePreviousRoute()) {
            return;
		}
        UIController.getInstance().setMode(UIController.MODE_DRIVE);
    }

   @Override
    public void onSoundStart(SoundManager soundManager, Sound sound) {
        soundManager.play(sound);
    }

    @Override
    public void onExceedSoundEvent(SoundManager soundManager, Sound sound) {
        if (sound == null) {
            //since : sdk 0.9.3
            //반복음 사운드 재생종료 인터페이스 변경
            soundManager.stopExceedSound();
        } else {
            soundManager.playExceedSound(sound);
        }
    }

    @Override
    public void onSoundEnd() {

    }

}
