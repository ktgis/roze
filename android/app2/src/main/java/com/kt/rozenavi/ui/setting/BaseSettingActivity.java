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

import android.os.AsyncTask;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kt.rozenavi.R;
import com.kt.rozenavi.databinding.LayoutSettingActionBarBinding;
import com.kt.rozenavi.utils.UIUtils;

/**
 * 설정화면 저장기능 Activity
 * option menu로 저장기능 구현
 */
public abstract class BaseSettingActivity extends AppCompatActivity {
    /**
     * RozeOptions, SharedPreference에 저장하기 위해 옵션정보를 업데이트
     * 성공 / 실패에 대한 boolean 값을 반환
     */
    abstract protected boolean asyncSaveOptionData();

    /**
     * 옵션 저장 성공
     */
    protected void onSaveSuccess() {
        UIUtils.showToast(this, R.string.toast_message_option_save_success);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // 툴바 뒤로가기 버튼 동작시 finish 동작
        if (id == R.id.action_save) {
            saveConfig();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        //저장 메뉴 버튼 추가
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    public void saveConfig() {
        new SaveAsyncTask().execute(0);
    }

    /**
     * activity 타이틀바 설정
     * 화면명 및 버튼 기능 설정
     */
    protected void initTitleBar(LayoutSettingActionBarBinding titleBar, @StringRes int resId) {
        titleBar.titleTextview.setText(resId);
        titleBar.titleTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleBar.backButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        titleBar.saveButtonContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConfig();
            }
        });
    }

    /**
     * sound option 정보 저장
     * IO 작업이 일어나기 때문에 비동기 thread 처리
     */
    private class SaveAsyncTask extends AsyncTask<Integer, Integer, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            UIUtils.showProgressDialog(BaseSettingActivity.this);
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            return asyncSaveOptionData();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            UIUtils.dismissProgressDialog();

            if(result) {
                onSaveSuccess();
            }
        }
    }
}
