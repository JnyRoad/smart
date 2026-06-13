package com.tce.smart.platform.core.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class AreaTreeChildren {

	private Integer id;

	private String label;


	private String value;


	private Integer pid;

	private String pName;

	private String parkName;

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


}
