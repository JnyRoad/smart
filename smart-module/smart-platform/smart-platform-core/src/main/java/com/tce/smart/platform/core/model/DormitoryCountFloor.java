package com.tce.smart.platform.core.model;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宿舍管理列表-按楼层统计
 *
 * @author jinbo
 * @date 2019-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormitoryCountFloor extends Model<DormitoryCountFloor> {

	private static final long serialVersionUID = 1L;

	/**
	 * 楼层ID
	 */
	private Integer floorId;
	/**
	 * 楼层描述
	 */
	private String floorName;

	/**
	 * 床位总数
	 */
	private Integer total;

	/**
	 * 实际入住男员工人数
	 */
	private Integer manNumber;

	/**
	 * 男员工床位总数
	 */
	private Integer manTotal;

	/**
	 * 实际入住女员工人数
	 */
	private Integer womanNumber;

	/**
	 * 女员工床位总数
	 */
	private Integer womanTotal;

	/**
	 * 实际入住总数
	 */
	private Integer actualNumber;

	/**
	 * 剩余床位数
	 */
	private Integer surplus;

}
