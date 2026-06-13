package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.platform.api.dto.resp.approval.ApprovalProcessRecordReqDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 退宿申请表
 *
 * @author FUSHIPING
 * @date
 */
@Data
public class DormitoryQuitApplyDetailRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("ID")
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	private Long id;

	@ApiModelProperty("申请人工号")
	private String badge;

	@ApiModelProperty("申请人姓名")
	private String name;

	@ApiModelProperty("退宿原因")
	private String quitReason;

	@ApiModelProperty("退宿原因")
	private String quitReasonDesc;

	@ApiModelProperty("退宿宿舍")
	private List<String> dorDetailStr;

	@ApiModelProperty("备注")
	private String remark;

	@ApiModelProperty("上传图片ID")
	private List<String> imgs;

	@ApiModelProperty("申请人脸图片ID")
	private String faceId;

	@ApiModelProperty("二维码")
	private String QRcode;

	@ApiModelProperty("状态")
	private Integer status;

	@ApiModelProperty("审批状态")
	private String statusDesc;

	@ApiModelProperty("安保人员姓名")
	private String securityStaff;

	@ApiModelProperty("离开时间")
	private LocalDateTime leaveTime;

	@ApiModelProperty("申请离开时间")
	private LocalDateTime applyLeaveTime;

	@ApiModelProperty("申请时间")
	private LocalDateTime createTime;

	@ApiModelProperty("审批流程")
	private List<ApprovalProcessRecordReqDTO> processRecord;

	@ApiModelProperty("二维码code")
	private String smsCode;

	@ApiModelProperty("是否当前审批人 true是")
	private Boolean isApprove;

}
