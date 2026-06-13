package com.tce.smart.platform.api.dto.resp;

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
public class DeviceAuthVehicleRespDTO extends BaseDTO {
	@ApiModelProperty(value = "卡片号")
	private String cardNo;

	@ApiModelProperty(value = "设备编号")
	private String deviceCode;

	@ApiModelProperty(value = "设备名称")
	@Excel(name = "设备名称", isImportField = "deviceName")
	private String deviceName;

	@ApiModelProperty(value = "所在区域")
	@Excel(name = "所在区域", isImportField = "areaName")
	private String areaName;

	@ApiModelProperty(value = "车牌号")
	@Excel(name = "车牌号", isImportField = "plate")
	private String plate;

	@ApiModelProperty(value = "创建时间")
	@Excel(name = "创建时间", isImportField = "createTime", exportFormat = "yyyy-MM-dd HH:mm:ss")
	private Date createTime;

	@ApiModelProperty(value = "授权开始时间")
//	@Excel(name = "授权开始时间", isImportField = "startTime", exportFormat = "yyyy-MM-dd HH:mm:ss")
	private Date startTime;

	@ApiModelProperty(value = "授权截止时间")
//	@Excel(name = "授权截止时间", isImportField = "overTime", exportFormat = "yyyy-MM-dd HH:mm:ss")
	private Date overTime;


	private Integer serviceType;

	private Integer cardType;
}
