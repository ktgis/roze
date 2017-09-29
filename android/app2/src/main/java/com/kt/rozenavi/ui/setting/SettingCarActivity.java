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

package com.kt.rozenavi.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RadioGroup;

import com.kt.roze.RozeOptions;
import com.kt.roze.data.model.EnergyPrice;
import com.kt.rozenavi.R;
import com.kt.rozenavi.databinding.ActivitySettingCarBinding;
import com.kt.rozenavi.utils.UIUtils;

/**
 * 경로탐색시 차량정보 설정화면
 * 차량정보 설정 후 경로검색시 설정된 정보를 이용하여 검색
 */
public class SettingCarActivity extends BaseSettingActivity {
    private ActivitySettingCarBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting_car);
        init();
        setSavedCarInfoType();
    }

    /**
     * Intent 생성
     *
     * @param context context 객체
     * @return intent 객체
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, SettingCarActivity.class);
    }

    private void init() {
        initTitleBar(binding.titleBar, R.string.setting_car_activity_title);
    }

    @Override
    protected boolean asyncSaveOptionData() {
        RozeOptions rozeOptions = RozeOptions.getInstance();
        //차량타입 옵션
        saveOptionCarType(rozeOptions, binding.carTypeRadiogroup);
        //하이패스 옵션
        saveOptionHipass(rozeOptions, binding.carHipassRadiogroup);
        //유종타입 옵션
        saveOptionEnergyType(rozeOptions, binding.carEnergyRadiogroup);
        rozeOptions.saveConfig();
        return true;
    }

    /**
     * 차량 타입 옵션 설정
     */
    private void saveOptionCarType(RozeOptions rozeOptions, RadioGroup radioGroup) {
        int index = UIUtils.getCheckedRadioButtonIndex(radioGroup);
        rozeOptions.setCarType(RozeOptions.CarType.values()[index]);
    }

    /**
     * 하이패스 옵션 설정
     */
    private void saveOptionHipass(RozeOptions rozeOptions, RadioGroup radioGroup) {
        int index = UIUtils.getCheckedRadioButtonIndex(radioGroup);
        //사용옵션이 0번째 index
        rozeOptions.setHipass(index == 0);
    }

    /**
     * 유종 타입 옵션 설정
     */
    private void saveOptionEnergyType(RozeOptions rozeOptions, RadioGroup radioGroup) {
        int index = UIUtils.getCheckedRadioButtonIndex(radioGroup);
        rozeOptions.setEnergyType(EnergyPrice.EnergyType.values()[index]);
    }

    /**
     * RozeOptions에 현재 저장되어있는 정보 ui에 적용
     */
    private void setSavedCarInfoType() {
        RozeOptions rozeOptions = RozeOptions.getInstance();
        //차량타입 옵션
        UIUtils.checkRadioButton(binding.carTypeRadiogroup, rozeOptions.getCarType().ordinal());
        //하이패스 옵션
        UIUtils.checkRadioButton(binding.carHipassRadiogroup, rozeOptions.isHipass() ? 0 : 1);
        //유종타입 옵션
        UIUtils.checkRadioButton(binding.carEnergyRadiogroup, rozeOptions.getEnergyType().ordinal());
    }
}
