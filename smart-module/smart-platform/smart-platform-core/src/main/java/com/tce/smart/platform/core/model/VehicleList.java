package com.tce.smart.platform.core.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VehicleList extends BaseVO {
	/**
	*
	*/
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	private Long id;
    /**
   * 车牌号
   */
    private String vehiclePlate;

    /**
   * 车辆类型:(区分轿车、货车等)
   */
    private String vehicleTypeName;

    /**
	 * 员工姓名
	 */
	private String name;


	/**
	 * 部门名称
	 */
	private String depName;


	/**
	 * 电话
	 */
	private String phone;

	/**
	 * 员工状态 0-已离职 1-在职
	 */
	private Integer staffStatus;

	/**
	 * 福利层次
	 */
	private String jcheName;

	/**
	 * 是否删除：0未删；1：删除
	 */
	private Integer isDelete;

	/**
	 * 园区名称
	 */
	private String parkName;

	/**
	 * 园区ID
	 */
	private List<Integer> parkIds;

}
