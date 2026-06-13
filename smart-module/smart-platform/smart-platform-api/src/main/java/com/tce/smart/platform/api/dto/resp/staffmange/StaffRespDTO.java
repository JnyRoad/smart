package com.tce.smart.platform.api.dto.resp.staffmange;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @description: StaffRespDTO
 * @date: 2020/9/28 18:12
 * @author: wuling
 * @version: 1.0
 */
@Data
public class StaffRespDTO implements Serializable {
	private static final long serialVersionUID = 6843437617070002583L;

	@ApiModelProperty(value = "入住人员工号")
	private String staffBadge;

	@ApiModelProperty(value = "入住人员名称")
	private String staffName;

	@ApiModelProperty(value = "性别 0-男，1-女")
	private Integer sex;

	@ApiModelProperty(value = "部门名称")
	private String depName;

	@ApiModelProperty(value = "职级名称")
	private String jobName;
}
