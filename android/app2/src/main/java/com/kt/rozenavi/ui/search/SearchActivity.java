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
package com.kt.rozenavi.ui.search;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.roze.search.SearchLocationManager;
import com.kt.roze.search.model.SearchPlaceData;
import com.kt.roze.search.model.SearchPlaces;
import com.kt.roze.util.JsonFileUtil;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.search.model.RecentDestination;
import com.kt.rozenavi.utils.UIUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnTextChanged;

/**
 * 목적지 검색 Activity
 * 키워드를 이용하여 목적지를 검색하여 반환
 */
public class SearchActivity extends AppCompatActivity
        implements View.OnClickListener, SearchLocationManager.SearchManagerListener {
    /**
     * 검색 결과 x 좌표
     */
    public static final String RESULT_EXTRA_COORD_X = "x";
    /**
     * 검색 결과 y 좌표
     */
    public static final String RESULT_EXTRA_COORD_Y = "y";
    /**
     * 검색 결과 장소명
     */
    public static final String RESULT_EXTRA_DESTINATION_NAME = "name";

    @BindView(R.id.recyclerView_locations)
    protected RecyclerView searchResultView;
    @BindView(R.id.empty_textview)
    protected TextView emptyTextView;
    @BindView(R.id.edit_search_keyword)
    protected TextView searchTextView;
    @BindView(R.id.btn_top_button)
    protected ImageView topButton;
    @BindView(R.id.cancel_container)
    protected RelativeLayout cancleButton;
    @BindView(R.id.toggle_button)
    protected ImageView toggleButton;
    @BindDrawable(R.drawable.btn_back)
    protected Drawable backIcon;

    /**
     * 결과 recyclerview용 LinearLayoutManager
     */
    LinearLayoutManager mLayoutManager;
    /**
     * 결과 recyclerview adpater
     */
    private SearchRecyclerViewAdapter searchRecyclerViewAdapter = null;
    /**
     * 목적지 검색 객체
     */
    private SearchLocationManager searchLocationManager;
    /**
     * 이전 검색 데이터 모델
     */
    private RecentDestination recentDestination;

    private int numOfPage;
    private int totalCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        searchLocationManager = new SearchLocationManager();
        recentDestination = new RecentDestination();
        initSearchView();
        initRecentDestination();

        searchResultView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if(mLayoutManager.getChildCount() + mLayoutManager.findFirstVisibleItemPosition() >= mLayoutManager.getItemCount()) {

                        if(mLayoutManager.getItemCount() == totalCount) {
                            UIUtils.showToast(getApplicationContext(), R.string.toast_message_last_search_result);
                        } else if(searchTextView.getText().length() > 0) {
                            numOfPage++;
                            searchLocation(searchTextView.getText().toString(), numOfPage);
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0) {
                    topButton.setVisibility(View.VISIBLE);
                }
                if(dy < 0) {
                    topButton.setVisibility(View.GONE);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    @OnClick(R.id.btn_top_button)
    public void onClickTopButton(View v){
        topButton.setVisibility(View.GONE);
        searchResultView.setLayoutFrozen(true);
        mLayoutManager.scrollToPositionWithOffset(0, 0);
        searchResultView.setLayoutFrozen(false);
    }

    @Override
    public void finish() {
        super.finish();
        //종료 애니메이션 삭제
        overridePendingTransition(0, 0);
    }

    /**
     * 검색 바 화면 초기화
     */
    private void initSearchView() {
        toggleButton.setImageDrawable(backIcon);
    }

    /**
     * 이전 검색 결과 있는경우 리스트에 표시
     */
    private void initRecentDestination() {
        List<SearchPlaceData> placeList;
        //이전 검색 데이터 모델 생성
        recentDestination = JsonFileUtil.loadJsonFile(
                new File(getExternalFilesDir(null), RecentDestination.HISTORY_FILE_NAME),
                RecentDestination.class);
        if (recentDestination != null) {
            placeList = new ArrayList<>(recentDestination.getDestinations());
        } else {
            placeList = new ArrayList<>();
        }

        setRecentDestination(placeList);
    }

    /**
     * 뒤로 가기 버튼 클릭 시 Activity 종료
     */
    @OnClick(R.id.toggle_container)
    public void onBackClick(View view) {
        finish();
    }

    /**
     * 취소 버튼 클릭 시 검색 바 텍스트 삭제 처리
     */
    @OnClick(R.id.cancel_container)
    public void onCancleClick(View view) {
        searchTextView.setText("");
    }

    /**
     * 검색 바 텍스트 입력 시 취소 버튼 Visible / Gone 처리
     */
    @OnTextChanged(R.id.edit_search_keyword)
    public void onSearchTextChanged(CharSequence s) {
        if (s.length() > 0) {
            cancleButton.setVisibility(View.VISIBLE);
            searchTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0);
        } else {
            cancleButton.setVisibility(View.GONE);
            searchTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.search_bar_search_icon, 0);
        }
    }

    @OnEditorAction(R.id.edit_search_keyword)
    public boolean onSearch(TextView v) {
        UIUtils.hideKeyboard(this, getCurrentFocus());
        String keyword = v.getText().toString();
        if (TextUtils.isEmpty(keyword)) {
            keyword = "서초소방서";
        }
        numOfPage = 0;
        if(searchRecyclerViewAdapter != null) {
            searchRecyclerViewAdapter.clearData();
        }

        searchLocation(keyword, numOfPage);
        return true;
    }

    /**
     * 목적지 키워드 검색
     *
     * @param keyword 키워드
     */
    public void searchLocation(String keyword, int numOfPage) {
        searchLocationManager.search(keyword, this, numOfPage);
    }

    /**
     * 검색 결과 리스트에서 선택한 항목을 이전검색데이터에 저장
     *
     * @param data 검색 데이터
     */
    private void saveRecentDestination(SearchPlaceData data) {
        if (data.x == 0 || data.y == 0) {
            return;
        }

        if (recentDestination == null) {
            recentDestination = new RecentDestination();
        }
        recentDestination.addPlaceData(data);

        boolean isSuccess = JsonFileUtil.saveJsonFile(
                new File(getExternalFilesDir(null), RecentDestination.HISTORY_FILE_NAME),
                recentDestination);
        sendResult(isSuccess, isSuccess ? data : null);
    }

    /**
     * 검색 데이터 리스트를 결과 리스트에 설정
     *
     * @param placeList 검색 데이터 리스트
     */
    public void setRecentDestination(List<SearchPlaceData> placeList) {
        if (placeList.size() == 0) {
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
        }
        if(searchResultView.getAdapter() != null) {
            if(searchRecyclerViewAdapter != null) {
                for(SearchPlaceData item : placeList){
                    searchRecyclerViewAdapter.add(item);
                }
            }
        } else {

            searchRecyclerViewAdapter = new SearchRecyclerViewAdapter(placeList, this);
            searchResultView.setAdapter(searchRecyclerViewAdapter);
            mLayoutManager = new LinearLayoutManager(getApplicationContext());
            searchResultView.setLayoutManager(mLayoutManager);



        }
    }

    /**
     * 이전검색 데이터에 저장 성공여부에 맞추어 검색결과를 이전화면으로 전달
     *
     * @param isSuccess       저장 성공여부
     * @param searchPlaceData 검색 데이터
     */
    public void sendResult(boolean isSuccess, SearchPlaceData searchPlaceData) {
        if (isSuccess) {
            Intent intentToReturn = new Intent();
            intentToReturn.putExtra(RESULT_EXTRA_COORD_X, searchPlaceData.x);
            intentToReturn.putExtra(RESULT_EXTRA_COORD_Y, searchPlaceData.y);
            intentToReturn.putExtra(RESULT_EXTRA_DESTINATION_NAME, searchPlaceData.name);
            setResult(RESULT_OK, intentToReturn);
            finish();
        } else {
            UIUtils.showToast(this, R.string.toast_message_save_recent_fail);
        }
    }

    //--View.OnClickListener
    @Override
    public void onClick(View v) {
        //click된 view를 이용하여 index 반환
        int itemPosition = searchResultView.getChildAdapterPosition(v);
        SearchPlaceData placeData = searchRecyclerViewAdapter.getItem(itemPosition);
        //이전 검색데이터에 저장
        saveRecentDestination(placeData);
    }
    //--View.OnClickListener

    //--SearchLocationManager.SearchManagerListener
    @Override
    public void onSearchFinished(SearchPlaces searchPlaces) {
        totalCount = searchPlaces.TotalCount;
        setRecentDestination(searchPlaces.data);
    }

    @Override
    public void onSearchFailed() {
        UIUtils.showToast(this, R.string.toast_message_search_fail);
    }
    //--SearchLocationManager.SearchManagerListener
}
