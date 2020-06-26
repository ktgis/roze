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
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.RadioGroup;

import com.kt.roze.NavigationManager;
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
public class SettingSoundActivity extends BaseSettingActivity {
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
        initTitleBar(binding.titleBar, R.string.setting_sound_activity_title);
    }

    @Override
    protected boolean asyncSaveOptionData() {
        RozeOptions rozeOptions = RozeOptions.getInstance();

        SoundOptions soundOptions = RozeOptions.getInstance().getSoundOption();
        //고정식 카메라 옵션
        soundOptions.setFixedSpeedCamera(binding.soundOptionFixedSpeedCamera.isChecked());
        //이동식 카메라 옵션
        soundOptions.setMovableSpeedCamera(binding.soundOptionMovableSpeedCamera.isChecked());
        //신호위반 카메라 옵션
        soundOptions.setSignalViolationCamera(binding.soundOptionSignalViolationCamera.isChecked());
        //버스전용 카메라 옵션
        soundOptions.setBusCamera(binding.soundOptionBusCamera.isChecked());
        //교통정보수집 카메라 옵션
        soundOptions.setTrafficCamera(binding.soundOptionTrafficCamera.isChecked());
        //주정차단속 카메라 옵션
        soundOptions.setStopCamera(binding.soundOptionStopCamera.isChecked());
        //과적차량단속 카메라 옵션
        soundOptions.setOverloadCamera(binding.soundOptionOverloadCamera.isChecked());
        //끼어들기단속 카메라 옵션
        soundOptions.setInterruptCamera(binding.soundOptionInterruptCamera.isChecked());
        //방범CCTV 옵션
        soundOptions.setCctv(binding.soundOptionCctv.isChecked());
        //갓길단속 옵션
        soundOptions.setShoulder(binding.soundOptionShoulder.isChecked());
        //급회전구간 옵션
        soundOptions.setSharpCurve(binding.soundOptionSharpCurve.isChecked());
        //사고다발구간 옵션
        soundOptions.setAccidentBlackSpot(binding.soundOptionAccidentBlackSpot.isChecked());
        //좁아지는지역 옵션
        soundOptions.setLaneDecrease(binding.soundOptionLaneDecrease.isChecked());
        //낙석주의 옵션
        soundOptions.setRockSlide(binding.soundOptionRockSlide.isChecked());
        //미끄럼주의 옵션
        soundOptions.setSlipperySurface(binding.soundOptionSlipperySurface.isChecked());
        //과속방지턱 옵션
        soundOptions.setSpeedBump(binding.soundOptionSpeedBump.isChecked());
        //안개주의 옵션
        soundOptions.setFog(binding.soundOptionFog.isChecked());
        //추락주의 옵션
        soundOptions.setFall(binding.soundOptionFall.isChecked());
        //철길건널목 옵션
        soundOptions.setRailroadCrossing(binding.soundOptionRailroadCrossing.isChecked());
        //급경사 옵션
        soundOptions.setScarp(binding.soundOptionScarp.isChecked());
        //야생동물보호 옵션
        soundOptions.setDeerCrossing(binding.soundOptionDeerCrossing.isChecked());

        //하단의 기능은 현재 미구현 입니다.
        //스쿨존 옵션
        soundOptions.setSchoolZone(binding.soundOptionSchoolZone.isChecked());
        //꼬리물기 옵션
        soundOptions.setBumpertobumperCamera(binding.soundOptionBumperToBumper.isChecked());
        //높이제한 옵션
        soundOptions.setHeight(binding.soundOptionHeight.isChecked());
        //중량제한 옵션
        soundOptions.setWeight(binding.soundOptionWeight.isChecked());
        //램프미터링 옵션
        soundOptions.setLampMetering(binding.soundOptionLampMetering.isChecked());
        //제한구역 옵션
        soundOptions.setRestrictionArea(binding.soundOptionRestrictionArea.isChecked());
        //상수도 보호구역 옵션
        soundOptions.setWaterProtArea(binding.soundOptionWaterProtArea.isChecked());
        //발성타입 옵션
        saveOptionSoundType(rozeOptions, binding.soundTypeRadiogroup);

        rozeOptions.saveConfig();
        return true;
    }

    /**
     * 발성타입 옵션 설정
     */
    private void saveOptionSoundType(RozeOptions rozeOptions, RadioGroup radioGroup) {
        int index = UIUtils.getCheckedRadioButtonIndex(radioGroup);
        RozeOptions.SoundType soundType = RozeOptions.SoundType.values()[index];

        // 발성 옵션 변경발생 시 발성데이터 초기화 처리
        if (soundType != rozeOptions.getSoundType()) {
            if (SoundResourceManager.initSoundData(this, soundType)) {
                rozeOptions.setSoundType(soundType);
            } else {
                UIUtils.showToast(this, R.string.toast_message_sound_init_fail);
            }
        }
    }

    /**
     * RozeOptions에 현재 저장되어있는 정보 ui에 적용
     */
    private void setSavedSoundOption() {
        //발성타입 옵션
        UIUtils.checkRadioButton(binding.soundTypeRadiogroup, RozeOptions.getInstance().getSoundType().ordinal());
        //현재 경로안내중일경우 발성타입 변경 방지를 위한 숨김처리
        if (NavigationManager.getInstance().getMode() == NavigationManager.Mode.NAVIGATING) {
            binding.soundTypeRadiogroupLayout.setVisibility(View.GONE);
        }

        SoundOptions soundOptions = RozeOptions.getInstance().getSoundOption();
        //고정식 카메라 옵션
        binding.soundOptionFixedSpeedCamera.setChecked(soundOptions.isFixedSpeedCamera());
        //이동식 카메라 옵션
        binding.soundOptionMovableSpeedCamera.setChecked(soundOptions.isMovableSpeedCamera());
        //신호위반 카메라 옵션
        binding.soundOptionSignalViolationCamera.setChecked(soundOptions.isSignalViolationCamera());
        //버스전용 카메라 옵션
        binding.soundOptionBusCamera.setChecked(soundOptions.isBusCamera());
        //교통정보수집 카메라 옵션
        binding.soundOptionTrafficCamera.setChecked(soundOptions.isTrafficCamera());
        //주정차단속 카메라 옵션
        binding.soundOptionStopCamera.setChecked(soundOptions.isStopCamera());
        //과적차량단속 카메라 옵션
        binding.soundOptionOverloadCamera.setChecked(soundOptions.isOverloadCamera());
        //끼어들기단속 카메라 옵션
        binding.soundOptionInterruptCamera.setChecked(soundOptions.isInterruptCamera());
        //방범CCTV 옵션
        binding.soundOptionCctv.setChecked(soundOptions.isCctv());
        //갓길단속 옵션
        binding.soundOptionShoulder.setChecked(soundOptions.isShoulder());
        //급회전구간 옵션
        binding.soundOptionSharpCurve.setChecked(soundOptions.isSharpCurve());
        //사고다발구간 옵션
        binding.soundOptionAccidentBlackSpot.setChecked(soundOptions.isAccidentBlackSpot());
        //좁아지는지역 옵션
        binding.soundOptionLaneDecrease.setChecked(soundOptions.isLaneDecrease());
        //낙석주의 옵션
        binding.soundOptionRockSlide.setChecked(soundOptions.isRockSlide());
        //미끄럼주의 옵션
        binding.soundOptionSlipperySurface.setChecked(soundOptions.isSlipperySurface());
        //과속방지턱 옵션
        binding.soundOptionSpeedBump.setChecked(soundOptions.isSpeedBump());
        //안개주의 옵션
        binding.soundOptionFog.setChecked(soundOptions.isFog());
        //추락주의 옵션
        binding.soundOptionFall.setChecked(soundOptions.isFall());
        //철길건널목 옵션
        binding.soundOptionRailroadCrossing.setChecked(soundOptions.isRailroadCrossing());
        //급경사 옵션
        binding.soundOptionScarp.setChecked(soundOptions.isScarp());
        //야생동물보호 옵션
        binding.soundOptionDeerCrossing.setChecked(soundOptions.isDeerCrossing());

        //하단의 기능은 현재 미구현 입니다.
        //스쿨존 옵션
        binding.soundOptionSchoolZone.setChecked(soundOptions.isSchoolZone());
        //꼬리물기 옵션
        binding.soundOptionBumperToBumper.setChecked(soundOptions.isBumpertobumperCamera());
        //높이제한 옵션
        binding.soundOptionHeight.setChecked(soundOptions.isHeight());
        //중량제한 옵션
        binding.soundOptionWeight.setChecked(soundOptions.isWeight());
        //램프미터링 옵션
        binding.soundOptionLampMetering.setChecked(soundOptions.isLampMetering());
        //제한구역 옵션
        binding.soundOptionRestrictionArea.setChecked(soundOptions.isRestrictionArea());
        //상수도 보호구역 옵션
        binding.soundOptionWaterProtArea.setChecked(soundOptions.isWaterProtArea());
    }
}
