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
package com.kt.rozenavi.ui.search.place.model.autocomplete

import com.google.gson.annotations.SerializedName

/**
 * 자동완성 결과 데이터 클래스
 */
class AutoComplete(
        /**
         * 검색된 자동완성 결과 리스트 정보를 반환한다.
         * @return  자동완성 결과 리스트
         */
        @field:SerializedName("suggests") val autoCompletePoi: List<AutoCompletePoi>?) {
    /**
     * AutoComplete String List 반환
     */
    fun getAutoCompleteString(): List<String> {
        return autoCompletePoi?.map {
            it.terms
        } ?: emptyList()
    }
}

/**
 * 자동완성 상세 정보 데이터 클래스
 */
class AutoCompletePoi(
        /**
         * 자동완성 검색을 통해 추천된 키워드 정보를 반환한다.
         * @return 추천된 키워드 정보
         */
        @field:SerializedName("terms") val terms: String)