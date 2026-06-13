package com.tce.smart.data.api.dto.msg.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 短期来访明细
 * @date: 2021/4/1 0001 17:21
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntryFactoryApplyShortDetailReqDTO {
	/**
	 * 姓名
	 */
	private String xmm;

	/**
	 * 性别
	 */
	private String xb;

	/**
	 * 工号
	 */
	private String ghao;

	/**
	 * 职务
	 */
	private String zhiww;


	/**
	 * 进入开始时间段
	 */
	private String jrkssjd;

	/**
	 * 进入开始日期
	 */
	private String jrksrq;

	/**
	 * 进入结束日期
	 */
	private String jrjsrq;

	/**
	 * 进入结束时间段
	 */
	private String jrjssjd;
}
