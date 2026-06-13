package com.tce.smart.bridge.netty.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Li.JiaJun
 * @since 2022/3/18 17:14
 */
@Getter
@AllArgsConstructor
public enum WaterFunctionCodeEnum {

    /**
     * 功能码
     */
    QUERY_REQUEST_RESPONSE("8C", "水表读数查询 请求|响应"),

    VALVE_REQUEST("05", "水表集中器内置阀门阀控请求"),

    QUERY_FILE_RESPONSE("8A", "水表集中器查询档案 请求|响应"),

    DOWNLOAD_FILE_REQUEST("84", "水表集中器下载档案请求"),

    WATER_DAY_READING_REQUEST("8D", "水表集中器日冻结读数响应"),
    ;

    private String code;

    private String desc;
}
