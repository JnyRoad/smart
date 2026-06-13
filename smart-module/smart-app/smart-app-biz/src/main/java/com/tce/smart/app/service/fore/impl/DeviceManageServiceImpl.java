package com.tce.smart.app.service.fore.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.app.ao.fore.DeviceRegisterAo;
import com.tce.smart.app.api.entity.AppUserDevice;
import com.tce.smart.app.mapper.fore.DeviceManageMapper;
import com.tce.smart.app.service.fore.DeviceManageService;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.enums.UserDeviceBindFlagEnum;
import com.tce.smart.tool.exception.TCEException;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fushiping
 * @date 2019/7/3 18:13
 **/

@Service
@Slf4j
public class DeviceManageServiceImpl implements DeviceManageService {

	@Autowired
	DeviceManageMapper mapper;

	@Override
	public Boolean handleBaseinfo(DeviceRegisterAo deviceRegisterAo) {
		checkAo(deviceRegisterAo);
		findUser(deviceRegisterAo);
		return true;
	}

	@Override
	public List<AppUserDevice> getDeviceByBadge(String badge) {
		if (StringUtil.isNullOrEmpty(badge)) {
			return null;
		}

		List<AppUserDevice> appUserDeviceList = null;
		try {
			QueryWrapper<AppUserDevice> queryWrapper = new QueryWrapper<>();
			queryWrapper.lambda().eq(AppUserDevice::getBadge, badge);
			appUserDeviceList = mapper.selectList(queryWrapper);
		} catch (Exception e) {
			log.error("查询用户设备异常", e);
		}

		return appUserDeviceList;
	}

	public void findUser(DeviceRegisterAo deviceRegisterAo) {
		// 获得用户名
		String badge = SecurityUtils.getUser().getUsername();
		// 根据用户名和设备编号获得人员设备记录
		AppUserDevice appUserDevice = mapper.selectOne(Wrappers.<AppUserDevice>query().lambda()
				.eq(AppUserDevice::getDeviceNo, deviceRegisterAo.getDeviceNo()).eq(AppUserDevice::getBadge, badge));
		if (ObjectUtils.isNotNull(appUserDevice)) {
			if (!appUserDevice.getDevicePushId().equals(deviceRegisterAo.getDevicePushId())) {
				BeanUtils.copyProperties(deviceRegisterAo, appUserDevice);
				updateDevice(appUserDevice);
            }
		} else {
			AppUserDevice insertDevice = new AppUserDevice();
			BeanUtils.copyProperties(deviceRegisterAo, insertDevice);
			insertDevice.setBadge(badge);
			insertDevice(insertDevice);
		}
	}

	public void updateDevice(AppUserDevice appUserDevice) {
		appUserDevice.setUpdateTime(LocalDateTime.now());
		mapper.updateById(appUserDevice);
	}

	public Integer insertDevice(AppUserDevice appUserDevice) {
		appUserDevice.setCreateTime(LocalDateTime.now());
		appUserDevice.setUpdateTime(LocalDateTime.now());
		appUserDevice.insert();
		return appUserDevice.getId();
	}

	public void checkAo(DeviceRegisterAo deviceRegisterAo) {
		if (StringUtil.isNullOrEmpty(deviceRegisterAo.getDeviceNo())) {
			throw new TCEException(ExceptionTypeEnum.DIVICE_NO_ERROR);
		}
		if (StringUtil.isNullOrEmpty(deviceRegisterAo.getDevicePushId())) {
			throw new TCEException(ExceptionTypeEnum.DIVICE_PUSH_ID_ERROR);
		}
		if (ObjectUtils.isNull(deviceRegisterAo.getOsType())) {
			throw new TCEException(ExceptionTypeEnum.DIVICE_OS_TYPE_ERROR);
		}
	}

	@Override
	public Boolean bindDevice(String badge, String deviceNo) {
		Boolean isSuccess = false;
		AppUserDevice appUserDevice = null;
		try {
			appUserDevice = mapper.selectOne(Wrappers.<AppUserDevice>query().lambda()
					.eq(AppUserDevice::getDeviceNo, deviceNo).eq(AppUserDevice::getBadge, badge));
		} catch (Exception e) {
			log.error("获取用户设备信息异常", e);
//			throw new TCEException("获取用户设备信息异常");
		}

		//更新设备绑定标识
		if (ObjectUtils.isNotNull(appUserDevice)) {
			AppUserDevice updatePo = new AppUserDevice();
			updatePo.setId(appUserDevice.getId());
			updatePo.setBindFlag(UserDeviceBindFlagEnum.BIND.getCode());
			updatePo.setUpdateTime(LocalDateTime.now());

			try {
				this.mapper.updateById(updatePo);
				isSuccess = true;
			} catch (Exception e) {
				log.error("更新用户设备信息异常", e);
//				throw new TCEException("获取用户设备信息异常");
			}
		}

		return isSuccess;
	}

	@Override
	public List<AppUserDevice> queryBindDevice(String badge) {
		List<AppUserDevice> userDeviceList = null;
		try {
			userDeviceList = mapper.selectList(Wrappers.<AppUserDevice>query().lambda()
					.eq(AppUserDevice::getBindFlag, UserDeviceBindFlagEnum.BIND.getCode())
					.eq(AppUserDevice::getBadge, badge));
		} catch (Exception e) {
			log.error("获取用户绑定设备信息异常", e);
			throw new TCEException("获取用户绑定设备信息异常");
		}

		return userDeviceList;
	}

	@Override
	public List<AppUserDevice> queryByDeviceNo(String deviceNo) {
		QueryWrapper<AppUserDevice> bindWrapper = new QueryWrapper<>();
		bindWrapper.lambda()
				.eq(AppUserDevice::getDeviceNo, deviceNo)
				.orderByDesc(AppUserDevice::getBindFlag)//优先排已绑定
				//.orderByDesc(AppUserDevice::getCreateTime)//优先排序创建时间
				.orderByDesc(AppUserDevice::getUpdateTime);

		try {
			return mapper.selectList(bindWrapper);
		} catch (Exception e) {
			log.error("获取用户绑定设备信息异常", e);
			throw new TCEException("获取用户绑定设备信息异常");
		}
	}
}
