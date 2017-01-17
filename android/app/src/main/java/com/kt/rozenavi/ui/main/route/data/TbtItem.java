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

package com.kt.rozenavi.ui.main.route.data;

import lombok.Getter;
import lombok.Setter;

/**
 * tbt 데이터 클래스
 * 경로상세 리스트 뷰에서 사용하는 tbt정보 클래스
 * 좌표, 노드명칭, tbt타입, 다음 tbt까지의 거리를 제공
 */
public class TbtItem {
    /**
     * tbt 위치 x 좌표
     * utmk 좌표
     */
    @Getter
    @Setter
    private double x;
    /**
     * tbt 위치 y 좌표
     * utmk 좌표
     */
    @Getter
    @Setter
    private double y;
    /**
     * tbt 노드 명칭
     * ic/jc 등의 명칭을 가지는 노드일 경우는 해당 명칭을 제공
     * 노드명칭이 없는 경우는 null값을 제공
     */
    @Getter
    @Setter
    private String name;
    /**
     * tbt 타입
     *
     * @see com.kt.roze.guidance.RGType
     */
    @Getter
    @Setter
    private int type;
    /**
     * 다음 tbt까지의 거리
     * 현재 tbt에서 다음 tbt까지의 거리
     * m단위
     */
    @Getter
    @Setter
    private int nextDistance;
}
