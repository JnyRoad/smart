package com.tce.smart.platform.core.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备查询
 *
 * @author 王艳勇
 * @date 2019-04-13 18:19:30
 */
@Data
public class DeviceVO implements Serializable {

	private static final long serialVersionUID = 1L;

    /**
   * 主键
   */
    private String id;

    /**
   * 园区主键
   */
    private Integer parkId;

    /**
   * 设备通道号
   */
    private Integer channelNo;

    /**
     * 设备通道号
     */
    private Integer channelManager;

    /**
   * 设备厂家：1-海康；2-大华；
   */
    private Integer deviceVendor;

    /**
   * 设备类型：1-门禁；2-闸机；3-道闸；4-摄像头
   */
    private Integer deviceType;

    /**
   * 设备IP
   */
    private String deviceIp;

    /**
   * 设备端口
   */
    private Integer devicePort;

    /**
   * 设备登录用户名
   */
    private String deviceUsername;

    /**
   * 设备登录密码
   */
    private String devicePassword;

    /**
   * 创建时间
   */
    private Date createTime;

    /**
   * 设备名称
   */
    private String deviceName;

    /**
     * 设备序列号
     */
    private String deviceCode;

    /**
   * 启用状态：1-启用；2-禁用；
   */
    private Integer enableStatus;

    /**
   * 接通状态：0-未连接；1-离线；2-在线；
   */
    private Integer connectStatus;

    /**
   * 更新时间
   */
    private Date updateTime;

    /**
     * 设备子类型 待确认
     */
    private String deviceSubtype;

    /**
     * 设备协议
     */
    private String protocolType;

	/**
	 * 区域ID
	 */
	private Integer areaId;

	/**
	 * 区域名称
	 */
	private String areaName;

	/**
	 * 区域父级
	 */
	private Integer pid;

    /**
     * LED屏IP地址【可选，设备类型为车辆出入口抓拍机时有效】
     */
    private String ledScreenIp;
    /**
     * LED屏IP地址【可选，设备类型为车辆出入口抓拍机时有效】
     */
    private Integer ledScreen;

    /**
     * 进出类型：1-进；2-出；
     */
    private Integer eventType;
	/**
	 * 是否为ISC同步的设备 0-否 1-是
	 */
	private Integer isSync;
	/**
	 * 设备标识：1、考勤机；2、门禁
	 */
	private Integer deviceTag;

	/**
	 * 设备能力：1-仅人脸识别；2-仅刷卡；3-人脸+刷卡
	 * 默认为3（人脸+刷卡），保持向后兼容性
	 */
	private Integer deviceCapability;
}
