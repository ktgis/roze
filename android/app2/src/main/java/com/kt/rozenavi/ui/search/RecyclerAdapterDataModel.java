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

package com.kt.rozenavi.ui.search;

/**
 * recyclerviewadpater 모델 인터페이스 클래스
 * 아이템을 추가하거나 반환하고 필요시 전체 삭제하는 기능을 제공
 */
interface RecyclerAdapterDataModel<T> {
    /**
     * 아이템 추가
     *
     * @param item 아이템 객체
     */
    void add(T item);

    /**
     * 아이템 반환
     *
     * @param index 아이템 index
     * @return 아이템 객체
     */
    T getItem(int index);

    /**
     * 아이템 전체 삭제
     */
    void clear();
}
