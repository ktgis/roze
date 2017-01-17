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

package com.kt.rozenavi.utils;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Handler 클래스
 * message queue에 관계없이 메모리 해제를 위해 사용
 */
public class WeakReferenceHandler extends Handler {
    /**
     * OnMessageHandler 인터페이스가 구현된 객체
     */
    private final WeakReference<OnMessageHandler> target;

    public WeakReferenceHandler(OnMessageHandler activity) {
        target = new WeakReference<>(activity);
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        OnMessageHandler messageHandler = target.get();
        if (messageHandler == null) return;
        messageHandler.handleMessage(msg);
    }

    /**
     * Handler 메시지 처리 인터페이스
     */
    public interface OnMessageHandler {
        void handleMessage(Message msg);
    }
}
