package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.DeviceTagSetReqDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTagRelation;

/**
 * @author sunfujian
 * @date 2021/7/29 11:25
 */
public interface SmtDeviceTagRelationService extends IService<SmtDeviceTagRelation> {

	Boolean saveBatch(DeviceTagSetReqDTO tagSetReqDTO);

	Boolean exist(String deviceId, Long tagId);
}
