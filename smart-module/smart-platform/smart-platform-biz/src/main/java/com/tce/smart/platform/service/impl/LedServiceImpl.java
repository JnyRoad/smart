package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.Led;
import com.tce.smart.platform.api.dto.ParkingLotUpdateDTO;
import com.tce.smart.platform.api.dto.QueryLedDTO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.service.LedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 显示信息
 */
@Slf4j
@Service
public class LedServiceImpl implements LedService {

	@Autowired
	RemoteDispatcherService remoteDispatcherService;

	@Resource
	private SmtDeviceService smtDeviceService;

	@Override
	public Result<Led> set(Led led){
		if(CollUtil.isEmpty(led.getLedAreaList())){
			new Result<>(false,"区域信息不可为空");
		}
		if(null == led.getParkId()){
			//通过设备编号查询所属园区
			SmtDevice device = smtDeviceService.getById(led.getDeviceCode());
			led.setParkId(device.getParkId());
		}
		DispatcherDTO<Led> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(EventEnum.PARKING_LED_SET.getCode());
		dispatcherDTO.setDeviceId(led.getDeviceCode());
		dispatcherDTO.setParkId(led.getParkId());
		dispatcherDTO.setData(led);
		Result result = remoteDispatcherService.dispatch(dispatcherDTO,SecurityConstants.FROM_IN);
		return result;
	}

	@Override
	public Led get(QueryLedDTO queryLedDTO){
		DispatcherDTO<QueryLedDTO> dispatcherDTO = new DispatcherDTO<>();
		if(null == queryLedDTO.getParkId()){
			//通过设备编号查询所属园区
			SmtDevice device = smtDeviceService.getById(queryLedDTO.getDeviceCode());
			queryLedDTO.setParkId(device.getParkId());
		}
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(EventEnum.PARKING_LED_GET.getCode());
		dispatcherDTO.setDeviceId(queryLedDTO.getDeviceCode());
		dispatcherDTO.setParkId(queryLedDTO.getParkId());
		dispatcherDTO.setData(queryLedDTO);
		Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN);
		log.info("LetResult:{}",result);
		Led led = new Led();
		if(result.isSuccess() && ObjectUtil.isNotNull(result.getData())){
			//这里是两层data
			led = JSONUtil.toBean(JSONUtil.parseObj(result.getData()).getJSONObject("data"),Led.class);
		}
		if(null != led) {
			log.info("#####LetResult:{}", led);
			led.setDeviceCode(queryLedDTO.getDeviceCode());
			led.setDisplayScene(queryLedDTO.getDisplayScene());
		}
		return led;
	}
}
