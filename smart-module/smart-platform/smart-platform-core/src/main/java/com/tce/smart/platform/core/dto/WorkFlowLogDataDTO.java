package com.tce.smart.platform.core.dto;

import lombok.Data;

/**
 *
 * @ClassName WorkFlowLogDataDTO.java
 * @Author mingkai.wu
 * @Date 2019-04-29 09:05
 * @Description 接收OA审核信息
 */
@Data
public class WorkFlowLogDataDTO {

	/**
	 * 操作者ID (OA系统ID)
	 */
	private String OPERATOR;

	/**
	 * 操作在工号
	 */
	private String WORKCODE;

	/**
	 * 操作者姓名
	 */
	private String LASTNAME;

	/**
	 * 操作日期
	 */
	private String OPERATEDATE;

	/**
	 * 操作时间
	 */
	private String OPERATETIME;

	/**
	 * 操作类型
	 */
	private String LOGTYPE;

	/**
	 * 操作类型说明
	 */
	private String DESCRIPTION;

	/**
	 * 签批意见
	 */
	private String REMARK;

	/**
	 * 节点名称
	 */
	private String NODENAME;

	/**
	 * 当前节点
	 */
	private String CURRENTNODETYPE;

}
