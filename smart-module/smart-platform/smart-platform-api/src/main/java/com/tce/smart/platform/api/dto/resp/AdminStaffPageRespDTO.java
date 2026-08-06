package com.tce.smart.platform.api.dto.resp;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * 后台员工列表最小响应。
 *
 * 仅承载管理列表和权限操作所需的非敏感展示信息，禁止添加手机号、证件号、人脸
 * 文件标识、住址等个人资料。
 */
@Data
@ApiModel("后台员工列表最小响应")
public class AdminStaffPageRespDTO {

	@ApiModelProperty("员工主键")
	private Long staffId;

	@ApiModelProperty("工号")
	private String badge;

	@ApiModelProperty("姓名")
	private String name;

	@ApiModelProperty("BU 名称")
	private String compName;

	@ApiModelProperty("中心")
	private String depAbbr;

	@ApiModelProperty("部门名称")
	private String depName;

	@ApiModelProperty("职层名称")
	private String jcheName;

	@ApiModelProperty("岗位名称")
	private String jobName;

	@ApiModelProperty("入职时间")
	private Date createTime;

	@ApiModelProperty("员工状态")
	private Integer status;

	@ApiModelProperty("所属园区名称")
	private String parkName;

	@ApiModelProperty("是否已录入人脸")
	private Boolean hasFace;

	@ApiModelProperty("通关权限策略名称")
	private String deviceAuth;

	@ApiModelProperty("APP 权限策略名称")
	private String appAuth;
}
