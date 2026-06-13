package com.tce.smart.platform.api.dto.resp.dormitorymange;

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
 * @description: 内宿申请记录查询DTO
 * @date: 2020/12/29
 * @author: wuling
 * @version: 1.0
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DormitoryApplySearchRespDTO implements Serializable {
	private static final long serialVersionUID = -4137098015208980791L;

	@ApiModelProperty(value = "标识Id")
	private Long id;

	@ApiModelProperty(value = "园区Id")
	private Integer parkId;

	@ApiModelProperty(value = "园区名称")
	private String parkName;

	@ApiModelProperty(value = "员工工号")
	private String staffBadge ;

	@ApiModelProperty(value = "员工姓名")
	private String staffName;

	@ApiModelProperty(value = "状态 1.申请中 2.审批通过 3.已退回 4.撤销")
	private Integer status;

	@ApiModelProperty(value = "喜好类型")
	private Integer likeType;

	@ApiModelProperty(value = "喜好类型描述")
	private String likeTypeDesc;

	@ApiModelProperty(value = "申请备注")
	private String applyRemark;

	@ApiModelProperty(value = "BU")
	private String compName;

	@ApiModelProperty(value = "部门")
	private String depName;

	@ApiModelProperty(value = "职层")
	private String jcheName;

	@ApiModelProperty(value = "申请时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date applyDate;

	@ApiModelProperty(value = "回复时间")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date resultDate;
}
