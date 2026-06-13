package com.tce.smart.platform.wrapper;

import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.resp.DeviceTagListDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTag;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author sunfujian
 * @date 2021/7/29 11:41
 */
@Component
public class DeviceTagListWrapper extends BaseWrapper<SmtDeviceTag, DeviceTagListDTO> {

	@Override
	protected DeviceTagListDTO warp(SmtDeviceTag smtDeviceTag) throws IOException {
		DeviceTagListDTO deviceTagListDTO = new DeviceTagListDTO();
		deviceTagListDTO.setId(smtDeviceTag.getId());
		deviceTagListDTO.setTagName(smtDeviceTag.getTagName());
		return deviceTagListDTO;
	}
}
