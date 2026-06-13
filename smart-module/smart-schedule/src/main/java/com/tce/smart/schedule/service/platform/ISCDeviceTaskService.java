package com.tce.smart.schedule.service.platform;

/**
 * ISC平台任务下发
 * @author sunfujian
 * @date 2021/8/25 9:39
 */
public interface ISCDeviceTaskService {

	/**
	 * 人员权限下发
	 */
	void downAccess();

	void authConfigProcessHandle();

	/**
	 * ISC下载权限进度处理
	 */
	void authConfigDownResultHandle();

	/**
	 * 人员权限删除
	 */
	void delAccess();

	/**
	 * 同步ISC平台设备信息
	 */
	void syncDevice();

	void getTemperature();
}
