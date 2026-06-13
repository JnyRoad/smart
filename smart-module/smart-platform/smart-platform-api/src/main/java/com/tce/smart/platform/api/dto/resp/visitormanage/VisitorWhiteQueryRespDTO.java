package com.tce.smart.platform.api.dto.resp.visitormanage;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 访客审批白名单查询响应DTO
 * @date: 2020/12/29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VisitorWhiteQueryRespDTO implements Serializable {
	private static final long serialVersionUID = -3394154427914705331L;

	@ApiModelProperty(value = "记录Id")
	private Long id;

	@ApiModelProperty(value = "园区Id")
	private Integer parkId;

	@ApiModelProperty(value = "园区名称")
	private String parkName;

	@ApiModelProperty(value = "员工工号")
	private String staffBadge;

	@ApiModelProperty(value = "员工姓名")
	private String staffName;

	@ApiModelProperty(value = "BUID")
	private Integer compId;

	@ApiModelProperty(value = "BU名称")
	private String compName;

	@ApiModelProperty(value = "部门ID")
	private Integer depId;

	@ApiModelProperty(value = "部门名称")
	private String depName;

	@ApiModelProperty(value = "岗位ID")
	private String jobId;

	@ApiModelProperty(value = "岗位名称")
	private String jobName;

	@ApiModelProperty(value = "创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createTime;
}
