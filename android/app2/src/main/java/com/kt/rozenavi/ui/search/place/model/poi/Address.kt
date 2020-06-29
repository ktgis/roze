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
import com.kt.rozenavi.place.utils.PlaceUtils

/**
 * Address 클래스는 Poi 검색 시 검색된 신주소 / 구주소 상세 데이터를 담고있다.
 * 전달받은 Address 객체를 통해 검색된 Poi의 신주소 / 구주소 상세정보를 추출할 수 있다.
 */
class Address(
        /**
         * 검색된 Poi의 특별 * 광역시 / 도 정보를 반환한다.
         * @return  특별 * 광역시 / 도 정보
         */
        @field:SerializedName("siDo") val siDo: String?,
        /**
         * 검색된 Poi의 시 / 군 / 구 정보를 반환한다.
         * @return  시 / 군 / 구 정보 정보
         */
        @field:SerializedName("siGunGu") val siGunGu: String?,
        /**
         * 검색된 Poi의 읍 / 면 / 동 정보를 반환한다.
         * @return  읍 / 면 / 동 정보
         */
        @field:SerializedName("eupMyeonDong") val eupMyeonDong: String?,
        /**
         * 검색된 지오코드의 도로명 정보를 반환한다.
         * @return  도로명 정보
         */
        @field:SerializedName("street") val street: String?,
        /**
         * 검색된 지오코드의 도로명 번호 정보를 반환한다.
         * @return  도로명 번호 정보
         */
        @field:SerializedName("streetNumber") val streetNumber: String?,
        /**
         * 검색된 Poi의 리 정보를 반환한다.
         * @return  리 정보
         */
        @field:SerializedName("ri") val ri: String?,
        /**
         * 검색된 Poi의 지번 정보를 반환한다.
         * @return  지번 정보
         */
        @field:SerializedName("houseNumber") val houseNumber: String?) {

    /**
     * 검색된 Poi의 신주소 정보를 반환한다.
     * @return  신주소 표기법에 따른 주소정보
     */
    fun getNewAddress(): String? {
        return PlaceUtils.getSingleStringWithDelimiter(
                " ",
                siDo,
                siGunGu,
                street,
                streetNumber)
    }


    /**
     * 검색된 Poi의 구주소 정보를 반환한다.
     * @return  신주소 표기법에 따른 주소정보
     */
    fun getOldAddress(): String? {
        return PlaceUtils.getSingleStringWithDelimiter(
                " ",
                siDo,
                siGunGu,
                eupMyeonDong,
                streetNumber,
                ri,
                houseNumber)
    }

    fun getAddress(): String? {
        val newAddress = getNewAddress()
        if (!newAddress.isNullOrEmpty()) {
            return newAddress
        }
        return getOldAddress()
    }
}