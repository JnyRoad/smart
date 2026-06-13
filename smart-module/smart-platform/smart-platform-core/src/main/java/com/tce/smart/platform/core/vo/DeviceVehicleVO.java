package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/**
 * 关联车辆
 */
@Data
public class DeviceVehicleVO extends BaseVO {
	/**
	 * 卡牌ID
	 */
	private String cardNo;
	/**
	 * 姓名
	 */
	private String name;
	/**
	 * 车牌号
	 */
	private String plate;
	/**
	 * 创建时间
	 */
	private String createTime;

	/**
	 * 1:删除中；0：未删除
	 */
	private Integer status;
}
