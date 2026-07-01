package com.tce.smart.platform.service;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.model.DeviceDecommissionPlan;

/**
 * 设备下线编排服务：给定一个设备ID，计算它牵连了哪些权限组，并按计算结果执行清理。
 */
public interface DeviceDecommissionService {

	/**
	 * 只读计算：给定设备ID，算出会影响哪些权限组及后续处理方式（解绑 / 连带删组 / 保护不删组）。
	 */
	DeviceDecommissionPlan plan(String deviceId);

	/**
	 * 按 plan 的结果执行清理：撤销权限、解绑设备、按需级联删组。
	 */
	void execute(DeviceDecommissionPlan plan);

	/**
	 * 计算 plan、执行清理、再删除设备本身，整体在一个事务里完成。
	 */
	Result decommissionDevice(String deviceId);
}
