package com.tce.smart.platform.core.vo;

import javax.validation.constraints.NotBlank;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

public class DeviceAreaVO {

	/**
	 * 地点表
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;
	/**
	 * 所属园区id
	 */
	@NotBlank(message = "所属园区id不能为空")
	private Integer parkId;
	/**
	 * 地点名称
	 */
	@NotBlank(message = "地点名称不能为空")
	private String areaName;

}
