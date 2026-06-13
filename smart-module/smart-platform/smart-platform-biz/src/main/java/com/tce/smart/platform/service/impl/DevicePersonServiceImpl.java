package com.tce.smart.platform.service.impl;

import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.platform.api.dto.DeviceDataQueryDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.service.IDevicePersonService;
import com.tce.smart.tool.util.ImageUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 设备人员关联
 *
 * @author 王艳勇
 * @date 2019-04-15 15:15:38
 */
@Slf4j
@Service
@AllArgsConstructor
public class DevicePersonServiceImpl implements IDevicePersonService {

	//private final RemoteDeviceDataService remoteDeviceDataService;

	@Override
	public String image(SmtDeviceTask smtDeviceTask) {
		// 注释原因, devicemanager/v1/ace/face/query 未实现
		/*DeviceDataQueryDTO deviceDataQueryDTO = new DeviceDataQueryDTO();
		deviceDataQueryDTO.setDeviceCode(smtDeviceTask.getDeviceCode());
		deviceDataQueryDTO.setCardNo(smtDeviceTask.getCardNo());
		String result = remoteDeviceDataService.image(deviceDataQueryDTO, SecurityConstants.FROM_IN);
		if (StrUtil.isNotBlank(result)) {
			return ImageUtils.changeFullBase64(result);
		}
		return result;*/
		return "";
	}
}
