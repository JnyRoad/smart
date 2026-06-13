package com.tce.smart.platform.api.dto.resp.dormitoryrepairs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.platform.api.dto.resp.approval.ApprovalProcessRecordReqDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * @description: 宿舍报修记录详情实体类
 * @date: 2020-07-24 18:11
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SmtDormitoryRepairsDetailDTO extends SmtDormitoryRepairsDTO {

	/**
	 * 故障描述
	 */
	@ApiModelProperty("故障描述")
	private String faultDesc;

	/**
	 * 故障图片地址列表-访问地址
	 */
	@ApiModelProperty("故障图片地址列表-访问地址")
	private List<String> imgs;

	/**
	 * 园区名称
	 */
	@ApiModelProperty("园区名称")
	private String parkName;

	/**
	 * 回复列表
	 */
	@ApiModelProperty("回复列表")
	private List<RepairReply> repairReplyList;

	@ApiModelProperty("审批列表")
	List<ApprovalProcessRecordReqDTO> approvalProcess;

	@Builder
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class RepairReply{

		/**
		 * 回复人姓名
		 */
		@ApiModelProperty("回复人姓名")
		private String replyName;

		/**
		 * 回复描述
		 */
		@ApiModelProperty("回复描述")
		private String replyDesc;

		/**
		 * 回复时间
		 */
		@ApiModelProperty("回复时间")
		@JsonFormat(pattern = "yyyy-MM-dd")
		private Date replyTime;
	}
}
