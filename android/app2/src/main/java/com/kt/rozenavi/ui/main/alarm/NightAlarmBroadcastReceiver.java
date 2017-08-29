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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kt.roze.RozeOptions;

public class NightAlarmBroadcastReceiver extends BroadcastReceiver {

    /**
     * 알람 매니저 리시버 역할
     * 알람 수신 시 로즈 옵션의 주 / 야간 옵션을 변경(true / false)
     * 알람 매니저 재 설정
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        RozeOptions options = RozeOptions.getInstance();
        options.setNight(!options.isNight());
        NightAlarmManager.updateAlarmManager(context);
    }
}
