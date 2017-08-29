/*
 *  Copyright (c) 2016 kt corp. All rights reserved.
 *
 *  This is a proprietary software of kt corp, and you may not use this file
 *  except in compliance with license agreement with kt corp. Any redistribution
 *  or use of this software, with or without modification shall be strictly
 *  prohibited without prior written approval of kt corp, and the copyright
 *   notice above does not evidence any actual or intended publication of such
 *  software.
 *
 */

package com.kt.rozenavi.ui.search.model;


import com.kt.roze.search.model.SearchPlaceData;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;

/**
 * 이전 검색 데이터 모델 클래스
 * 검색했던 목적지 정보를 저장 및 로드하는 기능을 제공
 * {@link com.kt.roze.util.JsonFileUtil}을 통해 생성
 */
@AllArgsConstructor
public class RecentDestination {
    /**
     * 검색 데이터 저장 파일명
     */
    public static final String HISTORY_FILE_NAME = "recentDestination.json";
    /**
     * 최대 저장 데이터 개수
     */
    private static final int RECENT_DEST_COUNT = 10;
    /**
     * 이전 검색 데이터 리스트
     */
    private List<SearchPlaceData> destinations;

    public RecentDestination() {
        this(new ArrayList<SearchPlaceData>());
    }

    /**
     * 이전 검색 데이터 리스트 반환
     *
     * @return 이전 검색 데이터
     */
    public List<SearchPlaceData> getDestinations() {
        if (destinations == null) {
            destinations = new ArrayList<>();
        }
        return destinations;
    }

    /**
     * 검색 데이터 추가
     * 최대 개수가 넘는 경우 가장 먼저 들어온 데이터 순으로 삭제
     *
     * @param data 검색 데이터
     */
    public void addPlaceData(SearchPlaceData data) {
        int removeIndex = getPlaceDataIndex(data);
        if (removeIndex != -1) {
            destinations.remove(removeIndex);
        }

        destinations.add(0, data);

        if (destinations.size() > RECENT_DEST_COUNT) {
            destinations.remove(destinations.size() - 1);
        }
    }

    /**
     * 검색 데이터 index 반환
     * 검색 데이터 리스트에서 파라매터로 전달된 데이터의 index 반환
     * 포함되어있지 않은경우 -1 반환
     *
     * @param data 검색 데이터 객체
     * @return 데이터 index
     */
    private int getPlaceDataIndex(SearchPlaceData data) {
        SearchPlaceData place;
        for (int i = 0; i < destinations.size(); i++) {
            place = destinations.get(i);
            if (place.name.equalsIgnoreCase(data.name)) {
                return i;
            }
        }

        return -1;
    }
}
