package com.tce.smart.platform.api.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 设备信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Data
public class SmtDeviceDTO implements Serializable {
private static final long serialVersionUID = -7866188467446308828L;

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
    @NotNull(message = "设备厂家不可空")
    private Integer deviceVendor;
    /**
   * 设备类型：1-门禁；2-闸机；3-道闸；4-摄像头
   * C++设备类型：1:闸机；2：道闸；3：摄像头；4：出入口抓拍机
   */
    @NotNull(message = "设备厂家不可空")
    private Integer deviceType;
    /**
   * 设备IP
   */
    @NotBlank(message = "设备IP不可空")
    private String deviceIp;
    /**
   * 设备端口
   */
    @NotNull(message = "设备端口不可空")
    private Integer devicePort;
    /**
   * 设备登录用户名
   */
    @NotBlank(message = "设备登录名不可空")
    private String deviceUsername;
    /**
   * 设备登录密码
   */
    @NotBlank(message = "设备登录密码不可空")
    private String devicePassword;
    /**
   * 设备地址
   */
    private String deviceAddress;
    /**
   * 创建时间
   */
    private Date createTime;
    /**
   * 设备名称
   */
    @NotBlank(message = "设备名称不可空")
    private String deviceName;

    /**
     * 设备序列号
     */
    @NotBlank(message = "设备序列号不可空")
    private String deviceCode;

    /**
   * 启用状态：1-启用；2-禁用；
   */
    @NotNull
    @Range(min = 1, max = 2, message = "启用状态：1-启用；2-禁用")
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
     * 停车场ID
     */
    private String deviceSubtype;

    /**
     * 设备协议
     */
    private String protocolType;

    /**
     *  LED屏IP地址【可选，设备类型为车辆出入口抓拍机时有效】
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
	 * 设备能力：1-仅人脸识别；2-仅刷卡；3-人脸+刷卡
	 * 默认为3（人脸+刷卡），保持向后兼容性
	 */
	private Integer deviceCapability;
}
