package com.tce.smart.platform.api.dto.req.dailySd;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2022/7/8 10:29
 */
@Data
public class DailyMeterQueryDTO extends BaseDTO {

	@ApiModelProperty("楼栋Id")
	private Integer dormitoryId;

	@ApiModelProperty("楼层Id")
	private Integer floorId;

	@ApiModelProperty("房间Id")
	private Integer roomId;

	@ApiModelProperty(value = "抄表开始日期")
	private String startTime;

	@ApiModelProperty(value = "抄表结束日期")
	private String endTime;
}
