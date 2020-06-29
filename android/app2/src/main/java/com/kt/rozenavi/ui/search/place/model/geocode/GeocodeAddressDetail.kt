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
package com.kt.rozenavi.ui.search.place.model.geocode

import com.google.gson.annotations.SerializedName
import com.kt.geom.model.LatLng
import com.kt.rozenavi.ui.search.place.model.poi.Address

/**
 * GeocodeAddressDetail 클래스는 지오코드 검색 시 신주소 / 구주소 상세 데이터를 담고있다.
 * 전달받은 GeocodeAddressDetail 객체를 통해 검색한 지오코드의 신주소 / 구주소 상세정보를 추출할 수 있다.
 */
class GeocodeAddressDetail(
        /**
         * 검색된 지오코드의 특별 * 광역시 / 도 정보를 반환한다.
         * @return  특별 * 광역시 / 도 정보
         */
        @field:SerializedName("siDo") private val siDo: String?,
        /**
         * 검색된 지오코드의 시 / 군 / 구 정보를 반환한다.
         * @return  시 / 군 / 구 정보 정보
         */
        @field:SerializedName("siGunGu") private val siGunGu: String?,
        /**
         * 검색된 지오코드의 읍 / 면 / 동 정보를 반환한다.
         * @return  읍 / 면 / 동 정보
         */
        //parcelAddress
        @field:SerializedName("eupMyeonDong") private val eupMyeonDong: String?,
        /**
         * 검색된 지오코드의 리 정보를 반환한다.
         * @return  리 정보
         */
        @field:SerializedName("ri") private val ri: String?,
        /**
         * 검색된 지오코드의 지번 정보를 반환한다.
         * @return  지번 정보
         */
        @field:SerializedName("houseNumber")
        val houseNumber: String?,
        /**
         * 검색된 지오코드의 도로명 정보를 반환한다.
         * @return  도로명 정보
         */
        //roadAddress
        @field:SerializedName("street") private val street: String?,
        /**
         * 검색된 지오코드의 도로명 번호 정보를 반환한다.
         * @return  도로명 번호 정보
         */
        @field:SerializedName("streetNumber") private val streetNumber: String?,
        /**
         * 검색된 지오코드의 위도 / 경도 좌표를 갖고있는 객체를 반환한다.
         * @return  위도 / 경도 좌표를 갖고있는 객체
         */
        @field:SerializedName("geographicInformation")
        private val geocodeCoordInfo: GeocodeCoordInfo?) {

    fun getAddress(): Address {
        return Address(siDo,
                siGunGu,
                eupMyeonDong,
                street,
                streetNumber,
                ri,
                houseNumber)
    }

    fun getLatLng(): LatLng? {
        return geocodeCoordInfo?.latLng
    }
}