package com.tce.smart.platform.api.dto.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class DormitoryQuitApplyRespDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("ID")
	@JsonFormat(shape=JsonFormat.Shape.STRING)
	private Long id;

	@ApiModelProperty("园区ID")
	private Integer parkId;

	@ApiModelProperty("申请人工号")
	private String badge;

	@ApiModelProperty("申请人姓名")
	private String name;

	@ApiModelProperty("退宿原因")
	private String quitReason;

	@ApiModelProperty("退宿原因")
	private String quitReasonDesc;

	@ApiModelProperty("申请离开时间")
	private String applyLeaveTime;

	@ApiModelProperty("退宿申请状态")
	private Integer quitStatus;

	@ApiModelProperty("审批状态")
	private Integer status;

	@ApiModelProperty("审批状态")
	private String statusDesc;

	@ApiModelProperty("退宿宿舍")
	private List<String> dorDetailStr;

	@ApiModelProperty("申请时间")
	private LocalDateTime createTime;

	@ApiModelProperty("备注")
	private String remark;

	@ApiModelProperty("是否处理")
	private String isHandle;

}
