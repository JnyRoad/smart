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
 * @description: 访客审批代理人查询响应DTO
 * @date: 2020/12/29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class VisitorProxyQueryRespDTO implements Serializable {
	private static final long serialVersionUID = -7400937885867282664L;

	@ApiModelProperty(value = "记录Id")
	private Long id;

	@ApiModelProperty(value = "园区Id")
	private Integer parkId;

	@ApiModelProperty(value = "园区名称")
	private String parkName;

	@ApiModelProperty(value = "被访人员工工号")
	private String interVieweeBadge;

	@ApiModelProperty(value = "被访人姓名")
	private String interVieweeName;

	@ApiModelProperty(value = "被访人BUID")
	private Integer interVieweeCompId;

	@ApiModelProperty(value = "被访人BU名称")
	private String interVieweeCompName;

	@ApiModelProperty(value = "被访人部门ID")
	private Integer interVieweeDepId;

	@ApiModelProperty(value = "被访人部门名称")
	private String interVieweeDepName;

	@ApiModelProperty(value = "被访人岗位ID")
	private Integer interVieweeJobId;

	@ApiModelProperty(value = "被访人岗位名称")
	private String interVieweeJobName;

	@ApiModelProperty(value = "代理人员工工号")
	private String proxyBadge;

	@ApiModelProperty(value = "代理人姓名")
	private String proxyName;

	@ApiModelProperty(value = "代理人BUID")
	private Integer proxyCompId;

	@ApiModelProperty(value = "代理人BU名称")
	private String proxyCompName;

	@ApiModelProperty(value = "代理人部门ID")
	private Integer proxyDepId;

	@ApiModelProperty(value = "代理人部门名称")
	private String proxyDepName;

	@ApiModelProperty(value = "代理人岗位ID")
	private Integer proxyJobId;

	@ApiModelProperty(value = "代理人岗位名称")
	private String proxyJobName;

	@ApiModelProperty(value = "创建时间")
	@JsonFormat(pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date createTime;
}
