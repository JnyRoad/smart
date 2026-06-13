package com.tce.smart.platform.core.vo;

import java.math.BigDecimal;

import lombok.Data;

/**
 * 区域查询结果
 * @author 齐佩
 *
 */
@Data
public class SearchAreaVO  {

	/**
	 * 地点表
	 */
	private Integer id;
	/**
	 * 所属园区id
	 */
	private Integer parkId;
	/**
	 * 地点名称
	 */
	private String areaName;

	/**
	 * 上级地点id
	 */
	private Integer pid;


	/**
	 * 区域经度
	 */
	private BigDecimal areaLongitude;
	/**
	 * 区域纬度
	 */
	private BigDecimal areaLatitude;

	/**
	 * 备注
	 */
	private String remark;

	private String parentAreaName;

	private String parkName;
}
