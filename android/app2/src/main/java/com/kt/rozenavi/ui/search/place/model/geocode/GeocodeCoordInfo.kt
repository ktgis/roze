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
import com.kt.geom.model.UTMK

/**
 * GeocodeCoordInfo 클래스는 지오코드 검색 시 검색된 주소의 좌표 정보를 담고있다.
 * 전달받은 GeocodeCoordInfo 객체를 통해 검색한 지오코드의 좌표정보를 추출할 수 있다.
 */
class GeocodeCoordInfo(
        /**
         * 검색된 지오코드의 UTMK 좌표 정보를 반환한다.
         * @return  UTMK 좌표 정보
         */
        @field:SerializedName("utmkPoint") val utmk: UTMK?,
        /**
         * 검색된 지오코드의 위도 / 경도 좌표 정보를 반환한다.
         * @return  위도 / 경도 좌표 정보
         */
        @field:SerializedName("point") val latLng: LatLng?)