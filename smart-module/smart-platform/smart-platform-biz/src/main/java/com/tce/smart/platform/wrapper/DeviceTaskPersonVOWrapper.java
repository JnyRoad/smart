package com.tce.smart.platform.wrapper;

import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.vo.DeviceTaskPersonVO;
import com.tce.smart.platform.service.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 *
 */
@Component
@AllArgsConstructor
public class DeviceTaskPersonVOWrapper extends BaseWrapper<SmtDeviceTask, DeviceTaskPersonVO> {
	private final ImageService imageService;
    @Override
    protected DeviceTaskPersonVO warp(SmtDeviceTask smtDeviceTask) throws IOException {
		DeviceTaskPersonVO deviceTaskPerson = new DeviceTaskPersonVO();
		deviceTaskPerson.setCardNo(smtDeviceTask.getCardNo());
		deviceTaskPerson.setName(smtDeviceTask.getGeneral());
		if(StrUtil.isNotBlank(smtDeviceTask.getImageId())){
			deviceTaskPerson.setFaceImage(imageService.buildImageUrl(smtDeviceTask.getImageId()));
		}
        return deviceTaskPerson;
    }
}
