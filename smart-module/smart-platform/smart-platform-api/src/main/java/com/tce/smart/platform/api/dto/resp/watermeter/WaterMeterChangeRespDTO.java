package com.tce.smart.platform.api.dto.resp.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Li.JiaJun
 * @since 2022/5/12 11:08
 */
@Data
public class WaterMeterChangeRespDTO extends BaseDTO {

	@ApiModelProperty("更换前通信地址")
	private String beforeAddress;

	@ApiModelProperty("更换前水表序号")
	private Integer beforeSeq;

	@ApiModelProperty("更换前水表下行通道")
	private String beforePort;

	@ApiModelProperty("更换前用户大类")
	private String beforeLargeClass;

	@ApiModelProperty("更换前集中器")
	private String beforeConcentrator;

	@ApiModelProperty("更换后通信地址")
	private String afterAddress;

	@ApiModelProperty("更换后水表序号")
	private Integer afterSeq;

	@ApiModelProperty("更换后水表下行通道")
	private String afterPort;

	@ApiModelProperty("更换后用户大类")
	private String afterLargeClass;

	@ApiModelProperty("更换后集中器")
	private String afterConcentrator;

	@ApiModelProperty("更换人")
	private String createUserName;

	@ApiModelProperty("更换时间")
	private LocalDateTime createTime;
}
