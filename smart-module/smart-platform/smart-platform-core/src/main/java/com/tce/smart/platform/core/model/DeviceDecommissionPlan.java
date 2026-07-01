package com.tce.smart.platform.core.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备下线影响计算结果：一台设备当前绑定了哪些权限组，以及每个权限组在设备解绑后应该如何处理。
 * plan() 只读计算此结构，execute() 严格按此结构执行，两者共享同一份计算，保证预览和实际执行一致。
 */
@Data
public class DeviceDecommissionPlan {

	private String deviceId;

	private List<AffectedAuthority> affectedAuthorities = new ArrayList<>();

	@Data
	public static class AffectedAuthority {

		private Integer authorityId;

		private String authorityName;

		/** 该权限组去掉本设备后还剩几台设备 */
		private Integer remainingDeviceCount;

		/** 该权限组下受影响的员工数 */
		private Integer staffCount;

		/** 该权限组下受影响的车辆数 */
		private Integer vehicleCount;

		/** 是否是区域默认权限组或系统内置权限组：变空后不会被自动删除 */
		private boolean protectedAuthority;

		/** 是否会因为设备解绑后变空而被级联删除 */
		private boolean willCascadeDelete;
	}
}
