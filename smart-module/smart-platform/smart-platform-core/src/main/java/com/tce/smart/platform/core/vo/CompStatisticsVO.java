package com.tce.smart.platform.core.vo;

import lombok.Data;

/**
 * 组织机构统计信息
 * @author QIPEI
 *
 */
@Data
public class CompStatisticsVO {

	/**
	 * BU组织机构的名称
	 */
	private String compName;

	/**
	 * 统计数据的值
	 */
	private Integer vlaue;
}
