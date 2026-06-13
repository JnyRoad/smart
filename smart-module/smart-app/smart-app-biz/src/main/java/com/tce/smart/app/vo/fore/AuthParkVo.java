package com.tce.smart.app.vo.fore;

import lombok.Data;
/**
 * 车辆通行权限信息
 * @author qipei
 *
 */
@Data
public class AuthParkVo {

	/**
	 * 车辆园区权限编号
	 */
	private String vehicleAuthkId;

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 * 权限描述
	 */
	private String authDesc;

	private String reason;


}
