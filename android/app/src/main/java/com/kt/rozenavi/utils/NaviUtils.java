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

import android.location.Location;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.widget.TextView;

import com.kt.roze.NavigationManager;
import com.kt.roze.guidance.RGType;
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
     * 안전운행 모드일때 기본 줌 정보
     */
    public static final double DRIVE_MODE_DEFAULT_ZOOM = 11.0;
    /**
     * 경로안내 모드일때 기본 줌 정보
     */
    public static final double NAVIGATION_MODE_DEFAULT_ZOOM = 12.0;
    /**
     * 기본 지도 틸트 값
     */
    public static final int MAP_TILT_DEFAULT = 45;
    /**
     * 지도상에 표출할 내차마커 가로/세로 크기
     * dp사이즈
     */
    public static final int CAR_MARKER_ICON_SIZE = 65;

    /**
     * 지도상에 표출할 출발지/도착지/경유지 마커 가로 크기
     * dp사이즈
     */
    public static final int ROUTE_LOCATION_MARKER_ICON_WIDTH = 47;
    /**
     * 지도상에 표출할 출발지/도착지/경유지 마커 세로 크기
     * dp사이즈
     */
    public static final int ROUTE_LOCATION_MARKER_ICON_HEIGHT = 57;

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
        if (location.hasSpeed()) {
            return (int) (location.getSpeed() * 3.6);
        }
        return 0;
    }

    /**
     * 거리정보를 포맷팅하여 반환할때 거리값과 단위값을 크기를 다르게 변형하여 반환
     * 거리정보는 단위정보보다 1.5배로 키워서 반환
     *
     * @param textView 입력될 textview
     * @param distance 거리값
     */
    public static void setSizeSpanDistance(TextView textView, int distance) {
        SpannableString spannableString;
        if (distance < 1000) { //m단위
            spannableString = new SpannableString(convertDistanceUnit(distance));
            //마지막 'm' 한글자 제외하고 1.5배로 변경
            spannableString.setSpan(new RelativeSizeSpan(1.5f), 0, spannableString.length() - 1,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else { //km단위
            spannableString = new SpannableString(convertDistanceUnit(distance));
            //마지막 'km' 두글자 제외하고 1.5배로 변경
            spannableString.setSpan(new RelativeSizeSpan(1.5f), 0, spannableString.length() - 2,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
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
     * gps상태와 네비게이션 모드에 따라서 마커 아이콘 리소스 아이디 반환
     *
     * @param isGpsOn gps 수신여부
     * @return 리소스 아이디
     */
    public static int selectCarMarkerIconResourceId(boolean isGpsOn) {
        int resId;

        if (isGpsOn) {
            resId = (NavigationManager.getInstance().getMode() == NavigationManager.Mode.TRACKING) ?
                    R.drawable.tracking_on : R.drawable.car_on;
            return resId;
        } else {
            resId = (NavigationManager.getInstance().getMode() == NavigationManager.Mode.TRACKING) ?
                    R.drawable.tracking_off : R.drawable.car_off;
            return resId;
        }
    }

    /**
     * 숫자에 해당하는 숫자 이미지 리소스 아이디 반환
     *
     * @param number 숫자
     * @return 이미지 리소스 아이디
     */
    public static int getNumberResourceId(int number) {
        switch (number) {
            case 1:
                return R.drawable.img_hfigure1;
            case 2:
                return R.drawable.img_hfigure2;
            case 3:
                return R.drawable.img_hfigure3;
            case 4:
                return R.drawable.img_hfigure4;
            case 5:
                return R.drawable.img_hfigure5;
            case 6:
                return R.drawable.img_hfigure6;
            case 7:
                return R.drawable.img_hfigure7;
            case 8:
                return R.drawable.img_hfigure8;
            case 9:
                return R.drawable.img_hfigure9;
            default:
                return R.drawable.img_hfigure0;
        }
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
}
