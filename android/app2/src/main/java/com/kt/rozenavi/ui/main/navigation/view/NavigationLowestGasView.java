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

package com.kt.rozenavi.ui.main.navigation.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.maps.GMap;
import com.kt.maps.model.Point;
import com.kt.maps.overlay.Marker;
import com.kt.roze.RozeOptions;
import com.kt.roze.data.model.EnergyPrice;
import com.kt.roze.guidance.model.OilPriceGuidance;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.navigation.util.MapHelper;
import com.kt.rozenavi.utils.CommonUtils;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 최저가 주유소 정보 View
 */
public class NavigationLowestGasView extends RelativeLayout {
    @BindView(R.id.lowest_gas_textview)
    protected TextView lowestGasTextView;

    private GMap gMap;
    private List<EnergyPrice> gasPriceList;
    private List<Marker> gasPriceMarkerList = new ArrayList<>();
    private Marker lowestGasMarker;

    private MapHelper mapHelper = MapHelper.getInstance();

    public NavigationLowestGasView(Context context) {
        super(context);
        initView(context);
    }

    public NavigationLowestGasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NavigationLowestGasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_navigation_lowest_gas, this);
        ButterKnife.bind(this);
    }

    public void initMap(GMap gMap) {
        this.gMap = gMap;
    }

    /**
     * 최저가 주유소 정보 리스트 설정
     */
    public void setLowestGasStationList(List<EnergyPrice> gasPriceList) {
        if (CommonUtils.isEmpty(gasPriceList)) {
            return;
        }
        this.gasPriceList = gasPriceList;
        gasPriceMarkerList = mapHelper.setOilMarkers(gMap, gasPriceList);
    }

    public void updateLowestGasStation(boolean isShow, List<OilPriceGuidance> list) {
        if (isShow) {
            //Since 1.3.0 주유소 정보에 직접 접근하던 부분을 get func 을 통해 접근하도록 변경했습니다.
            EnergyPrice lowPrice = list.get(0).getPrice();
            EnergyPrice.EnergyType energyType = RozeOptions.getInstance().getEnergyType();
            lowestGasTextView.setText(
                    getResources().getText(NaviUtils.getEnergyTypeStringRes(energyType)) +
                            "최저가 주유소까지 " + NaviUtils.convertDistanceUnit(list.get(0).getRemainDistance()) +
                            "남았습니다.");
            for (int i = 0, size = list.size(); i < size; i++) {
                if (gasPriceList.get(i).id == lowPrice.id) {
                    lowestGasMarker = gasPriceMarkerList.get(i);
                    lowestGasMarker.setIconSize(new Point(44, 57));
                    lowestGasMarker.bringToFront();
                    break;
                }
            }
            setVisibility(View.VISIBLE);
        } else {
            if (lowestGasMarker != null) {
                lowestGasMarker.setIconSize(new Point(30, 38));
            }
            setVisibility(View.GONE);
        }
    }

    public void updateLowestGasStationDistance(int distance) {
        EnergyPrice.EnergyType energyType = RozeOptions.getInstance().getEnergyType();
        lowestGasTextView.setText(
                getResources().getText(NaviUtils.getEnergyTypeStringRes(energyType)) +
                        "최저가 주유소까지 " + NaviUtils.convertDistanceUnit(distance) +
                        "남았습니다.");
    }

    public void releaseMap() {
        clearOverlay();
        gMap = null;
    }

    public void clearOverlay() {
        mapHelper.removeOverlays(gMap, gasPriceMarkerList);
        gasPriceMarkerList.clear();
    }
}
