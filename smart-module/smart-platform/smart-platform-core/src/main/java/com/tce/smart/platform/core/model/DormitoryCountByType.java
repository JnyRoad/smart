package com.tce.smart.platform.core.model;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宿舍监控-按性别和房间类型统计
 *
 * @author jinbo
 * @date 2019-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormitoryCountByType extends Model<DormitoryCountByType> {

	private static final long serialVersionUID = 1L;

	/**
	 * 园区ID
	 */
	private Integer parkId;
	/**
	 * 园区名称
	 */
	private String parkName;
	/**
	 * 房间类型
	 */
	private Integer roomType;
	/**
	 * 房间类型描述
	 */
	private String typeName;

	/**
	 * 床位总数
	 */
	private Integer total;

	/**
	 * 实际入住男员工人数
	 */
	private Integer manNumber;

	/**
	 * 实际入住女员工人数
	 */
	private Integer womanNumber;

	/**
	 * 剩余床位数
	 */
	private Integer surplus;

	/**
	 * 男员工床位总数
	 */
	private Integer manTotal;

	/**
	 * 男员工剩余床位数
	 */
	private Integer manSurplus;

	/**
	 * 女员工床位总数
	 */
	private Integer womanTotal;

	/**
	 * 女员工剩余床位数
	 */
	private Integer womanSurplus;

}
