package com.tce.smart.platform.api.dto.resp.securityzone;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 员工表
 *
 * @author
 * @date
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecurityStaffRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("ID")
	@JsonFormat(shape= JsonFormat.Shape.STRING)
	private Long id;

	@ApiModelProperty("员工姓名")
	private String name;

	@ApiModelProperty("员工工号")
	private String badge;

	@ApiModelProperty("岗位名称")
	private String jobName;

	@ApiModelProperty("buname")
	private String compName;

	@ApiModelProperty("remark")
	private String remark;

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

	@ApiModelProperty("员工已关联保密区")
	private List<SecurityZone> securityZones;

	@ApiModelProperty("关联权限列表")
	private List<RelationAuth> authList;

	@Data
	public static class SecurityZone{

	@ApiModelProperty("保密区名")
	private String securityName;

	@ApiModelProperty("保密区code")
	private String securityCode;
	}

	@Data
	public static class RelationAuth{

		@ApiModelProperty("权限名")
		private String authName;

		@ApiModelProperty("权限ID")
		private String authId;
	}

}
