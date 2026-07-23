package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.DeviceDataDTO;
import com.tce.smart.platform.api.dto.DeviceStatusDTO;
import com.tce.smart.platform.api.dto.resp.device.DeviceFTDTO;
import com.tce.smart.platform.core.dto.DeviceDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.enums.DeviceSyncEnum;
import com.tce.smart.platform.core.service.SmtDevicePersonService;
import com.tce.smart.platform.core.service.SmtDeviceService;
import com.tce.smart.platform.core.service.SmtDeviceVehicleService;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.DeviceConstants;
import com.tce.smart.tool.enums.DeviceTypeEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

//import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
//import com.tce.smart.dispatcher.api.enums.EventEnum;
//import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;

/**
 * 设备信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:09:27
 */
@Service
@Slf4j
@AllArgsConstructor
public class DeviceServiceImpl implements IDeviceService, InitializingBean {

	private final SmtDeviceService smtDeviceService;

	private final SmtDeviceAreaService smtDeviceAreaService;

	private final SmtDeviceVehicleService smtDeviceVehicleService;

	private final SmtDevicePersonService smtDevicePersonService;

	private final SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;

//    private final RemoteDeviceService remoteDeviceService;

	private final RemoteDispatcherService remoteDispatcherService;

	private final SmtDeviceTagRelationService deviceTagRelationService;

	private final SmtParkService smtParkService;


	/**
	 * 添加设备信息
	 *
	 * @param entity 设备信息
	 * @return 返回结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean saveDevice(DeviceDTO entity) {
		String id = IdUtil.simpleUUID();
		entity.setId(id);
		entity.setCreateTime(LocalDateTime.now());
		if (ArrayUtil.isNotEmpty(entity.getAreaIds()) && entity.getAreaIds().length == 3) {
			entity.setAreaId(entity.getAreaIds()[entity.getAreaIds().length - 1]);
			entity.setParkId(entity.getAreaIds()[0]);
		}
		//调取添加设备接口
		DeviceDataDTO deviceInfo = getDeviceInfo(entity);
		//Result result = remoteDeviceService.add(deviceInfo, SecurityConstants.FROM_IN);

		// 园区分发
		DispatcherDTO<DeviceDataDTO> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(EventEnum.DEVICE_ADD.getCode());
		dispatcherDTO.setParkId(entity.getParkId());
		dispatcherDTO.setDeviceId(entity.getId());
		dispatcherDTO.setData(deviceInfo);
		Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);

		//log.info("设备添加 result:{}", result);
		entity.setConnectStatus(DeviceConstants.UNCONNECTED);
		if(result.isSuccess()){
			JSONObject object = JSONUtil.parseObj(result.getData());
			if(object.getInt("code").intValue() == DeviceConstants.SUCCESS){
				entity.setConnectStatus(DeviceConstants.ON_LINE);
			}
		}
		// 默认启用状态
		entity.setEnableStatus(DeviceConstants.ENABLE);
		SmtDevice device = new SmtDevice();
		BeanUtils.copyProperties(entity, device);
		boolean backResult = smtDeviceService.save(device);

		if (backResult) {
			if (entity.getAreaId() != null) {
				//绑定区域信息
				SmtDeviceArea smtDeviceArea = new SmtDeviceArea();
				smtDeviceArea.setAreaId(entity.getAreaId());
				smtDeviceArea.setDeviceId(id);
				smtDeviceArea.setCreateTime(LocalDateTime.now());
				backResult = smtDeviceAreaService.save(smtDeviceArea);
			}
			if (ArrayUtil.isNotEmpty(entity.getTagIds())) {
				List<SmtDeviceTagRelation> deviceTagRelList = new ArrayList<>(entity.getTagIds().length);
				for (Long tagId : entity.getTagIds()) {
					SmtDeviceTagRelation relation = new SmtDeviceTagRelation();
					relation.setDeviceId(id);
					relation.setTagId(tagId);
					deviceTagRelList.add(relation);
				}
				deviceTagRelationService.saveBatch(deviceTagRelList);
			}
		}
		return backResult;
	}

	/**
	 * 添加设备接口属性处理
	 *
	 * @param entity 设备信息
	 * @return 返回结果
	 */
	private DeviceDataDTO getDeviceInfo(DeviceDTO entity) {
		DeviceDataDTO deviceInfo = new DeviceDataDTO();
		BeanUtils.copyProperties(entity, deviceInfo);
		deviceInfo.setDeviceCode(entity.getId());
		deviceInfo.setAreaId(entity.getAreaId() + "");
		deviceInfo.setLedScreenPort(entity.getLedScreen());
		deviceInfo.setEntryExitType(entity.getEventType());
		deviceInfo.setDeviceType(entity.getDeviceType());
//        设备类型：1-门禁；2-闸机；3-道闸；4-摄像头
//        * 1:闸机；2：道闸；3：摄像头；4：出入口抓拍机
		// 这块需要重新设计，暂时这样转换处理
//        if(entity.getDeviceType().equals(3)) {
//        	deviceInfo.setDeviceType(4);
//        }else if(entity.getDeviceType().equals(2)) {
//        	deviceInfo.setDeviceType(1);
//        }else if(entity.getDeviceType().equals(4)) {
//        	deviceInfo.setDeviceType(3);
//        }else {
//        	deviceInfo.setDeviceType(2);
//        }
		if(entity.getDeviceType().equals(2)) {
	deviceInfo.setDeviceType(1);
        }
		return deviceInfo;

	}

	/**
	 * 删除设备接口属性处理
	 *
	 * @param deviceCode 设备编码
	 * @return 返回结果
	 */
	private DeviceDataDTO getDeviceInfo(String  deviceCode) {
		DeviceDataDTO deviceInfo = new DeviceDataDTO();
		deviceInfo.setDeviceCode(deviceCode);
		return deviceInfo;

	}

	/**
	 * 更新设备信息
	 *
	 * @param entity 设备信息
	 * @return 返回结果
	 */
	@Transactional
	@Override
	public Result updateDevice(DeviceDTO entity) {
		if (StrUtil.isBlank(entity.getId())) {
			return new Result<>(false, "设备ID不可为空");
		}
		entity.setUpdateTime(LocalDateTime.now());
		//调取删除设备接口
		SmtDevice oldDevice = smtDeviceService.getById(entity.getId());
		// ISC平台的设备只更新基础信息
		if (DeviceSyncEnum.YES.getCode().equals(oldDevice.getIsSync())) {
			if (ArrayUtil.isNotEmpty(entity.getAreaIds()) && entity.getAreaIds().length == 3) {
				entity.setAreaId(entity.getAreaIds()[entity.getAreaIds().length - 1]);
				entity.setParkId(entity.getAreaIds()[0]);
			}
			SmtDevice device = new SmtDevice();
			BeanUtils.copyProperties(entity, device);
			boolean backResult = smtDeviceService.updateById(device);
			if (backResult) {
				updateDeviceOtherInfo(entity);
			}
			return new Result<>(backResult);
		}
		if (!(oldDevice.getConnectStatus().equals(DeviceConstants.UNCONNECTED))) {
			// 园区分发
			DispatcherDTO<DeviceDataDTO> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setEventType(EventEnum.DEVICE_DELETE.getCode());
			dispatcherDTO.setParkId(entity.getParkId());
			dispatcherDTO.setDeviceId(entity.getId());
			dispatcherDTO.setData(getDeviceInfo(entity.getId()));
			Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
					SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			//log.info("设备删除 result:{}" + result);

			//注意 当相应为 (code=0, msg=success, data={"code":407,"message":"设备不存在"} 也应当作为删除成功处理
			if (!result.getCode().equals(DeviceConstants.SUCCESS)) {
				JSONObject jsonObject = JSONUtil.parseObj(result.getData());
				if(!(jsonObject.containsKey("code") && jsonObject.getInt("code") == 407)){
					return new Result<>(false, "设备更新失败");
				}
			}
		}
		//添加设备
		DeviceDataDTO deviceInfo = getDeviceInfo(entity);
		DispatcherDTO<DeviceDataDTO> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(EventEnum.DEVICE_ADD.getCode());
		dispatcherDTO.setParkId(entity.getParkId());
		dispatcherDTO.setDeviceId(entity.getId());
		dispatcherDTO.setData(deviceInfo);
		Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		log.info("设备添加成功 dispatcherDTO.getDeviceId():{}" + dispatcherDTO.getDeviceId());
		entity.setConnectStatus(DeviceConstants.UNCONNECTED);
		if(result.isSuccess()){
			JSONObject object = JSONUtil.parseObj(result.data());
			//C++端可能响应 {"code":406,"message":"设备已存在"}
			if(object.getInt("code").intValue() == DeviceConstants.SUCCESS){
				entity.setConnectStatus(DeviceConstants.ON_LINE);
			}
		}

		if (ArrayUtil.isNotEmpty(entity.getAreaIds()) && entity.getAreaIds().length == 3) {
			entity.setAreaId(entity.getAreaIds()[entity.getAreaIds().length - 1]);
			entity.setParkId(entity.getAreaIds()[0]);
		}
		SmtDevice device = new SmtDevice();
		BeanUtils.copyProperties(entity, device);
		boolean backResult = smtDeviceService.updateById(device);
		if (backResult) {
			updateDeviceOtherInfo(entity);
		}
		return new Result<>(backResult);
	}

	private void updateDeviceOtherInfo(DeviceDTO entity) {
		//删除设备区域记录
		smtDeviceAreaService.remove(Wrappers.<SmtDeviceArea>query().lambda().eq(SmtDeviceArea::getDeviceId, entity.getId()));
		if (entity.getAreaId() != null) {
			//绑定区域信息
			SmtDeviceArea smtDeviceArea = new SmtDeviceArea();
			smtDeviceArea.setAreaId(entity.getAreaId());
			smtDeviceArea.setDeviceId(entity.getId());
			smtDeviceArea.setCreateTime(LocalDateTime.now());
			smtDeviceAreaService.save(smtDeviceArea);
		}
		deviceTagRelationService.remove(Wrappers.<SmtDeviceTagRelation>lambdaQuery().eq(SmtDeviceTagRelation::getDeviceId, entity.getId()));
		if (ArrayUtil.isNotEmpty(entity.getTagIds())) {
			List<SmtDeviceTagRelation> deviceTagRelList = new ArrayList<>(entity.getTagIds().length);
			for (Long tagId : entity.getTagIds()) {
				SmtDeviceTagRelation relation = new SmtDeviceTagRelation();
				relation.setDeviceId(entity.getId());
				relation.setTagId(tagId);
				deviceTagRelList.add(relation);
			}
			deviceTagRelationService.saveBatch(deviceTagRelList);
		}
	}

	@Override
	public Result removeDevice(String id) {
		if (StrUtil.isBlank(id)) {
			return new Result<>(false, "设备ID不可为空");
		}
		//调取删除设备接口
		//Result result = remoteDeviceService.delete(id, SecurityConstants.FROM_IN);
		// 园区分发
		SmtDevice smtDevice = smtDeviceService.getById(id);
		DispatcherDTO<DeviceDataDTO> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(EventEnum.DEVICE_DELETE.getCode());
		dispatcherDTO.setParkId(smtDevice.getParkId());
		dispatcherDTO.setDeviceId(id);
		dispatcherDTO.setData(getDeviceInfo(id));
		Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (result.getCode() != DeviceConstants.SUCCESS) {
			return new Result<Boolean>(false, "设备更新失败");
		}
		smtDeviceAuthorityRelationService.remove(Wrappers.<SmtDeviceAuthorityRelation>query().lambda().eq(SmtDeviceAuthorityRelation::getDeviceId, id));
		smtDeviceVehicleService.remove(Wrappers.<SmtDeviceVehicle>query().lambda().eq(SmtDeviceVehicle::getDeviceId, id));
		smtDevicePersonService.remove(Wrappers.<SmtDevicePerson>query().lambda().eq(SmtDevicePerson::getDeviceId, id));
		return new Result<>(smtDeviceService.removeById(id));
	}

	public void updateDeviceStatus(DeviceStatusDTO.DeviceStatus deviceStatus) {
		SmtDevice entity = new SmtDevice();
		entity.setId(deviceStatus.getDeviceCode());
		entity.setConnectStatus(deviceStatus.getDeviceStatus());
		smtDeviceService.updateById(entity);
	}

	@Override
	public void initDeviceStatus() {
		List<SmtDevice> parkIds = smtDeviceService.list(Wrappers.<SmtDevice>query().lambda()
				.select(SmtDevice::getParkId)
				.groupBy(SmtDevice::getParkId));

		if (CollUtil.isNotEmpty(parkIds)) {
			parkIds.forEach(d -> {
				try {
					List<String> deviceList = smtDeviceService.getDeviceIds(d.getParkId());
					if (CollUtil.isNotEmpty(deviceList)) {
						this.initDeviceStatusHandle(d.getParkId(), deviceList);
					}
				}catch (Exception e){}
			});
		}
	}

	@Override
	public List<DeviceFTDTO> getDeviceFaceTemperature(Integer parkId) {
		List<Integer> parkList = SecurityUtils.getUser().getParkIdList();
		//查询所有闸机设备体温检测配置
		List<SmtDevice> devices = smtDeviceService.list(new LambdaQueryWrapper<SmtDevice>()
				.eq(SmtDevice::getParkId, parkId)
				.eq(SmtDevice::getDeviceType, DeviceTypeEnum.DEVICE_TYPE_1.getCode())
				.in(SmtDevice::getParkId,parkList)
		);
		List<DeviceFTDTO> dtoList = new ArrayList<>();
		if(CollectionUtils.isNotEmpty(devices)) {
			//查询园区记录
			List<Integer> parkIds = devices.stream().map(SmtDevice::getParkId).collect(Collectors.toList());
			Collection<SmtPark> smtParks = smtParkService.listByIds(parkIds);
			Map<Integer, List<SmtPark>> parkMap = smtParks.stream().collect(Collectors.groupingBy(SmtPark::getId));
			devices.forEach(item -> {
				DeviceFTDTO respDTO = new DeviceFTDTO();
				BeanUtils.copyProperties(item,respDTO);
				respDTO.setParkName(parkMap.get(item.getParkId()).get(0).getParkName());
				respDTO.setDeviceId(item.getId());
				respDTO.setThermalThreshold(String.valueOf(item.getThermalThreshold()));
				dtoList.add(respDTO);
			});
		}
		return dtoList;
	}

	@Override
	public Boolean saveDeviceFaceTemperature(List<DeviceFTDTO> deviceFTDTOs) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		deviceFTDTOs.forEach(item -> {
			if(!parkIds.contains(item.getParkId())){
				//是否为关联园区
				return;
			}
			SmtDevice device = new SmtDevice();
			device.setId(item.getDeviceId());
			device.setParkId(item.getParkId());
			device.setThermalEnable(item.getThermalEnable());
			try {
				device.setThermalThreshold(new BigDecimal(item.getThermalThreshold()).doubleValue());
			}catch (Exception e){}
			device.setUpdateTime(LocalDateTime.now());
			saveDeviceFT(device);
		});

		return true;
	}

	@Transactional(rollbackFor = Exception.class)
	public void saveDeviceFT(SmtDevice device){
		//更新设备设置
		boolean update = smtDeviceService.updateById(device);
		if(update){
			//访问设备
			Map<String, Object> param = new HashMap<>();
			param.put("deviceCode",device.getId());
			param.put("thermalEnable",device.getThermalEnable());
			param.put("thermalThreshold",device.getThermalThreshold());
			DispatcherDTO<Map<String, Object>> dispatcherDTO = new DispatcherDTO<>();
			dispatcherDTO.setEventId(IdUtil.simpleUUID());
			dispatcherDTO.setEventType(EventEnum.FACE_IMAGE_THERMAL.getCode());
			dispatcherDTO.setParkId(device.getParkId());
			dispatcherDTO.setData(param);
			Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
					SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
			if(!result.isSuccess()){
				//回滚
				log.error("设置体温检测异常:({})",result.getData());
				throw new TCEException("设置体温检测异常");
			}
		}
	}

	private void initDeviceStatusHandle(Integer parkId, List<String> deviceList) {
		//DeviceStatusDTO deviceStatusDTO = remoteDeviceService.queryStatus(deviceList, SecurityConstants.FROM_IN);
		// 园区分发
		Map<String, Object> queryDeviceStatusReq = new HashMap<>();
		queryDeviceStatusReq.put("deviceList", deviceList);

		DeviceStatusDTO deviceStatusDTO = DeviceStatusDTO.DeviceStatusDTOBuilder.builder().build();
		DispatcherDTO<Map<String, Object>> dispatcherDTO = new DispatcherDTO<>();
		dispatcherDTO.setEventId(IdUtil.simpleUUID());
		dispatcherDTO.setEventType(EventEnum.DEVICE_STATUS_LIST_SEARCH.getCode());
		dispatcherDTO.setParkId(parkId);
		dispatcherDTO.setData(queryDeviceStatusReq);
		Result result = remoteDispatcherService.dispatch(dispatcherDTO, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		if (result.isSuccess()) {
			//log.info("业务分发接口返回:{}",result);
			JSONArray JsonArray = JSONUtil.parseObj(result.data()).getJSONArray("data");
			if (Objects.nonNull(JsonArray)) {
				List<DeviceStatusDTO.DeviceStatus> deviceStatusList = dataObject2DeviceStatus(JsonArray);
				deviceStatusDTO.setDeviceStatusList(deviceStatusList);
			}
			//log.info("园区{} 初始化设备状态结果：{}", parkId, JSONUtil.toJsonStr(deviceStatusDTO));
			if (ObjectUtil.isNotNull(deviceStatusDTO)) {
				List<DeviceStatusDTO.DeviceStatus> deviceStatusList = deviceStatusDTO.getDeviceStatusList();
				List<SmtDevice> smtDevices = new ArrayList<>();
				deviceStatusList.forEach(deviceStatus -> {
					SmtDevice entity = new SmtDevice();
					entity.setId(deviceStatus.getDeviceCode());
					entity.setConnectStatus(deviceStatus.getDeviceStatus());
					smtDevices.add(entity);

				});
				smtDeviceService.updateBatchById(smtDevices);
			}
		}
	}

	/**
	 * 将dataObject转成DeviceStatus
	 *
	 * @param JsonArray
	 * @return
	 */
	private List<DeviceStatusDTO.DeviceStatus> dataObject2DeviceStatus(JSONArray JsonArray) {
		List<DeviceStatusDTO.DeviceStatus> listDeviceStatusInfo = new ArrayList<>();
		if (null != JsonArray && JsonArray.size() > 0) {
			JSONObject jsonObject;
			DeviceStatusDTO.DeviceStatus deviceStatus;
			for (Object element : JsonArray) {
				deviceStatus = new DeviceStatusDTO.DeviceStatus();
				jsonObject = JSONUtil.parseObj(element.toString());
				deviceStatus.setDeviceCode(jsonObject.get("deviceCode").toString());
				deviceStatus.setDeviceStatus(Integer.valueOf(jsonObject.get("deviceStatus").toString()));
				listDeviceStatusInfo.add(deviceStatus);
			}
		}
		return listDeviceStatusInfo;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		/**
		 * platform启动后查询设备状态的流程先停止了 改为定时任务  因为
		 * platform启动时自动去查询更新设备状态的流程
		 * platform服务启动完成-》调用dispatcher服务-》调用platform服务
		 * dispatcher服务报错了 找不到platform服务实例
		 */
//		log.info("设备状态初始化执行");
//		try {
//			this.initDeviceStatus();
//		}catch (Exception e){
//			log.error(e.getMessage(), e);
//		}
	}
}
