package com.tce.smart.bridge.netty.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Li.JiaJun
 * @since 2022/3/18 17:13
 */
@Getter
@AllArgsConstructor
public enum EleFunctionCodeEnum {

    /**
     * 功能码
     */
    QUERY_REQUEST_RESPONSE("0C", "电表读数查询 请求|响应"),

    QUERY_DAY_REQUEST_RESPONSE("0D", "电表日冻结读数查询 请求|响应"),

    QUERY_FILE_RESPONSE("0A", "电表集中器查询档案 请求|响应"),

    DOWNLOAD_FILE_REQUEST("04", "电表集中器下载档案请求"),

    BRAKE_STATE_QUERY("10", "电表闸门状态查询"),
    ;

    private String code;

    private String desc;
}
