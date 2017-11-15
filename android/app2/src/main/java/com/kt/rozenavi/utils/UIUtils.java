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

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.kt.place.PlaceManager;
import com.kt.roze.NavigationManager;
import com.kt.rozenavi.BuildConfig;

/**
 * 공통 UI 기능 활용을 위한 유틸리티 클래스
 * toast 메시지 표시 기능, progress dialog 표시 기능 등을 제공한다
 */
public class UIUtils {
    /**
     * 앱 종료 재확인 인터벌 상수
     */
    public static final long APP_FINISH_INTERVAL_IN_MILLISECOND = 2000;

    /**
     * progress dialog 인스턴스 객체
     */
    private static ProgressDialog_CircleCar dialog;

    /**
     * 키보드 숨김 기능
     * 필요시 명시적으로 키보드를 숨겨야 할때 활용
     *
     * @param context context 객체
     * @param view    대상 view 객체
     */
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * toast 메시지 표시 기능
     *
     * @param context context 객체
     * @param text    표시할 String 메시지
     */
    public static void showToast(Context context, String text) {
        if (context == null) {
            return;
        }
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    /**
     * toast 메시지 표시 기능
     *
     * @param context context 객체
     * @param resId   표시할 string resource id값
     */
    public static void showToast(Context context, int resId) {
        if (context == null) {
            return;
        }
        Toast.makeText(context, resId, Toast.LENGTH_SHORT).show();
    }

    /**
     * toast 메시지 표시 기능
     * activity의 runOnUiThread 기능을 이용하여 표시
     *
     * @param activity activity 객체
     * @param resId    표시할 string resource id값
     */
    public static void showToastRunOnUiThread(final Activity activity, final int resId) {
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast(activity, resId);
            }
        });
    }

    /**
     * toast 메시지 표시 기능
     * activity의 runOnUiThread 기능을 이용하여 표시
     *
     * @param activity activity 객체
     * @param text     표시할 String 메시지
     */
    public static void showToastRunOnUiThread(final Activity activity, final String text) {
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast(activity, text);
            }
        });
    }

    /**
     * progress dialog 표시
     * 앞서 progress dialog가 표시되고 있는 상황일때는 종료한뒤 동작
     *
     * @param context context 객체
     */
    public static void showProgressDialog(Context context) {
        dismissProgressDialog();

        dialog = new ProgressDialog_CircleCar(context);
        dialog.show();
    }

    /**
     * progress dialog 종료
     */
    public static void dismissProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
        dialog = null;
    }

    /**
     * 앱 / 라이브러리 버전정보 text생성
     *
     * @return 버전정보 text
     */
    public static String getVersionString() {
        String version = "";
        version = version + String.format("Roze SDK Version %s", NavigationManager.getInstance().version());
        version = version + "\n";
        version = version + String.format("Place SDK Version %s", PlaceManager.getInstance().version());
        version = version + "\n";
        version = version + String.format("App Version %s", BuildConfig.VERSION_NAME);
        return version;
    }

    /**
     * index에 해당하는 RadioButton 체크
     *
     * @param radioGroup radioGroup 객체
     * @param index      index 정보
     */
    public static void checkRadioButton(RadioGroup radioGroup, int index) {
        radioGroup.check(radioGroup.getChildAt(index).getId());
    }

    /**
     * 체크된 RadioButton의 index 정보 반환
     *
     * @param radioGroup radioGroup 객체
     * @return index 정보
     */
    public static int getCheckedRadioButtonIndex(RadioGroup radioGroup) {
        return radioGroup.indexOfChild(radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));
    }

    /**
     * RadioGroup에 포함된 하위 RadioButton enabled 값 설정
     *
     * @param radioGroup radioGroup 객체
     * @param isEnabled 활성화 여부
     */
    public static void setEnabledRadioGroup(RadioGroup radioGroup, boolean isEnabled) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(isEnabled);
        }
    }
}
