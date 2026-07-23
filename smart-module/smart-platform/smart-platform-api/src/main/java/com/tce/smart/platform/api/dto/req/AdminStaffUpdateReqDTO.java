package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 后台员工基础信息最小修改请求。
 *
 * 证件号、手机号、住址、照片、组织归属等敏感或可越权字段不在本契约中出现。
 */
@Data
@ApiModel("后台员工基础信息修改请求")
public class AdminStaffUpdateReqDTO {

	@ApiModelProperty(value = "员工主键", required = true)
	private Long staffId;

	@ApiModelProperty("姓名")
	private String name;

	@ApiModelProperty("性别")
	private Integer sex;

	@ApiModelProperty("岗位名称")
	private String jobName;

	@ApiModelProperty("人员状态")
	private Integer status;
}
