package com.tce.smart.platform.api.dto.resp.commonsd;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @description: 查询房间水电抄表DTO
 * @date: 2020/10/12 0012 17:46
 * @author: wuling
 * @version: 1.0
 */
@Data
public class RoomSDMeterreadRespDTO implements Serializable {
	private static final long serialVersionUID = -5597450253413726891L;

	@ApiModelProperty(value = "房间列表")
	private List<RoomMeterStatus> roomMeterStatuses;

	@ApiModelProperty(value = "抄表月份")
	@JsonFormat(pattern = "yyyy-MM")
	@DateTimeFormat(pattern = "yyyy-MM")
	private Date meterMonth;

	@Data
	public static class RoomMeterStatus{

		@ApiModelProperty(value = "房间ID")
		private Integer roomId;

		@ApiModelProperty(value = "抄表状态 0.未抄表 1.未抄完 2.已抄完")
		private Integer status;
	}
}
