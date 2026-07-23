package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 后台批量离职确认使用的临时员工最小响应。
 *
 * 页面只需要主键、工号与姓名来确认待操作记录，禁止返回证件、手机号、人脸或地址资料。
 */
@Data
@ApiModel("后台临时员工批量确认响应")
public class AdminTemporaryStaffRespDTO {

	@ApiModelProperty("员工主键")
	private Long staffId;

	@ApiModelProperty("工号")
	private String badge;

	@ApiModelProperty("姓名")
	private String name;
}
