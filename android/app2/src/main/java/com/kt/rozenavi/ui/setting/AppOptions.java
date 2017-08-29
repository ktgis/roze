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

import com.kt.roze.RozeOptions;
import com.kt.roze.util.JsonFileUtil;
import com.kt.roze.util.FileUtil;

import java.io.File;

import lombok.Getter;
import lombok.Setter;

/**
 * Client에서 필요한 Option을 Setting 한다.
 * App 종료 시 Reset 되는 Data만 저장한다.
 */
public class AppOptions {

    private static AppOptions instance;
    private final String optionFileName = "appoption.json";
    private AppOptionData data;

    /**
     * Debug Mode - 지도에 GPS 신호 표시 여부
     */
    @Getter
    @Setter
    private boolean isGpsDebugCheck = false;

    /**
     * Debug Mode - GPS Slider 사용 여부
     */
    @Getter
    @Setter
    private boolean isEnableGpsSlider = false;

    private AppOptions() {
        initData();
    }

    public static AppOptions getInstance() {
        if (instance == null) {
            instance = new AppOptions();
        }
        return instance;
    }

    /**
     * 내부 저장소(telos)에 json File 로 설정값을 저장한다.
     * AppOptions 생성 시에 Load 한다
     */
    private void initData() {
        data = JsonFileUtil.loadJsonFile(getSaveFile(optionFileName), AppOptionData.class);
        if (data == null) {
            data = new AppOptionData();
            JsonFileUtil.saveJsonFile(getSaveFile(optionFileName), data);
        }
    }

    /**
     * AppOptions 변경된 Option Data를 json File로 저장한다.
     * 옵션 변경 후 호출한다.
     */
    public void saveConfig() {
        if (data == null) {
            data = new AppOptionData();
        }
        JsonFileUtil.saveJsonFile(getSaveFile(optionFileName), data);
    }

    public boolean getEnableNightAlarm() {
        return data.isEnableNightAlarm();
    }

    public void setEnableNightAlarm(boolean isEnable) {
        data.setEnableNightAlarm(isEnable);
    }

    /**
     * 내부 저장소 이름을 가져옵니다.
     */
    private File getSaveFile(String fileName) {
        String path = RozeOptions.getInstance().getDataPath();
        return FileUtil.getFile(path, fileName);
    }

    public void close() {
        instance = null;
    }
}
