package com.tce.smart.platform.api.dto.req.sddto;

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
public class RoomSDMeterreadReqDTO implements Serializable {
	private static final long serialVersionUID = 793131149625473307L;

	@ApiModelProperty(value = "房间列表",required = true)
	private List<Integer> roomIds;

	@ApiModelProperty(value = "抄表月份",required = true)
	@JsonFormat(pattern = "yyyy-MM")
	@DateTimeFormat(pattern = "yyyy-MM")
	private Date meterMonth;
}
