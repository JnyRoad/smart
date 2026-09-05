package com.tce.smart.platform.api.dto.resp.admittance;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理端访客手动授权选项。
 *
 * 申请单及人员 ID 使用字符串，避免前端 JavaScript 对 Long 的精度损失；车辆列表暂时固定为空。
 */
@Data
public class VisitorManualAuthOptionsRespDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty("入厂申请单 ID")
	private String applyId;

	@ApiModelProperty("权限开始时间，已扣除现有下发提前小时数")
	private String startTime;

	@ApiModelProperty("权限结束时间")
	private String endTime;

	@ApiModelProperty("本申请人员")
	private List<FellowOption> fellows = new ArrayList<>();

	@ApiModelProperty("本申请车辆；当前版本固定为空")
	private List<VehicleOption> vehicles = new ArrayList<>();

	@ApiModelProperty("当前园区可用于人员下发的公共权限组")
	private List<AuthorityOption> authorities = new ArrayList<>();

	@Data
	public static class FellowOption implements Serializable {
		private static final long serialVersionUID = 1L;

		@ApiModelProperty("人员 ID")
		private String id;

		@ApiModelProperty("人员姓名")
		private String name;
	}

	@Data
	public static class VehicleOption implements Serializable {
		private static final long serialVersionUID = 1L;

		@ApiModelProperty("车辆 ID")
		private String id;

		@ApiModelProperty("车牌号")
		private String plate;
	}

	@Data
	public static class AuthorityOption implements Serializable {
		private static final long serialVersionUID = 1L;

		@ApiModelProperty("权限组 ID")
		private Integer id;

		@ApiModelProperty("权限组名称")
		private String authorityName;

		@ApiModelProperty("权限组类型")
		private Integer type;

		@ApiModelProperty("权限组性质，0 公共")
		private Integer areaType;
	}
}
