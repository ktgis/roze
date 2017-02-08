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
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
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
    private static final int REQUEST_CODE_APP_DETAIL_SETTING = 10001;

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
        if (grantResults == null || grantResults.length == 0) {
            return;
        }
        int resultCode = RESULT_OK;
        // 모든 필요한 권한이 다 있어야 한다.
        // 필요한 권한 리스트와 결과 리스트가 다르면 누락된 권한이 있다고 판단
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

        if (resultCode == Activity.RESULT_OK) { //권한을 모두 허용
            //바로 mainactivity로 이동
            handler.sendEmptyMessage(0);
        } else { //권한 거부 항목이 있는경우
            //사용자가 '다시 보지 않음'(never ask again) 항목을 체크 하고 거부 했는지 확인
            for (int i = 0; i < permissions.length; i++) {
                //다시 보지 않음 항목이 있는경우 false 반환
                //권한 허용을 하는 경우 false를 반환하기 때문에 허용여부 한번 더 확인
                if (grantResults[i] != PermissionChecker.PERMISSION_GRANTED &&
                        !ActivityCompat.shouldShowRequestPermissionRationale(this,
                                permissions[i])) {
                    showPermissionAlertDialog(false);
                    return;
                }
            }
            //거부 항목만 있는경우
            showPermissionAlertDialog(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_CODE_APP_DETAIL_SETTING) {
            return;
        }

        //퍼미션 재확인
        if (checkPermissions()) {
            handler.sendEmptyMessageDelayed(0, 500);
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
     *
     * @param isShowRationale 다시 보지 않음 체크 여부
     */
    private void showPermissionAlertDialog(final boolean isShowRationale) {
        new AlertDialog.Builder(this).setTitle(R.string.permission_dialog_title)
                .setMessage(isShowRationale ? R.string.permission_dialog_content :
                        R.string.permission_dialog_content_setting)
                .setCancelable(false)
                .setPositiveButton(isShowRationale ?
                                R.string.permission_dialog_button_positive :
                                R.string.permission_dialog_button_positive_setting,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (isShowRationale) {
                                    checkPermissions();
                                } else {
                                    startAppDetailSetting();
                                }
                                dialog.dismiss();
                            }
                        })
                .setNegativeButton(R.string.permission_dialog_button_negative,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                                dialog.dismiss();
                            }
                        })
                .show();
    }

    /**
     * 앱 상세설정화면 이동
     * 권한 확인시 다시 보지 않기 항목을 선택시 사용자가 직접 상세설정에서
     * 권한을 주어야하기 때문에 해당 화면으로 이동
     */
    private void startAppDetailSetting() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, REQUEST_CODE_APP_DETAIL_SETTING);
    }

    //--WeakReferenceHandler.OnMessageHandler
    @Override
    public void handleMessage(Message msg) {
        startMainActivity();
        finish();
    }
    //--WeakReferenceHandler.OnMessageHandler
}
