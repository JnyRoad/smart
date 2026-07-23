package com.tce.smart.schedule.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.guard.core.entity.VcallCar;
import com.tce.smart.platform.api.dto.SmtParkLogisticsDTO;
import com.tce.smart.platform.api.dto.req.LogisticsAppointmentReqDTO;
import com.tce.smart.platform.api.feign.RemoteLogisticsAppointmentService;
import com.tce.smart.platform.api.feign.RemoteParkLogisticsService;
import com.tce.smart.schedule.config.TaskJob;
import com.tce.smart.schedule.service.comm.ISwitchService;
import com.tce.smart.schedule.service.guard.IVcallCarService;
import com.tce.smart.tool.constant.LogisticsAppointmentConstants;
import com.tce.smart.tool.enums.TimerTaskEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-module
 * @ClassName: ViewTimerTask
 * @Author jinbo
 * @Date 2019/5/5
 */
@Slf4j
@Component
@EnableScheduling
public class GuardTimerTask {

	@Autowired
	private RemoteLogisticsAppointmentService remoteLogisticsAppointmentService;
	@Autowired
	private RemoteParkLogisticsService remoteParkLogisticsService;
	@Autowired
	private IVcallCarService vcallCarService;
	@Autowired
	private ISwitchService iSwitchService;
	@Autowired
	private TaskJob taskJob;

	/**
	 * 同步物流车预约信息
	 * 每1分钟同步一次
	 */
	@Scheduled(fixedDelay = 1000 * 60)
	public void evwEmpHrTask() {
		if (taskJob.getLogisticsAppointmentType() && iSwitchService.process(TimerTaskEnum.LOGISTICS_APPOINTMENT_TYPE)) {
			Page<VcallCar> page = new Page<>();
			vcallCarService.getVcallCarPage(page);
			log.info("同步物流车预约信息,共有{}条数据", page.getRecords().size());
			saveVcallCar(page);
			while (page.hasNext()) {
				page.setCurrent(page.getCurrent() + 1);
				vcallCarService.getVcallCarPage(page);
				saveVcallCar(page);
			}
		}
	}

	private void saveVcallCar(Page<VcallCar> page) {
		final List<VcallCar> data = page.getRecords();
		if (CollectionUtils.isNotEmpty(data)) {
			for (VcallCar vcallCar : data) {
				Result<SmtParkLogisticsDTO> result = remoteParkLogisticsService
						.getByCompanyId(vcallCar.getCompanyId(), SecurityConstants.FROM_IN,
								SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
				if (result.isSuccess() && Objects.nonNull(result.getData())) {
					LogisticsAppointmentReqDTO logisticsAppointmentReqDTO = new LogisticsAppointmentReqDTO();
					logisticsAppointmentReqDTO.setParkId(result.getData().getParkId());
					logisticsAppointmentReqDTO.setPlanCode(vcallCar.getCallCode());
					logisticsAppointmentReqDTO.setStartTime(vcallCar.getCallSentTime());
					logisticsAppointmentReqDTO.setEndTime(DateUtil.endOfDay(DateUtil
							.offsetDay(vcallCar.getCallSentTime(), 2)));
					logisticsAppointmentReqDTO.setStatus(LogisticsAppointmentConstants.BOOKED);
					logisticsAppointmentReqDTO.setSupplier(vcallCar.getSupplierName());
					logisticsAppointmentReqDTO.setCompanyId(vcallCar.getCompanyId());
					// 有些数据是以“,”分割的字符串
					if (StrUtil.isNotEmpty(vcallCar.getCarIdStr())) {
						String[] carIdStrs = vcallCar.getCarIdStr().split(",");
						for (int j = 0; j < carIdStrs.length; j++) {
							logisticsAppointmentReqDTO.setVehiclePlate(carIdStrs[j]);
							if (StrUtil.isNotEmpty(vcallCar.getDriverNameStr())) {
								String[] driverNameStrs = vcallCar.getDriverNameStr().split(",");
								if (driverNameStrs.length > j) {
									logisticsAppointmentReqDTO.setDriverName(driverNameStrs[j]);
								}
							}

							if (StrUtil.isNotEmpty(vcallCar.getDriverTelStr())) {
								String[] driverTelStrs = vcallCar.getDriverTelStr().split(",");
								if (driverTelStrs.length > j) {
									logisticsAppointmentReqDTO.setDriverPhone(driverTelStrs[j]);
								}
							}
							result = remoteLogisticsAppointmentService
									.save(logisticsAppointmentReqDTO, SecurityConstants.FROM_IN,
											SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
							log.info(result.getMsg());
							try {
								Thread.sleep(100L);
							} catch (Exception e) {
								log.error(e.toString());
							}
						}
					}
				}
			}
		}
	}
}
