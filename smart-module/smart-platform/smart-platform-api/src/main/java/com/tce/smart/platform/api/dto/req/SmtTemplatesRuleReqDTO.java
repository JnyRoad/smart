package com.tce.smart.platform.api.dto.req;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * @description: SmtTemplatesRuleReqDTO
 * @date: 2020-07-13 17:33
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtTemplatesRuleReqDTO implements Serializable {
	private static final long serialVersionUID = 4978087513774174958L;

	/**
	 * 水电模板标识
	 */
	private Long tempId;

	/**
	 * 规则数据列表
	 */
	private List<Rules> rulesList;

	/**
	 * 规则数据
	 */
	@Data
	@NoArgsConstructor
	public static class Rules{
		/**
		 * 收费项目ID
		 */
		private Integer categoryId;

		/**
		 * 规则详细列表
		 */
		private List<RulesData> rulesDataList;
	}

	/**
	 * 收费规则详细
	 */
	@Data
	@NoArgsConstructor
	public static class RulesData{
		/**
		 * 标准用量
		 */
		private Double standardQty;

		/**
		 * 超出费用
		 */
		private BigDecimal overFee;

		/**
		 * 月份
		 */
		private Integer monthNum;
	}
}
