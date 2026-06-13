package com.tce.smart.bridge.netty.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Li.JiaJun
 * @since 2022/3/18 17:17
 */
@Getter
@AllArgsConstructor
public enum EleControlCodeEnum {
    /**
     * 控制码
     */
    DOWNLOAD_FILE_REQUEST("4A", "电表集中器下载档案请求"),
    ;
    private String code;

    private String desc;
}
