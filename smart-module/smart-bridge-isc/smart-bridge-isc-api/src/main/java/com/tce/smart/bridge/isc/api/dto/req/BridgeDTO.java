package com.tce.smart.bridge.isc.api.dto.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * @Description: TODO
 * @ProjectName smart-dispatcher
 * @ClassName: DispatcherDTO
 * @Author jinbo
 * @Date 2019/11/6
 */
@Data
public class BridgeDTO<T> implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 事件ID
	 */
	@ApiModelProperty(value = "事件ID")
	private String eventId;
	/**
	 * 事件类型
	 */
	@ApiModelProperty(value = "事件类型")
	private Integer eventType;
	/**
	 * 园区ID
	 */
	@ApiModelProperty(value = "园区ID")
	private Integer parkId;
	/**
	 * 设备ID
	 */
	@ApiModelProperty(value = "设备ID")
	private String deviceId;
	/**
	 * 业务数据
	 */
	@ApiModelProperty(value = "业务数据")
	private T data;
}
