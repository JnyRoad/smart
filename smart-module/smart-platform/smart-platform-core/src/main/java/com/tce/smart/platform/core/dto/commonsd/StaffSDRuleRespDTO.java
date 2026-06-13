package com.tce.smart.platform.core.dto.commonsd;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @description: 员工水电规则查询响应DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class StaffSDRuleRespDTO {

	/**
	 * 员工工号
	 */
	private String staffBadge;

	/**
	 * 收费项目
	 */
	private Integer categoryId;

	/**
	 * 标准用量
	 */
	private Double standardQty;

	/**
	 * 超出费用单价
	 */
	private BigDecimal overFee;
}
