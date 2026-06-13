package com.tce.smart.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.platform.core.entity.SmtVehicle;

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
public class VehicleDTO extends SmtVehicle {

	private static final long serialVersionUID = 1L;

	/**
	 * 部门ID
	 */
	private String depId;

	/**
	 * 车主名称
	 */
	private String name;

	/**
     * 员工ID
     */
	@JsonFormat(shape=JsonFormat.Shape.STRING)
    private Long staffId;

	/**
	 * 申请状态0-审批中   1-已审批   2-已拒绝
	 */
	private Integer status;

	/**
	 * 员工状态 0-已离职 1-在职
	 */
	private Integer staffStatus;

	/**
	 * 福利层次
	 */
	private String welfareLevel;

	/**
	 * 园区ID
	 */
	private String parkId;

	/**
	 *  车辆的权限ID
	 */
	private Integer authorityId;

	private String compId;

	/**
	 * 园区ID集合
	 */
	List<Integer> parkIds;

}
