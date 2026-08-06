package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 后台员工列表最小查询条件。
 *
 * 园区和 BU 范围完全由认证主体决定；手机号、证件、人脸文件标识等敏感数据
 * 既不能作为检索条件，也不能藉此推断员工资料。
 */
@Data
@ApiModel("后台员工列表最小查询条件")
public class AdminStaffPageQueryReqDTO {

	@ApiModelProperty("员工姓名关键字")
	private String name;

	@ApiModelProperty("员工工号关键字")
	private String badge;

	@ApiModelProperty("多个工号，使用空白字符分隔")
	private String badges;

	@ApiModelProperty("部门 ID")
	private String depId;

	@ApiModelProperty("中心关键字")
	private String depAbbr;

	@ApiModelProperty("岗位 ID")
	private String jobId;

	@ApiModelProperty("岗位名称关键字")
	private String jobName;

	@ApiModelProperty("职层 ID")
	private String jcheId;

	@ApiModelProperty("员工状态")
	private Integer status;

	@ApiModelProperty("是否已录入人脸，仅用于存在性筛选")
	private Boolean hasFace;

	@ApiModelProperty("入职开始时间")
	private String startTime;

	@ApiModelProperty("入职结束时间")
	private String endTime;
}
