package com.tce.smart.platform.api.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import com.tce.smart.common.core.dto.BaseDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * @author sunfujian
 * @date 2021/8/3 20:29
 */
@Data
public class IscTemperatureDTO extends BaseDTO {
	@ApiModelProperty(value = "体温")
	private String temp;

	@ApiModelProperty(value = "记录时间")
	private String alarmTime;

	@ApiModelProperty(value = "身份证号")
	private String certNo;

	@ApiModelProperty(value = "工号")
	private String jobNo;

	@ApiModelProperty(value = "资源点名称")
	private String resourceName;


}
