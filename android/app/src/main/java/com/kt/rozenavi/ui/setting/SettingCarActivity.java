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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.RadioGroup;

import com.kt.roze.RozeOptions;
import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.PreferenceUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 경로탐색시 차량정보 설정화면
 * 차량정보 설정 후 경로검색시 설정된 정보를 이용하여 검색
 */
public class SettingCarActivity extends AppCompatActivity
        implements RadioGroup.OnCheckedChangeListener {
    //기본 설정 승용차
    public static final int DEFAULT_CAR_TYPE = 2;

    @BindView(R.id.car_type_radiogroup)
    protected RadioGroup carTypeRadioGroup;
    @BindView(R.id.car_hipass_radiogroup)
    protected RadioGroup carHipassRadioGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_car);
        ButterKnife.bind(this);
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
        carTypeRadioGroup.setOnCheckedChangeListener(this);
        carHipassRadioGroup.setOnCheckedChangeListener(this);
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
        int index = PreferenceUtils.getInt(this, PreferenceUtils.KEY_CAR_TYPE, DEFAULT_CAR_TYPE);
        carTypeRadioGroup.check(carTypeRadioGroup.getChildAt(index).getId());
        boolean isHipass = PreferenceUtils.getBoolean(this, PreferenceUtils.KEY_CAR_HIPASS,
                RozeOptions.getInstance().isHipass());
        index = isHipass ? 1 : 0;
        carHipassRadioGroup.check(carHipassRadioGroup.getChildAt(index).getId());
        RozeOptions.getInstance().setHipass(isHipass);

    }

    /**
     * 차량타입 선택 정보 index preference에 저장
     *
     * @param id 버튼 index
     */
    private void selectCarType(int id) {
        int index = carTypeRadioGroup.indexOfChild(carTypeRadioGroup.findViewById(id));
        PreferenceUtils.putInt(this, PreferenceUtils.KEY_CAR_TYPE, index);
    }

    /**
     * 차량highpass 선택 정보 index preference에 저장
     *
     * @param id 버튼 index
     */
    private void selectCarHighpass(int id) {
        int index = carHipassRadioGroup.indexOfChild(carHipassRadioGroup.findViewById(id));
        boolean isHipass = (index == 1);
        PreferenceUtils.putBoolean(this, PreferenceUtils.KEY_CAR_HIPASS, isHipass);
        //두번째 radio버튼이 사용버튼 index == 1 이면 true
        RozeOptions.getInstance().setHipass(isHipass);
    }

    //--RadioGroup.OnCheckedChangeListener
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        int id = radioGroup.getId();
        if (id == R.id.car_type_radiogroup) {
            selectCarType(i);
        } else if (id == R.id.car_hipass_radiogroup) {
            selectCarHighpass(i);
        }
    }
    //--RadioGroup.OnCheckedChangeListener
}
