package com.tce.smart.app.ao.fore;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App设备注册Ao
 *
 * @author mkwu
 * @date 2019-07-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceRegisterAo extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = 9076609539773041507L;

	/**
	 * 设备名称
	 */
	private String deviceName;

	/**
	 * 设备编码
	 */
	private String deviceNo;

	/**
	 * 推送标识 IOS送DeviceToke，Android送ClientId
	 */
	private String devicePushId;

	/**
	 * 系统类型 1安卓，2-IOS
	 */
	private Integer osType;

}
