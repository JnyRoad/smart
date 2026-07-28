package com.tce.smart.dispatcher.service.impl;

import cn.hutool.json.JSONUtil;
import com.tce.smart.bridge.api.dto.req.BridgeDTO;
import com.tce.smart.bridge.api.dto.req.ImageDTO;
import com.tce.smart.bridge.api.feign.RemoteBridgeService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.service.DispatcherService;
import com.tce.smart.dispatcher.service.RemoteFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-dispatcher
 * @ClassName: DispatcherServiceImpl
 * @Author jinbo
 * @Date 2019/11/6
 */
@Slf4j
@Service
public class DispatcherServiceImpl implements DispatcherService {
	@Autowired
	private RemoteFeignService remoteFeignService;
	/**
	 * 1、获取 Bridge
	 * 2、转发请求
	 *
	 * @param dispatcherDTO
	 * @return
	 */
	@Override
	public <T> String dispatch(DispatcherDTO<T> dispatcherDTO) {
		log.info("向园区转发请求，parkId={}，eventType={}，eventId={}，deviceId={}", dispatcherDTO.getParkId(), dispatcherDTO.getEventType(), dispatcherDTO.getEventId(), dispatcherDTO.getDeviceId());
//		EventEnum eventEnum = EventEnum.eventEnum(dispatcherDTO.getEventType());
//		if (EventEnum.isISC(eventEnum)) {
//			String result = hikvisionService.dispatch(convert(dispatcherDTO));
//			log.info("ISC平台响应数据：{}", JSONUtil.toJsonStr(result));
//			return result;
//		}
		RemoteBridgeService remoteBridgeService = remoteFeignService.getBridge(dispatcherDTO.getParkId());
		if (Objects.isNull(remoteBridgeService)) {
			// 必须抛异常：错误文案作为正常数据返回会被上游当成功解析，造成任务卡死
			log.error("未找到园区对应的Bridge服务, ParkId：{}", dispatcherDTO.getParkId());
			throw new TCEException("未找到园区对应的Bridge服务, ParkId：" + dispatcherDTO.getParkId());
		}
		Result<String> result = remoteBridgeService.dispatch(convert(dispatcherDTO), SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		log.info("向ParkID为{}的bridge服务转发请求，响应为：{}",dispatcherDTO.getParkId(), JSONUtil.toJsonStr(result));
		if (result == null) {
			throw new TCEException("Bridge服务响应为空");
		}
		if (!Boolean.TRUE.equals(result.isSuccess())) {
			throw new TCEException(result.getCode(), result.getMessage());
		}
		return result.data();
	}

	/**
	 * 获取园区图片
	 *
	 * @param parkId
	 * @param id
	 * @return
	 */
	@Override
	public String getImage(Integer parkId, String id) {
		log.info("查询园区图片，parkId={}，imageId={}", parkId, id);
		RemoteBridgeService remoteBridgeService = remoteFeignService.getBridge(parkId);
		if (Objects.isNull(remoteBridgeService)) {
			log.error("未找到园区对应的Bridge服务, ParkId：{}", parkId);
			throw new TCEException("未找到园区对应的Bridge服务, ParkId：" + parkId);
		}
		ImageDTO imageDTO = new ImageDTO();
		imageDTO.setId(id);
		Result<String> result = remoteBridgeService.getImage(imageDTO, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		return result.get();
	}

	@Override
	public String getThumbnail(Integer parkId, String id) {
		log.info("查询园区图片（缩略图），parkId={}，thumbnailId={}", parkId, id);
		RemoteBridgeService remoteBridgeService = remoteFeignService.getBridge(parkId);
		if (Objects.isNull(remoteBridgeService)) {
			log.error("未找到园区对应的Bridge服务, ParkId：{}", parkId);
			throw new TCEException("未找到园区对应的Bridge服务, ParkId：" + parkId);
		}
		ImageDTO imageDTO = new ImageDTO();
		imageDTO.setId(id);
		Result<String> result = remoteBridgeService.getThumbnail(imageDTO, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		return result.get();
	}
//
//	@Override
//	public String saveImage(Integer parkId, String base64Image) {
//		RemoteBridgeService remoteBridgeService = remoteFeignService.getBridge(parkId);
//		if (Objects.isNull(remoteBridgeService)) {
//			log.error("未找到园区对应的Bridge服务, ParkId：{}", parkId);
//		}
//		ImageDTO imageDTO = new ImageDTO();
//		imageDTO.setBase64Image(base64Image);
//		Result<String> result = remoteBridgeService.saveImage(imageDTO, SecurityConstants.FROM_IN);
//		return result.data();
//	}
//
//	@Override
//	public String saveThumbnail(Integer parkId, String base64Image) {
//		RemoteBridgeService remoteBridgeService = remoteFeignService.getBridge(parkId);
//		if (Objects.isNull(remoteBridgeService)) {
//			log.error("未找到园区对应的Bridge服务, ParkId：{}", parkId);
//		}
//		ImageDTO imageDTO = new ImageDTO();
//		imageDTO.setBase64Image(base64Image);
//		Result<String> result = remoteBridgeService.saveThumbnail(imageDTO, SecurityConstants.FROM_IN);
//		return result.data();
//	}

	private <T> BridgeDTO<T> convert(DispatcherDTO<T> dispatcherDTO) {
		BridgeDTO<T> bridgeDTO = new BridgeDTO<>();
		bridgeDTO.setEventId(dispatcherDTO.getEventId());
		bridgeDTO.setEventType(dispatcherDTO.getEventType());
		bridgeDTO.setParkId(dispatcherDTO.getParkId());
		bridgeDTO.setDeviceId(dispatcherDTO.getDeviceId());
		bridgeDTO.setData(dispatcherDTO.getData());
		return bridgeDTO;
	}
}
