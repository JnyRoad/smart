package com.tce.smart.data.api.dto.msg.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description: 长期来访明细
 * @date: 2021/4/1 0001 17:21
 * @author: wuling
 * @version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EntryFactoryApplyLongDetailReqDTO {
	/**
	 * 姓名
	 */
	private String xm;

	/**
	 * 性别
	 */
	private String xb;

	/**
	 * 证件类型
	 */
	private String zjlx;

	/**
	 * 证件号码
	 */
	private String zjhm;

	/**
	 * 身份证户籍地
	 */
	private String huji;

	/**
	 * 进入时间段
	 */
	private String jrsjd;

	/**
	 * 来访证办理
	 */
	private String lfzbl;

	/**
	 * 证件附件
	 */
	private String zjfj;

	/**
	 * 进入开始日期
	 */
	private String jrksrq;

	/**
	 * 进入开始时间段
	 */
	private String jrkssjd;

	/**
	 * 进入结束日期
	 */
	private String jrjsrq;

	/**
	 * 进入结束时间段
	 */
	private String jrjssjd;

}
