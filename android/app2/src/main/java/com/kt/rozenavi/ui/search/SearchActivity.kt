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
package com.kt.rozenavi.ui.search

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kt.geom.model.UTMK
import com.kt.roze.util.JsonFileUtil
import com.kt.rozenavi.R
import com.kt.rozenavi.ui.search.model.RecentDestination
import com.kt.rozenavi.ui.search.place.model.poi.Poi
import com.kt.rozenavi.ui.search.viewmodel.SearchViewModel
import com.kt.rozenavi.utils.UIUtils
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.layout_search_bar.*
import java.io.File
import java.util.*

/**
 * 목적지 검색 Activity
 * 키워드를 이용하여 목적지를 검색하여 반환
 */
class SearchActivity : AppCompatActivity(), OnKeyboardVisibilityListener {
    companion object {
        /**
         * 검색 결과 UTMK X 좌표
         */
        const val RESULT_EXTRA_COORD_X = "x"

        /**
         * 검색 결과 UTMK Y 좌표
         */
        const val RESULT_EXTRA_COORD_Y = "y"

        /**
         * 목적지명
         */
        const val RESULT_EXTRA_DESTINATION_NAME = "name"

    }

    /**
     * 결과 recyclerview adpater
     */
    private lateinit var searchRecyclerViewAdapter: SearchRecyclerViewAdapter
    private lateinit var autocompleteRecyclerViewAdapter: AutocompleteRecyclerViewAdapter

    /**
     * 결과 recyclerview 용 LinearLayoutManager
     */
    private var mLayoutManager: LinearLayoutManager = LinearLayoutManager(this)
    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        initData()
        initView()
        initObserver()
    }

    private fun initData() {
        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        setKeyboardVisibilityListener(this)
    }

    private fun setKeyboardVisibilityListener(onKeyboardVisibilityListener: OnKeyboardVisibilityListener) {
        val parentView = (findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0)
        parentView.viewTreeObserver.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            private var alreadyOpen = false
            private val defaultKeyboardHeightDP = 100
            private val EstimatedKeyboardDP = defaultKeyboardHeightDP + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) 48 else 0
            private val rect = Rect()
            override fun onGlobalLayout() {
                val estimatedKeyboardHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        EstimatedKeyboardDP.toFloat(), parentView.resources.displayMetrics).toInt()
                parentView.getWindowVisibleDisplayFrame(rect)
                val heightDiff = parentView.rootView.height - (rect.bottom - rect.top)
                val isShown = heightDiff >= estimatedKeyboardHeight
                if (isShown == alreadyOpen) {
                    Log.i("Keyboard state", "Ignoring global layout change...")
                    return
                }
                alreadyOpen = isShown
                onKeyboardVisibilityListener.onVisibilityChanged(isShown)
            }
        })
    }

    private fun initView() {
        initAutoCompleteRecyclerview()
        initPoiRecyclerview()
        initSearchView()
        initRecentDestination()
    }

    private fun initAutoCompleteRecyclerview() {
        autocompleteRecyclerViewAdapter = AutocompleteRecyclerViewAdapter(ArrayList(),
                View.OnClickListener { v ->
                    val itemPosition = recyclerView_autocompletes.getChildAdapterPosition(v)
                    val keyword = autocompleteRecyclerViewAdapter.getItem(itemPosition)
                    searchRecyclerViewAdapter.clearData()
                    searchLocation(keyword)
                })
        recyclerView_autocompletes.adapter = autocompleteRecyclerViewAdapter
        recyclerView_autocompletes.layoutManager = LinearLayoutManager(this)
    }

    private fun initPoiRecyclerview() {
        val pois: List<Poi> = ArrayList()
        searchRecyclerViewAdapter = SearchRecyclerViewAdapter(pois, View.OnClickListener { v: View? ->
            val childView = v ?: return@OnClickListener
            val itemPosition = recyclerView_locations.getChildAdapterPosition(childView)
            val poiData = searchRecyclerViewAdapter.getItem(itemPosition)
            //이전 검색데이터에 저장
            saveRecentDestination(poiData)
        })
        recyclerView_locations.adapter = searchRecyclerViewAdapter
        recyclerView_locations.layoutManager = mLayoutManager
        recyclerView_locations.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                //do nothing
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    btn_top_button.visibility = View.VISIBLE
                }
                if (dy < 0) {
                    btn_top_button.visibility = View.GONE
                }
                super.onScrolled(recyclerView, dx, dy)
            }
        })

        //리스트 Top 버튼 클릭 시
        btn_top_button.setOnClickListener {
            btn_top_button.visibility = View.GONE
            mLayoutManager.scrollToPositionWithOffset(0, 0)
        }
    }

    /**
     * 검색 바 화면 초기화
     */
    private fun initSearchView() {
        //상단 좌측 < 버튼 클릭 시
        toggle_container.setOnClickListener {
            finish()
        }
        toggle_button.setImageResource(R.drawable.btn_back)
        //검색 바 데이터 변경
        edit_search_keyword.doOnTextChanged { text, _, _, _ ->
            val s = text?.toString()
            if (s.isNullOrEmpty()) {
                autocompleteRecyclerViewAdapter.clearData()
                return@doOnTextChanged
            }
            autoComplete(s.toString())
        }
        //검색 실행 시
        edit_search_keyword.setOnEditorActionListener { v, _, _ ->
            if (edit_search_keyword.length() > 0) {
                searchRecyclerViewAdapter.clearData()
                searchLocation(v.text.toString())
                true
            } else {
                false
            }
        }

        //취소 버튼 클릭 시 검색 바 텍스트 삭제 처리
        cancel_container.setOnClickListener {
            edit_search_keyword.setText("")
        }
    }

    /**
     * 이전 검색 결과 있는경우 리스트에 표시
     */
    private fun initRecentDestination() {
        val placeList: List<Poi>?
        //이전 검색 데이터 모델 생성
        val prevDestination = getRecentDestination()
        placeList = if (prevDestination != null) {
            ArrayList(prevDestination.destinations)
        } else {
            null
        }
        setRecentDestination(placeList)
    }

    private fun initObserver() {
        viewModel.toastMessage.observe(this, androidx.lifecycle.Observer {
            UIUtils.showToast(this, it)
        })

        viewModel.searchResult.observe(this, androidx.lifecycle.Observer { place ->
            //검색 결과로 수신한 POI를 표출한다.
            val placePois: MutableList<Poi> = ArrayList()
            //주소 검색 결과
            val residentialPoi = place?.getResidentialPoiList()
            if (!residentialPoi.isNullOrEmpty()) {
                placePois.addAll(residentialPoi)
            }

            place?.let {
                if (it.getResultCount() > 0) {
                    it.pois?.let { poiList ->
                        placePois.addAll(poiList)
                    }
                }
            }

            if (placePois.size > 0) {
                setRecentDestination(placePois)
            } else {
                UIUtils.showToast(applicationContext, "검색 결과가 없습니다.")
            }
        })

        viewModel.autoCompleteResult.observe(this, androidx.lifecycle.Observer { autoCompleteResult ->
            autocompleteRecyclerViewAdapter.clearData()
            if (autoCompleteResult.isNullOrEmpty()) {
                return@Observer
            }
            for (string in autoCompleteResult) {
                autocompleteRecyclerViewAdapter.add(string)
            }
        })
    }

    /**
     * 목적지 키워드 검색
     *
     * @param keyword 키워드
     */
    private fun autoComplete(keyword: String) {
        if (keyword.isEmpty()) {
            autocompleteRecyclerViewAdapter.clearData()
            return
        }
        viewModel.requestAutoComplete(keyword)
    }

    /**
     * 목적지 키워드 검색
     *
     * @param keyword 키워드
     */
    private fun searchLocation(keyword: String) {
        edit_search_keyword.clearFocus()
        UIUtils.hideKeyboard(this, edit_search_keyword)
        viewModel.requestSearchPoi(keyword)
    }

    private fun showAutoComplete() {
        autocomplete_container.visibility = View.VISIBLE
        autocompleteRecyclerViewAdapter.clearData()
        if (edit_search_keyword.length() > 0) {
            autoComplete(edit_search_keyword.text.toString())
        }
    }

    private fun hideAutoComplete() {
        autocomplete_container.visibility = View.GONE
    }

    /**
     * 검색 결과 리스트에서 선택한 항목을 이전검색데이터에 저장
     *
     * @param data 검색 데이터
     */
    private fun saveRecentDestination(data: Poi) {
        val utmk = data.getUTMK() ?: return
        if (utmk.x == 0.0 || utmk.y == 0.0) {
            return
        }
        val recentDestination = getRecentDestination()
                ?: RecentDestination()

        recentDestination.addPlaceData(data)
        val isSuccess = JsonFileUtil.saveJsonFile(
                File(getExternalFilesDir(null),
                        RecentDestination.HISTORY_FILE_NAME),
                recentDestination
        )
        val sendResult = if (isSuccess) data else null
        sendResult(isSuccess, sendResult)
    }

    /**
     * 검색 데이터 리스트를 결과 리스트에 설정
     *
     * @param placeList 검색 데이터 리스트
     */
    private fun setRecentDestination(placeList: List<Poi>?) {
        if (placeList.isNullOrEmpty()) {
            empty_textview.visibility = View.VISIBLE
            return
        } else {
            empty_textview.visibility = View.GONE
        }

        if (recyclerView_locations.adapter != null) {
            for (poi in placeList) {
                searchRecyclerViewAdapter.add(poi)
            }
        }
    }

    /**
     * 이전검색 데이터에 저장 성공여부에 맞추어 검색결과를 이전화면으로 전달
     *
     * @param isSuccess       저장 성공여부
     * @param searchPlaceData 검색 데이터
     */


    private fun sendResult(isSuccess: Boolean, searchPlaceData: Poi?) {
        val coord: UTMK? = searchPlaceData?.getUTMK()
        if (!isSuccess || coord == null) {
            UIUtils.showToast(this, R.string.toast_message_save_recent_fail)
            return
        }


        val intentToReturn = Intent()
        intentToReturn.putExtra(RESULT_EXTRA_COORD_X, coord.x)
        intentToReturn.putExtra(RESULT_EXTRA_COORD_Y, coord.y)
        intentToReturn.putExtra(RESULT_EXTRA_DESTINATION_NAME, searchPlaceData.getPoiName())
        setResult(Activity.RESULT_OK, intentToReturn)
        finish()
    }

    private fun getRecentDestination(): RecentDestination? {
        return JsonFileUtil.loadJsonFile(
                File(getExternalFilesDir(null), RecentDestination.HISTORY_FILE_NAME),
                RecentDestination::class.java)
    }

    /**
     * 키보드 표출 여부 따라 자동완성 리스트를  보임(키보드 표출 시) /
     * 숨김 (키보드 숨김 시)처리 한다.
     */
    override fun onVisibilityChanged(visible: Boolean) {
        if (visible) {
            showAutoComplete()
        } else {
            hideAutoComplete()
        }
    }
}