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

package com.kt.rozenavi.ui.main;

/**
 * MainActivity에 포함되는 뷰에서 공통으로 처리되는 인터페이스 클래스
 * 시작, 중지, 종료에 해당하는 기능을 제공
 */
public interface MainViewInterface {
    /**
     * 뷰 종료시 처리되어야 하는 기능 구현
     * 할당된 메모리 해제, 외부 참조 변수 해제 등
     */
    void destroy();

    /**
     * 뷰 시작시 처리되어야 하는 기능 구현
     * 표시되어야 하는 내부 뷰 객체 생성, 뷰 visible 정보 설정 등
     */
    void start();

    /**
     * 뷰 중지시 처리되어야 하는 기능 구현
     * 중지시 내부 뷰 객체 기능 정지, 뷰 visible 정보 설정 등
     */
    void stop();
}
