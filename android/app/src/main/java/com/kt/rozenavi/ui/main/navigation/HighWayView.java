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

import com.kt.roze.data.model.EnergyPrice;
import com.kt.roze.guidance.model.BaseHighway;
import com.kt.roze.guidance.model.HighwayGuidance;
import com.kt.roze.guidance.model.SAGasStation;
import com.kt.roze.guidance.model.SAGuidance;
import com.kt.roze.guidance.model.TGGuidance;
import com.kt.roze.resource.GasStationResourceManager;
import com.kt.roze.resource.LaneResourceManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 고속도로 정보 표시 View
 * 3개까지 고속도로 정보를 표시하고 있다.
 */
public class HighWayView extends RelativeLayout {

    /**
     * 각 고속도로 안내점 Layout
     */
    @BindView(R.id.highway_tbt_1_layout)
    protected LinearLayout tbtLayout1;
    @BindView(R.id.highway_tbt_2_layout)
    protected LinearLayout tbtLayout2;
    @BindView(R.id.highway_tbt_3_layout)
    protected LinearLayout tbtLayout3;

    /**
     * 각 고속도로 안내점 명칭
     */
    @BindView(R.id.highway_tbt_1_direction_textview)
    protected TextView tbtDirectionTextView1;
    @BindView(R.id.highway_tbt_2_direction_textview)
    protected TextView tbtDirectionTextView2;
    @BindView(R.id.highway_tbt_3_direction_textview)
    protected TextView tbtDirectionTextView3;
    /**
     * 현재 위치로 부터 각 안내점 까지의 거리
     */
    @BindView(R.id.highway_tbt_1_distance_textview)
    protected TextView tbtDistanceTextView1;
    @BindView(R.id.highway_tbt_3_distance_textview)
    protected TextView tbtDistanceTextView3;
    @BindView(R.id.highway_tbt_2_distance_textview)
    protected TextView tbtDistanceTextView2;
    /**
     * 각 안내점의 휴계소 정보
     */
    @BindView(R.id.extra_highway_data_1)
    protected LinearLayout extra_data_1;
    @BindView(R.id.extra_highway_data_2)
    protected LinearLayout extra_data_2;
    @BindView(R.id.extra_highway_data_3)
    protected LinearLayout extra_data_3;

    /**
     * 하이패스 차로 정보
     */
    @BindView(R.id.hipass_lane_layout)
    protected RelativeLayout hipass_lane_layout;
    @BindView(R.id.hipass_lane_inform)
    protected LinearLayout hipass_lane_inform;

    int firstDistance = 0;
    int secondDistance = 0;
    int thirdDistance = 0;

    private List<HighwayGuidance> guidances;

    public HighWayView(Context context) {
        super(context);
        initView(context);
    }

    public HighWayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public HighWayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_guidance_highway, this);
        ButterKnife.bind(this);
        setVisibility(GONE);
    }

    /**
     * 고속도로 안내점 정보를 개별 View에 전달한다.
     *
     * @param guidances 고속도로 안내점 정보
     */
    public void setHighwayGuidances(List<HighwayGuidance> guidances) {
        this.guidances = guidances;

        if (guidances == null) {
            setVisibility(View.GONE);
            return;
        }

        HighwayGuidance h;
        for (int i = 0; i < guidances.size(); i++) {
            h = guidances.get(i);
            setHighWayItem(i, h);
            setItemVisibility(i, View.VISIBLE);
        }
        setVisibility(View.VISIBLE);

        for (int i = guidances.size(); i < 3; i++) {
            setItemVisibility(i, View.GONE);
        }
    }

    /**
     * 개별 view에 고속도로 안내점 정보를 Set 한다.
     *
     * @param index 리스트의 고속도로 안내점 인덱스
     * @param h     고속도로 안내점 정보
     */
    public void setHighWayItem(int index, HighwayGuidance h) {
        StringBuilder builder = new StringBuilder();

        //Highway 요금 정보
        if (h.getType() == BaseHighway.Type.TG) {
            builder.append(h.getNodeName());
            TGGuidance TG = (TGGuidance) h;
            if (TG.getToll() != 0) {
                builder.append("\n요금 : ").append(TG.getToll());
            }
        } else if (h.getType() == BaseHighway.Type.RA) {
            builder.append("졸음쉼터");
        } else {
            builder.append(h.getNodeName());
        }

        switch (index) {
            case 0:
                tbtDirectionTextView1.setText(builder.toString());
                firstDistance = h.getDistance();
                setExtraData(h, extra_data_1);
                break;
            case 1:
                tbtDirectionTextView2.setText(builder.toString());
                secondDistance = h.getDistance();
                setExtraData(h, extra_data_2);
                break;
            case 2:
                tbtDirectionTextView3.setText(builder.toString());
                thirdDistance = h.getDistance();
                setExtraData(h, extra_data_3);
                break;
            default:
                break;
        }
    }

    /**
     * 휴계소 정보를 View에 Set한다.
     *
     * @param h 고속도로 정보
     * @param l 휴계소 View
     */
    private void setExtraData(HighwayGuidance h, LinearLayout l) {
        if (h.getType() != BaseHighway.Type.SA) {
            l.setVisibility(View.GONE);
            return;
        }
        l.removeAllViews();

        SAGuidance sa = (SAGuidance) h;

        List<SAGasStation> gasStations = sa.getGasInforms();
        if (gasStations == null || gasStations.isEmpty()) {
            l.setVisibility(View.GONE);
            return;
        }

        int resId;
        for (SAGasStation g : gasStations) {
            resId = GasStationResourceManager.getSAGasResourceID(g.getBrand(),
                    EnergyPrice.EnergyType.valueOf(g.getEnergySrc()));
            if (resId != GasStationResourceManager.RESOURCE_NOT_FOUND) {
                l.addView(getSaItemView(l.getContext(), resId));
                if (g.getPrice() != -1) {
                    TextView tx = new TextView(l.getContext());
                    tx.append(String.format("\n%d원", g.getPrice()));
                    l.addView(tx);
                }
            }
        }
        l.setVisibility(View.VISIBLE);
    }

    private ImageView getSaItemView(Context context, int resId) {
        ImageView iv = new ImageView(context);
        iv.setImageResource(resId);
        return iv;
    }

    public void setItemVisibility(int index, int visible) {
        switch (index) {
            case 0:
                tbtLayout1.setVisibility(visible);
                break;
            case 1:
                tbtLayout2.setVisibility(visible);
                break;
            case 2:
                tbtLayout3.setVisibility(visible);
                break;
            default:
                break;
        }
    }

    private ImageView getLaneSeparator(RelativeLayout layout) {
        ImageView laneSeparatorImage = new ImageView(layout.getContext());
        laneSeparatorImage.setImageResource(LaneResourceManager.getSeparatorId());
        return laneSeparatorImage;
    }

    private TextView getHipassItemView(Context context, int number) {
        TextView laneText = new TextView(context);
        if (number >= 0) {
            laneText.setText(String.format("%d", number));
        } else {
            laneText.setText("...");
        }
        laneText.setTextSize(30);
        return laneText;
    }

    /**
     * HIPASS 차로 정보를 View에 set한다.
     *
     * @param tg 요금소 정보
     */
    private void updateHipassLanes(TGGuidance tg) {
        if (tg == null) {
            hipass_lane_layout.setVisibility(View.GONE);
            return;
        }

        int laneCount = tg.getLaneCount();
        byte[] lanes = tg.getHighpassLanes();

        if (lanes.length == 0) {
            hipass_lane_layout.setVisibility(View.GONE);
            return;
        }

        boolean ret = true;
        boolean lastRet = true;
        int size = lanes.length;
        hipass_lane_inform.removeAllViews();
        hipass_lane_inform.addView(getLaneSeparator(hipass_lane_layout));

        for (int i = lanes[0]; i < laneCount; i++) {
            for (byte b : lanes) {
                if (b == i) {
                    ret = true;
                    break;
                }
                ret = false;
            }

            if (ret && lastRet) {
                hipass_lane_inform.addView(
                        getHipassItemView(hipass_lane_layout.getContext(), i + 1));
                hipass_lane_inform.addView(getLaneSeparator(hipass_lane_layout));
            } else if (ret && !lastRet) {
                hipass_lane_inform.addView(getHipassItemView(hipass_lane_layout.getContext(), -1));
                hipass_lane_inform.addView(getLaneSeparator(hipass_lane_layout));
                hipass_lane_inform.addView(
                        getHipassItemView(hipass_lane_layout.getContext(), i + 1));
                hipass_lane_inform.addView(getLaneSeparator(hipass_lane_layout));
            }

            if (lanes[size - 1] == i) {
                break;
            }
            lastRet = ret;
        }
        hipass_lane_layout.setVisibility(View.VISIBLE);
    }

    /**
     * 현재 위치에서 각 고속도로 안내점까지의 거리를 표시</br>
     * 라이브러리에서는 현재 위치에서 첫번째 아이템까지의 거리만 전달한다.</br>
     * 첫번째 이후 안내점은 전달된 거리를 이용하여 변화량을 계산하여 App에서 계산하도록 한다.
     *
     * @param distance 거리(m)
     */
    public void updateDistance(int distance) {
        int remainDistance = distance - firstDistance;
        int firstHighwayDistance = firstDistance + remainDistance;


        if (guidances != null && guidances.size() > 0 && firstHighwayDistance < 600) {
            HighwayGuidance firstGuidance = guidances.get(0);
            if (firstGuidance.getType() == BaseHighway.Type.TG) {
                TGGuidance tg = (TGGuidance) firstGuidance;
                updateHipassLanes(tg);
            } else {
                updateHipassLanes(null);
            }
        } else {
            updateHipassLanes(null);
        }
        tbtDistanceTextView1.setText(NaviUtils.convertDistanceUnit(firstHighwayDistance));
        tbtDistanceTextView2.setText(
                NaviUtils.convertDistanceUnit(secondDistance + remainDistance));
        tbtDistanceTextView3.setText(NaviUtils.convertDistanceUnit(thirdDistance + remainDistance));
    }
}
