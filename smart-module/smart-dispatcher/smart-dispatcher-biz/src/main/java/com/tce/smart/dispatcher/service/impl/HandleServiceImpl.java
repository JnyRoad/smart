package com.tce.smart.dispatcher.service.impl;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.resp.BridgeDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.service.HandleService;
import com.tce.smart.platform.api.dto.BridgeListenerDTO;
import com.tce.smart.platform.api.feign.RemoteIntergrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Description: TODO
 * @ProjectName smart-dispatcher
 * @ClassName: HandleServiceImpl
 * @Author jinbo
 * @Date 2019/11/7
 */
@Slf4j
@Service
public class HandleServiceImpl implements HandleService {
	@Autowired
	private RemoteIntergrationService remoteIntergrationService;

	@Override
	public boolean handle(BridgeDTO<String> bridgeDTO) {
		try {
			Integer eventType = bridgeDTO.getEventType();
			EventEnum event = EventEnum.eventEnum(eventType);
			log.info("处理园区响应事件：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
			switch (event) {
				case DEVICE_ADD_CARD:
					return replyOfAddCard(bridgeDTO);
				case DEVICE_DELETE_CARD:
					return replyOfDeleteCard(bridgeDTO);
				case DEVICE_UPDATE_CARD:
					return replyOfUpdateCard(bridgeDTO);
				case PERSON_ACCESS_RECORD:
					return replyOfAccess(bridgeDTO);
				case DEVICE_STATUS_CHANGE:
					return changeOfDevice(bridgeDTO);
				case PARKING_ENTRANCE_RECORD:
					return replyOfGate(bridgeDTO);
				case CROSS_BORDER_RECORD:
					return replyOfCrossBorder(bridgeDTO);
				case CAMERA_FACE_SNAP_RECORD:
					return replyOfCamera(bridgeDTO);
				case PIDC_ID_INFO_NTY:
					return replyOfTerminal(bridgeDTO);
				case WATER_REPEATER_READ:
					return replyOfWaterMeterReading(bridgeDTO);
				case WATER_REPEATER_IN_VALVE_STATE:
					return replyOfInValveUpdate(bridgeDTO);
				case WATER_REPEATER_OUT_VALVE_STATE:
					return replyOfOutValveUpdate(bridgeDTO);
				case ELE_REPEATER_READ:
					return replyOfEleMeterReading(bridgeDTO);
				case ELE_REPEATER_BRAKE_STATE:
					return replyOfEleBrakeUpdate(bridgeDTO);
				default:
					throw new TCEException("未定义的事件类型：" + eventType + "，ID：" + bridgeDTO.getEventId());
			}
		} catch (Exception e) {
			log.error("处理异常：{}", e.getMessage(), e);
		}
		return false;
	}

	/**
	 * 水表读数 响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean replyOfWaterMeterReading(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.replyOfWaterMeterReading(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	/**
	 * 电表读数 响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean replyOfEleMeterReading(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.replyOfEleMeterReading(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	/**
	 * 控制水表内置阀门 响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean replyOfInValveUpdate(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.replyOfInValveUpdate(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	/**
	 * 控制水表外置阀门 响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean replyOfOutValveUpdate(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.replyOfOutValveUpdate(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	/**
	 * 控制电表闸门	响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean replyOfEleBrakeUpdate(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.replyOfEleBrakeUpdate(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	/**
	 * 添加闸机/门禁卡片 响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean replyOfAddCard(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.replyOfAddCard(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	/**
	 * 删除闸机/门禁卡片 响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean replyOfDeleteCard(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.replyOfDeleteCard(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	/**
	 * 更新闸机/门禁卡片 响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean replyOfUpdateCard(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.replyOfUpdateCard(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	/**
	 * 人员通行记录通知 响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean replyOfAccess(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.replyOfAccess(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	/**
	 * 接收设备状态变更通知 响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean changeOfDevice(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.changeOfDevice(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	/**
	 * 接收车辆通行记录通知 响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean replyOfGate(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.replyOfGate(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	/**
	 * 接收越界报警 响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean replyOfCrossBorder(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.replyOfCrossBorder(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	/**
	 * 接收人证比对结果 响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean replyOfTerminal(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.replyOfTerminal(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	/**
	 * 接收人脸抓拍结果 响应结果
	 *
	 * @param bridgeDTO
	 * @return
	 */
	private boolean replyOfCamera(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = convert(bridgeDTO);
		Result<Boolean> result = remoteIntergrationService.replyOfCamera(bridgeListenerDTO, SecurityConstants.FROM_IN);
		boolean process = false;
		if (result.isSuccess()) {
			process = result.getData();
		}
		if (!process) {
			log.warn("处理园区响应事件失败：ParkId={}，EventId={}，EventType={}", bridgeDTO.getParkId(), bridgeDTO.getEventId(), bridgeDTO.getEventType());
		}
		return process;
	}

	private BridgeListenerDTO convert(BridgeDTO<String> bridgeDTO) {
		BridgeListenerDTO bridgeListenerDTO = new BridgeListenerDTO();
		bridgeListenerDTO.setContent(bridgeDTO.getData());
		return bridgeListenerDTO;
	}
}
