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

package com.kt.rozenavi.ui.main.navigation;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.android.databinding.library.baseAdapters.BR;

import com.kt.roze.guidance.RouteGuidanceListener;
import com.kt.roze.guidance.model.TurnGuidance;
import com.kt.roze.resource.TurnResourceManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.navigation.data.TbtGuidanceData;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * TBT 정보를 표출한다.
 * TBT 정보는 총 2개까지 표출 하며, Second TBT 정보의 유무에 따라
 * View Gone, Visible 처리로 Second View 표출을 제어한다.
 * FirstView는 네비게이션이 종료 될 떄 까지 항상 표출된다.
 */
public class TbtGuidanceView extends RelativeLayout {
    /**
     * 두번째 TBT 정보
     */
    @BindView(R.id.second_tbt_view)
    protected LinearLayout secondTbtView;
    /**
     * TBT 정보 observable 데이터 객체
     */
    private TbtGuidanceData tbtGuidanceData = new TbtGuidanceData();

    public TbtGuidanceView(Context context) {
        super(context);
        initView(context);
    }

    public TbtGuidanceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TbtGuidanceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_guidance_tbt, this);
        ButterKnife.bind(this);
        DataBindingUtil.bind(getChildAt(0)).setVariable(BR.tbtGuidanceData, tbtGuidanceData);
    }

    /**
     * 뷰에 첫번째 TBT 정보를 Set 한다.
     * TBT 이미지(항상), 거리정보(항상), 방면정보(존재시), 요금정보(존재시)
     *
     * @param firstTurn 첫번째 TBT Guidance
     */
    private void updateFirstTurn(TurnGuidance firstTurn) {
        tbtGuidanceData.setFirstTurnDistance(firstTurn.nextDistance);
        String directionText = "";
        if (firstTurn.getToll() != 0) {
            directionText = "요금정보 : " + firstTurn.getToll();
        } else if (firstTurn.directionNames != null && firstTurn.directionNames.size() > 0) {
            for (String s : firstTurn.directionNames) {
                directionText = directionText + s + " ";
            }
        }
        tbtGuidanceData.setDirectionText(directionText);

        int resId = TurnResourceManager.getResourceId(firstTurn.turnCode);
        if (resId > TurnResourceManager.RESOURCE_NOT_FOUND) {
            tbtGuidanceData.setFirstTurnResourceId(resId);
        }
    }

    /**
     * Set Second TBT Informatio
     *
     * @param secondTurn 두번째 TBT 정보
     */
    private void updateSecondTurn(TurnGuidance secondTurn) {

        if (secondTurn == null) {
            secondTbtView.setVisibility(View.INVISIBLE);
            return;
        } else {
            secondTbtView.setVisibility(View.VISIBLE);
        }

        int resId = TurnResourceManager.getResourceId(secondTurn.turnCode);
        if (resId > TurnResourceManager.RESOURCE_NOT_FOUND) {
            tbtGuidanceData.setSecondTurnResourceId(resId);
        }

        tbtGuidanceData.setSecondTurnDistance(secondTurn.nextDistance);
    }

    /**
     * 첫번쨰 TBT와 현재위치의 거리차를 표시한다.
     * onTurnDistanceChangedEvent 전달 될 때 마다 갱신된다.
     *
     * @param distance 첫번쨰 TBT와 현재위치의 거리차(m)
     * @see RouteGuidanceListener#onTurnDistanceChangedEvent(int)
     */
    public void updateTBTDistance(int distance) {
        tbtGuidanceData.setFirstTurnDistance(distance);
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
        if (guidances.size() > 1) {
            TurnGuidance secondTurn = guidances.get(1);
            if (secondTurn != null) {
                updateSecondTurn(secondTurn);
            } else {
                updateSecondTurn(null);
            }
        } else {
            updateSecondTurn(null);
        }
    }
}
