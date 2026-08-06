package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/** 后台临时人员最小查询条件，禁止携带证件、电话和人脸内容。 */
@Data
@ApiModel("后台临时人员查询条件")
public class AdminTemporaryStaffQueryReqDTO {

	@ApiModelProperty("部门ID")
	private Long depId;

	@ApiModelProperty("工号关键字")
	private String badge;

	@ApiModelProperty("姓名关键字")
	private String name;

	@ApiModelProperty("是否已上传人脸，仅用于筛选")
	private Boolean isFace;

	@ApiModelProperty("工号集合，使用逗号分隔")
	private String badges;
}
