package com.kt.rozenavi.utils

import com.kt.maps.GMap
import com.kt.maps.overlay.RoutePath
import com.kt.maps.overlay.TrafficRoutePath
import com.kt.maps.overlay.TrafficRoutePathColorMode
import com.kt.naviextension.routepath.data.RoutePathOption

/**
 * Route Path 관련 Util Class
 */
object RoutePathUtils {
    /**
     * 경로 요약 화면에서 여러개의 경로 중 경로 하나를 선택했을 때의 동작.
     * 1. 선택한 경로의 Color 를 fillColor 로 변경해 준다.
     * 2. 선택하지 않은 경로의 Color 를 disabledColor 로 변경해준다.
     * 3. 선택한 경로의 화면 zIndex 를 (경로중) 최 상위로 변경해준다.
     */
    @JvmStatic
    fun changeSelectedRoutePath(selectedIndex: Int,
                                routePathList: MutableList<RoutePath>,
                                routePathOption: RoutePathOption): Boolean {
        val selectedPath = routePathList.elementAtOrNull(selectedIndex)
        routePathList.forEach { path ->
            if (selectedPath == path) {
                path.fillColor = routePathOption.fillColor
                selectedPath.bringToFront()
            } else {
                path.fillColor = routePathOption.disabledColor
                path.resetZIndex()
            }
        }
        return true
    }

    /**
     * 경로 요약 화면에서 여러개의 경로 중 경로 하나를 선택했을 때의 동작.
     * 1. 선택한 경로의 Color 를 Traffic Color 로 변경해 준다.
     * 2. 선택하지 않은 경로의 Color 를 DisabledColor 로 변경해준다.
     * 3. 선택한 경로의 화면 zIndex 를 (경로중) 최 상위로 변경해준다.
     */
    @JvmStatic
    fun changeSelectedTrafficRoutePath(selectedIndex: Int,
                                       routePathList: MutableList<TrafficRoutePath>
    ): Boolean {
        val selectedPath = routePathList.elementAtOrNull(selectedIndex)
        routePathList.forEach { path ->
            if (selectedPath == path) {
                path.applyPathColorMode(TrafficRoutePathColorMode.Traffic)
                selectedPath.bringToFront()
            } else {
                path.applyPathColorMode(TrafficRoutePathColorMode.Disabled)
                path.resetZIndex()
            }
        }
        return true
    }

    /**
     * Route Path 생성 시 기본 옵션을 설정한다.
     * Route Path 색상 등 변경 필요 시 이 부분 수정.
     */
    @JvmStatic
    fun createNormalRoutePathOption(gMap: GMap,
                                    bufferWidth: Int,
                                    strokeWidth: Int,
                                    strokeColor: Int,
                                    passedColor: Int,
                                    fillColor: Int,
                                    disabledColor: Int
    ): RoutePathOption {
        return RoutePathOption(
                bufferWidth = (gMap.resolution * bufferWidth).toDouble(),
                strokeWidth = strokeWidth,
                strokeColor = strokeColor,
                passedColor = passedColor,
                fillColor = fillColor,
                disabledColor = disabledColor
        )
    }
}