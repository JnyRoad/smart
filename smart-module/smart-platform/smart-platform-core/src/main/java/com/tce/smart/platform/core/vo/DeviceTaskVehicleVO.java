package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/**
 * 关联车辆
 */
@Data
public class DeviceTaskVehicleVO extends BaseVO {

	private String cardNo;
	/**
	 * 姓名
	 */
	//private String name;
	/**
	 * 车牌号
	 */
	private String plate;

}
