package com.tce.smart.platform.core.vo;

import lombok.Data;

/**
 * 补贴类型信息
 * @author qipei
 *
 */

@Data
public class AllowanceInfoVO {
	/**
	 * 补贴类型名称
	 */
	private String allowanceTypeName;
	/**
	 * 补贴类型
	 */
	private Integer allowanceType;

	/**'
	 * 计算规则
	 */
	private String computaionRule;
	/**
	 * 补贴金额
	 */
	private String  amount;

}
