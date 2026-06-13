package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 宿舍管理列表-按楼统计VO
 *
 * @author jinbo
 * @date 2019-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormitoryCountListByBuildingVO extends BaseVO {

	private static final long serialVersionUID = 1L;

	/**
	 * 楼栋描述
	 */
	private String dormitoryName;

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
	 * 按楼层统计信息
	 */
	private List<DormitoryCountListByFloorVO> dormitoryCountListByFloorVOList;

}
