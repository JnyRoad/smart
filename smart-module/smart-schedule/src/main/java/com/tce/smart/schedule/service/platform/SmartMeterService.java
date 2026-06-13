package com.tce.smart.schedule.service.platform;

/**
 * @author sunfujian
 * @since 2021/11/3 17:10
 */
public interface SmartMeterService {
	/**
	 * 查询水表读数
	 */
	void readWaterMeterValue();

	/**
	 * 查询电表读数
	 */
	void readEleMeterValue();

	/**
	 * 异步查询电表闸门状态
	 */
	void readEleMeterState();

	/**
	 * 查询集中器设备状态
	 */
	void queryDeviceStatus();
}
