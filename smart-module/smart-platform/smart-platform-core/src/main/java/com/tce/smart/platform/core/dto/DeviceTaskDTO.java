package com.tce.smart.platform.core.dto;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceTaskDTO extends Model<DeviceTaskDTO> {
private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@NotNull(message = "data不可空")
	private SmtDeviceTask data;

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
