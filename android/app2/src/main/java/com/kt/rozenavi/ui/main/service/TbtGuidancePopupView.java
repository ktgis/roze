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
package com.kt.rozenavi.ui.main.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.roze.guidance.RouteGuidanceListener;
import com.kt.roze.guidance.model.TurnGuidance;
import com.kt.roze.resource.TurnResourceManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.MainActivity;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTouch;

/**
 * TBT 정보를 표출한다.
 * TBT 정보는 첫번째 TBT정보만 출력한다.
 * TbtGuidancePopupView는 경로안내 시작 후 경로안내 종료 사이에 앱이 백그라운드 동작 시 출력된다.
 */
public class TbtGuidancePopupView extends RelativeLayout {

    @BindView(R.id.tbt_popup_imageView)
    protected ImageView tbtImageView;
    @BindView(R.id.tbt_popup_remainView)
    protected TextView tbtRemainTextView;

    private WindowManager windowManager;
    private int lastX;
    private int lastY;

    public int xPosition;
    public int yPosition;

    private static final int MAX_CLICK_DURATION = 100;
    private long startClickTime;

    public TbtGuidancePopupView(Context context) {
        super(context);
        initView(context);
    }

    public TbtGuidancePopupView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TbtGuidancePopupView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_guidance_tbt_popup, this);
        ButterKnife.bind(this);
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * 뷰에 첫번째 TBT 정보를 Set 한다.
     * TBT 이미지(항상), 거리정보(항상), 방면정보(존재시), 요금정보(존재시)
     *
     * @param firstTurn 첫번째 TBT Guidance
     */
    private void updateFirstTurn(TurnGuidance firstTurn) {
        NaviUtils.setSizeSpanDistance(tbtRemainTextView, firstTurn.nextDistance);

        int resId = TurnResourceManager.getResourceId(firstTurn.turnCode);
        if (resId > TurnResourceManager.RESOURCE_NOT_FOUND) {
            tbtImageView.setImageResource(resId);
        }
    }

    @OnTouch({R.id.tbt_popup, R.id.tbt_popup_remainView, R.id.tbt_popup_imageView})
    protected boolean onTouch(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startClickTime = Calendar.getInstance().getTimeInMillis();
                lastX = (int) event.getRawX();
                lastY = (int) event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();

                int location[] = new int[2];
                getLocationInWindow(location);
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) getLayoutParams();

                params.x = params.x + (x - lastX);
                params.y = params.y + (y - lastY);

                xPosition = params.x;
                yPosition = params.y;

                windowManager.updateViewLayout(this, params);
                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (isClick()) {
                    onClick();
                }
                break;
        }
        return true;
    }

    /**
     * 클릭 여부 체크 Function
     */
    private boolean isClick() {
        long clickDuration = Calendar.getInstance().getTimeInMillis() - startClickTime;
        return clickDuration < MAX_CLICK_DURATION;
    }

    /**
     * 클릭 이벤트 처리 Function
     */
    private void onClick() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        try {
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 1, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * 첫번쨰 TBT와 현재위치의 거리차를 표시한다.
     * onTurnDistanceChangedEvent 전달 될 때 마다 갱신된다.
     *
     * @param distance 첫번쨰 TBT와 현재위치의 거리차(m)
     * @see RouteGuidanceListener#onTurnDistanceChangedEvent(int)
     */
    public void updateTBTDistance(int distance) {
        NaviUtils.setSizeSpanDistance(tbtRemainTextView, distance);
    }

    /**
     * TBT 정보를 화면에 표시한다.
     * LocationEvent에 따라 발생되는 onTurnChangedEvent 전달 될 때 마다 갱신된다.
     *
     * @param guidances TBT 정보가 없을 떄 null이 Setting 된다. 첫번째, 두번째 TBT 정보가 List로 전달된다.
     * @see RouteGuidanceListener#onTurnChangedEvent(List)
     */
    public void updateTBTViews(List<TurnGuidance> guidances) {
        if (guidances == null || guidances.size() == 0) {
            return;
        }
        updateFirstTurn(guidances.get(0));
    }
}
