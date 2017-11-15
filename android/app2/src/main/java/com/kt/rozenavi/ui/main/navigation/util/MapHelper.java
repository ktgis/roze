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

package com.kt.rozenavi.ui.main.navigation.util;

import android.graphics.Color;

import com.kt.geom.model.UTMK;
import com.kt.maps.GMap;
import com.kt.maps.model.Point;
import com.kt.maps.model.ResourceDescriptorFactory;
import com.kt.maps.overlay.Marker;
import com.kt.maps.overlay.MarkerOptions;
import com.kt.maps.overlay.Overlay;
import com.kt.maps.overlay.Path;
import com.kt.roze.data.model.Accident;
import com.kt.roze.data.model.EnergyPrice;
import com.kt.roze.data.model.Link;
import com.kt.roze.data.model.Turn;
import com.kt.roze.guidance.RGType;
import com.kt.roze.guidance.model.SafetySpotGuidance;
import com.kt.roze.resource.AccidentResourceManager;
import com.kt.roze.resource.GasStationResourceManager;
import com.kt.rozenavi.utils.CommonUtils;
import com.kt.rozenavi.utils.MapUtils;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * Navigation View 관련 Map 을 이용한 UI Setting 을 도와준다.
 * //TODO Destroy 시점 확인 필요.
 */

public class MapHelper {
    private static MapHelper instance;

    public static MapHelper getInstance() {
        if (instance == null) {
            instance = new MapHelper();
        }
        return instance;
    }

    public static void destroy() {
        instance = null;
    }

    /**
     * 경로 선 상에 TBT Arrow를 추가한다.
     *
     * @param gMap      Map 객체
     * @param turn      추가 대상 Turn
     * @param turnArrow 이전에 그렸던(제거 해야할) Turn Arrow
     * @return 신규로 생성한 Turn Arrow
     */
    public Path drawTBTPath(GMap gMap, Turn turn, Path turnArrow, List<Link> links) {
        removeOverlay(gMap, turnArrow);
        if (gMap == null || turn == null) {
            return null;
        }
        Path tbtPath = createTbtPath(gMap, turn, links);
        if (tbtPath != null) {
            gMap.addOverlay(tbtPath);
        }
        return tbtPath;
    }

    /**
     * 경로 상의 TBT Arrow 표시 제거
     */
    public void removeOverlay(GMap gMap, Overlay overlay) {
        if (gMap == null || overlay == null) {
            return;
        }
        gMap.removeOverlay(overlay);
    }

    /**
     * rp상에 포함된 turn정보를 지도 경로상에 표현할 tbt overlay로 생성
     */
    private Path createTbtPath(GMap gMap, Turn turn, List<Link> links) {
        Path arrow;
        List<UTMK> point;
        point = getTbtPathPoint(gMap, turn, links);

        if (point == null) {
            return null;
        }

        arrow = new Path();
        arrow.setPoints(point);
        arrow.setHasArrow(true);
        arrow.setStrokeColor(Color.argb(200, 0, 0, 0));
        arrow.setStrokeWidth(2);
        arrow.setFillColor(Color.argb(225, 255, 255, 255));
        //줌레벨 9.5 미만은 지도에 표시하지 않음
        arrow.setVisible(gMap.getViewpoint().zoom >= 9.5);
        //줌레벨 11 미만은 bufferwidth를 작게 변경
        if (gMap.getViewpoint().zoom < 11) {
            arrow.setBufferWidth(gMap.getResolution() * 2);
        } else {
            arrow.setBufferWidth(gMap.getResolution() * MapUtils.TBT_PATH_WIDTH_IN_DP);
        }
        return arrow;
    }

    /**
     * tbt overlay 좌표list 생성
     * link list에서 입력된 turn에 해당하는 tbt overlay를 생성할 수 있도록
     * 좌표 list를 반환
     *
     * @param turn tbt overlay를 생성할 turn 정보
     * @return 좌표 리스트
     */
    private List<UTMK> getTbtPathPoint(GMap gMap, Turn turn, List<Link> links) {
        List<UTMK> coords = new ArrayList<>();

        //tbt정보를 생성할수 없을때 null 리턴
        if (links == null || links.size() == 0 || turn == null || links.size() <= turn.linkIndex ||
                links.size() <= (turn.linkIndex + 1)) {
            return null;
        }

        int maxLength;
        if (gMap.getViewpoint().zoom < 11) {
            maxLength = (int) (30 * gMap.getResolution());
        } else {
            maxLength = (int) (50 * gMap.getResolution());
        }
        if (turn.type == RGType.STARTING_POINT || turn.type == RGType.DESTINATION ||
                turn.type == RGType.GO_STRAIGHT || turn.type == RGType.TG || turn.type == RGType.IC
                || turn.type == RGType.JC) {
            //일반 직진, TG, IC, JC tbt생성 안함
            //tbt path 생성안하는
            return null;
        } else if (turn.type == RGType.U_TURN) {
            //기준이 될 좌표 설정
            int uturnLength = links.get(turn.linkIndex + 1).length;
            if (uturnLength < 30) {
                //u turn이기 때문에 링크 전체를 설정
                coords.addAll(links.get(turn.linkIndex + 1).getNodes());
                return makeTbtPathPoint(coords, links, turn.linkIndex + 2, turn.linkIndex,
                        maxLength / 2);
            }
        }
        //기준이 될 좌표 설정
        coords.add(links.get(turn.linkIndex).getLastNode());
        return makeTbtPathPoint(coords, links, turn.linkIndex + 1, turn.linkIndex, maxLength / 2);
    }

    /**
     * tbt overlay point 구성
     * tbt 발생위치를 기준으로 이전링크 앞링크를 interval 에 맞추어 검색하여 point를 추가
     *
     * @param coords         point 가 포함될 list
     * @param frontLinkIndex 앞 링크 인덱스
     * @param backLinkIndex  이전 링크 인덱스
     * @param pathInterval   앞/뒤 화살표의 길이
     * @return tbt overlay point list
     */
    private List<UTMK> makeTbtPathPoint(List<UTMK> coords, List<Link> links, int frontLinkIndex
            , int backLinkIndex, int pathInterval) {
        //꼬리 부분 생성후 list의 앞에 추가
        coords.addAll(0, makeTbtArrowBackPointList(links, backLinkIndex, pathInterval));
        //화살표 부분 생성후 list의 뒤에 이어서 추가
        coords.addAll(makeTbtArrowFrontPointList(links, frontLinkIndex, pathInterval));
        //생성된 tbt point list 반환
        return coords;
    }

    /**
     * TBT overlay 화살표 부분 생성 path point 구성
     *
     * @param linkIndex tbt 위치 linkindex
     * @param interval  tbt overlay 화살표 부분 길이
     * @return 좌표 리스트
     */
    private List<UTMK> makeTbtArrowFrontPointList(List<Link> links, int linkIndex, int interval) {
        List<UTMK> coords = new ArrayList<>();
        //화살표 좌표 추가
        //node 이동 인덱스와 초기 비교 노드 설정
        UTMK point1 = links.get(linkIndex).getNode(0);
        UTMK point2 = links.get(linkIndex).getNode(1);
        double distance;
        while (linkIndex < (links.size() - 1) && interval > links.get(linkIndex).length) {
            //링크의 전체 노드 추가
            coords.addAll(links.get(linkIndex).getNodes());
            //남은길이를 링크의 길이만큼 제외
            interval = interval - links.get(linkIndex).length;
            //앞링크 인덱스를 하나 뒤로 이동
            linkIndex = linkIndex + 1;
            //앞으로 더 넘어갈수 없을때는 반복문 종료
            if (linkIndex == links.size() - 1) {
                break;
            }
        }
        //tbt 가 링크의 일부분만 구성되는 경우 처리
        for (int i = 0, size = links.get(linkIndex).getNodeSize() - 1; i < size; i++) {
            point1 = links.get(linkIndex).getNode(i);
            point2 = links.get(linkIndex).getNode(i + 1);
            //노드간 거리 체크
            distance = point1.distanceTo(point2);
            //남은 길이보다 길 경우 반복문 종료
            if (interval < distance) {
                break;
            }
            //노드를 추가
            coords.add(point2);
            //남은 길이에서 노드간 거리를 제외
            interval = (int) (interval - distance);
        }
        // 반복문이 종료된 후 남은 길이가 있는경우 노드 방향으로 남은거리만큼의 임의의 노드를 계산
        if (interval > 0) {
            int angle = (int) point2.angleTo(point1);
            //진행방향의 끝으로 지정
            coords.add(NaviUtils.getPointOverLineDistance((short) angle, point1, interval));
        }
        return coords;
    }

    /**
     * TBT overlay 꼬리 부분 생성 path point 구성
     *
     * @param linkIndex tbt 위치 linkindex
     * @param interval  tbt overlay 화살표 부분 길이
     * @return 좌표 리스트
     */
    private List<UTMK> makeTbtArrowBackPointList(List<Link> links, int linkIndex, int interval) {
        List<UTMK> coords = new ArrayList<>();
        //꼬리 좌표 추가
        //node 초기 비교 노드 설정
        UTMK point1 = links.get(linkIndex).getLastNode();
        UTMK point2 = links.get(linkIndex).getNode(links.get(linkIndex).getNodeSize() - 2);
        double distance;
        //링크 전체가 tbt 구간에 포함되는경우 반복문으로 확인 처리
        while (linkIndex > 0 && interval > links.get(linkIndex).length) {
            //링크의 전체 노드 추가
            coords.addAll(0, links.get(linkIndex).getNodes());
            //남은길이를 링크의 길이만큼 제외
            interval = interval - links.get(linkIndex).length;
            //이전링크 인덱스를 하나 뒤로 이동
            linkIndex = linkIndex - 1;
            //뒤로 더 넘어갈수 없을때는 반복문 종료
            if (linkIndex == 0) {
                break;
            }
        }
        //tbt 가 링크의 일부분만 구성되는 경우 처리
        for (int i = links.get(linkIndex).getNodeSize() - 1; i < 0; i--) {
            point1 = links.get(linkIndex).getNode(i);
            point2 = links.get(linkIndex).getNode(i - 1);
            //노드간 거리 체크
            distance = point1.distanceTo(point2);
            //남은 길이보다 길 경우 반복문 종료
            if (interval < distance) {
                break;
            }
            //노드를 추가
            coords.add(0, point2);
            //남은 길이에서 노드간 거리를 제외
            interval = (int) (interval - distance);
        }
        // 반복문이 종료된 후 남은 길이가 있는경우 노드 방향으로 남은거리만큼의 임의의 노드를 계산
        if (interval > 0) {
            int angle = (int) point2.angleTo(point1);
            //진행방향의 시작점으로 지정
            coords.add(0, NaviUtils.getPointOverLineDistance((short) angle, point1, interval));
        }
        return coords;
    }

    /**
     * 유고 정보를 Map 상에 표출한다
     *
     * @param gMap         맵 객체
     * @param accidentList 유고 정보 리스트
     * @return 표시한 유고 marker 목록
     */
    public List<Marker> setAccidentList(GMap gMap, List<Accident> accidentList) {
        if (gMap == null || CommonUtils.isEmpty(accidentList)) {
            return null;
        }
        List<Marker> accidentMarkerList = new ArrayList<>();
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
        return accidentMarkerList;
    }

    public List<Marker> addSpotMarker(GMap gMap, List<SafetySpotGuidance> spotList) {
        List<Marker> safetyMarkerList = new ArrayList<>();
        Marker marker;
        UTMK coord;

        if (gMap == null || CommonUtils.isEmpty(spotList)) {
            return safetyMarkerList;
        }

        List<Marker> addMarkerList = new ArrayList<>();
        for (SafetySpotGuidance spot : spotList) {
            marker = null;
            coord = spot.getCoord();
            for (Marker item : safetyMarkerList) {
                if (coord.compareTo(item.getPosition()) == 0) {
                    marker = item;
                }
            }
            if (marker == null) {
                createSpotMarker(gMap, spot, addMarkerList);
            }
        }
        safetyMarkerList.addAll(addMarkerList);
        return safetyMarkerList;
    }

    private void createSpotMarker(GMap gMap, SafetySpotGuidance spot, List<Marker> addMarkerList) {
        if (gMap == null) {
            return;
        }

        int resourceId = NaviUtils.getRgTypeImage(spot.getType());
        if (resourceId > 0) {
            MarkerOptions option = new MarkerOptions();
            option.position(spot.getCoord());
            option.icon(ResourceDescriptorFactory.fromResource(resourceId));
            option.anchor(new Point(0.5, 0.5));
            int iconSize = NaviUtils.getRgTypeIconSize(spot.getType());
            option.iconSize(new Point(iconSize, iconSize));
            Marker marker = new Marker(option);
            addMarkerList.add(marker);
            gMap.addOverlay(marker);
        }
    }

    /**
     * 최저가 주유소 정보 리스트 설정
     */
    public List<Marker> setOilMarkers(GMap gMap, List<EnergyPrice> gasPriceList) {
        List<Marker> gasPriceMarkerList = new ArrayList<>();
        if (gMap == null || CommonUtils.isEmpty(gasPriceList)) {
            return gasPriceMarkerList;
        }
        Marker oilMarker;
        for (EnergyPrice energyPrice : gasPriceList) {
            oilMarker = new Marker();
            oilMarker.setPosition(energyPrice.coord);
            oilMarker.setAnchor(new Point(1.0, 1.0));
            int resId = GasStationResourceManager.getPoiGasResourceID((short) energyPrice.brand);
            if (resId > 0) {
                oilMarker.setIcon(ResourceDescriptorFactory.fromResource(resId));
                oilMarker.setIconSize(new Point(30, 38));
            }
            oilMarker.setCaption(getEnergyPrice(energyPrice));
            gasPriceMarkerList.add(oilMarker);
            gMap.addOverlay(oilMarker);
        }
        return gasPriceMarkerList;
    }

    /**
     * 주유소 가격 표시 용
     * //TODO 디자인 받아서 고쳐야 할것
     *
     * @param energyPrice 주유소 정보 객체
     * @return 가격 정보
     */
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


    private int iconIndex = 0;

    /**
     * 지도의 특정 Marker 에 Frame Animation 효과를 준다
     *
     * @param marker        대상 maker
     * @param resourceArray frame animation resources
     * @return rx observable
     */
    public Disposable startMarkerFrameAnimation(final Marker marker, final int[] resourceArray) {
        if (marker == null) {
            return null;
        }
        iconIndex = 0;
        return Observable.interval(100, TimeUnit.MILLISECONDS)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (iconIndex >= resourceArray.length || iconIndex < 0) {
                            iconIndex = 0;
                        }
                        marker.setIcon(ResourceDescriptorFactory.fromResource
                                (resourceArray[iconIndex]));
                        iconIndex += 1;
                    }
                });
    }

    /**
     * 지도의 마커를 제거한다
     *
     * @param gMap     맵 객체
     * @param overlays 대상 마커
     */

    public void removeOverlays(GMap gMap, List<? extends Overlay> overlays) {
        if (overlays == null || gMap == null) {
            return;
        }
        for (Overlay o : overlays) {
            removeOverlay(gMap, o);
        }
    }
}
