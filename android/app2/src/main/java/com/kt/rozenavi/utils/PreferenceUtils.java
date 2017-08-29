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

import android.content.Context;
import android.content.SharedPreferences;

/**
 * shared preference 기능 활용을 위한 유틸리티 클래스
 * 차량타입, 경로타입에 대한 정보를 저장한다
 */
public class PreferenceUtils {
    /**
     * 경로검색 첫번째 route type 정보 key
     */
    public static final String KEY_ROUTE_TYPE_1 = "route_type_1";
    /**
     * 경로검색 두번째 route type 정보 key
     */
    public static final String KEY_ROUTE_TYPE_2 = "route_type_2";
    /**
     * 최종 위치 위도
     */
    public static final String KEY_LAST_LOCATION_LAT = "last_location_lat";
    /**
     * 최종 위치 경도
     */
    public static final String KEY_LAST_LOCATION_LON = "last_location_lon";
    /**
     * preference 명칭
     */
    private static final String PREFERENCE_NAME = "roze_navi";

    /**
     * shared preference 객체 생성
     * preference 명칭과 open type은 미리 정의된 내용으로 생성
     *
     * @param context context 객체
     * @return shared preference 객체
     */
    private static SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    /**
     * preference int값 입력
     *
     * @param context context 객체
     * @param key     입력 key 명칭
     * @param value   입력 int 값
     * @return 성공 여부
     */
    public static boolean putInt(Context context, String key, int value) {
        return getPreference(context).edit().putInt(key, value).commit();
    }

    /**
     * preference string값 입력
     *
     * @param context context 객체
     * @param key     key 명칭
     * @param value   입력 string 값
     * @return 성공 여부
     */
    public static boolean putString(Context context, String key, String value) {
        return getPreference(context).edit().putString(key, value).commit();
    }

    /**
     * preference boolean값 입력
     *
     * @param context context 객체
     * @param key     key 명칭
     * @param value   입력 boolean 값
     * @return 성공 여부
     */
    public static boolean putBoolean(Context context, String key, boolean value) {
        return getPreference(context).edit().putBoolean(key, value).commit();
    }

    /**
     * 저장된 preference int값 반환
     *
     * @param context      context 객체
     * @param key          key 명칭
     * @param defaultValue 해당 값이 없을경우 기본반환 값
     * @return key에 해당하는 int값
     */
    public static int getInt(Context context, String key, int defaultValue) {
        return getPreference(context).getInt(key, defaultValue);
    }

    /**
     * 저장된 preference string값 반환
     *
     * @param context      context 객체
     * @param key          key 명칭
     * @param defaultValue 해당 값이 없을경우 기본반환 값
     * @return key에 해당하는 string값
     */
    public static String getString(Context context, String key, String defaultValue) {
        return getPreference(context).getString(key, defaultValue);
    }

    /**
     * 저장된 preference boolean값 반환
     *
     * @param context      context 객체
     * @param key          key 명칭
     * @param defaultValue 해당 값이 없을경우 기본반환 값
     * @return key에 해당하는 boolean값
     */
    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        return getPreference(context).getBoolean(key, defaultValue);
    }

}
