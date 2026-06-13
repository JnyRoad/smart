package com.tce.smart.platform.api.dto.req.securityzone;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 员工筛选条件
 *
 * @author
 * @date
 */
@Data
public class SecurityStaffInputReqDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("ID")
	private Long id;

	@ApiModelProperty("员工姓名")
	private String name;

	@ApiModelProperty("员工工号")
	private String badge;

	@ApiModelProperty("岗位名称")
	private String jobName;

	@ApiModelProperty("buname")
	private String compName;

	@ApiModelProperty("中心")
	private String depAbbr;

	@ApiModelProperty("部门名称")
	private String depName;

	@ApiModelProperty("入职时间")
	private String createTime;

	@ApiModelProperty("职层名称")
	private String jcheName;

	@ApiModelProperty("错误工号")
	private List<String> errorBadge;

	@ApiModelProperty("员工部门")
	private String staffDepId;

	@ApiModelProperty("员工职务")
	private String staffJobId;

	@ApiModelProperty("是否有人脸图片 0 无；1 有")
	private Integer isImg;

	@ApiModelProperty("是否签署保密区 0 无；1 有")
	private Integer isSecurity;


	@ApiModelProperty("备注信息")
	private String remark;


	@ApiModelProperty("关联权限列表")
	private List<RelationAuth> authList;

	@Data
	public static class RelationAuth{

		@ApiModelProperty("权限名")
		private String authName;

		@ApiModelProperty("权限ID")
		private Integer authId;
	}

}
