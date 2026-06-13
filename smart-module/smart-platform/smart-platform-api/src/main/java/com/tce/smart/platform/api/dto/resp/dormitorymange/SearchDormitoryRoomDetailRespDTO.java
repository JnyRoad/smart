package com.tce.smart.platform.api.dto.resp.dormitorymange;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tce.smart.platform.api.dto.resp.staffmange.StaffRespDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: 查询宿舍详情相应DTO
 * @date: 2020/9/29 8:48
 * @author: wuling
 * @version: 1.0
 */
@Data
public class SearchDormitoryRoomDetailRespDTO implements Serializable {
	private static final long serialVersionUID = 3302031997345451879L;

	@ApiModelProperty(value = "园区ID")
	private Integer parkId;

	@ApiModelProperty(value = "楼栋ID")
	private Integer dormitoryId;

	@ApiModelProperty(value = "楼层ID")
	private Integer floorId;

	@ApiModelProperty(value = "房间类型")
	private Integer roomType;

	@ApiModelProperty(value = "房间入住状态 1-未满， 2-已满， 3-空房")
	private Integer inStatus;

	@ApiModelProperty(value = "房间性别类型 0-男，1-女，2-夫妻，3-其他")
	private Integer sex;

	@ApiModelProperty(value = "是否正常房间 1-正常，2-异常 默认正常")
	private Integer isNormal = 1;

	@ApiModelProperty(value = "房间标识ID")
	private Integer roomId;

	@ApiModelProperty(value = "房间编号")
	private String roomName;

	@ApiModelProperty(value = "房间类型名称")
	private String roomTypeName;

	@ApiModelProperty(value = "床位总数")
	private Integer bedTotal;

	@ApiModelProperty(value = "入住人数")
	private Integer actCount;

	@ApiModelProperty(value = "床位详情列表")
	private List<BedDetail> bedDetailList;

	/**
	 * 床位详情
	 */
	@Data
	public static class BedDetail{

		@ApiModelProperty(value = "员工入住表ID")
		private Integer dorStaffId;

		@ApiModelProperty(value = "床位ID")
		private Integer bedId;

		@ApiModelProperty(value = "床位编号")
		private Integer bedNumber;

		@ApiModelProperty(value = "床位入住人员在职状态 null-未住人, 1-在职， 2-离职未退宿， 3-未入职")
		private Integer inStatus;

		@ApiModelProperty(value = "员工信息")
		private StaffRespDTO staffInfo;

		@ApiModelProperty(value = "入住时间")
		@JsonFormat(pattern = "yyyy-MM-dd")
		private Date inTime;

	}

}
