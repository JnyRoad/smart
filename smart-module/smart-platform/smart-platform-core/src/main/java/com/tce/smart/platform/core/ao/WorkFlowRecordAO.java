package com.tce.smart.platform.core.ao;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tce.smart.common.core.ao.BaseAO;
import lombok.Data;

/**
 *
 * @ClassName FlowOverAo.java
 * @Author mingkai.wu
 * @Date 2019-04-29 09:05
 * @Description 审核完成推送消息
 */
@Data
public class WorkFlowRecordAO extends BaseAO {

	/**
	 * 操作者ID (OA系统ID)
	 */
	@JsonProperty("OPERATOR")
	private String operator;

	/**
	 * 操作在工号
	 */
	@JsonProperty("WORKCODE")
	private String workcode;

	/**
	 * 操作者姓名
	 */
	@JsonProperty("LASTNAME")
	private String lastname;

	/**
	 * 操作日期
	 */
	@JsonProperty("OPERATEDATE")
	private String operatedate;

	/**
	 * 操作时间
	 */
	@JsonProperty("OPERATETIME")
	private String operatetime;

	/**
	 * 操作类型
	 */
	@JsonProperty("LOGTYPE")
	private String logtype;

	/**
	 * 操作类型说明
	 */
	@JsonProperty("DESCRIPTION")
	private String description;

	/**
	 * 签批意见
	 */
	@JsonProperty("REMARK")
	private String remark;

	/**
	 * 节点名称
	 */
	@JsonProperty("NODENAME")
	private String nodename;

}
