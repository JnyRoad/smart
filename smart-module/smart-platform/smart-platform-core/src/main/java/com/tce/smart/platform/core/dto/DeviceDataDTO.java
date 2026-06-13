package com.tce.smart.platform.core.dto;

import java.io.Serializable;

import lombok.Data;

/**
 * 设备绑定车辆信息
 * @author Administrator
 *
 */
@Data
public class DeviceDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String deviceId;

    private Integer deviceType;

    private String name;

    private String plate;

    private String badge;

    private String startTime;

    private String endTime;
}
