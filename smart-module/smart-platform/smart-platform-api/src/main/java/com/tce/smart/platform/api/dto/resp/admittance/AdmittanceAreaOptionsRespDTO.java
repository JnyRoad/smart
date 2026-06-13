package com.tce.smart.platform.api.dto.resp.admittance;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class AdmittanceAreaOptionsRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("园区ID")
	private Integer parkId;

	@ApiModelProperty("当前页内联展示授权区域数量")
	private Integer inlineAreaLimit = 4;

	@ApiModelProperty("厂区列表")
	private List<FactoryOption> factories = new ArrayList<>();

	@Data
	public static class FactoryOption implements Serializable {
		private static final long serialVersionUID = 1L;

		@ApiModelProperty("厂区类型，兼容现有permitFactoryType")
		private String factoryType;

		@ApiModelProperty("厂区名称")
		private String factoryName;

		@ApiModelProperty("现有区域接口flag，1新工厂，0老工厂")
		private Integer areaFlag;

		@ApiModelProperty("展示顺序")
		private Integer sort;

		@ApiModelProperty("授权区域列表")
		private List<AreaOption> areas = new ArrayList<>();
	}

	@Data
	public static class AreaOption implements Serializable {
		private static final long serialVersionUID = 1L;

		@ApiModelProperty("授权区域编码，兼容现有areaType")
		private Integer areaCode;

		@ApiModelProperty("授权区域名称")
		private String areaName;

		@ApiModelProperty("是否常用区域")
		private Boolean isCommon = Boolean.FALSE;

		@ApiModelProperty("展示顺序")
		private Integer sort;
	}
}
