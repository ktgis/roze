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

package com.kt.rozenavi.utils;

import android.graphics.Color;
import android.location.Location;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;

import com.kt.geom.model.UTMK;
import com.kt.roze.NavigationManager;
import com.kt.roze.data.model.EnergyPrice;
import com.kt.roze.guidance.RGType;
import com.kt.roze.routing.RoutePlan;
import com.kt.rozenavi.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 내비게이션 기능을 구현하는데 필요한 공통 기능을 모아놓은 유틸리티 클래스
 * 속도 연산, 거리계산, 내비게이션에 필요한 단위 정보등을 변환해 주는 기능을 제공한다
 */
public class NaviUtils {
    /**
     * 지도상에 표출할 spot 마커 가로/세로 크기
     * dp사이즈
     */
    public static final int DEFAULT_SPOT_MARKER_ICON_SIZE = 30;
    /**
     * 지도상에 표출할 spot 마커 가로/세로 크기
     * dp사이즈
     */
    public static final int CAMERA_SPOT_MARKER_ICON_SIZE = 50;

    /**
     * gps 속도를 m/s단위에서 km/h단위로 변환
     * 속도가 없는경우는 0을 반환
     *
     * @param location location 객체
     * @return km/h 속도
     */
    public static int calculateSpeed(Location location) {
        if(location == null) {
            return 0;
        }

        if (location.hasSpeed()) {
            return (int) (location.getSpeed() * 3.6);
        }
        return 0;
    }

    /**
     * 거리정보를 포맷팅하여 반환할때 거리값과 단위표시 크기를 다르게 변형하여 반환
     * 단위표시를 거리값보다 작게 하여 상대적으로 거리값을 강조한다.
     *
     * @param textView 입력될 textview
     * @param distance 거리값
     */
    public static void setSizeSpanDistance(TextView textView, int distance) {
        SpannableString spannableString = new SpannableString(convertDistanceUnit(distance));
        int offset;
        if (distance < 1000) { //m단위
            //뒤에서 1글자
            offset = 1;
        } else { //km단위
            //뒤에서 2글자
            offset = 2;
        }
        //단위값만 0.7배로 변경
        spannableString.setSpan(new RelativeSizeSpan(0.7f),
                spannableString.length() - offset, spannableString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        textView.setText(spannableString, TextView.BufferType.SPANNABLE);
    }

    /**
     * 거리정보를 m/km단위로 맞추어 포맷팅한 텍스트 반환
     * 1000m가 넘는 거리는 km로 변경하여 반환
     *
     * @param meters 거리값
     * @return 변환된 거리정보
     */
    public static String convertDistanceUnit(int meters) {
        if (meters < 0) {
            meters = 0;
        }
        if (meters < 1000) {
            return meters + "m";
        } else {
            return String.format("%.1fKm", (meters / 1000f));
        }
    }

    /**
     * 도착시간 정보를 포맷팅한 텍스트 반환
     * 오전/오후 시간:분 형태로 반환
     *
     * @param timeInSecond 예상 경과시간정보(초단위)
     * @return 변환된 도착시간정보
     */
    public static String convertArrivedTime(long timeInSecond) {
        //예상 경과시간을 현재시간과 더한다
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.SECOND, (int) timeInSecond);

        return new SimpleDateFormat("aa h:mm").format(calendar.getTime());
    }

    /**
     * GPS 시간을 yyyy-mm-dd hh.mm.ss 형태로 변환
     */
    public static String gpsTimeTorealTime(long gpsTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh.mm.ss");
        Date date = new Date(gpsTime);
        return sdf.format(date);
    }

    /**
     * 남은시간 정보를 포맷팅한 텍스트 반환
     * 시간:분 형태로 반환
     *
     * @param timeInSecond 남은시간정보(초단위)
     * @return 변환된 남은시간정보
     */
    public static String convertRemainTime(long timeInSecond) {
        int hour = (int) (timeInSecond / 3600);
        int minute = (int) (timeInSecond / 60) % 60;
        return String.format("%02d:%02d", hour, minute);
    }

    public static String convertRemainTimeByFormat(long timeInSecond) {
        StringBuilder builder = new StringBuilder();
        int hour = (int) (timeInSecond / 3600);
        if (hour > 0) {
            builder.append(String.format("%d시간 ", hour));
        }
        int minute = (int) (timeInSecond / 60) % 60;
        if (hour > 0 || minute > 0) {
            builder.append(String.format("%02d분", minute));
        }

        return builder.toString();
    }

    /**
     * 남은시간 정보를 포맷팅한 텍스트 반환
     * 시간:분:초 형태로 반환하지만 남은시간의 크기에 따라 분:초 형태로
     * 반환될 수도 있음
     *
     * @param timeInSecond 남은시간정보(초단위)
     * @return 변환된 남은시간정보
     */
    public static String convertRemainTimeWithSec(long timeInSecond) {
        StringBuilder builder = new StringBuilder();
        int hour = (int) (timeInSecond / 3600);
        if (hour > 0) {
            builder.append(String.format("%02d:", hour));
        }
        int minute = (int) (timeInSecond / 60) % 60;
        if (hour > 0 || minute > 0) {
            builder.append(String.format("%02d:", minute));
        }
        int sec = (int) (timeInSecond % 60);
        builder.append(String.format("%02d", sec));

        return builder.toString();
    }

    /**
     * 금액정보를 포맷팅한 텍스트 반환
     * 1000단위마다 ','를 포함한 정보를 반환
     *
     * @param price 금액정보
     * @return 변환된 금액정보
     */
    public static String convertPrice(int price) {
        return String.format("%,d원", price);
    }

    /**
     * rgtype에 해당하는 tbt 안내 정보 반환
     * 해당하는 resource id가 없는 경우 -1 반환
     *
     * @param rgType rgtype 값
     * @return tbt 안내 텍스트 resource id
     */
    public static int getRgTypeString(int rgType) {
        switch (rgType) {
            case RGType.GO_STRAIGHT:
                return R.string.rgtype_go_strait;
            case RGType.DIRECTION_1:
                return R.string.rgtype_direction_1;
            case RGType.DIRECTION_2:
                return R.string.rgtype_direction_2;
            case RGType.TURN_RIGHT:
                return R.string.rgtype_turn_right;
            case RGType.DIRECTION_4:
                return R.string.rgtype_direction_4;
            case RGType.DIRECTION_5:
                return R.string.rgtype_direction_5;
            case RGType.DIRECTION_7:
                return R.string.rgtype_direction_7;
            case RGType.DIRECTION_8:
                return R.string.rgtype_direction_8;
            case RGType.TURN_LEFT:
                return R.string.rgtype_turn_left;
            case RGType.DIRECTION_10:
                return R.string.rgtype_direction_10;
            case RGType.DIRECTION_11:
                return R.string.rgtype_direction_11;
            case RGType.GO_OVERPASS:
                return R.string.rgtype_go_overpass;
            case RGType.RIGHT_OVERPASS:
                return R.string.rgtype_right_overpass;
            case RGType.LEFT_OVERPASS:
                return R.string.rgtype_left_overpass;
            case RGType.GO_UNDERPASS:
                return R.string.rgtype_go_underpass;
            case RGType.OVERPASS_RIGHT_SIDE:
                return R.string.rgtype_overpass_right_side;
            case RGType.OVERPASS_LEFT_SIDE:
                return R.string.rgtype_overpass_left_side;
            case RGType.UNDERPASS_RIGHT_SIDE:
                return R.string.rgtype_underpass_right_side;
            case RGType.UNDERPASS_LEFT_SIDE:
                return R.string.rgtype_underpass_left_side;
            case RGType.RIGHT_SIDE:
                return R.string.rgtype_right_side;
            case RGType.LEFT_SIDE:
                return R.string.rgtype_left_side;
            case RGType.GO_IN_HIGHWAY:
                return R.string.rgtype_go_in_highway;
            case RGType.RIGHT_IN_HIGHWAY:
                return R.string.rgtype_right_in_highway;
            case RGType.LEFT_IN_HIGHWAY:
                return R.string.rgtype_left_in_highway;
            case RGType.GO_IN_EXPRESSWAY:
                return R.string.rgtype_go_in_expressway;
            case RGType.RIGHT_IN_EXPRESSWAY:
                return R.string.rgtype_right_in_expressway;
            case RGType.LEFT_IN_EXPRESSWAY:
                return R.string.rgtype_left_in_expressway;
            case RGType.RIGHT_OUT_HIGHWAY:
                return R.string.rgtype_right_out_highway;
            case RGType.LEFT_OUT_HIGHWAY:
                return R.string.rgtype_left_out_highway;
            case RGType.JUNCTION_GO_STRAIGHT:
                return R.string.rgtype_junction_go_strait;
            case RGType.JUNCTION_RIGHT:
                return R.string.rgtype_junction_right;
            case RGType.JUNCTION_LEFT:
                return R.string.rgtype_junction_left;
            case RGType.U_TURN:
                return R.string.rgtype_u_turn;
            case RGType.NO_SOUND_GO_STRAIGHT:
                return R.string.rgtype_no_sound_go_strait;
            case RGType.TUNNEL:
                return R.string.rgtype_tunnel;
            case RGType.ROTARY_1:
                return R.string.rgtype_rotary_1;
            case RGType.ROTARY_2:
                return R.string.rgtype_rotary_2;
            case RGType.ROTARY_3:
                return R.string.rgtype_rotary_3;
            case RGType.ROTARY_4:
                return R.string.rgtype_rotary_4;
            case RGType.ROTARY_5:
                return R.string.rgtype_rotary_5;
            case RGType.ROTARY_6:
                return R.string.rgtype_rotary_6;
            case RGType.ROTARY_7:
                return R.string.rgtype_rotary_7;
            case RGType.ROTARY_8:
                return R.string.rgtype_rotary_8;
            case RGType.ROTARY_9:
                return R.string.rgtype_rotary_9;
            case RGType.ROTARY_10:
                return R.string.rgtype_rotary_10;
            case RGType.ROTARY_11:
                return R.string.rgtype_rotary_11;
            case RGType.ROTARY_12:
                return R.string.rgtype_rotary_12;
            //            case RGType.IC:
            //                return name;
            //            case RGType.JC:
            //                return name;
            //            case RGType.TG:
            //                return name;
            case RGType.STARTING_POINT:
                return R.string.rgtype_starting_point;
            case RGType.WAYPOINT:
                return R.string.rgtype_waypoint;
            case RGType.DESTINATION:
                return R.string.rgtype_destination;
            //            case RGType.WAYPOINT:
            //                return name;
            default:
                return -1;
        }
    }

    /**
     * rgtype에 맞는 이미지 리소스 아이디 반환
     * 현재는 단속카메라와 방지턱에 대한 리소스만 체크
     *
     * @param rgType rgtype
     * @return 이미지 리소스 아이디
     */
    public static int getRgTypeImage(short rgType) {
        switch (rgType) {
            case RGType.CAM_SPEED:
            case RGType.CAM_SPEED_SIGNAL:
            case RGType.CAM_SPEED_MOBILE:
            case RGType.CAM_INTERVAL_SPEED_START:
            case RGType.CAM_INTERVAL_SPEED_END:
                return R.drawable.img_camera_warning01;
            case RGType.CAUTION_BUMP_ONEWAY:
            case RGType.CAUTION_BUMP:
                return R.drawable.img_speed_bump;
            case RGType.CAUTION_HTRUCK_WATERPROTAREA_START:
            case RGType.CAUTION_HTRUCK_WATERPROTAREA_END:
                return R.drawable.r21;
            default:
                return -1;
        }
    }

    /**
     * rgtype에 맞는 마커 크기 반환
     *
     * @param rgType rgtype
     * @return 마커 아이콘 크기
     */
    public static int getRgTypeIconSize(short rgType) {
        switch (rgType) {
            case RGType.CAM_SPEED:
            case RGType.CAM_SPEED_SIGNAL:
            case RGType.CAM_SPEED_MOBILE:
            case RGType.CAM_INTERVAL_SPEED_START:
            case RGType.CAM_INTERVAL_SPEED_END:
                return CAMERA_SPOT_MARKER_ICON_SIZE;
            default:
                return DEFAULT_SPOT_MARKER_ICON_SIZE;
        }
    }

    public static int getRouteTypeStringRes(RoutePlan.RouteType type) {
        int resourceId;
        switch (type) {
            case HIGHWAY_ROAD:
                resourceId = R.string.route_type_highway;
                break;
            case FREE_ROAD:
                resourceId = R.string.route_type_free_road;
                break;
            case SHORTEST:
                resourceId = R.string.route_type_shortest;
                break;
            case BALANCED:
            default:
                resourceId = R.string.route_type_balanced;
                break;
        }
        return resourceId;
    }

    public static int getEnergyTypeStringRes(EnergyPrice.EnergyType energyType) {
        int resourceId;
        switch (energyType) {
            case PREMIUMGASOLINE:
                resourceId = R.string.energy_type_premium_gasoline;
                break;
            case DIESEL:
                resourceId = R.string.energy_type_diesel;
                break;
            case KEROSENE:
                resourceId = R.string.energy_type_kerosene;
                break;
            case LPG:
                resourceId = R.string.energy_type_lpg;
                break;
            case CNG:
                resourceId = R.string.energy_type_cng;
                break;
            case LNG:
                resourceId = R.string.energy_type_lng;
                break;
            case ELECTRICITY:
                resourceId = R.string.energy_type_electricity;
                break;
            case GASOLINE:
            default:
                resourceId = R.string.energy_type_gasoline;
                break;
        }
        return resourceId;
    }

    /**
     * 기준 좌표에서 angle방향으로 interval 만큼의 거리의 좌표를 반환
     *
     * @param angle    진행 각도
     * @param frompt   기준 좌표
     * @param interval 거리(m)
     * @return 계산된 좌표
     */
    public static UTMK getPointOverLineDistance(short angle, UTMK frompt, int interval) {
        double x = frompt.x + (interval * Math.sin(Math.toRadians(angle)));
        double y = frompt.y + (interval * Math.cos(Math.toRadians(angle)));
        return new UTMK(x, y);
    }

    public static int getHighwayTrafficColor(short trafficInfo) {
        int color;
        switch (trafficInfo) {
            case 1:
                color = Color.RED;
                break;
            case 2:
                color = Color.YELLOW;
                break;
            case 3:
                color = Color.GREEN;
                break;
            default:
                color = Color.LTGRAY;
                break;
        }
        return color;
    }
}
