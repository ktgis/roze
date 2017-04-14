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

package com.kt.rozenavi;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.kt.roze.RozeOptions;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

/**
 * application 클래스
 * 오픈소스 활용을 위해 multidexapplication 클래스를 상속
 */
public class RozeNaviApplication extends MultiDexApplication {

    /**
     * 참조 정보를 확인하는 leakcanary 객체
     */
    private RefWatcher refWatcher;

    /**
     * refwatcher 객체를 반환
     *
     * @param context context 객체
     * @return refwatcher 객체
     */
    public static RefWatcher getRefWatcher(Context context) {
        RozeNaviApplication application = (RozeNaviApplication) context.getApplicationContext();
        return application.refWatcher;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //참조정보를 확인할 수 있는 refwatcher 생성
        refWatcher = LeakCanary.install(this);
        //since : sdk 0.9.3
        //RozeOption의 개인화 데이터 저장을 위해 초기 initialize 기능 추가
        //AndroidManifest.xml에 meta-data로 RozeHomeDir 정보를 설정할 경우 initialize를 통해
        //저장된 폴더에 데이터 save / load 진행
        RozeOptions.initialize(this);
    }
}
