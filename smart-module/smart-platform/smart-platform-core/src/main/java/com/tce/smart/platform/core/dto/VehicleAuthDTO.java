package com.tce.smart.platform.core.dto;

import com.tce.smart.platform.core.entity.SmtVehicleApply;
import lombok.Data;

/**
 * @description: VehicleAuthDTO
 * @date: 2020/11/19 15:04
 * @author: wuling
 * @version: 1.0
 */
@Data
public class VehicleAuthDTO extends SmtVehicleApply {
	//车辆记录标识Id
	private Long vid;

	//员工职层Id
	private Integer jcheId;
}
