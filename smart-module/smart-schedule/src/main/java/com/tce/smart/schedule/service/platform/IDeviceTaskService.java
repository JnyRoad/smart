package com.tce.smart.schedule.service.platform;

/**
 * 设备任务信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
public interface IDeviceTaskService {

	/**
	 * 卡片下发
	 */
	void downCard();

	/**
	 * 车辆下发
	 */
	void downCar();

	/**
	 * 卡片删除
	 */
	void delCard();

	/**
	 * 车辆删除
	 */
	void delCar();


}
