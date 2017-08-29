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

import com.kt.rozenavi.R;
import com.kt.rozenavi.databinding.ActivitySettingRouteBinding;
import com.kt.rozenavi.utils.PreferenceUtils;

/**
 * 경로탐색시 경로타입 설정화면
 * 2개의 경로를 탐색할 수 있도록 2종류의 타입을 설정
 * 경로타입 설정후 경로검색시 설정된 정보를 이용하여 검색
 */
public class SettingRouteActivity extends SaveSettingActivity
        implements RadioGroup.OnCheckedChangeListener {
    public static final int DEFAULT_ROUTE_TYPE_1 = 0;
    public static final int DEFAULT_ROUTE_TYPE_2 = 2;

    private ActivitySettingRouteBinding binding;

    /**
     * 첫번째 경로타입 인덱스
     */
    private int firstRouteSelectIndex = -1;
    /**
     * 두번째 경로타입 인덱스
     */
    private int secondRouteSelectIndex = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting_route);
        init();
        setSavedRouteType();
    }

    /**
     * Intent 생성
     *
     * @param context context 객체
     * @return intent 객체
     */
    public static Intent newIntent(Context context) {
        return new Intent(context, SettingRouteActivity.class);
    }

    private void init() {
        initToolbar();
        binding.routeType1Radiogroup.setOnCheckedChangeListener(this);
        binding.routeType2Radiogroup.setOnCheckedChangeListener(this);
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
            getSupportActionBar().setTitle(R.string.activity_title_setting_route_type);
        }
    }

    @Override
    protected void asyncSaveOptionData() {
        PreferenceUtils.putInt(this, PreferenceUtils.KEY_ROUTE_TYPE_1, firstRouteSelectIndex);
        PreferenceUtils.putInt(this, PreferenceUtils.KEY_ROUTE_TYPE_2, secondRouteSelectIndex);
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
     * preference에 저장된 경로타입을 가져오고 없는경우 최적경로(0), 무료도로(2)로 설정
     */
    private void setSavedRouteType() {
        RadioGroup radioGroup = binding.routeType1Radiogroup;
        int index = PreferenceUtils.getInt(this, PreferenceUtils.KEY_ROUTE_TYPE_1,
                DEFAULT_ROUTE_TYPE_1);
        radioGroup.check(radioGroup.getChildAt(index).getId());

        radioGroup = binding.routeType2Radiogroup;
        index = PreferenceUtils.getInt(this, PreferenceUtils.KEY_ROUTE_TYPE_2,
                DEFAULT_ROUTE_TYPE_2);
        radioGroup.check(radioGroup.getChildAt(index).getId());
    }

    /**
     * 첫번째 경로타입 라디오 버튼 선택처리
     * 두번째 경로타입과 겹칠경우 두번째 라디오 버튼의 동일 타입을 변경후 비활성화
     * 라디오버튼 index preference에 저장
     *
     * @param id 버튼 index
     */
    private void selectFirstRouteType(int id) {
        RadioGroup radioGroup1 = binding.routeType1Radiogroup;
        RadioGroup radioGroup2 = binding.routeType2Radiogroup;

        //이전에 선택한 첫번째 경로가 있으면 두번째 경로 비활성화 된거 다시 활성화
        if (firstRouteSelectIndex != -1) {
            radioGroup2.getChildAt(firstRouteSelectIndex).setEnabled(true);
        }
        firstRouteSelectIndex = radioGroup1.indexOfChild(radioGroup1.findViewById(id));

        //두번재 경로와 첫번째 경로가 같다면 두번째 경로 선택지를 변경
        if (secondRouteSelectIndex == firstRouteSelectIndex) {
            radioGroup2.getChildAt(secondRouteSelectIndex).setEnabled(false);
            if (firstRouteSelectIndex == 0) {
                radioGroup2.check(R.id.route_type_2_free_road);
            } else {
                radioGroup2.check(R.id.route_type_2_balanced);
            }
        } else {
            radioGroup2.getChildAt(firstRouteSelectIndex).setEnabled(false);
        }
    }

    /**
     * 두번째 경로타입 라디오 버튼 선택처리
     * 라디오버튼 index preference에 저장
     *
     * @param id 버튼 index
     */
    private void selectSecondRouteType(int id) {
        RadioGroup radioGroup2 = binding.routeType2Radiogroup;
        secondRouteSelectIndex = radioGroup2.indexOfChild(radioGroup2.findViewById(id));
    }

    //--RadioGroup.OnCheckedChangeListener
    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        int id = radioGroup.getId();
        if (id == R.id.route_type_1_radiogroup) {
            selectFirstRouteType(i);
        } else if (id == R.id.route_type_2_radiogroup) {
            selectSecondRouteType(i);
        }
    }

    //--RadioGroup.OnCheckedChangeListener
}
