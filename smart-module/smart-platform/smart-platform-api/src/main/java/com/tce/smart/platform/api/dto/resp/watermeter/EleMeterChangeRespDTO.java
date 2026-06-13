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
public class EleMeterChangeRespDTO extends BaseDTO {

	@ApiModelProperty("更换前通信地址")
	private String beforeAddress;

	@ApiModelProperty("更换前电表序号")
	private Integer beforeSeq;

	@ApiModelProperty("更换前倍率")
	private Integer beforeRatio;

	@ApiModelProperty("更换前通信端口号")
	private String beforePort;

	@ApiModelProperty("更换前集中器")
	private String beforeConcentrator;

	@ApiModelProperty("更换后通信地址")
	private String afterAddress;

	@ApiModelProperty("更换后电表序号")
	private Integer afterSeq;

	@ApiModelProperty("更换后倍率")
	private Integer afterRatio;

	@ApiModelProperty("更换后通信端口号")
	private String afterPort;

	@ApiModelProperty("更换后集中器")
	private String afterConcentrator;

	@ApiModelProperty("更换人")
	private String createUserName;

	@ApiModelProperty("更换时间")
	private LocalDateTime createTime;
}
