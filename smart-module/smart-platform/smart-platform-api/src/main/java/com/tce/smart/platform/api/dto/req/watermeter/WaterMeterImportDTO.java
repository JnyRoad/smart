package com.tce.smart.platform.api.dto.req.watermeter;

import com.tce.smart.common.core.dto.BaseDTO;
import com.tce.smart.platform.api.annotation.ColumnAlias;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Li.JiaJun
 * @since 2022/3/29 15:30
 */
@Data
public class WaterMeterImportDTO extends BaseDTO {

	@ApiModelProperty("楼栋")
	@ColumnAlias(value = "楼栋")
	private String dormitory;

	@ApiModelProperty("房间号")
	@ColumnAlias(value = "房间号")
	private String room;

	@ApiModelProperty("设备名称")
	@ColumnAlias(value = "设备名称")
	private String name;

	@ApiModelProperty("集中器IP")
	@ColumnAlias(value = "集中器IP")
	private String concentratorIp;

	@ApiModelProperty("下行通道")
	@ColumnAlias(value = "下行通道")
	private String portDesc;

	@ApiModelProperty("用户大类")
	@ColumnAlias(value = "用户大类")
	private String largeClassDesc;

	@ApiModelProperty("设备序号")
	@ColumnAlias(value = "设备序号")
	private Integer seq;

	@ApiModelProperty("水表通信地址")
	@ColumnAlias(value = "水表通信地址")
	private String address;

	@ColumnAlias(value = "错误原因")
	@ApiModelProperty(value = "错误原因")
	private String mark;
}
