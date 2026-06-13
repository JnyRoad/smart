package com.tce.smart.bridge.core.dto;

import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2021/12/21 10:57
 */
@Data
public class DeviceDataDTO {

    /**
     * 设备编码【必选】
     */
    private String deviceCode;

    /**
     * 设备类型：5-水表集中器 6-阀门集中器(外置阀门) 7-电表集中器
     */
    private Integer deviceType;

    /**
     * 设备IP【必选】
     */
    private String deviceIp;

    /**
     * 设备端口【必选】
     */
    private Integer devicePort;

    /**
     * 水表序号
     */
    private Integer waterMeterSeq;

    /**
     * 阀门序号：0-11或者1-12
     */
    private Integer valveSeq;

    /**
     * 阀门(闸门)开关指令：0关，1开。
     */
    private Integer valveOnOff;
    /**
     * 电表序号
     */
    private Integer electricMeterSeq;
    /**
     * 电表地址
     */
    private String eleMeterAddress;
    /**
     * 电表通信端口号
     */
    private Integer eleMeterPort;
}
