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
import com.kt.rozenavi.ui.search.place.model.poi.Poi

/**
 * GeocodeAddress 클래스는 지오코드 검색 시 신주소 / 구주소 상세 데이터 리스트 객체를 담고있다.
 * 전달받은 GeocodeAddress 객체를 통해 검색한 지오코드의 신주소 / 구주소 객체를 추출할 수 있다.
 */
class GeocodeAddress(
        /**
         * 검색된 지오코드 정보에 대한 구주소 리스트 정보를 반환한다.
         * 반환되는 데이터는 Address 정보 및 위도 / 경도 좌표가 포함된다.
         * @return  구주소(ParcelAddress) 리스트
         */
        @field:SerializedName("parcelAddress") val parcelAddress: List<GeocodeAddressDetail>?,
        /**
         * 검색된 지오코드 정보에 대한 신주소 리스트 정보를 반환한다.
         * 반환되는 데이터는 Address 정보 및 위도 / 경도 좌표가 포함된다.
         * @return  신주소(RoadAddress) 리스트
         */
        @field:SerializedName("roadAddress") val roadAddress: List<GeocodeAddressDetail>?) {

    /**
     * GeocodeAddress 정보를 POI 로 변환
     */
    fun getPoi(): List<Poi> {
        val list = mutableListOf<Poi>()
        parcelAddress?.mapNotNull {
            val address = it.getAddress()
            createPoi(it, address.getOldAddress())
        }?.let { poi ->
            list.addAll(poi)
        }

        roadAddress?.mapNotNull {
            val address = it.getAddress()
            createPoi(it, address.getNewAddress())
        }?.let { poi ->
            list.addAll(poi)
        }
        return list
    }

    private fun createPoi(
            geocodeDetail: GeocodeAddressDetail,
            addressString: String?): Poi? {
        var address = addressString
        val latLng = geocodeDetail.getLatLng()
        if (address.isNullOrEmpty()) {
            address = geocodeDetail.getAddress().getAddress()
        }
        return if (address.isNullOrEmpty() || latLng == null) {
            null
        } else {
            Poi(null, address, null, null,
                    latLng, geocodeDetail.getAddress(), 0.toDouble(), null)
        }
    }
}