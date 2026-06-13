package com.tce.smart.platform.core.vo;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 宿舍管理列表-按楼层统计VO
 *
 * @author jinbo
 * @date 2019-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormitoryCountListByFloorVO extends BaseVO {

	private static final long serialVersionUID = 1L;

	/**
	 * 园区名称
	 */
	private String parkName;
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
	 * 剩余床位数
	 */
	private Integer surplus;

}
