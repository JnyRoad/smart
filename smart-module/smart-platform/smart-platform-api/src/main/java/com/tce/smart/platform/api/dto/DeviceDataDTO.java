package com.tce.smart.platform.api.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 设备信息
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/18 .
 * @Modified By:
 */
@Data
@NoArgsConstructor
public class DeviceDataDTO implements Serializable{
    private static final long serialVersionUID = -8663632322721350600L;
    /**
     * 设备编码【必选】
     */
    private String deviceCode;

    /**
     * 设备厂家,1-海康；2-大华
     */
    private Integer deviceVendor;

    /**
     * 设备类型,；1-人脸闸机；2-车辆道闸；3-防越界摄像机；4-车辆出入口抓拍机
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
     * 设备登录用户名【必选】
     */
    private String deviceUsername;

    /**
     * 设备登录密码【必选】
     */
    private String devicePassword;

    /**
     * 设备所属区域【必选】
     */
    private String areaId;
    /**
     *  LED屏IP地址【可选，设备类型为车辆出入口抓拍机时有效】
     */
    private String ledScreenIp;
    /**
     *  LED屏端口,默认10000【可选，设备类型为车辆出入口抓拍机时有效】
     */
    private Integer ledScreenPort;

    /**
     * 进出类型，1-进；2-出【可选，设备类型为车辆出入口抓拍机时有效】
     */
    private Integer entryExitType;

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

    @Builder
    public DeviceDataDTO(String deviceCode, Integer deviceVendor, Integer deviceType, String deviceIp, Integer devicePort, String deviceUsername, String devicePassword, String areaId, String ledScreenIp, Integer ledScreenPort, Integer entryExitType) {
        this.deviceCode = deviceCode;
        this.deviceVendor = deviceVendor;
        this.deviceType = deviceType;
        this.deviceIp = deviceIp;
        this.devicePort = devicePort;
        this.deviceUsername = deviceUsername;
        this.devicePassword = devicePassword;
        this.areaId = areaId;
        this.ledScreenIp = ledScreenIp;
        this.ledScreenPort = ledScreenPort;
        this.entryExitType = entryExitType;
    }
}
