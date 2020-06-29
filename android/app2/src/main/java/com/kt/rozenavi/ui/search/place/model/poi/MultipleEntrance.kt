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
package com.kt.rozenavi.ui.search.place.model.poi

import com.google.gson.annotations.SerializedName
import com.kt.geom.model.LatLng
import com.kt.geom.model.UTMK

/**
 * MultipleEntrance 클래스는 Poi 검색 시 해당 Poi 에 차가 진입할 수 있는 멀티입구점 좌표 정보를 담고있다.
 * 해당 정보는 Poi 검색 시 SearchMode 옵션에 SearchMode.NAVIGATION 을 설정했을 때 조회되며
 * 전달받은 MultipleEntrance 객체를 통해 검색된 Poi 의 멀티입구점 좌표리스트정보를 추출할 수 있다.
 */
class MultipleEntrance(
        /**
         * WGS84 멀티 입구점 좌표
         *
         * @return 멀티 입구점 좌표리스트
         */
        @field:SerializedName("multipleEntrance") val multipleEntranceData: List<LatLng>?) {

    /**
     * UTMK 멀티 입구점 좌표
     *
     * @return 멀티 입구점 좌표리스트
     */
    fun getMultipleEntranceUTMKData(): List<UTMK>? {
        if (multipleEntranceData.isNullOrEmpty()) {
            return null
        }
        return multipleEntranceData.map {
            UTMK.valueOf(it)
        }
    }
}