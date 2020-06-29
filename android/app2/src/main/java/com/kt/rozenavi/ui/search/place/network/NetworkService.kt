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
package com.kt.rozenavi.ui.search.place.network

import com.kt.rozenavi.ui.search.place.network.api.GeoApiService
import retrofit2.Retrofit

/**
 * Rx / Coroutine 을 지원하는 Retrofit 제공
 *
 */
object NetworkService {
    /**
     * 코루틴을 지원하는 Retrofit 반환
     */
    fun getRetrofitWithCoroutine(baseUrl: String?, key : String?): Retrofit? {
        if (baseUrl.isNullOrEmpty() || key.isNullOrEmpty()) {
            return null
        }
        return NetworkApiCreator.createRetrofitWithCoroutine(baseUrl, key)
    }

    /**
     * 검색 API 반환
     */
    fun getGeoApiService(retrofit: Retrofit): GeoApiService {
        return retrofit.create(GeoApiService::class.java)
    }
}