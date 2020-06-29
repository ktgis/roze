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
package com.kt.rozenavi.ui.search.place.network.api

import com.kt.rozenavi.ui.search.place.model.Place
import com.kt.rozenavi.ui.search.place.model.autocomplete.AutoComplete
import retrofit2.http.GET
import retrofit2.http.Query

interface GeoApiService {
    // 일반 검색, 내 위치 기반으로 일반적으로 검색하고 싶으면 쓰세요.
    @GET("search/v1.0/pois")
    suspend fun normalSearch(
            @Query("numberOfResults") numOfResult: Int,
            @Query("mode") searchMode: String,
            @Query("children") childPoiOption: String,
            @Query("terms") keyword: String
    ): Place

    // 자동완성은 무조건 내 위치 기반 키워드 검색한다.
    @GET("search/v1.0/utilities/autocomplete")
    suspend fun autoCompleteSearch(
            @Query("terms") keyword: String
    ): AutoComplete
}