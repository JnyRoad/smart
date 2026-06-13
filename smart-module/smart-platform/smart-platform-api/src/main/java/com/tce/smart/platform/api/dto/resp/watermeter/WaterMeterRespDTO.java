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
 * @since: 2021/8/19 20:41
 */
@Data
public class WaterMeterRespDTO extends BaseDTO {

	@ApiModelProperty("主键ID")
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	@ApiModelProperty("名称")
	private String name;

	@ApiModelProperty("楼栋名称")
	private String dormitoryName;

	@ApiModelProperty("楼栋ID")
	private Integer dormitoryId;

	@ApiModelProperty("园区名称")
	private String parkName;

	@ApiModelProperty("园区ID")
	private Integer parkId;

	@ApiModelProperty("状态：离线/在线")
	private String status;

	@ApiModelProperty("房间名称")
	private String roomName;

	@ApiModelProperty("房间ID")
	private Integer roomId;

	@ApiModelProperty("设备标签")
	private List<DeviceTagListDTO> tagList;

	@ApiModelProperty("当前读数")
	private String currentReading;

	@ApiModelProperty("阀门控制：0、关闭；1、开启；2、未关联阀门；3、关闭中；4、开启中")
	private Integer valveStatus;

	@ApiModelProperty("集中器ID")
	private Long concentratorId;

	@ApiModelProperty("集中器名称")
	private String concentratorName;

	@ApiModelProperty("水表序号")
	private Integer seq;

	@ApiModelProperty("水表通信端口")
	private String port;

	@ApiModelProperty("通信地址")
	private String address;

	@ApiModelProperty("水表大类")
	private String largeClass;

	@ApiModelProperty("水表大类描述")
	private String largeClassDesc;

	@ApiModelProperty("区域类型：0、宿舍；1、厂区")
	private Integer placeType;

	@ApiModelProperty("厂区ID")
	private Integer areaId;

	@ApiModelProperty("厂区名称")
	private String areaName;

	@ApiModelProperty("厂区节点树")
	private List<Integer> areaIds;
}
