package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 后台员工工号查询的最小响应。
 *
 * 该对象是外部接口契约，禁止扩充身份证、手机号、地址、照片等个人敏感字段。
 */
@Data
@ApiModel("员工工号查询响应")
public class StaffLookupRespDTO {

	@ApiModelProperty("员工ID")
	private Long staffId;

	@ApiModelProperty("工号")
	private String badge;

	@ApiModelProperty("姓名")
	private String name;

	@ApiModelProperty("部门名称")
	private String departmentName;
}
