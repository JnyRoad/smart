package com.tce.smart.platform.api.dto.req;

import com.tce.smart.platform.api.dto.SmtDeviceTaskDTO;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Data
public class DeviceTaskReqDTO implements Serializable {
private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@NotNull(message = "data不可空")
	private SmtDeviceTaskDTO data;

	/**
	 * 备注
	 */
	private String message;

	/**
	 * 异常码
	 */
	@NotNull(message = "异常码不可空")
	private Integer code;




}
