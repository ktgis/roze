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

package com.kt.rozenavi;

import android.content.Context;

import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.ExternalCacheDiskCacheFactory;
import com.bumptech.glide.module.AppGlideModule;

/**
 * Glide 옵션 설정 클래스
 */
@GlideModule
public class RozeNaviGlideModule extends AppGlideModule {
    private static final int DISK_CACHE_SIZE = 10 * 1024 * 1024;
    private static final String CACHE_FOLDER_NAME = "img_cache";

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        //disk cache는 지정한 위치에 지정한 크기만큼만 저장
        builder.setDiskCache(
                new ExternalCacheDiskCacheFactory(context, CACHE_FOLDER_NAME, DISK_CACHE_SIZE));
    }
}
