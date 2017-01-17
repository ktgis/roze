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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.kt.roze.RozeOptions;
import com.kt.roze.SoundOptions;
import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 경로안내시 음성안내 설정화면
 * 음성안내 설정 후 경로안내시 설정된 정보를 이용하여 음성안내
 */
public class SettingSoundActivity extends AppCompatActivity
        implements CheckBox.OnCheckedChangeListener {

    @BindView(R.id.sound_option_checkbox_layout)
    protected LinearLayout checkboxLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_sound);
        ButterKnife.bind(this);
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
        //checkbox listener 설정
        for (int i = 0; i < checkboxLayout.getChildCount(); i++) {
            ((CheckBox) checkboxLayout.getChildAt(i)).setOnCheckedChangeListener(this);
        }
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
    public boolean onCreateOptionsMenu(Menu menu) {
        //저장 메뉴 버튼 추가
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // 툴바 뒤로가기 버튼 동작시 finish 동작
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_save) {
            saveOption();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * RozeOptions에 현재 저장되어있는 정보 checkbox에 적용
     */
    private void setSavedSoundOption() {
        SoundOptions soundOptions = RozeOptions.getInstance().getSoundOption();
        if (soundOptions.isFixedSpeedCamera()) {
            ((CompoundButton) findViewById(R.id.sound_option_fixed_speed_camera)).setChecked(true);
        }
        if (soundOptions.isMovableSpeedCamera()) {
            ((CompoundButton) findViewById(R.id.sound_option_movable_speed_camera)).setChecked(
                    true);
        }
        if (soundOptions.isSignalViolationCamera()) {
            ((CompoundButton) findViewById(R.id.sound_option_signal_violation_camera)).setChecked(
                    true);
        }
        if (soundOptions.isBusCamera()) {
            ((CompoundButton) findViewById(R.id.sound_option_bus_camera)).setChecked(true);
        }
        if (soundOptions.isTrafficCamera()) {
            ((CompoundButton) findViewById(R.id.sound_option_traffic_camera)).setChecked(true);
        }
        if (soundOptions.isStopCamera()) {
            ((CompoundButton) findViewById(R.id.sound_option_stop_camera)).setChecked(true);
        }
        if (soundOptions.isOverloadCamera()) {
            ((CompoundButton) findViewById(R.id.sound_option_overload_camera)).setChecked(true);
        }
        if (soundOptions.isInterruptCamera()) {
            ((CompoundButton) findViewById(R.id.sound_option_interrupt_camera)).setChecked(true);
        }
        if (soundOptions.isCctv()) {
            ((CompoundButton) findViewById(R.id.sound_option_cctv)).setChecked(true);
        }
        if (soundOptions.isShoulder()) {
            ((CompoundButton) findViewById(R.id.sound_option_shoulder)).setChecked(true);
        }
        if (soundOptions.isSharpCurve()) {
            ((CompoundButton) findViewById(R.id.sound_option_sharp_curve)).setChecked(true);
        }
        if (soundOptions.isAccidentBlackSpot()) {
            ((CompoundButton) findViewById(R.id.sound_option_accident_black_spot)).setChecked(true);
        }
        if (soundOptions.isLaneDecrease()) {
            ((CompoundButton) findViewById(R.id.sound_option_lane_decrease)).setChecked(true);
        }
        if (soundOptions.isRockSlide()) {
            ((CompoundButton) findViewById(R.id.sound_option_rock_slide)).setChecked(true);
        }
        if (soundOptions.isSlipperySurface()) {
            ((CompoundButton) findViewById(R.id.sound_option_slippery_surface)).setChecked(true);
        }
        if (soundOptions.isSpeedBump()) {
            ((CompoundButton) findViewById(R.id.sound_option_speed_bump)).setChecked(true);
        }
        if (soundOptions.isFog()) {
            ((CompoundButton) findViewById(R.id.sound_option_fog)).setChecked(true);
        }
        if (soundOptions.isFall()) {
            ((CompoundButton) findViewById(R.id.sound_option_fall)).setChecked(true);
        }
        if (soundOptions.isRailroadCrossing()) {
            ((CompoundButton) findViewById(R.id.sound_option_railroad_crossing)).setChecked(true);
        }
        if (soundOptions.isScarp()) {
            ((CompoundButton) findViewById(R.id.sound_option_scarp)).setChecked(true);
        }
        if (soundOptions.isDeerCrossing()) {
            ((CompoundButton) findViewById(R.id.sound_option_deer_crossing)).setChecked(true);
        }
    }

    /**
     * AsyncTask를 이용하여 sound option 정보 저장
     */
    private void saveOption() {
        new SaveAsyncTask().execute(0);
    }


    //--CheckBox.OnCheckedChangeListener
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();
        SoundOptions soundOptions = RozeOptions.getInstance().getSoundOption();
        if (id == R.id.sound_option_fixed_speed_camera) {
            soundOptions.setFixedSpeedCamera(isChecked);
        } else if (id == R.id.sound_option_movable_speed_camera) {
            soundOptions.setMovableSpeedCamera(isChecked);
        } else if (id == R.id.sound_option_signal_violation_camera) {
            soundOptions.setSignalViolationCamera(isChecked);
        } else if (id == R.id.sound_option_bus_camera) {
            soundOptions.setBusCamera(isChecked);
        } else if (id == R.id.sound_option_traffic_camera) {
            soundOptions.setTrafficCamera(isChecked);
        } else if (id == R.id.sound_option_stop_camera) {
            soundOptions.setStopCamera(isChecked);
        } else if (id == R.id.sound_option_overload_camera) {
            soundOptions.setOverloadCamera(isChecked);
        } else if (id == R.id.sound_option_interrupt_camera) {
            soundOptions.setInterruptCamera(isChecked);
        } else if (id == R.id.sound_option_cctv) {
            soundOptions.setCctv(isChecked);
        } else if (id == R.id.sound_option_shoulder) {
            soundOptions.setShoulder(isChecked);
        } else if (id == R.id.sound_option_sharp_curve) {
            soundOptions.setSharpCurve(isChecked);
        } else if (id == R.id.sound_option_accident_black_spot) {
            soundOptions.setAccidentBlackSpot(isChecked);
        } else if (id == R.id.sound_option_lane_decrease) {
            soundOptions.setLaneDecrease(isChecked);
        } else if (id == R.id.sound_option_rock_slide) {
            soundOptions.setRockSlide(isChecked);
        } else if (id == R.id.sound_option_slippery_surface) {
            soundOptions.setSlipperySurface(isChecked);
        } else if (id == R.id.sound_option_speed_bump) {
            soundOptions.setSpeedBump(isChecked);
        } else if (id == R.id.sound_option_fog) {
            soundOptions.setFog(isChecked);
        } else if (id == R.id.sound_option_fall) {
            soundOptions.setFall(isChecked);
        } else if (id == R.id.sound_option_railroad_crossing) {
            soundOptions.setRailroadCrossing(isChecked);
        } else if (id == R.id.sound_option_scarp) {
            soundOptions.setScarp(isChecked);
        } else if (id == R.id.sound_option_deer_crossing) {
            soundOptions.setDeerCrossing(isChecked);
        }
    }
    //--CheckBox.OnCheckedChangeListener

    /**
     * sound option 정보 저장
     * IO 작업이 일어나기 때문에 비동기 thread 처리
     */
    class SaveAsyncTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            UIUtils.showProgressDialog(SettingSoundActivity.this);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                //너무 빠르게 동작하는 경우 progress가 안보여서 앞뒤로 잠시 sleep 추가
                Thread.sleep(500);
                RozeOptions.getInstance().saveConfig();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            UIUtils.dismissProgressDialog();
        }
    }
}
