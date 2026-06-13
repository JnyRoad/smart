package com.tce.smart.platform.api.dto.resp.watermeter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/19 20:12
 */
@Data
public class WaterMeterConcentratorRespDTO extends BaseDTO {

	@ApiModelProperty("主键ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	@ApiModelProperty("集中器名称")
	private String name;

	@ApiModelProperty("IP")
	private String ip;

	@ApiModelProperty("电表集中器通信端口号")
	private String port;

	@ApiModelProperty("状态：离线/在线")
	private String status;

	@ApiModelProperty("园区Id")
	private Integer parkId;

	@ApiModelProperty("园区名称")
	private String parkName;

	@ApiModelProperty("备注")
	private String remark;

	@ApiModelProperty("通信地址")
	private String address;
}
