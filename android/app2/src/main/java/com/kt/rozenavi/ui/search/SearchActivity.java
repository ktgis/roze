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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.geom.model.UTMK;
import com.kt.place.PlaceManager;
import com.kt.place.data.PlaceOptionData;
import com.kt.place.listener.AutoCompleteListener;
import com.kt.place.listener.PlaceApiListener;
import com.kt.place.model.Place;
import com.kt.place.model.PlaceError;
import com.kt.place.model.geocode.GeocodeAddress;
import com.kt.place.model.geocode.GeocodeAddressDetail;
import com.kt.place.model.poi.Address;
import com.kt.place.model.poi.Poi;
import com.kt.place.model.poi.SpecifiablePoi;
import com.kt.roze.util.JsonFileUtil;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.search.model.RecentDestination;
import com.kt.rozenavi.utils.UIUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
public class SearchActivity extends AppCompatActivity implements OnKeyboardVisibilityListener {

    /**
     * GeocodeAddress 처리 구분 변수
     */
    private static final int PARCELADDRESS = 0;
    private static final int ROADADDRESS = 1;
    /**
     * 결과 recyclerview adpater
     */
    private SearchRecyclerViewAdapter searchRecyclerViewAdapter = null;
    private AutocompleteRecyclerViewAdapter autocompleteRecyclerViewAdapter = null;
    /**
     * 목적지 검색 객체
     */
    private PlaceManager placeManager;
    /**
     * 이전 검색 데이터 모델
     */
    private RecentDestination recentDestination;
    /**
     * 결과 recyclerview용 LinearLayoutManager
     */
    private LinearLayoutManager mLayoutManager;

    private int numOfPage;
    private int totalCount;
    private int subTotalCount;
    private final int RESULT_COUNT = 20;
    private long baseTime;

    @BindView(R.id.recyclerView_locations)
    protected RecyclerView searchResultView;
    @BindView(R.id.autocomplete_container)
    protected RelativeLayout autocompleteContainer;
    @BindView(R.id.recyclerView_autocompletes)
    protected RecyclerView autocompleteResultView;
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

    private void initAutocompleRecyclerview() {
        List<String> autocompletes = new ArrayList<>();
        autocompleteRecyclerViewAdapter = new AutocompleteRecyclerViewAdapter(autocompletes,
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int itemPosition = autocompleteResultView.getChildAdapterPosition(v);
                        String keyword = autocompleteRecyclerViewAdapter.getItem(itemPosition);
                        numOfPage = 0;
                        searchRecyclerViewAdapter.clearData();
                        searchResultView.smoothScrollToPosition(0);
                        searchLocation(keyword, numOfPage);
                    }
                });
        autocompleteResultView.setAdapter(autocompleteRecyclerViewAdapter);
        autocompleteResultView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initPoiRecyclerview() {
        List<Poi> pois = new ArrayList<>();
        searchRecyclerViewAdapter = new SearchRecyclerViewAdapter(pois, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = searchResultView.getChildAdapterPosition(v);
                Poi poiData = searchRecyclerViewAdapter.getItem(itemPosition);
                //이전 검색데이터에 저장
                saveRecentDestination(poiData);
            }
        });

        mLayoutManager = new LinearLayoutManager(this);
        searchResultView.setAdapter(searchRecyclerViewAdapter);
        searchResultView.setLayoutManager(mLayoutManager);

        searchResultView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (mLayoutManager.getChildCount() + mLayoutManager.findFirstVisibleItemPosition()
                            >= mLayoutManager.getItemCount()) {
                        if (searchRecyclerViewAdapter.getItemCount() - subTotalCount == totalCount) {
                            UIUtils.showToast(getApplicationContext(), "마지막 검색 결과입니다.");
                        } else if (searchTextView.getText().length() > 0) {
                            numOfPage++;
                            searchLocation(searchTextView.getText().toString(), numOfPage);
                        }
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    topButton.setVisibility(View.VISIBLE);
                }
                if (dy < 0) {
                    topButton.setVisibility(View.GONE);
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });
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
        List<Poi> placeList;
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
     * 목적지 키워드 검색
     *
     * @param keyword 키워드
     */
    private void autoComplete(String keyword) {
        baseTime = System.currentTimeMillis();
        lastString = keyword;
        isLockedAutoComplete = true;
        if (keyword.length() == 0) {
            isLockedAutoComplete = false;
            autocompleteRecyclerViewAdapter.clearData();
            return;
        }
        placeManager.autoCompleteSearch(keyword, null, new AutoCompleteListener() {
            @Override
            public void onSuccess(List<String> placeAutoCompletes) {
                isLockedAutoComplete = false;
                lastString = "";
                autocompleteRecyclerViewAdapter.clearData();
                if (!placeAutoCompletes.isEmpty() && searchTextView.getText().length() > 0) {
                    for (String string : placeAutoCompletes) {
                        autocompleteRecyclerViewAdapter.add(string);
                    }
                } else {
                    autocompleteRecyclerViewAdapter.clearData();
                }
            }

            @Override
            public void onFail(PlaceError placeError) {
                isLockedAutoComplete = false;
                lastString = "";
                UIUtils.showToast(getApplicationContext(), placeError.getErrCode() + " : " + placeError.getErrMsg());
            }
        });
    }

    /**
     * 목적지 키워드 검색
     *
     * @param keyword 키워드
     */
    private void searchLocation(String keyword, final int numOfPage) {
        searchTextView.clearFocus();
        UIUtils.hideKeyboard(this, searchTextView);
        placeManager.normalSearch(keyword, null, numOfPage, RESULT_COUNT, PlaceOptionData.SearchMode.NAVIGATION, null,
                new PlaceApiListener() {
                    @Override
                    public void onSuccess(Place placeData) {
                        /**
                         * POI 결과 세팅 용 List
                         */
                        List<Poi> placePois = new ArrayList<>();
                        // page 0일때 specifiablepoi, residentialaddress 설정
                        if (numOfPage == 0) {
                            placePois.addAll(setSpecifiablePois(placeData));
                            placePois.addAll(setResidentialAddressPois(placeData));
                            subTotalCount = placePois.size();
                        }

                        // poi
                        if (placeData.getResultCount() > 0) {
                            totalCount = placeData.getTotalCount();
                            for (Poi poi : placeData.getPois()) {
                                placePois.add(poi);
                            }
                        }

                        if (placePois.size() > 0) {
                            setRecentDestination(placePois);
                        } else {
                            UIUtils.showToast(getApplicationContext(), "검색 결과가 없습니다.");
                        }
                    }

                    @Override
                    public void onFail(PlaceError placeError) {
                        UIUtils.showToast(getApplicationContext(),
                                placeError.getErrCode() + " : " + placeError.getErrMsg());
                    }
                });
    }

    private List<Poi> setSpecifiablePois(Place placeData) {
        List<Poi> placePois = new ArrayList<>();
        if (placeData.getSpecifiablePoiInfo() != null) {
            List<SpecifiablePoi> specifiablePois = placeData.getSpecifiablePois();
            for (SpecifiablePoi specifiablePoi : specifiablePois) {
                Address address = new Address(null, specifiablePoi.getSiDo(),
                        specifiablePoi.getSiGunGu(), specifiablePoi.getEupMyeonDong(), null, null,
                        null, null);
                Poi poi = new Poi(specifiablePoi.getId(), specifiablePoi.getName(),
                        specifiablePoi.getSubName(), specifiablePoi.getBranchName(),
                        specifiablePoi.getLatLng(), address, 0, null, null, null, null);
                placePois.add(poi);
            }
        }
        return placePois;
    }

    private List<Poi> setResidentialAddressPois(Place placeData) {
        List<Poi> placePois = new ArrayList<>();
        if (placeData.getResidentialAddress() != null) {
            List<GeocodeAddress> geocodeAddresses = placeData.getResidentialAddress();
            for (GeocodeAddress geocodeAddress : geocodeAddresses) {
                placePois.addAll(setResidentialAddressDetailPois(geocodeAddress.getParcelAddress(), PARCELADDRESS));
                placePois.addAll(setResidentialAddressDetailPois(geocodeAddress.getRoadAddress(), ROADADDRESS));
            }
        }
        return placePois;
    }

    private List<Poi> setResidentialAddressDetailPois(List<GeocodeAddressDetail> detailAddressList, int detailType) {
        List<Poi> placePois = new ArrayList<>();
        if (detailAddressList != null) {
            for (GeocodeAddressDetail detailAddress : detailAddressList) {
                Poi poi = null;
                Address address = convertAddressToGeocodeAddress(detailAddress);
                switch (detailType) {
                    case PARCELADDRESS :
                        poi = new Poi(null,
                                (((address.getEupMyeonDong() == null ? ""
                                        : address.getEupMyeonDong()) + " " + (
                                        address.getRi() == null ? "" : address.getRi())).trim()
                                        + " " + (address.getHouseNumber() == null ? ""
                                        : address.getHouseNumber())).trim(), null, null,
                                detailAddress.getGeocodeCoordInfo().getLatLng(), address, 0, null,
                                null, null, null);
                        break;
                    case ROADADDRESS :
                        poi = new Poi(null,
                                ((address.getStreet() == null ? "" : address.getStreet()) + " " +
                                        (address.getStreetNumber() == null ? ""
                                                : address.getStreetNumber())).trim(), null, null,
                                detailAddress.getGeocodeCoordInfo().getLatLng(), address, 0, null,
                                null, null, null);
                        break;
                }
                placePois.add(poi);
            }
        }
        return placePois;
    }

    /**
     * GeocodeAddress 정보를 Poi 데이터 설정을 위해 Address 정보로 변환
     * @param geocodeAddress GeocodeAddress 정보 (ParcelAddress : 구 주소 정보 / RoadAddress : 신 주소 정보)
     * @return Address 객체
     */
    private Address convertAddressToGeocodeAddress(GeocodeAddressDetail geocodeAddress) {
        return new Address(geocodeAddress.getCountry(),
                geocodeAddress.getSiDo(), geocodeAddress.getSiGunGu(),
                geocodeAddress.getEupMyeonDong(),
                geocodeAddress.getStreet(), geocodeAddress.getStreetNumber(),
                geocodeAddress.getRi(), geocodeAddress.getHouseNumber());
    }

    private void hideAutoComplete() {
        if (autocompleteResultView != null) {
            autocompleteContainer.setVisibility(View.GONE);
        }
    }

    private void showAutoComplete() {
        if (autocompleteResultView != null) {
            autocompleteContainer.setVisibility(View.VISIBLE);
            autocompleteRecyclerViewAdapter.clearData();
            if (searchTextView.length() > 0) {
                autoComplete(searchTextView.getText().toString());
            }
        }
    }

    private Timer autoCompleteTimer;
    private boolean isLockedAutoComplete = false;

    private void stopAutoCompleteTimer() {
        if (autoCompleteTimer != null) {
            autoCompleteTimer.cancel();
            autoCompleteTimer = null;
        }
    }

    private String lastString = "";

    private void setAutoCompleteTimer() {
        autoCompleteTimer = new Timer();
        autoCompleteTimer.schedule(new TimerTask() {
            @SuppressWarnings({"MissingPermission"})
            @Override
            public void run() {
                if (isLockedAutoComplete) {
                    stopAutoCompleteTimer();
                    setAutoCompleteTimer();
                } else {
                    final String keyword = searchTextView.getText().toString();
                    if ((TextUtils.isEmpty(keyword) && TextUtils.isEmpty(lastString))) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                autocompleteRecyclerViewAdapter.clearData();
                                return;
                            }
                        });
                    }

                    if (!keyword.equals(lastString)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                autoComplete(keyword);
                            }
                        });
                    }
                }
            }
        }, 400);
    }

    /**
     * 검색 결과 리스트에서 선택한 항목을 이전검색데이터에 저장
     *
     * @param data 검색 데이터
     */
    private void saveRecentDestination(Poi data) {
        UTMK utmk = data.getUTMK();
        if (utmk.x == 0 || utmk.y == 0) {
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
    private void setRecentDestination(List<Poi> placeList) {
        if (placeList.size() == 0) {
            emptyTextView.setVisibility(View.VISIBLE);
        } else {
            emptyTextView.setVisibility(View.GONE);
        }
        if (searchResultView.getAdapter() != null) {
            if (searchRecyclerViewAdapter != null) {
                for (Poi poi : placeList) {
                    searchRecyclerViewAdapter.add(poi);
                }
            }
        }
    }

    /**
     * 이전검색 데이터에 저장 성공여부에 맞추어 검색결과를 이전화면으로 전달
     *
     * @param isSuccess       저장 성공여부
     * @param searchPlaceData 검색 데이터
     */
    private void sendResult(boolean isSuccess, Poi searchPlaceData) {
        if (isSuccess) {
            UTMK utmk = searchPlaceData.getUTMK();
            Intent intentToReturn = new Intent();
            intentToReturn.putExtra(RESULT_EXTRA_COORD_X, utmk.x);
            intentToReturn.putExtra(RESULT_EXTRA_COORD_Y, utmk.y);
            intentToReturn.putExtra(RESULT_EXTRA_DESTINATION_NAME, searchPlaceData.getPoiName());
            setResult(RESULT_OK, intentToReturn);
            finish();
        } else {
            UIUtils.showToast(this, R.string.toast_message_save_recent_fail);
        }
    }

    private void setKeyboardVisibilityListener(final OnKeyboardVisibilityListener onKeyboardVisibilityListener) {
        final View parentView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        parentView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            private boolean alreadyOpen;
            private final int defaultKeyboardHeightDP = 100;
            private final int EstimatedKeyboardDP =
                    defaultKeyboardHeightDP + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? 48 : 0);
            private final Rect rect = new Rect();

            @Override
            public void onGlobalLayout() {
                int estimatedKeyboardHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                        EstimatedKeyboardDP, parentView.getResources().getDisplayMetrics());
                parentView.getWindowVisibleDisplayFrame(rect);
                int heightDiff = parentView.getRootView().getHeight() - (rect.bottom - rect.top);
                boolean isShown = heightDiff >= estimatedKeyboardHeight;

                if (isShown == alreadyOpen) {
                    Log.i("Keyboard state", "Ignoring global layout change...");
                    return;
                }
                alreadyOpen = isShown;
                onKeyboardVisibilityListener.onVisibilityChanged(isShown);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        setKeyboardVisibilityListener(this);

        placeManager = new PlaceManager("전달받은 API Key 입력");
        recentDestination = new RecentDestination();
        initAutocompleRecyclerview();
        initPoiRecyclerview();
        initSearchView();
        initRecentDestination();
    }

    @OnEditorAction(R.id.edit_search_keyword)
    protected boolean onRequestSearch(TextView v) {
        if (searchTextView.length() > 0) {
            numOfPage = 0;
            searchResultView.smoothScrollToPosition(0);
            searchRecyclerViewAdapter.clearData();
            searchLocation(v.getText().toString(), numOfPage);
            return true;
        }
        return false;
    }

    /**
     * 뒤로 가기 버튼 클릭 시 Activity 종료
     */
    @OnClick(R.id.toggle_container)
    protected void onBackClick(View view) {
        finish();
    }

    /**
     * 취소 버튼 클릭 시 검색 바 텍스트 삭제 처리
     */
    @OnClick(R.id.cancel_container)
    protected void onCancleClick(View view) {
        searchTextView.setText("");
    }

    @OnClick(R.id.btn_top_button)
    protected void onClickTopButton(View v) {
        topButton.setVisibility(View.GONE);
        searchResultView.setLayoutFrozen(true);
        mLayoutManager.scrollToPositionWithOffset(0, 0);
        searchResultView.setLayoutFrozen(false);
    }

    /**
     * 검색 바 데이터 변경
     */
    @OnTextChanged(R.id.edit_search_keyword)
    protected void onTextAfterChanged(Editable s) {
        if (baseTime == 0) {
            stopAutoCompleteTimer();
            baseTime = System.currentTimeMillis();
            autoComplete(s.toString());
        } else {
            if ((System.currentTimeMillis() - baseTime) > 400 && !isLockedAutoComplete) {
                stopAutoCompleteTimer();
                baseTime = System.currentTimeMillis();
                autocompleteRecyclerViewAdapter.clearData();
                // 자동완성 메서드 호출
                autoComplete(s.toString());
            } else {
                setAutoCompleteTimer();
            }
        }
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        if (visible) {
            showAutoComplete();
        } else {
            hideAutoComplete();
        }
    }

    @Override
    public void finish() {
        super.finish();
        //종료 애니메이션 삭제
        overridePendingTransition(0, 0);
    }
}
