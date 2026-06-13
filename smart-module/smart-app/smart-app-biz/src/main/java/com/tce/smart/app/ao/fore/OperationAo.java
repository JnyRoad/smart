package com.tce.smart.app.ao.fore;

import lombok.Data;

/**
 * 简历筛选参数
 *
 * @author qipei
 *
 */
@Data
public class OperationAo {

	/**
	 * 应聘id集合 格式:"id||id"
	 */
	String applicationId;

	/**
	 * 操作类型
	 */
	private Integer operationType;

	/**
	 * 备注
	 */
	private String operationDesc;

	/**
	 * 邀约时间
	 */
	private String appointTime;

}