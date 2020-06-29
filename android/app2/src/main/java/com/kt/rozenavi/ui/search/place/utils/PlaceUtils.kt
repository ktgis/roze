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
package com.kt.rozenavi.place.utils

import com.kt.geom.model.Coord
import com.kt.geom.model.LatLng
import com.kt.geom.model.UTMK

/**
 * 검색에 필요한 공통 기능을 모아놓은 유틸리티 클래스
 */
object PlaceUtils {
    /**
     * 입력받은 좌표를 WGS84좌표로 변환
     * @param coord     공통 좌표
     * @return          WGS84 좌표
     */
    fun convertLocationToLatLng(coord: Coord): LatLng {
        return if (coord is UTMK) {
            LatLng.valueOf(coord)
        } else coord as LatLng
    }

    /**
     * 문자열 좌 * 우측의 공백을 제거하는 메소드
     *
     * @param string 대상 문자열
     * @return trimed string with white space removed from stard to end.
     */
    private fun trim(string: String): String {
        return string.trim { it <= ' ' }
    }

    fun getSingleStringWithDelimiter(delimiter: String, vararg args: String?): String {
        if (args.isEmpty()) {
            return ""
        }
        val builder = StringBuilder()
        for (s in args) {
            if (s != null) {
                builder.append(s)
                builder.append(delimiter)
            }
        }
        return trim(builder.toString())
    }
}