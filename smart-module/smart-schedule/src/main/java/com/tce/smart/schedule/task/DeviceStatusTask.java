package com.tce.smart.schedule.task;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.feign.RemoteDeviceService;
import com.tce.smart.schedule.config.TaskJob;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.tool.enums.TimerTaskEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @Description: 更新设备状态定时任务
 * @ProjectName smart-module
 * @Author wuling
 * @Date 2020/09/23
 */
@Slf4j
@Component
@EnableScheduling
public class DeviceStatusTask {

	@Autowired
	private ISwitchService iSwitchService;
	@Autowired
	private RemoteDeviceService remoteDeviceService;
	@Autowired
	private TaskJob taskJob;
	/**
	 * 定时查询设备连接状态
	 * 每5分钟同步一次
	 */
	@Scheduled(fixedDelay = 1000 * 60 * 5)
	public void deviceStatusTask() {
		if (taskJob.getDeviceStatus() && iSwitchService.process(TimerTaskEnum.DEVICE_STATUS)) {
			try {
				log.info("发起设备状态查询");
				remoteDeviceService.queryDeviceStatus(SecurityConstants.FROM_IN,
						SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			} catch (Exception e) {
				log.error("查询设备状态异常", e);
			}
		}
	}
}
