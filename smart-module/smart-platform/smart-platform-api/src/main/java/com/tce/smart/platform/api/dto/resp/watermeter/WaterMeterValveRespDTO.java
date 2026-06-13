package com.tce.smart.platform.api.dto.resp.watermeter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.tce.smart.common.core.dto.BaseDTO;
import com.tce.smart.platform.api.dto.resp.DeviceTagListDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/20 11:18
 */
@Data
public class WaterMeterValveRespDTO extends BaseDTO {

	@ApiModelProperty("主键ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	@ApiModelProperty("名称")
	private String name;

	@ApiModelProperty("序号")
	private Integer seq;

	@ApiModelProperty("园区ID")
	private Integer parkId;

	@ApiModelProperty("园区名称")
	private String parkName;

	@ApiModelProperty("阀门集中器ID")
	private Long concentratorId;

	@ApiModelProperty("集中器名称")
	private String concentratorName;

	@ApiModelProperty("阀门是否开启：0、关闭；1、开启")
	private Integer isOpen;

	@ApiModelProperty("本地远程状态 0.本地 1.远程")
	private Integer remoteStatus;

	@ApiModelProperty("备注")
	private String remark;

	@ApiModelProperty("设备标签")
	private List<DeviceTagListDTO> tagList;
}
