package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 宿舍管理列表VO
 *
 * @author jinbo
 * @date 2019-04-30
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DormitoryCountListVO extends BaseVO {

	private static final long serialVersionUID = 1L;
	/**
	 * 楼栋数
	 */
	private String buildingNumber;
	/**
	 * 容纳人数
	 */
	private String total;
	/**
	 * 入住人数
	 */
	private String actualNumber;
	/**
	 * 按楼栋统计信息
	 */
	private List<DormitoryCountListByBuildingVO> dormitoryCountListByBuildingVOList;

}
