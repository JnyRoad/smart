package com.tce.smart.platform.api.dto.resp.commonsd;

import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author Li.JiaJun
 * @since 2022/7/21 17:53
 */
@Data
public class SdMeterreadDetailChangeDTO extends BaseDTO {

	@ApiModelProperty("上月止度")
	private Double preMonthNum;

	@ApiModelProperty("本月止度")
	private Double curMonthNum;

	@ApiModelProperty(value = "用量/实用")
	private Double use;

	@ApiModelProperty("收费项目：1、热水；2、冷水；3、电")
	private Integer categoryId;

	@ApiModelProperty("换表日期")
	private LocalDateTime createTime;
}
