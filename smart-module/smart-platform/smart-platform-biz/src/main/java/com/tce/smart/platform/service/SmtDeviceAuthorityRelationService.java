package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.model.DeviceTree;
import com.tce.smart.tool.enums.DeviceAuthorityEnum;

import java.util.List;

/**
 * 设备权限表
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:34
 */
public interface SmtDeviceAuthorityRelationService extends IService<SmtDeviceAuthorityRelation> {

	List<DeviceTree> getDevice(String areaId, List<Integer> types);

	List<DeviceTree> getPark(List<Integer> parkIds);

	List<DeviceTree> getArea(Integer parkId, Integer pId);

	List<String> getDeviceIds(Integer authorityId, List<Integer> parkIds);

	List<SmtDeviceAuthorityRelation> getRelation(DeviceAuthorityEnum deviceAuthorityEnum, Integer parkId);

	List<SmtDeviceAuthorityRelation> getRelationByAuthId(List<Integer> authId);

	List<SmtDeviceAuthorityRelation> getRelationAuth(Integer parkId, Integer businessCode, DeviceAuthorityEnum deviceAuthorityEnum);

	List<SmtDeviceAuthorityRelation> getMulRelationAuth(Long staffId, List<Integer> parkIds, Integer businessCode, DeviceAuthorityEnum deviceAuthorityEnum);

	List<SmtDeviceAuthorityRelation> getRelationByDeviceId(String deviceId);
}
