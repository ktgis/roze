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
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.UIUtils;

/**
 * 설정화면 저장기능 Activity
 * option menu로 저장기능 구현
 */
public abstract class SaveSettingActivity extends AppCompatActivity {

    /**
     * RozeOptions, SharedPreference에 저장하기 위해 옵션정보를 업데이트
     */
    abstract protected void asyncSaveOptionData();

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
     * sound option 정보 저장
     * IO 작업이 일어나기 때문에 비동기 thread 처리
     */
    class SaveAsyncTask extends AsyncTask<Integer, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            UIUtils.showProgressDialog(SaveSettingActivity.this);
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            asyncSaveOptionData();
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            UIUtils.dismissProgressDialog();
        }
    }
}
