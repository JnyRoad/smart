package com.tce.smart.platform.api.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @program: smart-module
 * @description:
 * @author: Wuling
 * @create: 2021-06-11 10:18
 **/

@Data
public class DormitoryMeterQueryDTO implements Serializable {
	private static final long serialVersionUID = 1234717581306869508L;

	@ApiModelProperty("楼栋Id列表")
	private String dormitoryIds;

	@ApiModelProperty(value = "抄表月份",required = true)
	@DateTimeFormat(pattern = "yyyy-MM")
	private Date meterMonth;
}
