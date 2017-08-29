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
package com.kt.rozenavi.ui.main.route.view;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

class RouteDestinationTouchHelperCallback extends ItemTouchHelper.Callback {

    private final RouteDestinationRecyclerViewAdapter adapter;

    RouteDestinationTouchHelperCallback(RouteDestinationRecyclerViewAdapter adapter) {
        this.adapter = adapter;
    }

    /**
     * 드래그 기능 활성 / 비활성 처리
     * return true : 활성
     * return false : 비활성
     */
    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    /**
     * 슬라이딩 기능 활성 / 비활성 처리
     * return true : 활성
     * return false : 비활성
     */
    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    /**
     * 동작 플래그 처리
     * dragFlags : 드래그 플래그 처리
     * swipeFlags : 슬라이딩 플래그 처리
     */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        int swipeFlags = 0;

        return makeMovementFlags(dragFlags, swipeFlags);
    }

    /**
     * View 이동 처리
     */
    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    /**
     * View 삭제 처리
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    }
}
