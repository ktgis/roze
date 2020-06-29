package com.kt.rozenavi.ui.search.repository

import com.kt.rozenavi.ui.search.place.network.NetworkService
import com.kt.rozenavi.ui.search.place.network.api.GeoApiService
import com.kt.rozenavi.ui.search.place.network.data.ResultWrapper
import com.kt.rozenavi.ui.search.place.network.utils.NetworkUtils
import com.kt.rozenavi.ui.search.place.model.Place
import com.kt.rozenavi.ui.search.place.model.autocomplete.AutoComplete
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit

/**
 * 검색 기능 관련 Repository
 */
class PlaceRepository private constructor(retrofit: Retrofit) {
    companion object {
        private const val NUMBER_OF_RESULTS = 20
        fun of(key: String): PlaceRepository? {
            val retrofit = NetworkService.getRetrofitWithCoroutine(
                    "https://gis.kt.com",
                    key

            ) ?: return null
            return PlaceRepository(retrofit)
        }
    }

    private val geoApiService: GeoApiService = NetworkService.getGeoApiService(retrofit)

    suspend fun searchPoi(keyword: String)
            : ResultWrapper<Place> {
        return NetworkUtils.safeApiCall(Dispatchers.IO) {
            requestPoiSearch(
                    keyword = keyword
            )
        }
    }

    private suspend fun requestPoiSearch(keyword: String)
            : Place {
        return geoApiService.normalSearch(
                NUMBER_OF_RESULTS,
                "NAVIGATION",
                "OFF",
                keyword)
    }

    suspend fun autoCompleteSearch(keyword: String): ResultWrapper<AutoComplete> {
        return NetworkUtils.safeApiCall(Dispatchers.IO) {
            geoApiService.autoCompleteSearch(keyword)
        }
    }
}