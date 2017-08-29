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
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.RadioGroup;

import com.kt.roze.RozeOptions;
import com.kt.roze.data.model.EnergyPrice;
import com.kt.rozenavi.R;
import com.kt.rozenavi.databinding.ActivitySettingCarBinding;

/**
 * 경로탐색시 차량정보 설정화면
 * 차량정보 설정 후 경로검색시 설정된 정보를 이용하여 검색
 */
public class SettingCarActivity extends SaveSettingActivity {
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
        initToolbar();
    }

    /**
     * toolbar 타이틀 설정
     * 뒤로가기 버튼 활성화
     */
    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(R.string.activity_title_setting_car_type);
        }
    }

    @Override
    protected void asyncSaveOptionData() {
        RozeOptions rozeOptions = RozeOptions.getInstance();
        RadioGroup radioGroup = binding.carTypeRadiogroup;
        int index = radioGroup.indexOfChild(
                radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));
        rozeOptions.setCarType(RozeOptions.CarType.values()[index]);

        radioGroup = binding.carHipassRadiogroup;
        index = radioGroup.indexOfChild(
                radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));
        rozeOptions.setHipass((index == 1));

        radioGroup = binding.carEnergyRadiogroup;
        index = radioGroup.indexOfChild(
                radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));

        rozeOptions.setEnergyType(EnergyPrice.EnergyType.values()[index]);

        rozeOptions.saveConfig();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 툴바 뒤로가기 버튼 동작시 finish 동작
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * preference에 저장된 차량정보를 가져오고 없는경우 기본값 설정
     */
    private void setSavedCarInfoType() {
        RozeOptions rozeOptions = RozeOptions.getInstance();

        RadioGroup radioGroup = binding.carTypeRadiogroup;
        radioGroup.check(radioGroup.getChildAt(rozeOptions.getCarType().ordinal()).getId());

        radioGroup = binding.carHipassRadiogroup;
        radioGroup.check(radioGroup.getChildAt(rozeOptions.isHipass() ? 1 : 0).getId());

        radioGroup = binding.carEnergyRadiogroup;
        radioGroup.check(radioGroup.getChildAt(rozeOptions.getEnergyType().ordinal()).getId());
    }
}
