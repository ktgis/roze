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
import com.kt.roze.SoundOptions;
import com.kt.roze.resource.SoundResourceManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.databinding.ActivitySettingSoundBinding;
import com.kt.rozenavi.utils.UIUtils;

/**
 * 경로안내시 음성안내 설정화면
 * 음성안내 설정 후 경로안내시 설정된 정보를 이용하여 음성안내
 */
public class SettingSoundActivity extends SaveSettingActivity {
    private ActivitySettingSoundBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting_sound);
        init();
        setSavedSoundOption();
    }

    /**
     * Intent 생성
     *
     * @param context context 객체
     * @return intent 객체
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, SettingSoundActivity.class);
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
            getSupportActionBar().setTitle(R.string.activity_title_setting_sound_option);
        }
    }

    @Override
    protected void asyncSaveOptionData() {
        RozeOptions rozeOptions = RozeOptions.getInstance();

        SoundOptions soundOptions = RozeOptions.getInstance().getSoundOption();
        soundOptions.setFixedSpeedCamera(binding.soundOptionFixedSpeedCamera.isChecked());
        soundOptions.setMovableSpeedCamera(binding.soundOptionMovableSpeedCamera.isChecked());
        soundOptions.setSignalViolationCamera(binding.soundOptionSignalViolationCamera.isChecked());
        soundOptions.setBusCamera(binding.soundOptionBusCamera.isChecked());
        soundOptions.setTrafficCamera(binding.soundOptionTrafficCamera.isChecked());
        soundOptions.setStopCamera(binding.soundOptionStopCamera.isChecked());
        soundOptions.setOverloadCamera(binding.soundOptionOverloadCamera.isChecked());
        soundOptions.setInterruptCamera(binding.soundOptionInterruptCamera.isChecked());
        soundOptions.setCctv(binding.soundOptionCctv.isChecked());
        soundOptions.setShoulder(binding.soundOptionShoulder.isChecked());
        soundOptions.setSharpCurve(binding.soundOptionSharpCurve.isChecked());
        soundOptions.setAccidentBlackSpot(binding.soundOptionAccidentBlackSpot.isChecked());
        soundOptions.setLaneDecrease(binding.soundOptionLaneDecrease.isChecked());
        soundOptions.setRockSlide(binding.soundOptionRockSlide.isChecked());
        soundOptions.setSlipperySurface(binding.soundOptionSlipperySurface.isChecked());
        soundOptions.setSpeedBump(binding.soundOptionSpeedBump.isChecked());
        soundOptions.setFog(binding.soundOptionFog.isChecked());
        soundOptions.setFall(binding.soundOptionFall.isChecked());
        soundOptions.setRailroadCrossing(binding.soundOptionRailroadCrossing.isChecked());
        soundOptions.setScarp(binding.soundOptionScarp.isChecked());
        soundOptions.setDeerCrossing(binding.soundOptionDeerCrossing.isChecked());

        RadioGroup radioGroup = binding.soundTypeRadiogroup;
        int index = radioGroup.indexOfChild(
                radioGroup.findViewById(radioGroup.getCheckedRadioButtonId()));
        RozeOptions.SoundType soundType = RozeOptions.SoundType.values()[index];

        // 발성 옵션 체크해서 변경발생 시 발성데이터 초기화 처리
        if (soundType != rozeOptions.getSoundType()) {
            if (SoundResourceManager.initSoundData(this, soundType)) {
                rozeOptions.setSoundType(soundType);
            } else {
                UIUtils.showToast(this, R.string.toast_message_sound_init_fail);
            }
        }

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
     * RozeOptions에 현재 저장되어있는 정보 ui에 적용
     */
    private void setSavedSoundOption() {
        //RozeOption에서 soundtype에 해당하는 정보를 받아와서 ui에 적용할 수 잇도록 수정
        int index = RozeOptions.getInstance().getSoundType().ordinal();
        RadioGroup radioGroup = binding.soundTypeRadiogroup;
        radioGroup.check(radioGroup.getChildAt(index).getId());

        SoundOptions soundOptions = RozeOptions.getInstance().getSoundOption();
        binding.soundOptionFixedSpeedCamera.setChecked(soundOptions.isFixedSpeedCamera());
        binding.soundOptionMovableSpeedCamera.setChecked(soundOptions.isMovableSpeedCamera());
        binding.soundOptionSignalViolationCamera.setChecked(soundOptions.isSignalViolationCamera());
        binding.soundOptionBusCamera.setChecked(soundOptions.isBusCamera());
        binding.soundOptionTrafficCamera.setChecked(soundOptions.isTrafficCamera());
        binding.soundOptionStopCamera.setChecked(soundOptions.isStopCamera());
        binding.soundOptionOverloadCamera.setChecked(soundOptions.isOverloadCamera());
        binding.soundOptionInterruptCamera.setChecked(soundOptions.isInterruptCamera());
        binding.soundOptionCctv.setChecked(soundOptions.isCctv());
        binding.soundOptionShoulder.setChecked(soundOptions.isShoulder());
        binding.soundOptionSharpCurve.setChecked(soundOptions.isSharpCurve());
        binding.soundOptionAccidentBlackSpot.setChecked(soundOptions.isAccidentBlackSpot());
        binding.soundOptionLaneDecrease.setChecked(soundOptions.isLaneDecrease());
        binding.soundOptionRockSlide.setChecked(soundOptions.isRockSlide());
        binding.soundOptionSlipperySurface.setChecked(soundOptions.isSlipperySurface());
        binding.soundOptionSpeedBump.setChecked(soundOptions.isSpeedBump());
        binding.soundOptionFog.setChecked(soundOptions.isFog());
        binding.soundOptionFall.setChecked(soundOptions.isFall());
        binding.soundOptionRailroadCrossing.setChecked(soundOptions.isRailroadCrossing());
        binding.soundOptionScarp.setChecked(soundOptions.isScarp());
        binding.soundOptionDeerCrossing.setChecked(soundOptions.isDeerCrossing());
    }
}
