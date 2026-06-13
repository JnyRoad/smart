package com.tce.smart.platform.api.dto.req.watermeter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 10:53
 */
@Data
public class WaterMeterQueryDTO extends BaseDTO {

	@ApiModelProperty("集中器ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long concenId;

	@ApiModelProperty("园区ID")
	private Integer parkId;

	@ApiModelProperty("楼栋ID")
	private Integer dormitoryId;

	@ApiModelProperty("房间ID")
	private Integer roomId;

	@ApiModelProperty("名称")
	private String name;

	@ApiModelProperty("状态：0、未连接；1、离线；2、在线")
	private Integer status;

	@ApiModelProperty("标签ID集合")
	private List<Long> tagIds;

	@ApiModelProperty("通信地址")
	private String address;

	@ApiModelProperty("设备位置")
	private String place;
}
