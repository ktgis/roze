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

package com.kt.rozenavi.ui.setting;

import lombok.Data;

/**
 * App 재 실행 시에 유지되어야 하는 Option Setting Class
 */
@Data
class AppOptionData {
    /**
     * 주 야간 Style 적용 여부
     */
    private boolean isEnableNightAlarm = true;
}
