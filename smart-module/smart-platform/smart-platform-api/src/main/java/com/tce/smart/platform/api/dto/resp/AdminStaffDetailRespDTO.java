package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 后台员工详情的受控最小响应。
 *
 * 本对象仅提供后台人员识别与组织展示所需字段。身份证、手机号、地址、人脸及
 * 其他联系资料不得通过此契约扩散；需要处理敏感资料的场景必须使用单独授权流程。
 */
@Data
@ApiModel("后台员工受控详情响应")
public class AdminStaffDetailRespDTO {

	@ApiModelProperty("员工ID")
	private Long staffId;

	@ApiModelProperty("工号")
	private String badge;

	@ApiModelProperty("姓名")
	private String name;

	@ApiModelProperty("性别")
	private Integer sex;

	@ApiModelProperty("BU名称")
	private String companyName;

	@ApiModelProperty("部门名称")
	private String departmentName;

	@ApiModelProperty("岗位名称")
	private String jobName;

	@ApiModelProperty("员工状态")
	private Integer status;
}
