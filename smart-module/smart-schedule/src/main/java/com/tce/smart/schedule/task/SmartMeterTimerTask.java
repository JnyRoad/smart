package com.tce.smart.schedule.task;

import cn.hutool.core.date.DateUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.feign.RemoteDailySettlementService;
import com.tce.smart.schedule.config.TaskJob;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.schedule.service.platform.SmartMeterService;
import com.tce.smart.tool.enums.TimerTaskEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 智能水电表定时任务
 *
 * @author sunfujian
 * @since 2021/11/16 15:39
 */
@Slf4j
@Component
@EnableScheduling
public class SmartMeterTimerTask {

	@Autowired
	private ISwitchService iSwitchService;
	@Autowired
	private SmartMeterService smartMeterService;
	@Autowired
	private RemoteDailySettlementService remoteDailySettlementService;
	@Autowired
	private TaskJob taskJob;

	/**
	 * 定时查询设备连接状态
	 * 每5分钟同步一次
	 */
	@Scheduled(cron = "${task.meter.checkOnline}")
	public void deviceStatusTask() {
		if (taskJob.getSmartMeterStatus() && iSwitchService.process(TimerTaskEnum.SMART_METER_STATUS)) {
			try {
				log.info("发起智能水电表状态查询");
				smartMeterService.queryDeviceStatus();
			} catch (Exception e) {
				log.error("查询设备状态异常", e);
			}
		}
	}

	/**
	 * 水表读数查询任务
	 * 每天凌晨1点执行
	 */
	@Scheduled(cron = "${task.meter.water.reading}")
	public void waterMeterReadingTask() {
		if (taskJob.getWaterMeterReading() && iSwitchService.process(TimerTaskEnum.WATER_METER_READING)) {
			try {
				log.info("水表读数查询任务开始,{}", DateUtil.now());
				smartMeterService.readWaterMeterValue();
			} catch (Exception e) {
				log.error("水表读数查询任务执行异常", e);
			}
		}
	}

	/**
	 * 电表读数查询任务
	 * 每天凌晨1点执行
	 */
	@Scheduled(cron = "${task.meter.ele.reading}")
	public void eleMeterReadingTask() {
		if (taskJob.getEleMeterReading() && iSwitchService.process(TimerTaskEnum.ELE_METER_READING)) {
			try {
				log.info("电表读数查询任务开始,{}", DateUtil.now());
				smartMeterService.readEleMeterValue();
				smartMeterService.readEleMeterState();
			} catch (Exception e) {
				log.error("电表读数查询任务执行异常", e);
			}
		}
	}

	/**
	 * 水电日结算
	 */
	@Scheduled(cron = "${task.meter.day.calc}")
	public void genDailySettlementRecord() {
		if (taskJob.getGenSettlementDaily() && iSwitchService.process(TimerTaskEnum.GEN_SETTLEMENT_DAILY)) {
			try {
				log.info("水电日结算开始,{}", DateUtil.now());
				remoteDailySettlementService.genDailyRecord(SecurityConstants.FROM_IN,
						SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			} catch (Exception e) {
				log.error("水电日结算执行异常", e);
			}
		}
	}
}
