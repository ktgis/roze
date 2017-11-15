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

package com.kt.rozenavi.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.kt.roze.RozeOptions;
import com.kt.roze.routing.RouteSubOptions;
import com.kt.rozenavi.R;
import com.kt.rozenavi.databinding.ActivitySettingRouteAdvBinding;
import com.kt.rozenavi.utils.UIUtils;

/**
 * 경로탐색시 추가옵션 설정화면
 * 회피옵션 설정후 경로검색시 설정된 정보를 이용하여 검색
 *
 * 현재 미구현 기능
 */
public class SettingRouteAdvActivity extends BaseSettingActivity {
    private ActivitySettingRouteAdvBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting_route_adv);
        init();
        setSavedRouteOption();
    }

    /**
     * Intent 생성
     *
     * @param context context 객체
     * @return intent 객체
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, SettingRouteAdvActivity.class);
    }

    private void init() {
        initTitleBar(binding.titleBar, R.string.setting_route_adv_activity_title);
    }

    @Override
    protected boolean asyncSaveOptionData() {
        RozeOptions rozeOptions = RozeOptions.getInstance();

        if (!validateInputValue()) {
            return false;
        }

        //실시간 교통정보 옵션
        saveOptionTrafficTSMS(rozeOptions, binding.routeOptionTrafficSwitch.isChecked());
        //교통 패턴정보 옵션
        saveOptionTrafficPattern(rozeOptions, binding.routeOptionTrafficPatternSwitch.isChecked());
        //비분리도로 유턴 회피 옵션
        saveOptionUturnDetourDivideRoad(rozeOptions, binding.routeOptionDividedRadiogroup);
        //차선수 제한 유턴 회피 옵션
        saveOptionUturnDetourLane(rozeOptions, binding.routeOptionLaneRadiogroup, binding.routeOptionLaneEdittext);
        //높이제한 회피 옵션
        saveOptionDetourHeight(rozeOptions, binding.routeOptionHeightRadiogroup, binding.routeOptionHeightEdittext);
        //중량제한 회피 옵션
        saveOptionDetourWeight(rozeOptions, binding.routeOptionWeightRadiogroup, binding.routeOptionWeightEdittext);
        //상수도 회피 옵션
        saveOptionDetourWaterSupply(rozeOptions, binding.routeOptionWaterRadiogroup);
        //제한구역 회피 옵션
        saveOptionDetourRestrictions(rozeOptions, binding.routeOptionRestrictionsRadiogroup);
        //좁은길 회피 옵션
        saveOptionDetourNarrow(rozeOptions, binding.routeOptionNarrowRadiogroup, binding.routeOptionNarrowEdittext);

        rozeOptions.saveConfig();
        return true;
    }

    /**
     * 입력값 유효성 검사
     */
    private boolean validateInputValue() {
        //차선수 유턴 제한 체크
        if(TextUtils.isEmpty(binding.routeOptionLaneEdittext.getText())) {
            UIUtils.showToastRunOnUiThread(SettingRouteAdvActivity.this, R.string.toast_message_uturn_lane_value_fail);
            return false;
        }
        //높이 체크
        if(TextUtils.isEmpty(binding.routeOptionHeightEdittext.getText())) {
            UIUtils.showToastRunOnUiThread(SettingRouteAdvActivity.this, R.string.toast_message_height_value_fail);
            return false;
        }
        //중량 체크
        if(TextUtils.isEmpty(binding.routeOptionWeightEdittext.getText())) {
            UIUtils.showToastRunOnUiThread(SettingRouteAdvActivity.this, R.string.toast_message_weight_value_fail);
            return false;
        }
        //좁은길 체크
        if(TextUtils.isEmpty(binding.routeOptionNarrowEdittext.getText())) {
            UIUtils.showToastRunOnUiThread(SettingRouteAdvActivity.this, R.string.toast_message_narrow_lane_value_fail);
            return false;
        }
        return true;
    }

    /**
     * 좁은길 회피 옵션 설정
     */
    private void saveOptionDetourNarrow(RozeOptions rozeOptions, RadioGroup radioGroup, EditText editText) {
        int index = UIUtils.getCheckedRadioButtonIndex(radioGroup);
        rozeOptions.setUseSafetyDetour_Narrow(RouteSubOptions.DetourKind.values()[index]);
        //좁은 차선값 설정
        rozeOptions.setSafetyDetour_Narrow(Integer.parseInt(editText.getText().toString()));
    }

    /**
     * 제한구역 회피 옵션 설정
     */
    private void saveOptionDetourRestrictions(RozeOptions rozeOptions, RadioGroup radioGroup) {
        int index = UIUtils.getCheckedRadioButtonIndex(radioGroup);
        rozeOptions.setUseSafetyDetour_Driving_Restrictions(RouteSubOptions.DetourKind.values()[index]);
    }

    /**
     * 상수도 보호구역 회피 옵션 설정
     */
    private void saveOptionDetourWaterSupply(RozeOptions rozeOptions, RadioGroup radioGroup) {
        int index = UIUtils.getCheckedRadioButtonIndex(radioGroup);
        rozeOptions.setUseSafetyDetour_Watersupply(RouteSubOptions.DetourKind.values()[index]);
    }

    /**
     * 중량 제한 회피 옵션 설정
     */
    private void saveOptionDetourWeight(RozeOptions rozeOptions, RadioGroup radioGroup, EditText editText) {
        int index = UIUtils.getCheckedRadioButtonIndex(radioGroup);
        rozeOptions.setUseSafetyDetour_Weight(RouteSubOptions.DetourKind.values()[index]);
        //제한되는 중량값 설정
        rozeOptions.setSafetyDetour_Weight(Float.parseFloat(editText.getText().toString()));
    }

    /**
     * 높이 제한 회피 옵션 설정
     */
    private void saveOptionDetourHeight(RozeOptions rozeOptions, RadioGroup radioGroup, EditText editText) {
        int index = UIUtils.getCheckedRadioButtonIndex(radioGroup);
        rozeOptions.setUseSafetyDetour_Height(RouteSubOptions.DetourKind.values()[index]);
        //제한되는 높이값 설정
        rozeOptions.setSafetyDetour_Height(Float.parseFloat(editText.getText().toString()));
    }

    /**
     * 차선수 제한 유턴 회피 옵션 설정
     */
    private void saveOptionUturnDetourLane(RozeOptions rozeOptions, RadioGroup radioGroup, EditText editText) {
        int index = UIUtils.getCheckedRadioButtonIndex(radioGroup);
        rozeOptions.setUseUturnDetour_Lane(RouteSubOptions.DetourKind.values()[index]);

        //제한되는 차선 갯수 설정
        rozeOptions.setUTurnDetour_Lane(Integer.parseInt(editText.getText().toString()));
    }

    /**
     * 비분리도로 유턴 회피 옵션 설정
     */
    private void saveOptionUturnDetourDivideRoad(RozeOptions rozeOptions, RadioGroup radioGroup) {
        int index = UIUtils.getCheckedRadioButtonIndex(radioGroup);
        rozeOptions.setUseUturnDetour_DividedRoad(RouteSubOptions.DetourKind.values()[index]);
    }

    /**
     * 실시간 교통정보 옵션 설정
     */
    private void saveOptionTrafficTSMS(RozeOptions rozeOptions, boolean isOn) {
        rozeOptions.setUseTrafficTSMS(isOn ? RouteSubOptions.TrafficKind.ON : RouteSubOptions.TrafficKind.OFF);
    }

    /**
     * 교통 패턴정보 옵션 설정
     */
    private void saveOptionTrafficPattern(RozeOptions rozeOptions, boolean isOn) {
        rozeOptions.setUseTrafficPattern(isOn ? RouteSubOptions.TrafficKind.ON : RouteSubOptions.TrafficKind.OFF);
    }

    /**
     * RozeOptions에 현재 저장되어있는 정보 ui에 적용
     */
    private void setSavedRouteOption() {
        RozeOptions rozeOptions = RozeOptions.getInstance();
        //실시간 교통정보 옵션
        binding.routeOptionTrafficSwitch
                .setChecked(rozeOptions.getUseTrafficTSMS() == RouteSubOptions.TrafficKind.ON);
        //교통 패턴정보 옵션
        binding.routeOptionTrafficPatternSwitch
                .setChecked(rozeOptions.getUseTrafficPattern() == RouteSubOptions.TrafficKind.ON);
        //비분리도로 유턴 회피 옵션
        UIUtils.checkRadioButton(binding.routeOptionDividedRadiogroup,
                rozeOptions.getUseUturnDetour_DividedRoad().ordinal());
        //차선수 제한 유턴 회피 옵션
        binding.routeOptionLaneEdittext.setText(String.valueOf(rozeOptions.getUTurnDetour_Lane()));
        UIUtils.checkRadioButton(binding.routeOptionLaneRadiogroup, rozeOptions.getUseUturnDetour_Lane().ordinal());
        //높이제한 회피 옵션
        binding.routeOptionHeightEdittext.setText(String.valueOf(rozeOptions.getSafetyDetour_Height()));
        UIUtils.checkRadioButton(binding.routeOptionHeightRadiogroup,
                rozeOptions.getUseSafetyDetour_Height().ordinal());
        //중량제한 회피 옵션
        binding.routeOptionWeightEdittext.setText(String.valueOf(rozeOptions.getSafetyDetour_Weight()));
        UIUtils.checkRadioButton(binding.routeOptionWeightRadiogroup,
                rozeOptions.getUseSafetyDetour_Weight().ordinal());
        //상수도 회피 옵션
        UIUtils.checkRadioButton(binding.routeOptionWaterRadiogroup,
                rozeOptions.getUseSafetyDetour_Watersupply().ordinal());
        //제한구역 회피 옵션
        UIUtils.checkRadioButton(binding.routeOptionRestrictionsRadiogroup,
                rozeOptions.getUseSafetyDetour_Driving_Restrictions().ordinal());
        //좁은길 회피 옵션
        binding.routeOptionNarrowEdittext.setText(String.valueOf(rozeOptions.getSafetyDetour_Narrow()));
        UIUtils.checkRadioButton(binding.routeOptionNarrowRadiogroup,
                rozeOptions.getUseSafetyDetour_Narrow().ordinal());
    }
}
