package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/** 后台临时人员查询响应，只保留管理列表与基础组织展示所需字段。 */
@Data
@ApiModel("后台临时人员最小详情")
public class AdminTemporaryStaffDetailRespDTO {

	@ApiModelProperty("员工主键")
	private Long staffId;

	@ApiModelProperty("工号")
	private String badge;

	@ApiModelProperty("姓名")
	private String name;

	@ApiModelProperty("性别")
	private Integer sex;

	@ApiModelProperty("岗位")
	private String jobName;

	@ApiModelProperty("部门ID")
	private String depId;

	@ApiModelProperty("部门名称")
	private String depName;

	@ApiModelProperty("职层ID")
	private String jcheId;

	@ApiModelProperty("职层名称")
	private String jcheName;

	@ApiModelProperty("人员状态")
	private Integer status;

	@ApiModelProperty("入职时间")
	private String entryTime;

	@ApiModelProperty("派遣单位")
	private String dispatch;
}
