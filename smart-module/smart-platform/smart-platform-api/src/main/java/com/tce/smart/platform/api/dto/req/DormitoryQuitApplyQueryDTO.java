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
public class DormitoryQuitApplyQueryDTO implements Serializable {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty("园区ID")
	private Integer parkId;

	@ApiModelProperty("申请人工号")
	private String badge;

	@ApiModelProperty("申请人姓名")
	private String name;

	@ApiModelProperty("申请人ID")
	private Long staffId;

	@ApiModelProperty("状态")
	private Integer status;

	@ApiModelProperty("房间ID")
	private List<Integer> roomIds;

	@ApiModelProperty("房间号")
	private Integer roomNum;

	@ApiModelProperty("申请开始时间")
	private String startTime;

	@ApiModelProperty("申请结束时间")
	private String endTime;

	@ApiModelProperty("个人园区列表")
	private List<Integer> parkList;


}
