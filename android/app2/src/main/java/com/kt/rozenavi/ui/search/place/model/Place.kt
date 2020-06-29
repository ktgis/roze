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
package com.kt.rozenavi.ui.search.place.model

import com.google.gson.annotations.SerializedName
import com.kt.rozenavi.ui.search.place.model.geocode.GeocodeAddress
import com.kt.rozenavi.ui.search.place.model.poi.Poi

/**
 * Place 클래스는 Poi 검색 시 결과 데이터를 담고있다.
 * 전달받은 Place 객체를 통해 검색한 Poi 의 상세정보를 추출할 수 있다.
 */
class Place(
        /**
         * 검색한 키워드로 검색된 총 Poi 개수를 반환한다.
         * @return 키워드로 서버에서 검색된 Poi의 수
         */
        @field:SerializedName("numberOfPois") val totalCount: Int,
        /**
         * 검색된 Poi 리스트 정보를 반환한다.
         * @return Poi 리스트
         */
        @field:SerializedName("pois") val pois: List<Poi>?,
        /**
         * 검색된 지오코드 리스트 정보를 반환한다.
         * @return 지오코드 리스트
         */
        @field:SerializedName("residentialAddress")
        val residentialAddress: List<GeocodeAddress>?) {

    /**
     * 검색된 Poi 의 총 개수를 반환한다.
     * @return 검색된 Poi 의 수
     */
    fun getResultCount(): Int {
        return pois?.size ?: 0
    }

    /**
     * 특수 검색을 통해 검색된 SpecifiablePoiInfo 를 반환한다.
     * 여기서 특수 검색이란 시청역 주차장, 강남 맛집과 같이 지명과 키워드가 혼재된 검색을 말한다.
     * 해당 메서드를 직접 호출하기 앞서 getSpecifiablePoiInfo() 메서드 호출을 통해 해당 객체에
     * 데이터 설정 유/무를 확인해야한다.
     * @return 특수 검색된 Poi 리스트
     */
    fun getResidentialPoiList(): List<Poi> {
        val poi = mutableListOf<Poi>()
        residentialAddress?.map {
            if (!it.getPoi().isNullOrEmpty()) {
                poi.addAll(it.getPoi())
            }
        }
        return poi
    }
}