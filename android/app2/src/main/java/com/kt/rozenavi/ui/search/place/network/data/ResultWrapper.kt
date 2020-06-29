package com.kt.rozenavi.ui.search.place.network.data

import com.kt.roze.RozeError

/**
 * 네트워크 통신 결과 전달 Sealed class
 */
sealed class ResultWrapper<out T> {
    /**
     * 성공 시나리오. 요청한 값 전달
     */
    data class Success<out T>(val value: T) : ResultWrapper<T>()

    /**
     * 실패 시나리오. 실패 원인 전달
     */
    data class GenericError(val error: RozeError? = null) : ResultWrapper<Nothing>()
}
