package com.tce.smart.app.ao.fore;

import com.tce.smart.common.core.ao.BaseAO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * App员工人脸信息采集
 *
 * @author mkwu
 * @date 2019-07-31
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class QueryConsumeRsAo extends BaseAO {

	/**
	 * 序列号
	 */
	private static final long serialVersionUID = -5339754715856309455L;

	/**
	 * 账户类型 1-公司账户，2-个人账户
	 */
	private Integer acctType;

	/**
	 * 查询月份
	 */
	private String queryDate;

	/**
	 *  员工号
	 */
	private String empNo;
}
