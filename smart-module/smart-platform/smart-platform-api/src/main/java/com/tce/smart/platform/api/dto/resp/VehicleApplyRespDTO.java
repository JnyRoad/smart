package com.tce.smart.platform.api.dto.resp;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

/**
 * 我的车辆的入园列表
 * @author dell
 *
 */
@Data
public class VehicleApplyRespDTO extends BaseVO {


	private Integer id;

	private String parkName;

	private Integer parkId;

	private Integer status;

	private String vehiclePlate;

	/**
	 * 拒绝原因
	 */
	private String reason;

}
