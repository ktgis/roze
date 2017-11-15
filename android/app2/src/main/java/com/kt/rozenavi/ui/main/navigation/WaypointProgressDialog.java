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
package com.kt.rozenavi.ui.main.navigation;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.kt.roze.NavigationManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.UIUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Dialog 정보를 표출한다.
 * Dialog 정보는 "예 / 아니오" 총 2개의 선택 버튼을 표출 하며
 * Dialog 화면 내 버튼 클릭에 따라 다음 경유지 탐색과
 * 기존 경유지 재탐색 처리를 제어한다.
 * Dialog 표출 시 Timer가 생성되며, Timer 종료 시 Dialog를 종료한다.
 */
public class WaypointProgressDialog extends Dialog {

    @BindView(R.id.waypoint_btn_Positive_Count)
    protected TextView countDownView;

    @BindView(R.id.waypoint_btn_Positive_Progress)
    protected ImageView progressView;

    private NavigationManager navigationManager = NavigationManager.getInstance();
    private CountDownTimer dialogTimer;
    private final long durationTime = 3000;
    private final long intervalTime = 1000;

    public WaypointProgressDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDialogView();
        setAnimation();
        setDialogTimer();
    }

    /**
     * Dialog View 초기화
     */
    private void initDialogView() {
        setContentView(R.layout.dialog_waypoint);
        ButterKnife.bind(this);
    }

    /**
     * Dialog 애니메이션 설정
     */
    private void setAnimation() {
        Animation rotateAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_animation);
        progressView.startAnimation(rotateAnimation);
    }

    /**
     * Dialog 타이머 설정
     */
    private void setDialogTimer() {
        dialogTimer = new CountDownTimer(durationTime, intervalTime) {

            public void onTick(long millisUntilFinished) {
                countDownView.setText(String.valueOf(millisUntilFinished / intervalTime));
            }

            public void onFinish() {
                navigationManager.rerouteForPassingWayPoint(0, NavigationManager.RouteMode.USER_REROUTE);
                endDialog();
                UIUtils.showToast(getContext(), R.string.waypoint_dialog_pass_waypoint);
            }
        }.start();
    }

    /**
     * Negative / Positive Button Listener 처리
     */
    @OnClick(R.id.waypoint_btn_Negative_textview)
    public void clickNegative() {
        endDialog();
    }

    @OnClick(R.id.waypoint_btn_Positive_textview)
    public void clickPositive() {
        endDialog();
        navigationManager.rerouteForPassingWayPoint(0, NavigationManager.RouteMode.USER_REROUTE);
        UIUtils.showToast(getContext(), R.string.waypoint_dialog_pass_waypoint);
    }

    /**
     * Dialog 종료 처리
     */
    private void endDialog() {
        dialogTimer.cancel();
        super.dismiss();
    }
}
