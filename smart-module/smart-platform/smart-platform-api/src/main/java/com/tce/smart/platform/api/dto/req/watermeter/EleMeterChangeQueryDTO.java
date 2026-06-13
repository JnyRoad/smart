package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2022/5/12 11:02
 */
@Data
public class EleMeterChangeQueryDTO extends BaseDTO {

	@ApiModelProperty("更换前设备通信地址")
	private String beforeAddress;

	@ApiModelProperty("更换后设备通信地址")
	private String afterAddress;

	@ApiModelProperty("开始时间：yyyy-MM-dd HH:ss")
	private String startTime;

	@ApiModelProperty("结束时间：yyyy-MM-dd HH:ss")
	private String endTime;
}
