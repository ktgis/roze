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
package com.kt.rozenavi.ui.main.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Binder;
import android.os.IBinder;
import android.view.WindowManager;

import com.kt.roze.guidance.model.TurnGuidance;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.MainActivity;

import java.util.List;

/**
 * TBT Guidance 서비스 등록 및 Popup 처리 클랙스
 * 경로 안내 중 발생되는 TBT Guidance에 대해 Service 처리하여
 * 앱이 For-Ground, Back-Ground에 상관없이 정상 안내가 가능하도록 구현
 */
public class TbtGuidancePopupService extends Service {

    /**
     * Tbt Guidance를 Popup으로 출력 할 View
     */
    public static TbtGuidancePopupView tbtGuidancePopupView;

    /**
     * Popup의 Window상 x좌표
     */
    private int xPosition;
    /**
     * Popup의 Window상 y좌표
     */
    private int yPosition;

    /**
     * 상태알림바 Notification 등록을 위한 인스턴스
     */
    private NotificationManager notificationManager;
    private static final int NOTIFICATIONID = 1234534;

    IBinder mBinder = new TbtGuidancePopupBinder();

    /**
     * 서비스 바인딩관련 클래스
     */
    public class TbtGuidancePopupBinder extends Binder {
        public TbtGuidancePopupService getService() { // 서비스 객체를 리턴
            return TbtGuidancePopupService.this;
        }
    }

    /**
     * 등록된 서비스 바인딩 시 호출되는 클래스
     * return IBinder mBinder
     */
    @Override
    public IBinder onBind(Intent intent) {
        if (tbtGuidancePopupView == null) {
            tbtGuidancePopupView = new TbtGuidancePopupView(getApplicationContext());
        }

        // 액티비티에서 bindService() 를 실행하면 호출됨
        // 리턴한 IBinder 객체는 서비스와 클라이언트 사이의 인터페이스 정의한다
        return mBinder; // 서비스 객체를 리턴
    }

    /**
     * 안드로이드 단말 상단 상태알림바 등록
     */
    public void showNotification() {

        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);
        }

        Intent intent;
        PendingIntent pendingIntent;

        intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        pendingIntent = PendingIntent.getActivity(getApplicationContext(), 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification status = new android.support.v4.app.NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ico_rozenavi)
                .setContentTitle(getResources().getString(R.string.notification_content_title))
                .setContentText(getResources().getString(R.string.notification_content_text))
                .setContentIntent(pendingIntent)
                .setColor(getResources().getColor(R.color.colorPrimary))    // 아이콘 색상이 변함
                .build();
        status.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTIFICATIONID, status);
    }

    /**
     * 안드로이드 단말 상단 상태알림바 등록 해제
     */
    public void removeNotification() {
        if (notificationManager == null) {
            return;
        }
        notificationManager.cancel(NOTIFICATIONID);
        notificationManager = null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * 실행중인 앱이 내려갔을 때 팝업 View를 Window에 붙여서 출력한다.
     */
    public void addTbtPopupView() {
        if (tbtGuidancePopupView == null) {
            tbtGuidancePopupView = new TbtGuidancePopupView(getApplicationContext());
        }

        if (tbtGuidancePopupView.isAttachedToWindow()) {
            return;
        }

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        if (hasPosition()) {
            params.x = xPosition;
            params.y = yPosition;
        }

        windowManager.addView(tbtGuidancePopupView, params);
    }

    /**
     * Popup View의 기존 X, Y 좌표 존재 유/무 체크
     */
    private boolean hasPosition() {
        return xPosition != 0 && yPosition != 0;
    }

    /**
     * Popup 화면 제거
     */
    public void removeTbtPopupView() {
        if (tbtGuidancePopupView == null) {
            return;
        }

        if (!tbtGuidancePopupView.isAttachedToWindow()) {
            return;
        }

        xPosition = tbtGuidancePopupView.xPosition;
        yPosition = tbtGuidancePopupView.yPosition;

        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.removeView(tbtGuidancePopupView);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        tbtGuidancePopupView = null;
        super.onDestroy();
    }

    /**
     * TBT 정보 표시
     */
    public void updateTBTViews(List<TurnGuidance> guidances) {
        tbtGuidancePopupView.updateTBTViews(guidances);
    }

    /**
     * 현재 위치에서 첫번째 TBT 까지의 거리
     *
     * @param distance 거리(m)
     */
    public void updateTBTDistance(int distance) {
        tbtGuidancePopupView.updateTBTDistance(distance);
    }
}