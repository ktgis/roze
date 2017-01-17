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

package com.kt.rozenavi.ui.main;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.UIUtils;
import com.kt.rozenavi.utils.WeakReferenceHandler;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 간단한 Splash 역할을 하는 Activity
 * 필요한 권한 체크도 진행
 */
public class SplashActivity extends Activity implements WeakReferenceHandler.OnMessageHandler {
    @BindView(R.id.activity_splash_version_textview)
    protected TextView versionTextView;
    /**
     * 필요권한 리스트
     */
    private List<String> needPermissions;
    /**
     * 내부 핸들러 객체
     * 타이머 역할
     */
    private WeakReferenceHandler handler = new WeakReferenceHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        init();
        //모든 권한이 승인되어있을경우 1초정도 화면을 보여주고 메인화면으로 전환
        if (checkPermissions()) {
            handler.sendEmptyMessageDelayed(0, 1000);
        }
    }

    private void init() {
        versionTextView.setText(UIUtils.getVersionString());
    }

    /**
     * 권한 승인여부 확인
     * 필요권한 중에 허용되지 않은 권한이 있는경우 요청 하며 false 반환
     * 없는 경우 true 반환
     *
     * @return 모든 권한 승인여부
     */
    private boolean checkPermissions() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        needPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                needPermissions.add(permission);
            }
        }

        if (needPermissions.isEmpty()) {
            return true;
        }

        requestPermissions(needPermissions);
        return false;
    }

    /**
     * 확인하는 권한이 승인이 되어있는지 여부 반환
     *
     * @param permission 권한 명칭
     * @return 승인여부
     */
    private boolean hasPermission(String permission) {
        return (PackageManager.PERMISSION_GRANTED ==
                ContextCompat.checkSelfPermission(this, permission));
    }

    /**
     * 승인이 필요한 권한 목록으로 권한 승인 요청
     *
     * @param needPermissions 필요 권한 목록
     */
    public void requestPermissions(List<String> needPermissions) {
        ActivityCompat.requestPermissions(this, needPermissions.toArray(
                new String[needPermissions.size()]), 0);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        int resultCode = RESULT_OK;
        // 모든 필요한 권한이 다 있어야 한다.
        if (grantResults.length == needPermissions.size()) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    resultCode = RESULT_CANCELED;
                    break;
                }
            }
        } else {
            resultCode = RESULT_CANCELED;
        }

        if (resultCode == Activity.RESULT_OK) {
            handler.sendEmptyMessage(0);
        } else {
            showPermissionAlertDialog();
        }
    }

    /**
     * main activity 화면으로 이동
     */
    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }

    /**
     * 권한 체크 결과 사용자가 허용하지 않은 권한이 있는경우
     * 권한 승인에 대한 알림 팝업
     * 권한확인 재시도, 앱종료
     */
    private void showPermissionAlertDialog() {
        new AlertDialog.Builder(this).setTitle(R.string.permission_dialog_title)
                .setMessage(R.string.permission_dialog_content)
                .setCancelable(false)
                .setPositiveButton(R.string.permission_dialog_button_positive,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int id) {
                                checkPermissions();
                            }
                        })
                .setNegativeButton(R.string.permission_dialog_button_negative,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(
                                    DialogInterface dialog,
                                    int id) {
                                finish();
                            }
                        })
                .show();
    }

    //--WeakReferenceHandler.OnMessageHandler
    @Override
    public void handleMessage(Message msg) {
        startMainActivity();
        finish();
    }
    //--WeakReferenceHandler.OnMessageHandler
}
