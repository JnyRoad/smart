package com.tce.smart.schedule.service.platform.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.NumberConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.DeviceDataDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.entity.watermeter.*;
import com.tce.smart.platform.core.mapper.SmtParkMapper;
import com.tce.smart.platform.core.mapper.watermeter.*;
import com.tce.smart.platform.core.vo.SmtEleMeterVO;
import com.tce.smart.platform.core.vo.SmtWaterMeterVO;
import com.tce.smart.schedule.service.platform.SmartMeterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能水电服务
 * @author sunfujian
 * @since 2021/11/3 17:11
 */
@Slf4j
@Service
@EnableAsync
@AllArgsConstructor
public class SmartMeterServiceImpl implements SmartMeterService {

	private final SmtWaterMeterMapper waterMeterMapper;

	private final SmtWaterMeterConcentratorMapper waterMeterConcentratorMapper;

	private final SmtWaterValveConcentratorMapper waterValveConcentratorMapper;

	private final SmtEleMeterConcentratorMapper eleMeterConcentratorMapper;

	private final SmtEleMeterMapper eleMeterMapper;

	private final SmtParkMapper parkMapper;

	private final RemoteDispatcherService remoteDispatcherService;

	@Override
	public void readWaterMeterValue() {
		Page page = new Page();
		long current = 1L;
		page.setSize(500L);
		page.setCurrent(current);
		IPage<SmtWaterMeterVO> waterMeterPage;
		int size = 0;
		do {
			waterMeterPage = waterMeterMapper.getPage(page);
			if (waterMeterPage == null || waterMeterPage.getRecords().size() == 0) {
				log.info("水表为空");
				return;
			}
			current += 1;
			page.setCurrent(current);
			size += waterMeterPage.getSize();
			waterMeterPage.getRecords().forEach(item -> {
				// 每条请求延迟500ms
				ThreadUtil.sleep(500);
				SmtWaterMeter smtWaterMeter = waterMeterMapper.selectById(item.getId());
				try {
					// 园区分发
					DeviceDataDTO deviceInfo = new DeviceDataDTO();
					deviceInfo.setDeviceCode(item.getConcentratorId().toString());
					deviceInfo.setDeviceIp(item.getIp());
					deviceInfo.setDevicePort(item.getPort());
					deviceInfo.setWaterMeterSeq(item.getSeq());

					DispatcherDTO<DeviceDataDTO> dispatcherDTO = new DispatcherDTO<>();
					dispatcherDTO.setEventId(IdUtil.simpleUUID());
					dispatcherDTO.setEventType(EventEnum.WATER_METER_READ.getCode());
					dispatcherDTO.setParkId(item.getParkId());
					dispatcherDTO.setDeviceId(item.getConcentratorId().toString());
					dispatcherDTO.setData(deviceInfo);
					Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
					if (!result.isSuccess()) {
						throw new SmartException("水表" + item.getId() + "读数请求失败");
					}
					JSONObject object = JSONUtil.parseObj(result.getData());
					String code = "code";
					log.info("水表{}读数数据：{}", item.getId(), object);
					if (!object.containsKey(code)) {
						throw new SmartException("数据格式不正确，水表读数请求失败");
					}
					smtWaterMeter.setIsOnline(object.getBool("data") ? NumberConstants.TWO : NumberConstants.ONE);
				} catch (Exception e) {
					smtWaterMeter.setIsOnline(NumberConstants.ONE);
					log.error("水表{}读数请求失败", item.getId());
				}
				smtWaterMeter.setUpdateUserId(0);
				smtWaterMeter.setUpdateTime(LocalDateTime.now());
				waterMeterMapper.updateById(smtWaterMeter);
			});
		} while(waterMeterPage.getTotal() > size);
	}

	@Override
	public void readEleMeterValue() {
		Page page = new Page();
		long current = 1L;
		page.setSize(500L);
		page.setCurrent(current);
		IPage<SmtEleMeterVO> eleMeterPage;
		int size = 0;
		do {
			eleMeterPage = eleMeterMapper.getPage(page);
			if (eleMeterPage == null || eleMeterPage.getRecords().size() == 0) {
				log.info("电表为空");
				return;
			}
			current += 1;
			page.setCurrent(current);
			size += eleMeterPage.getSize();
			eleMeterPage.getRecords().forEach(item -> {
				// 每条请求延迟500ms
				ThreadUtil.sleep(500);
				SmtEleMeter smtEleMeter = eleMeterMapper.selectById(item.getId());
				try {
					// 园区分发
					DeviceDataDTO deviceInfo = new DeviceDataDTO();
					deviceInfo.setDeviceCode(item.getConcentratorId().toString());
					deviceInfo.setDeviceIp(item.getIp());
					deviceInfo.setDevicePort(item.getPort());
					deviceInfo.setElectricMeterSeq(item.getSeq());
					deviceInfo.setEleMeterAddress(smtEleMeter.getAddress());
					deviceInfo.setEleMeterPort(Integer.parseInt(smtEleMeter.getPort()));

					DispatcherDTO<DeviceDataDTO> dispatcherDTO = new DispatcherDTO<>();
					dispatcherDTO.setEventId(IdUtil.simpleUUID());
					dispatcherDTO.setEventType(EventEnum.ELE_METER_READ.getCode());
					dispatcherDTO.setParkId(item.getParkId());
					dispatcherDTO.setDeviceId(item.getConcentratorId().toString());
					dispatcherDTO.setData(deviceInfo);
					Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);

					if (!result.isSuccess()) {
						throw new SmartException("电表" + item.getId() + "读数请求失败");
					}
					JSONObject object = JSONUtil.parseObj(result.getData());
					String code = "code";
					log.info("电表{}读数数据：{}", item.getId(), object);
					if (!object.containsKey(code)) {
						throw new SmartException("数据格式不正确，电表读数请求失败");
					}
					smtEleMeter.setIsOnline(object.getBool("data") ? NumberConstants.TWO : NumberConstants.ONE);
				} catch (Exception e) {
					smtEleMeter.setIsOnline(NumberConstants.ONE);
					log.error("电表{}读数请求失败", item.getId());
				}
				smtEleMeter.setUpdateUserId(0);
				smtEleMeter.setUpdateTime(LocalDateTime.now());
				eleMeterMapper.updateById(smtEleMeter);
			});
		} while(eleMeterPage.getTotal() > size);
	}

	@Async
	@Override
	public void readEleMeterState() {
		Page page = new Page();
		long current = 1L;
		page.setSize(500L);
		page.setCurrent(current);
		IPage<SmtEleMeterVO> eleMeterPage;
		int size = 0;
		do {
			eleMeterPage = eleMeterMapper.getPage(page);
			if (eleMeterPage == null || eleMeterPage.getRecords().size() == 0) {
				log.info("电表为空");
				return;
			}
			current += 1;
			page.setCurrent(current);
			size += eleMeterPage.getSize();
			eleMeterPage.getRecords().forEach(item -> {
				// 每条请求延迟500ms
				ThreadUtil.sleep(500);
				SmtEleMeter smtEleMeter = eleMeterMapper.selectById(item.getId());
				try {
					// 园区分发
					DeviceDataDTO deviceInfo = new DeviceDataDTO();
					deviceInfo.setDeviceCode(item.getConcentratorId().toString());
					deviceInfo.setDeviceIp(item.getIp());
					deviceInfo.setDevicePort(item.getPort());
					deviceInfo.setElectricMeterSeq(item.getSeq());
					deviceInfo.setEleMeterAddress(smtEleMeter.getAddress());
					deviceInfo.setEleMeterPort(Integer.parseInt(smtEleMeter.getPort()));

					DispatcherDTO<DeviceDataDTO> dispatcherDTO = new DispatcherDTO<>();
					dispatcherDTO.setEventId(IdUtil.simpleUUID());
					dispatcherDTO.setEventType(EventEnum.ELE_METER_BRAKE_READ.getCode());
					dispatcherDTO.setParkId(item.getParkId());
					dispatcherDTO.setDeviceId(item.getConcentratorId().toString());
					dispatcherDTO.setData(deviceInfo);
					Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);

					if (!result.isSuccess()) {
						throw new SmartException("电表" + item.getId() + "闸门请求失败");
					}
					JSONObject object = JSONUtil.parseObj(result.getData());
					String code = "code";
					log.info("电表{}闸门数据：{}", item.getId(), object);
					if (!object.containsKey(code)) {
						throw new SmartException("数据格式不正确，电表闸门请求失败");
					}
				} catch (Exception e) {
					log.error("电表{}闸门请求失败", item.getId());
				}
			});
		} while(eleMeterPage.getTotal() > size);
	}

	@Override
	public void queryDeviceStatus() {
		List<SmtPark> parkList = parkMapper.selectList(Wrappers.emptyWrapper());
		if (CollUtil.isEmpty(parkList)) {
			log.info("园区列表为空");
			return;
		}
		List<Integer> parkIds = parkList.stream().map(SmtPark::getId).collect(Collectors.toList());
		// 水表集中器
		List<SmtWaterMeterConcentrator> waterMeterConcentratorList = waterMeterConcentratorMapper
				.selectList(Wrappers.<SmtWaterMeterConcentrator>lambdaQuery()
						.in(SmtWaterMeterConcentrator::getParkId, parkIds));
		log.info("水表集中器列表：{}", waterMeterConcentratorList);
		if (CollUtil.isNotEmpty(waterMeterConcentratorList)) {
			for (SmtWaterMeterConcentrator waterMeter : waterMeterConcentratorList) {
				Boolean isOnline = checkOnline(EventEnum.METER_CHECK_ONLINE.getCode(), waterMeter.getId(),
						waterMeter.getParkId(), waterMeter.getIp(), waterMeter.getPort());
				if (isOnline) {
					waterMeter.setIsOnline(NumberConstants.TWO);
				} else {
					waterMeter.setIsOnline(NumberConstants.ONE);
				}
				waterMeter.setUpdateUserId(0);
				waterMeter.setUpdateTime(LocalDateTime.now());
				waterMeterConcentratorMapper.updateById(waterMeter);
			}
		}
		// 外置阀门集中器
		List<SmtWaterValveConcentrator> valveConcentratorList = waterValveConcentratorMapper
				.selectList(Wrappers.<SmtWaterValveConcentrator>lambdaQuery()
						.in(SmtWaterValveConcentrator::getParkId, parkIds));
		log.info("外置阀门集中器列表：{}", valveConcentratorList);
		if (CollUtil.isNotEmpty(valveConcentratorList)) {
			for (SmtWaterValveConcentrator valveConcentrator : valveConcentratorList) {
				Boolean isOnline = checkOnline(EventEnum.VALVE_CHECK_ONLINE.getCode(), valveConcentrator.getId(),
						valveConcentrator.getParkId(), valveConcentrator.getIp(), valveConcentrator.getPort());
				if (isOnline) {
					valveConcentrator.setIsOnline(NumberConstants.TWO);
				} else {
					valveConcentrator.setIsOnline(NumberConstants.ONE);
				}
				valveConcentrator.setUpdateUserId(0);
				valveConcentrator.setUpdateTime(LocalDateTime.now());
				waterValveConcentratorMapper.updateById(valveConcentrator);
			}
		}
		// 电表集中器
		List<SmtEleMeterConcentrator> eleMeterConcentratorList = eleMeterConcentratorMapper
				.selectList(Wrappers.<SmtEleMeterConcentrator>lambdaQuery()
						.in(SmtEleMeterConcentrator::getParkId, parkIds));
		log.info("电表集中器列表：{}", eleMeterConcentratorList);
		if (CollUtil.isNotEmpty(eleMeterConcentratorList)) {
			for (SmtEleMeterConcentrator eleMeter : eleMeterConcentratorList) {
				Boolean isOnline = checkOnline(EventEnum.METER_CHECK_ONLINE.getCode(), eleMeter.getId(),
						eleMeter.getParkId(), eleMeter.getIp(), eleMeter.getPort());
				if (isOnline) {
					eleMeter.setIsOnline(NumberConstants.TWO);
				} else {
					eleMeter.setIsOnline(NumberConstants.ONE);
				}
				eleMeter.setUpdateUserId(0);
				eleMeter.setUpdateTime(LocalDateTime.now());
				eleMeterConcentratorMapper.updateById(eleMeter);
			}
		}
	}

	/**
	 * 检测水电表集中器(外置阀门)是否在线
	 * @param ip
	 * @param port
	 * @return
	 */
	private Boolean checkOnline(Integer eventType, Long deviceId, Integer parkId, String ip, String port) {
		try {
			// 调取添加设备接口
			DeviceDataDTO deviceInfo = getDeviceInfo(deviceId, ip, port);
			// 园区分发
			DispatcherDTO<DeviceDataDTO> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setEventType(eventType);
			dispatcherDTO.setParkId(parkId);
			dispatcherDTO.setDeviceId(deviceId.toString());
			dispatcherDTO.setData(deviceInfo);
			Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
			if(!result.isSuccess()){
				throw new SmartException("检测集中器(阀门)是否在线失败");
			}
			JSONObject object = JSONUtil.parseObj(result.getData());
			String code = "code";
			log.info("数据：{}，设备ID：{}", object, deviceId);
			if (!object.containsKey(code)) {
				throw new SmartException("数据格式不正确，检测集中器(阀门)是否在线失败");
			}
			return object.getBool("data");
		} catch (Exception e) {
			log.error("检测集中器(阀门){}是否在线失败", deviceId, e);
		}
		return Boolean.FALSE;
	}

	/**
	 * 添加设备接口属性处理
	 * @return 返回结果
	 */
	private DeviceDataDTO getDeviceInfo(Long deviceId, String ip, String port) {
		DeviceDataDTO deviceInfo = new DeviceDataDTO();
		deviceInfo.setDeviceCode(deviceId.toString());
		deviceInfo.setDeviceIp(ip);
		deviceInfo.setDevicePort(Integer.parseInt(port));
		return deviceInfo;
	}
}
