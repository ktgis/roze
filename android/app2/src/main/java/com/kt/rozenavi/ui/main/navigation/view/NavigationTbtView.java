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

package com.kt.rozenavi.ui.main.navigation.view;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.maps.GMap;
import com.kt.maps.overlay.Path;
import com.kt.roze.data.model.Link;
import com.kt.roze.data.model.Route;
import com.kt.roze.data.model.Turn;
import com.kt.roze.guidance.RouteGuidanceListener;
import com.kt.roze.guidance.model.TurnGuidance;
import com.kt.roze.resource.TurnResourceManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.ui.main.navigation.util.MapHelper;
import com.kt.rozenavi.utils.CommonUtils;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * TBT 정보를 표출한다.
 * TBT 정보는 총 2개까지 표출 하며, Second TBT 정보의 유무에 따라
 * View Gone, Visible 처리로 Second View 표출을 제어한다.
 * First TBT View는 네비게이션이 종료 될 떄 까지 항상 표출된다.
 */
public class NavigationTbtView extends RelativeLayout {
    /**
     * 첫번째 TBT 정보
     */
    @BindView(R.id.first_tbt_imageView)
    protected ImageView firstTbtImageView;
    @BindView(R.id.first_tbt_remain_textview)
    protected TextView firstRemainTextView;
    /**
     * 두번째 TBT 정보
     */
    @BindView(R.id.second_tbt_view)
    protected LinearLayout secondTbtView;
    @BindView(R.id.second_tbt_imageView)
    protected ImageView secondTbtImageView;
    @BindView(R.id.second_tbt_remain_textview)
    protected TextView secondRemainTextView;

    /**
     * 방면정보
     */
    @BindView(R.id.direction_data_textview)
    protected TextView directionTextView;

    private static MapHelper mapHelper = MapHelper.getInstance();

    private static final int DEFAULT_DISTANCE = 0;
    private static final int FIRST_TURN = 0;
    private static final int SECOND_TURN = 1;

    private int firstTurnIndex = -1;
    private int secondTurnIndex = -1;

    private GMap gMap;
    private Path firstTbt;
    private Path secondTbt;

    private List<Link> links;
    private SparseArray<Turn> turns;

    public NavigationTbtView(Context context) {
        super(context);
        initView(context);
    }

    public NavigationTbtView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public NavigationTbtView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_navigation_tbt, this);
        ButterKnife.bind(this);
        //경로 진입 전 0M를 표기 하기 위해
        NaviUtils.setSizeSpanDistance(firstRemainTextView, DEFAULT_DISTANCE);
        NaviUtils.setSizeSpanDistance(secondRemainTextView, DEFAULT_DISTANCE);
    }

    /**
     * 뷰에 첫번째 TBT 정보를 Set 한다.
     * TBT 이미지(항상), 거리정보(항상), 방면정보(존재시), 요금정보(존재시)
     *
     * @param firstTurn 첫번째 TBT Guidance
     */
    private void updateFirstTurn(TurnGuidance firstTurn) {
        //경로 상 Arrow Update 를 위해 이전 link Index가 필요하다.
        firstTurnIndex = firstTurn.linkIndex;
        NaviUtils.setSizeSpanDistance(firstRemainTextView, firstTurn.nextDistance);
        directionTextView.setText(getDirectionInfo(firstTurn));
        directionTextView.setSelected(true);
        updateTurnImageView(firstTurn.turnCode, firstTbtImageView);
        firstTbt = mapHelper.drawTBTPath(gMap, turns.get(firstTurnIndex), firstTbt, links);
    }

    /**
     * 뷰에 두번째 TBT 정보를 Set 한다.
     * TBT 이미지(항상), 거리정보(항상), 방면정보(존재시), 요금정보(존재시)
     *
     * @param turn 첫번째 TBT Guidance
     */
    private void updateSecondTurn(TurnGuidance turn) {
        if (turn == null) {
            secondTbtView.setVisibility(View.INVISIBLE);
            secondTurnIndex = -1;
            return;
        } else {
            //경로 상 Arrow Update 를 위해 이전 link Index가 필요하다.
            secondTurnIndex = turn.linkIndex;
            secondTbtView.setVisibility(View.VISIBLE);
        }
        updateTurnImageView(turn.turnCode, secondTbtImageView);
        NaviUtils.setSizeSpanDistance(secondRemainTextView, turn.nextDistance);
        secondTbt = mapHelper.drawTBTPath(gMap, turns.get(turn.linkIndex), secondTbt, links);
    }

    /**
     * 방면정보 제공. 요금이 있을 때는 요금으로 제공한다.
     */
    private String getDirectionInfo(TurnGuidance turn) {
        String directionText = "";
        if (TurnGuidance.isNodeType(turn.turnCode) && !CommonUtils.isEmpty(turn.nodeName)) {
            directionText = turn.nodeName + " ";
        } else if (turn.directionNames != null && turn.directionNames.size() > 0) {
            for (String s : turn.directionNames) {
                directionText = directionText + s + " ";
            }
        }

        if (turn.getToll() != 0) {
            if (CommonUtils.isEmpty(directionText)) {
                directionText = getContext().getString(R.string.navigation_turn_toll, turn.getToll());
            } else {
                directionText += getContext().getString(R.string.navigation_turn_toll, turn.getToll());
            }

        }
        return directionText;
    }

    private void updateTurnImageView(short turnCode, ImageView turnImage) {
        if (turnImage == null) {
            return;
        }
        int resId = TurnResourceManager.getResourceId(turnCode);
        if (resId > TurnResourceManager.RESOURCE_NOT_FOUND) {
            turnImage.setImageResource(resId);
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
        NaviUtils.setSizeSpanDistance(firstRemainTextView, distance);
    }

    /**
     * Turn 정보 설정
     * LocationEvent에 따라 발생되는 onTurnChangedEvent 전달 될 때 마다 갱신된다.
     *
     * @param guidances TBT 정보가 없을 떄 null이 Setting 된다. 첫번째, 두번째 TBT 정보가 List로 전달된다.
     * @see RouteGuidanceListener#onTurnChangedEvent(List)
     */
    public void updateTBTViews(@NonNull List<TurnGuidance> guidances) {
        if (CommonUtils.isEmpty(guidances)) {
            return;
        }
        updateFirstTurn(guidances.get(FIRST_TURN));
        if (guidances.size() > SECOND_TURN) {
            TurnGuidance secondTurn = guidances.get(SECOND_TURN);
            updateSecondTurn(secondTurn);
        } else {
            updateSecondTurn(null);
        }
    }

    public void clearOverlay() {
        mapHelper.removeOverlay(gMap, firstTbt);
        mapHelper.removeOverlay(gMap, secondTbt);
    }

    public void releaseMap() {
        clearOverlay();
        gMap = null;
    }

    public void initMap(GMap gMap) {
        this.gMap = gMap;
    }

    public void setRoute(Route route) {
        this.links = route.links;
        turns = new SparseArray<>();
        for (Turn turn : route.turns) {
            turns.put(turn.linkIndex, turn);
        }
    }

    /**
     * View Point 변경 시 호출
     */
    public void updateTbtPath() {
        if (firstTbt != null && firstTurnIndex >= 0) {
            firstTbt = mapHelper.drawTBTPath(gMap, turns.get(firstTurnIndex), firstTbt, links);
        }
        if (secondTbt != null && secondTurnIndex > 0) {
            secondTbt = mapHelper.drawTBTPath(gMap, turns.get(secondTurnIndex), secondTbt, links);
        }
    }
}
