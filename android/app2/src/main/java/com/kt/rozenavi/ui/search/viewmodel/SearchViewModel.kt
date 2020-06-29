package com.kt.rozenavi.ui.search.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.kt.roze.RozeError
import com.kt.rozenavi.KeyStore
import com.kt.rozenavi.ui.search.place.network.data.ResultWrapper
import com.kt.rozenavi.ui.search.place.model.Place
import com.kt.rozenavi.ui.search.repository.PlaceRepository
import kotlinx.coroutines.*
import java.lang.StringBuilder

/**
 * 검색 결과 및 자동완성 제공
 */
class SearchViewModel(application: Application) : AndroidViewModel(application) {
    private val placeRepository = PlaceRepository.of(KeyStore.gisKey)
    val searchResult = MutableLiveData<Place>()
    val autoCompleteResult = MutableLiveData<List<String>>()
    val toastMessage = MutableLiveData<String>()

    //자동 완성 변경 여부
    private var prevRequestString: String? = null

    fun requestSearchPoi(keyword: String) {
        val repository = placeRepository ?: return
        viewModelScope.launch(Dispatchers.Main) {
            when (val result = doSearchPoi(repository, keyword)) {
                is ResultWrapper.GenericError -> {
                    val errorMessage = getErrorMessage(result.error)
                    if (errorMessage.isNotEmpty()) {
                        toastMessage.value = errorMessage
                    }
                }
                is ResultWrapper.Success -> searchResult.value = result.value
            }
        }
    }

    private suspend fun doSearchPoi(repository: PlaceRepository,
                                    keyword: String)
            : ResultWrapper<Place> {
        return repository.searchPoi(
                keyword = keyword
        )
    }

    fun requestAutoComplete(keyword: String) {
        val repository = placeRepository ?: return
        if (keyword == prevRequestString) {
            return
        }
        prevRequestString = keyword
        viewModelScope.launch {
            delay(300)
            if (keyword != prevRequestString) {
                return@launch
            }

            when (val result = repository.autoCompleteSearch(keyword)) {
                is ResultWrapper.GenericError -> {
                    autoCompleteResult.value = emptyList()
                    val errorMessage = getErrorMessage(result.error)
                    if (errorMessage.isNotEmpty()) {
                        toastMessage.value = errorMessage
                    }
                }
                is ResultWrapper.Success -> autoCompleteResult.value =
                        result.value.getAutoCompleteString()
            }
        }
    }

    private fun getErrorMessage(error: RozeError?): String {
        val builder = StringBuilder()
        error?.code?.toString().let {
            builder.append("error code : $it ")
        }
        error?.message?.let {
            builder.append(": $it")
        }
        return builder.toString()
    }
}
