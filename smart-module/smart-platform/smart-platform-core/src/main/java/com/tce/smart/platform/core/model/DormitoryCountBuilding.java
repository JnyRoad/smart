package com.tce.smart.platform.core.model;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.platform.core.vo.DormitoryCountListByFloorVO;
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
public class DormitoryCountBuilding extends Model<DormitoryCountBuilding>  {

	private static final long serialVersionUID = 1L;

	/**
	 * 楼栋ID
	 */
	private Integer dormitoryId;

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
	 * 实际入住总人数
	 */
	private Integer totalUseNumber;

	/**
	 * 每层的统计
	 */
	private List<DormitoryCountListByFloorVO> dormitoryCountListByFloorVOList;

	/**
	 * 剩余床位数
	 */
	private Integer surplus;

}
