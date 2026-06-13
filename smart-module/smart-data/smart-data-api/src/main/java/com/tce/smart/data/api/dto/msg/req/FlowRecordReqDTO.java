package com.tce.smart.data.api.dto.msg.req;


import lombok.Data;

/**
 *
 * @ClassName FlowOverAo.java
 * @Author mingkai.wu
 * @Date 2019-04-29 09:05
 * @Description 审核完成推送消息
 */
@Data
public class FlowRecordReqDTO {

	/**
	 * 操作者ID (OA系统ID)
	 */
	private String operator;

	/**
	 * 操作在工号
	 */
	private String workcode;

	/**
	 * 操作者姓名
	 */
	private String lastname;

	/**
	 * 操作日期
	 */
	private String operatedate;

	/**
	 * 操作时间
	 */
	private String operatetime;

	/**
	 * 操作类型
	 */
	private String logtype;

	/**
	 * 操作类型说明
	 */
	private String Description;

	/**
	 * 签批意见
	 */
	private String remark;

}
