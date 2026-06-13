package com.tce.smart.platform.api.dto.resp.approval;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 访客审批流程表
 * @author fushiping
 * @date 2019/10/21
 */
@Data
public class ApprovalProcessRecordReqDTO implements Serializable {


	private static final long serialVersionUID = 3641159389632669110L;

	@ApiModelProperty(value = "流程编号")
	private String businessId;

	@ApiModelProperty(value = "审批状态")
	private Integer status;

	@ApiModelProperty(value = "审批节点")
	private Integer recordNode;

	@ApiModelProperty(value = "审批状态名称")
	private String statusName;

	@ApiModelProperty(value = "审批人列表")
	List<StaffInfo> staffInfos;


	@Data
	public static class StaffInfo{
		/**
		 * 员工姓名
		 */
		@ApiModelProperty(value = "员工姓名")
		private String staffName;
		/**
		 * 员工号
		 */
		@ApiModelProperty(value = "员工号")
		private String staffBadge;

		@ApiModelProperty(value = "审批结果code")
		private Integer result;

		@ApiModelProperty(value = "审批结果描述")
		private String resultDesc;
		/**
		 * 创建时间
		 */
		@ApiModelProperty(value = "创建时间")
		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
		private LocalDateTime createDate;
		/**
		 * 审批备注
		 */
		@ApiModelProperty(value = "审批备注")
		private String remark;

		/**
		 * 审批时间
		 */
		@ApiModelProperty(value = "审批时间")
		@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
		private LocalDateTime recordDate;
	}
}
