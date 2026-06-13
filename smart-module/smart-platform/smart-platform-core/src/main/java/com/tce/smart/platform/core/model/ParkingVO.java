package com.tce.smart.platform.core.model;

import com.tce.smart.common.core.vo.BaseVO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ParkingVO extends BaseVO {

	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	private String id;
	/**
	 * 园区ID
	 */
	private Integer parkId;

	/**
	 * 园区ID
	 */
	private String parkName;
	/**
	 * 停车场名称
	 */
	private String name;
	/**
	 * 总车位
	 */
	private Integer totalCount;
}
