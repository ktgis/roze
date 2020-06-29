package com.kt.rozenavi.ui.search.place.network.utils

import com.kt.roze.RozeError
import com.kt.rozenavi.ui.search.place.network.data.ResultWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

object NetworkUtils {
    suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher,
                                apiCall: suspend () -> T)
            : ResultWrapper<T> {
        return withContext(dispatcher) {
            try {
                ResultWrapper.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                val errorResponse = convertErrorBody(throwable)
                ResultWrapper.GenericError(errorResponse)
            }
        }
    }

    private fun convertErrorBody(throwable: Throwable): RozeError? {
        return try {
            RozeError(throwable)
        } catch (exception: Exception) {
            null
        }
    }
}
