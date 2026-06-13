package com.tce.smart.platform.core.dto;

import com.tce.smart.platform.core.entity.SmtVehicle;
import com.tce.smart.platform.core.entity.SmtVehicleApply;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 车辆记录查询
 *
 * @author 王艳勇
 * @date 2019-04-13 18:19:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleApplyDTO extends SmtVehicleApply {

	private static final long serialVersionUID = 1L;

	/**
	 * 部门ID
	 */
	private String startTime;

	/**
	 * 车主名称
	 */
	private String endTime;

	private List<Integer> parkIds;

}
