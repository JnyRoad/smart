package com.tce.smart.platform.api.dto.req;

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
public class DormitoryQuitApplyEditReqDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("ID")
	private Long id;

	@ApiModelProperty("园区ID")
	private Integer parkId;

	@ApiModelProperty("申请人工号")
	private String badge;

	@ApiModelProperty("申请人姓名")
	private String name;

	@ApiModelProperty("申请人ID")
	private Long staffId;

	@ApiModelProperty("退宿房间ID")
	private List<Integer> roomIds;

	@ApiModelProperty("退宿宿舍ID")
	private List<Integer> dormitoryIds;

	@ApiModelProperty("退宿原因")
	private Integer quitReason;

	@ApiModelProperty("备注")
	private String remark;

	@ApiModelProperty("上传图片")
	private List<String> imgs;

	@ApiModelProperty("状态")
	private Integer status;

	@ApiModelProperty("申请离开时间")
	private LocalDateTime applyLeaveTime;

}
