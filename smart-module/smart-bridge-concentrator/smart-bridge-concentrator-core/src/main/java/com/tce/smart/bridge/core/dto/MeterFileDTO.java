package com.tce.smart.bridge.core.dto;

import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2022/3/14 9:06
 */
@Data
public class MeterFileDTO {

    /**
     * 设备IP【必选】
     */
    private String deviceIp;

    /**
     * 设备端口【必选】
     */
    private Integer devicePort;
    /**
     * 水电表数量
     */
    private Integer meterNum;
    /**
     * 集中器通信地址
     */
    private String concentratorAddress;
    /**
     * json字符串存储水电表信息：表序号，表通信地址等
     */
    private String meterJson;
}
