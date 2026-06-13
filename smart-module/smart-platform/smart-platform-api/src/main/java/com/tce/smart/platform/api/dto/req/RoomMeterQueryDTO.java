package com.tce.smart.platform.api.dto.req;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.bytebuddy.asm.Advice;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * @program: smart-module
 * @description:
 * @author: Wuling
 * @create: 2021-06-11 10:18
 **/

@Data
public class RoomMeterQueryDTO implements Serializable {
	private static final long serialVersionUID = 1234717581306869508L;

	@ApiModelProperty("楼栋Id")
	private Integer dormitoryId;

	@ApiModelProperty("楼栋Id")
	private List<Integer> dormitoryIds;

	@ApiModelProperty("楼层Id")
	private Integer floorId;

	@ApiModelProperty("房间Id")
	private Integer roomId;

	@ApiModelProperty(value = "抄表月份")
	@DateTimeFormat(pattern = "yyyy-MM")
	private Date meterMonth;

	@ApiModelProperty(value = "开始日期")
	private String startTime;

	@ApiModelProperty(value = "结束日期")
	private String endTime;
}
