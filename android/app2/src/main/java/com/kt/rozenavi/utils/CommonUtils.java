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

package com.kt.rozenavi.utils;

import java.util.Collection;

/**
 * 분류 안된 기본 유틸성 API
 */
public class CommonUtils {
    public static boolean isEmpty(Collection<?> list) {
        return (list == null || list.isEmpty());
    }

    public static <E> boolean isEmpty(E...array) {
        return (array == null || array.length == 0);
    }
}
