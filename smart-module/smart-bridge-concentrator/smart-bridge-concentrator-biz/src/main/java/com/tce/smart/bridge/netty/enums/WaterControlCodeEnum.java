package com.tce.smart.bridge.netty.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Li.JiaJun
 * @since 2022/3/18 17:23
 */
@Getter
@AllArgsConstructor
public enum WaterControlCodeEnum {
    /**
     * 控制码
     */
    VALVE_REQUEST("4A", "水表集中器内置阀门阀控请求"),

    DOWNLOAD_FILE_REQUEST("4A", "水表集中器下载档案请求"),
    ;
    private String code;

    private String desc;
}
