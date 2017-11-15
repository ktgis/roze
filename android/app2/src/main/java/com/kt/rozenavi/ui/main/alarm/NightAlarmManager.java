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
package com.kt.rozenavi.ui.main.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;

import com.kt.roze.RozeOptions;

import java.util.Calendar;

/**
 * 알람 설정, 알람 해제 등의 기능을 제공한다.
 */
public class NightAlarmManager {

    /**
     * 주간 설정 시간
     */
    private static final int DAYLIGHT = 7;
    /**
     * 야간 설정 시간
     */
    private static final int NIGHT = 19;

    /**
     * PendingIntent 옵션에 따라 BroadCast 된 PendingIntent 추출
     */
    private static PendingIntent pendingIntent(Context context, int pendingIntentCode) {
        Intent intent = new Intent(context, NightAlarmBroadcastReceiver.class);
        return PendingIntent.getBroadcast(context, 0, intent, pendingIntentCode);
    }

    /**
     * 알람 매니저 설정을 위한 시간 설정
     */
    private static Calendar setCalendar() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(calendar.HOUR_OF_DAY);

        if (hour >= DAYLIGHT && hour < NIGHT) {
            calendar.set(calendar.HOUR_OF_DAY, NIGHT);
        } else if (hour >= NIGHT || hour < DAYLIGHT) {
            calendar.add(calendar.DATE, 1);
            calendar.set(calendar.HOUR_OF_DAY, DAYLIGHT);
        }

        calendar.set(calendar.MINUTE, 0);
        calendar.set(calendar.SECOND, 0);
        calendar.set(calendar.MILLISECOND, 0);

        return calendar;
    }

    /**
     * 알람매니저 초기 설정 (앱 초기화 시 알람 매니저  설정)
     */
    public static void startAlarmManager(@NonNull Context context) {
        if (pendingIntent(context, PendingIntent.FLAG_NO_CREATE) == null) {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(calendar.HOUR_OF_DAY);
            RozeOptions.getInstance().setNight(!(hour >= DAYLIGHT && hour < NIGHT));
        } else {
            cancelAlarmManager(context);
        }
        setAlarmManager(context);
    }

    /**
     * 알람매니저 재 설정 (알람 수신 시 재 설정)
     */
    public static void updateAlarmManager(@NonNull Context context) {
        if (pendingIntent(context, PendingIntent.FLAG_NO_CREATE) == null) {
            cancelAlarmManager(context);
        } else {
            cancelAlarmManager(context);
            setAlarmManager(context);
        }
    }

    /**
     * 알람 매니저 설정
     */
    private static void setAlarmManager(Context context) {
        // AlarmManager 호출
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = pendingIntent(context, PendingIntent.FLAG_CANCEL_CURRENT);

        // OS Version별 AlarmManager 생성 관리
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, setCalendar().getTimeInMillis(), pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.RTC_WAKEUP, setCalendar().getTimeInMillis(), pendingIntent);
        } else {
            manager.set(AlarmManager.RTC_WAKEUP, setCalendar().getTimeInMillis(), pendingIntent);
        }
    }

    /**
     * 알람 매니저 해제
     */
    public static void cancelAlarmManager(@NonNull Context context) {

        PendingIntent pendingIntent = pendingIntent(context, PendingIntent.FLAG_NO_CREATE);
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (manager != null && pendingIntent != null) {
            // AlarmManager 해제
            manager.cancel(pendingIntent);
            pendingIntent.cancel();
        }
    }
}
