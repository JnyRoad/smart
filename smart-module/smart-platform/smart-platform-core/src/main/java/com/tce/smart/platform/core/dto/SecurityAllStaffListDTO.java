package com.tce.smart.platform.core.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 员工表
 *
 * @author
 * @date
 */
@Data
public class SecurityAllStaffListDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("staffId")
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	private Long staffId;

	@ApiModelProperty("securityId")
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	private Long securityId;

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

	@ApiModelProperty("员工状态")
	private Integer status;

	@ApiModelProperty("入职时间")
	private Date createTime;

	@ApiModelProperty("签署状态")
	private Integer signStatus;

	@ApiModelProperty("签署状态描述")
	private String signStatusDesc;

}
