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
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.kt.geom.model.UTMK;
import com.kt.maps.GMap;
import com.kt.maps.overlay.Path;
import com.kt.roze.data.model.Link;
import com.kt.roze.data.model.Route;
import com.kt.roze.data.model.Turn;
import com.kt.roze.guidance.RGType;
import com.kt.roze.guidance.RouteGuidanceListener;
import com.kt.roze.guidance.model.TurnGuidance;
import com.kt.roze.resource.TurnResourceManager;
import com.kt.rozenavi.R;
import com.kt.rozenavi.utils.MapUtils;
import com.kt.rozenavi.utils.NaviUtils;

import java.util.ArrayList;
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

    private static final int DEFAULT_DISTANCE = 0;

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
        NaviUtils.setSizeSpanDistance(firstRemainTextView, firstTurn.nextDistance);

        String directionText = "";
        if (firstTurn.getToll() != 0) {
            directionText = "요금정보 : " + firstTurn.getToll();
        } else if (firstTurn.directionNames != null && firstTurn.directionNames.size() > 0) {
            for (String s : firstTurn.directionNames) {
                directionText = directionText + s + " ";
            }
        }
        directionTextView.setText(directionText);

        int resId = TurnResourceManager.getResourceId(firstTurn.turnCode);
        if (resId > TurnResourceManager.RESOURCE_NOT_FOUND) {
            firstTbtImageView.setImageResource(resId);
        }
        updateFirstTbtPath(firstTurn.linkIndex);
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
            secondTbtImageView.setImageResource(resId);
        }
        NaviUtils.setSizeSpanDistance(secondRemainTextView, secondTurn.nextDistance);
        updateSecondTbtPath(secondTurn.linkIndex);
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


    /**
     * first tbt path 오버레이 업데이트
     * first, second tbt에 대한 overlay를 모두 삭제후 작업
     *
     * @param linkIndex first tbt 안내가 발생하는 link index
     */
    public void updateFirstTbtPath(int linkIndex) {
        if (gMap == null) {
            return;
        }
        if (firstTbt != null) {
            gMap.removeOverlay(firstTbt);
            firstTbt = null;
            firstTbtIndex = -1;
        }
        Turn turn = turns.get(linkIndex);
        if (turn == null) {
            return;
        }
        firstTbt = createTbtPath(turn);
        if (firstTbt != null) {
            gMap.addOverlay(firstTbt);
            firstTbtIndex = linkIndex;
        }
    }

    /**
     * second tbt path 오버레이 업데이트
     *
     * @param linkIndex second tbt 안내가 발생하는 link index
     */
    public void updateSecondTbtPath(int linkIndex) {
        if (gMap == null) {
            return;
        }
        if (secondTbt != null) {
            gMap.removeOverlay(secondTbt);
            secondTbt = null;
            secondTbtIndex = -1;
        }
        Turn turn = turns.get(linkIndex);
        if (turn == null) {
            return;
        }
        secondTbt = createTbtPath(turn);
        if (secondTbt != null) {
            gMap.addOverlay(secondTbt);
            secondTbtIndex = linkIndex;
        }
    }

    /**
     * rp상에 포함된 turn정보를 지도 경로상에 표현할 tbt overlay로 생성
     */
    private Path createTbtPath(Turn turn) {
        Path arrow;
        List<UTMK> point;
        point = getTbtPathPoint(turn);

        if (point == null) {
            return null;
        }

        arrow = new Path();
        arrow.setPoints(point);
        arrow.setHasArrow(true);
        arrow.setStrokeColor(Color.argb(200, 0, 0, 0));
        arrow.setStrokeWidth(2);
        arrow.setFillColor(Color.argb(225, 255, 255, 255));
        //줌레벨 9.5 미만은 지도에 표시하지 않음
        arrow.setVisible(gMap.getViewpoint().zoom >= 9.5);
        //줌레벨 11 미만은 bufferwidth를 작게 변경
        if (gMap.getViewpoint().zoom < 11) {
            arrow.setBufferWidth(gMap.getResolution() * 2);
        } else {
            arrow.setBufferWidth(gMap.getResolution() * MapUtils.TBT_PATH_WIDTH_IN_DP);
        }
        return arrow;
    }

    /**
     * tbt overlay 좌표list 생성
     * link list에서 입력된 turn에 해당하는 tbt overlay를 생성할 수 있도록
     * 좌표 list를 반환
     *
     * @param turn tbt overlay를 생성할 turn 정보
     * @return 좌표 리스트
     */
    private List<UTMK> getTbtPathPoint(Turn turn) {
        List<UTMK> coords = new ArrayList<>();

        //tbt정보를 생성할수 없을때 null 리턴
        if (links == null || links.size() == 0 || turn == null || links.size() <= turn.linkIndex ||
                links.size() <= (turn.linkIndex + 1)) {
            return null;
        }

        int maxLength;
        if (gMap.getViewpoint().zoom < 11) {
            maxLength = (int) (30 * gMap.getResolution());
        } else {
            maxLength = (int) (50 * gMap.getResolution());
        }
        if (turn.type == RGType.STARTING_POINT || turn.type == RGType.DESTINATION ||
                turn.type == RGType.GO_STRAIGHT || turn.type == RGType.TG || turn.type == RGType.IC
                || turn.type == RGType.JC) {
            //일반 직진, TG, IC, JC tbt생성 안함
            //tbt path 생성안하는
            return null;
        } else if (turn.type == RGType.U_TURN) {
            //기준이 될 좌표 설정
            int uturnLength = links.get(turn.linkIndex + 1).length;
            if (uturnLength < 30) {
                //u turn이기 때문에 링크 전체를 설정
                coords.addAll(links.get(turn.linkIndex + 1).getNodes());
                return makeTbtPathPoint(coords, turn.linkIndex + 2, turn.linkIndex, maxLength / 2);
            }
        }
        //기준이 될 좌표 설정
        coords.add(links.get(turn.linkIndex).getLastNode());
        return makeTbtPathPoint(coords, turn.linkIndex + 1, turn.linkIndex, maxLength / 2);
    }

    /**
     * tbt overlay point 구성
     * tbt 발생위치를 기준으로 이전링크 앞링크를 interval 에 맞추어 검색하여 point를 추가
     *
     * @param coords         point 가 포함될 list
     * @param frontLinkIndex 앞 링크 인덱스
     * @param backLinkIndex  이전 링크 인덱스
     * @param pathInterval   앞/뒤 화살표의 길이
     * @return tbt overlay point list
     */
    private List<UTMK> makeTbtPathPoint(List<UTMK> coords, int frontLinkIndex, int backLinkIndex,
            int pathInterval) {
        //꼬리 부분 생성후 list의 앞에 추가
        coords.addAll(0, makeTbtArrowBackPointList(backLinkIndex, pathInterval));

        //화살표 부분 생성후 list의 뒤에 이어서 추가
        coords.addAll(makeTbtArrowFrontPointList(frontLinkIndex, pathInterval));

        //생성된 tbt point list 반환
        return coords;
    }

    /**
     * TBT overlay 꼬리 부분 생성 path point 구성
     *
     * @param linkIndex tbt 위치 linkindex
     * @param interval  tbt overlay 화살표 부분 길이
     * @return 좌표 리스트
     */
    private List<UTMK> makeTbtArrowBackPointList(int linkIndex, int interval) {
        List<UTMK> coords = new ArrayList<>();
        //꼬리 좌표 추가
        //node 초기 비교 노드 설정
        UTMK point1 = links.get(linkIndex).getLastNode();
        UTMK point2 = links.get(linkIndex).getNode(links.get(linkIndex).getNodeSize() - 2);
        double distance;
        //링크 전체가 tbt 구간에 포함되는경우 반복문으로 확인 처리
        while (linkIndex > 0 && interval > links.get(linkIndex).length) {
            //링크의 전체 노드 추가
            coords.addAll(0, links.get(linkIndex).getNodes());
            //남은길이를 링크의 길이만큼 제외
            interval = interval - links.get(linkIndex).length;
            //이전링크 인덱스를 하나 뒤로 이동
            linkIndex = linkIndex - 1;
            //뒤로 더 넘어갈수 없을때는 반복문 종료
            if (linkIndex == 0) {
                break;
            }
        }
        //tbt 가 링크의 일부분만 구성되는 경우 처리
        for (int i = links.get(linkIndex).getNodeSize() - 1; i < 0; i--) {
            point1 = links.get(linkIndex).getNode(i);
            point2 = links.get(linkIndex).getNode(i - 1);
            //노드간 거리 체크
            distance = point1.distanceTo(point2);
            //남은 길이보다 길 경우 반복문 종료
            if (interval < distance) {
                break;
            }
            //노드를 추가
            coords.add(0, point2);
            //남은 길이에서 노드간 거리를 제외
            interval = (int) (interval - distance);
        }
        // 반복문이 종료된 후 남은 길이가 있는경우 노드 방향으로 남은거리만큼의 임의의 노드를 계산
        if (interval > 0) {
            int angle = (int) point2.angleTo(point1);
            //진행방향의 시작점으로 지정
            coords.add(0, getPointOverLineDistance((short) angle, point1, interval));
        }
        return coords;
    }

    /**
     * TBT overlay 화살표 부분 생성 path point 구성
     *
     * @param linkIndex tbt 위치 linkindex
     * @param interval  tbt overlay 화살표 부분 길이
     * @return 좌표 리스트
     */
    private List<UTMK> makeTbtArrowFrontPointList(int linkIndex, int interval) {
        List<UTMK> coords = new ArrayList<>();
        //화살표 좌표 추가
        //node 이동 인덱스와 초기 비교 노드 설정
        UTMK point1 = links.get(linkIndex).getNode(0);
        UTMK point2 = links.get(linkIndex).getNode(1);
        double distance;
        while (linkIndex < (links.size() - 1) && interval > links.get(linkIndex).length) {
            //링크의 전체 노드 추가
            coords.addAll(links.get(linkIndex).getNodes());
            //남은길이를 링크의 길이만큼 제외
            interval = interval - links.get(linkIndex).length;
            //앞링크 인덱스를 하나 뒤로 이동
            linkIndex = linkIndex + 1;
            //앞으로 더 넘어갈수 없을때는 반복문 종료
            if (linkIndex == links.size() - 1) {
                break;
            }
        }
        //tbt 가 링크의 일부분만 구성되는 경우 처리
        for (int i = 0, size = links.get(linkIndex).getNodeSize() - 1; i < size; i++) {
            point1 = links.get(linkIndex).getNode(i);
            point2 = links.get(linkIndex).getNode(i + 1);
            //노드간 거리 체크
            distance = point1.distanceTo(point2);
            //남은 길이보다 길 경우 반복문 종료
            if (interval < distance) {
                break;
            }
            //노드를 추가
            coords.add(point2);
            //남은 길이에서 노드간 거리를 제외
            interval = (int) (interval - distance);
        }
        // 반복문이 종료된 후 남은 길이가 있는경우 노드 방향으로 남은거리만큼의 임의의 노드를 계산
        if (interval > 0) {
            int angle = (int) point2.angleTo(point1);
            //진행방향의 끝으로 지정
            coords.add(getPointOverLineDistance((short) angle, point1, interval));
        }
        return coords;
    }

    /**
     * 기준 좌표에서 angle방향으로 interval 만큼의 거리의 좌표를 반환
     *
     * @param angle    진행 각도
     * @param frompt   기준 좌표
     * @param interval 거리(m)
     * @return 계산된 좌표
     */
    private UTMK getPointOverLineDistance(short angle, UTMK frompt, int interval) {
        double x = frompt.x + (interval * Math.sin(Math.toRadians(angle)));
        double y = frompt.y + (interval * Math.cos(Math.toRadians(angle)));
        return new UTMK(x, y);
    }

    private GMap gMap;
    private Path firstTbt;
    private int firstTbtIndex = -1;
    private Path secondTbt;
    private int secondTbtIndex = -1;

    private List<Link> links;
    private SparseArray<Turn> turns;

    public void clearOverlay() {
        if (gMap == null) {
            return;
        }
        if (firstTbt != null) {
            gMap.removeOverlay(firstTbt);
        }
        if (secondTbt != null) {
            gMap.removeOverlay(secondTbt);
        }
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

    public void updateTbtPath() {
        if (firstTbtIndex >= 0) {
            updateFirstTbtPath(firstTbtIndex);
        }
        if (secondTbtIndex >= 0) {
            updateSecondTbtPath(secondTbtIndex);
        }
    }
}
