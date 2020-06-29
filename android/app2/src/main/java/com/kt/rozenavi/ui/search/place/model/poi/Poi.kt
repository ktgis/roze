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
import com.kt.rozenavi.place.utils.PlaceUtils

/**
 * Poi 클래스는 Poi 검색 시 검색된 Poi의 데이터를 담고있다.
 * 전달받은 Poi 객체를 통해 검색된 Poi의 상세정보를 추출할 수 있다.
 */
class Poi(@field:SerializedName("id") private val id: String?,
          /**
           * 검색된 Poi의 명칭 정보를 반환한다.
           *
           * @return Poi의 명칭 정보
           */
          @field:SerializedName("name") val name: String?,
          /**
           * 검색된 Poi의 서브 명칭 정보를 반환한다.
           *
           * @return Poi의 서브 명칭 정보
           */
          @field:SerializedName("subName") val subName: String?,
          /**
           * 검색된 Poi의 브랜치 정보를 반환한다.
           * 여기서 브랜치 정보란 가맹점 정보를 말한다.
           *
           * @return Poi의 브랜치 정보
           */
          @field:SerializedName("branch") val branchName: String?,
          /**
           * 검색된 Poi의 좌표 정보를 반환한다.
           *
           * @return Poi의 좌표 정보
           */
          @field:SerializedName("point") val latLng: LatLng?,
          /**
           * 검색된 Poi의 주소 정보를 담고있는 Address 객체를 반환한다.
           *
           * @return Poi의 주소 정보를 담고있는 Address 객체
           */
          @field:SerializedName("address") val address: Address?,
          /**
           * 검색된 Poi와 검색 시 설정한 기준좌표 사이의 거리 정보를 반환한다.
           *
           * @return 검색된 Poi와 검색 시 설정한 기준좌표 사이의 거리 정보
           */
          @field:SerializedName("distance") val distance: Double?,
          @field:SerializedName("routeOptimization") private val multipleEntrance: MultipleEntrance?) {

    /**
     * 검색된 Poi의 UTMK 좌표 정보를 반환한다.
     *
     * @return Poi의 UTMK 좌표 정보
     */
    fun getUTMK(): UTMK? {
        return if (latLng == null) {
            null
        } else UTMK.valueOf(latLng)
    }

    /**
     * 검색된 Poi의 신주소 정보를 반환한다.
     *
     * @return 신주소 표기법에 따른 주소정보
     */
    private fun getNewAddress(): String? {
        return address?.getNewAddress()
    }

    /**
     * 검색된 Poi의 구주소 정보를 반환한다.
     *
     * @return 구주소 표기법에 따른 주소정보
     */
    private fun getOldAddress(): String? {
        return address?.getOldAddress()
    }

    /**
     * 신주소가 있는 경우 신주소 우선
     * 없는 경우 구주소 반환.
     */
    fun getPoiAddress(): String? {
        val address = getNewAddress()
        if (!address.isNullOrEmpty()) {
            return address
        }
        return getOldAddress()
    }

    /**
     * 검색된 Poi의 명칭들이 조합된 정보를 반환한다.
     *
     * @return Poi의 명칭들이 조합된 정보
     */
    fun getPoiName(): String {
        return PlaceUtils.getSingleStringWithDelimiter(
                " ",
                name,
                subName,
                branchName)
    }

    /**
     * 검색된 Poi에 대한 멀티입구점 UTMK 좌표리스트를 반환한다.
     * 해당 정보는 Poi 검색 시 SearchMode옵션에 SearchMode.NAVIGATION을 설정했을 때 조회된다.
     *
     * @return Poi에 대한 멀티입구점 UTMK 좌표리스트
     */
    fun getMultipleEntranceUTMK(): List<UTMK>? {
        if (multipleEntrance == null
                || multipleEntrance.getMultipleEntranceUTMKData().isNullOrEmpty()) {
            return null
        }
        return multipleEntrance.getMultipleEntranceUTMKData()
    }
}