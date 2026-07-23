package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * ISC 人员创建专用的内部员工资料。
 *
 * 字段用途固定：工号用于 ISC 人员标识，姓名与性别用于人员档案，出生日期与完整证件号
 * 用于 ISC 证件档案。该对象只允许 Smart Schedule 持服务令牌调用，禁止返回客户端或记日志。
 */
@Data
@ApiModel("ISC人员创建内部员工响应")
public class InternalScheduleIscPersonRespDTO {

	@ApiModelProperty("工号，仅 ISC 人员标识使用")
	private String badge;

	@ApiModelProperty("姓名，仅 ISC 人员档案使用")
	private String name;

	@ApiModelProperty("性别，仅 ISC 人员档案使用")
	private Integer sex;

	@ApiModelProperty("出生日期，仅 ISC 人员档案使用")
	private String birth;

	@ApiModelProperty("完整证件号，仅 ISC 受控服务端流程使用")
	private String certno;
}
