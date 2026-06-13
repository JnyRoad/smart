package com.tce.smart.platform.core.vo;

import com.tce.smart.common.core.vo.BaseVO;
import lombok.Data;

import java.io.Serializable;

/**
 * 宿舍监控-按性别统计VO
 *
 * @author jinbo
 * @date 2019-04-30
 */
@Data
public class DormitoryCountBySexVO extends BaseVO {

	private static final long serialVersionUID = 1L;

	/**
	 * 性别
	 */
	private String sex;
	/**
	 * 园区名称
	 */
	private String parkName;
	/**
	 * 宿舍名称
	 */
	private String dormitoryName;
	/**
	 * 房间类型描述
	 */
	private String typeName;

	/**
	 * 床位总数
	 */
	private Integer total;

	/**
	 * 实际入住人数
	 */
	private Integer actualNumber;

	/**
	 * 剩余床位数
	 */
	private Integer surplus;

}
