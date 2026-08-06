package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * App 模块筛选使用的员工最小组织资料。
 */
@Data
@ApiModel("内部员工模块响应")
public class InternalStaffModuleRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("工号")
	private String badge;

	@ApiModelProperty("BU 编号")
	private String compId;
}
