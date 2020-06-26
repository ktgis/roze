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
package com.kt.rozenavi.ui.main.route.data;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 목적지 데이터 클래스
 * 경로편집 화면에서 사용하는 출발지/목적지/경유지 정보 데이터 클래스
 * 좌표, 목적지 명칭을 제공
 */
public class LocationItem implements Parcelable {
    /**
     * 목적지 x 좌표
     * utmk 좌표
     */
    @Getter
    @Setter
    private double x;
    /**
     * 목적지 y 좌표
     * utmk 좌표
     */
    @Getter
    @Setter
    private double y;
    /**
     * 목적지 명칭
     */
    @Getter
    @Setter
    private String name;

    public LocationItem(double x, double y, String name) {
        this.x = x;
        this.y = y;
        this.name = name;
    }

    public LocationItem(Parcel in) {
        x = in.readDouble();
        y = in.readDouble();
        name = in.readString();
    }

    /**
     * 중복 체크 메서드
     */
    @Override
    public boolean equals(@NonNull Object obj) {
        LocationItem item = (LocationItem) obj;
        return isEqualName(item.getName()) && isEqualLocation(item.getX(), item.getY());
    }

    /**
     * 중복 체크 메서드 - 명칭 중복 체크
     */
    public boolean isEqualName(String Name) {
        return this.getName().equals(Name);
    }

    /**
     * 중복 체크 메서드 - x,y좌표 중복 체크
     */
    public boolean isEqualLocation(double x, double y) {
        return this.getX() == x && this.getY() == y;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(x);
        dest.writeDouble(y);
        dest.writeString(name);
    }

    public static final Creator<LocationItem> CREATOR = new Creator<LocationItem>() {
        @Override
        public LocationItem createFromParcel(Parcel in) {
            return new LocationItem(in);
        }

        @Override
        public LocationItem[] newArray(int size) {
            return new LocationItem[size];
        }
    };
}
