package com.tce.smart.platform.helper;

import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.DeviceDataDTO;
import com.tce.smart.platform.api.dto.EleIssueFileDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 水电表(阀门)组件
 *
 * @author Li.JiaJun
 * @since 2021/12/21 11:42
 */
@Slf4j
@Component
public class MeterHelper {

	@Resource
	private RemoteDispatcherService remoteDispatcherService;

	/**
	 * 检测水电表集中器(外置阀门)是否在线
	 *
	 * @param ip
	 * @param port
	 * @return
	 */
	public Boolean checkOnline(Integer eventType, Long deviceId, Integer parkId, String ip, String port) {
		// 调取添加设备接口
		DeviceDataDTO deviceInfo = getDeviceInfo(deviceId, ip, port);
		// 园区分发
		DispatcherDTO<DeviceDataDTO> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(eventType);
		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setDeviceId(deviceId.toString());
		dispatcherDTO.setData(deviceInfo);
		try {
			Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
					SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			log.info("水电表集中器(外置阀门)是否在线result: {}", result);
			if (!result.isSuccess()) {
				throw new SmartException("接口连接失败，集中器不在线或未连接");
			}
			JSONObject object = JSONUtil.parseObj(result.getData());
			String code = "code";
			if (!object.containsKey(code)) {
				throw new SmartException("数据格式不正确，集中器不在线或未连接");
			}
			return object.getBool("data");
		} catch (Exception e) {
			log.error("检测水电表(阀门)集中器是否在线失败", e);
		}
		return Boolean.FALSE;
	}

	/**
	 * 添加设备接口属性处理
	 *
	 * @return 返回结果
	 */
	private DeviceDataDTO getDeviceInfo(Long deviceId, String ip, String port) {
		DeviceDataDTO deviceInfo = new DeviceDataDTO();
		deviceInfo.setDeviceCode(deviceId.toString());
		deviceInfo.setDeviceIp(ip);
		deviceInfo.setDevicePort(Integer.parseInt(port));
		return deviceInfo;
	}

	/**
	 * 修改阀门状态：开阀，关阀
	 *
	 * @param eventType     事件类型
	 * @param deviceIp 		集中器IP
	 * @param port			集中器端口号
	 * @param deviceCode    水表集中器ID | 阀门集中器ID
	 * @param waterMeterSeq 水表序号
	 * @param seq           阀门序号
	 * @param status        阀门状态
	 * @param parkId        园区ID
	 * @return
	 */
	public Boolean changeValveStatus(Integer eventType, String deviceIp, String port, String deviceCode,
									 Integer waterMeterSeq, Integer seq, Integer status, Integer parkId) {
		if(EventEnum.WATER_METER_OUT_VALVE_REMOTE_CONTROL.getCode().equals(eventType)){
			log.info("水表阀门{}远程功能控制请求",deviceCode);
		} else if(EventEnum.WATER_METER_OUT_VALVE_CONTROL.getCode().equals(eventType)){
			log.info("水表阀门{}开关功能控制请求",deviceCode);
		}
		DeviceDataDTO dataDTO = new DeviceDataDTO();
		dataDTO.setDeviceIp(deviceIp);
		if (Objects.nonNull(port)) {
			dataDTO.setDevicePort(Integer.parseInt(port));
		}
		dataDTO.setDeviceCode(deviceCode);
		dataDTO.setWaterMeterSeq(waterMeterSeq);
		dataDTO.setValveSeq(seq);
		dataDTO.setValveOnOff(status);
		// 园区分发
		DispatcherDTO<DeviceDataDTO> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(eventType);
		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setDeviceId(deviceCode);
		dispatcherDTO.setData(dataDTO);
		try {
			Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
					SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);

			if (!result.isSuccess()) {
				log.info("水表阀门{}控制请求失败", deviceCode);
				return Boolean.FALSE;
			}
			JSONObject object = JSONUtil.parseObj(result.getData());
			String code = "code";
			log.info("水表阀门控制数据：{}", object);
			if (!object.containsKey(code)) {
				throw new SmartException("数据格式不正确，水表阀门控制请求失败");
			}
			return object.getBool("data");
		} catch (Exception e) {
			log.error("水表阀门控制请求失败", e);
			return Boolean.FALSE;
		}
	}

	/**
	 * 修改闸门状态：开闸，关闸
	 *
	 * @param eventType     事件类型
	 * @param deviceIp 		集中器IP
	 * @param port			集中器端口号
	 * @param deviceCode    电表集中器ID
	 * @param eleMeterSeq   电表序号
	 * @param eleMeterAddress 电表地址
	 * @param eleMeterPort  电表端口号
	 * @param status        阀门状态
	 * @param parkId        园区ID
	 * @return
	 */
	public Boolean changeBrakeStatus(Integer eventType, String deviceIp, String port, String deviceCode,
									 Integer eleMeterSeq, String eleMeterAddress, Integer eleMeterPort,
									 Integer status, Integer parkId) {
		DeviceDataDTO dataDTO = new DeviceDataDTO();
		dataDTO.setDeviceIp(deviceIp);
		if (Objects.nonNull(port)) {
			dataDTO.setDevicePort(Integer.parseInt(port));
		}
		dataDTO.setDeviceCode(deviceCode);
		dataDTO.setElectricMeterSeq(eleMeterSeq);
		dataDTO.setValveOnOff(status);
		dataDTO.setEleMeterAddress(eleMeterAddress);
		dataDTO.setEleMeterPort(eleMeterPort);
		// 园区分发
		DispatcherDTO<DeviceDataDTO> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(eventType);
		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setDeviceId(deviceCode);
		dispatcherDTO.setData(dataDTO);
		try {
			Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
					SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);

			if (!result.isSuccess()) {
				log.info("电表闸门{}控制请求失败", deviceCode);
				return Boolean.FALSE;
			}
			JSONObject object = JSONUtil.parseObj(result.getData());
			String code = "code";
			log.info("电表闸门控制数据：{}", object);
			if (!object.containsKey(code)) {
				throw new SmartException("数据格式不正确，电表闸门控制请求失败");
			}
			return object.getBool("data");
		} catch (Exception e) {
			log.error("电表闸门控制请求失败", e);
			return Boolean.FALSE;
		}
	}

	/**
	 * 电表集中器下载档案
	 *
	 * @param eventType
	 * @param deviceId
	 * @param address
	 * @param parkId
	 * @param meterNum
	 * @param meterJson
	 * @return
	 */
	public Boolean meterFile(Integer eventType, Long deviceId, String address, Integer parkId, String ip,
								String port, Integer meterNum, String meterJson) {
		EleIssueFileDTO dataDTO = new EleIssueFileDTO();
		dataDTO.setDeviceIp(ip);
		dataDTO.setDevicePort(Integer.parseInt(port));
		dataDTO.setMeterNum(meterNum);
		dataDTO.setConcentratorAddress(address);
		dataDTO.setMeterJson(meterJson);
		// 园区分发
		DispatcherDTO<EleIssueFileDTO> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(eventType);
		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setDeviceId(deviceId.toString());
		dispatcherDTO.setData(dataDTO);
		try {
			log.info("水电表集中器操作档案数据：{}", dispatcherDTO);
			Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
					SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);

			if (!result.isSuccess()) {
				log.info("水电表集中器{}操作档案请求请求失败", deviceId);
				return Boolean.FALSE;
			}
			JSONObject object = JSONUtil.parseObj(result.getData());
			String code = "code";
			log.info("水电表集中器操作档案数据：{}", object);
			if (!object.containsKey(code)) {
				throw new SmartException("数据格式不正确，水电表集中器操作档案请求失败");
			}
			return object.getBool("data");
		} catch (Exception e) {
			log.error("水电表集中器操作档案请求失败", e);
			return Boolean.FALSE;
		}
	}
}
