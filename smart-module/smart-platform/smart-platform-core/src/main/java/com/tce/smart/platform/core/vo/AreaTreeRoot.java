package com.tce.smart.platform.core.vo;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

/**
 * 区域树菜单，根节点
 * @author dell
 *
 */

@Data
public class AreaTreeRoot {

	private Integer id;

	private String label;


	private String value;

	private Integer pid;

	private String pName;

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
	private List<AreaTreeParent> children;


}
