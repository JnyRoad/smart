package com.tce.smart.platform.controller;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.BridgeListenerDTO;
import com.tce.smart.platform.api.dto.req.DeviceStateChangeDTO;
import com.tce.smart.platform.core.entity.SmtDevice;
import com.tce.smart.platform.core.service.SmtDeviceService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @description: IntergrationDeviceController
 * @date: 2020-07-02 16:55
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/inner/device")
public class IntergrationDeviceController extends BaseController {

	@Resource
	private SmtDeviceService smtDeviceService;

	/**
	 * 接收设备状态变更通知
	 *
	 * @param dto
	 * @return
	 */
	@PostMapping("/change")
	@ApiOperation("设备状态变更-内部接口")
	public Result stateChange(@RequestBody BridgeListenerDTO dto) {
//		if(log.isDebugEnabled()) {
//			log.debug("接收设备状态变更通知{}", dto.getContent());
//		}
		log.info("接收设备状态变更通知{}", dto.getContent());
		if (StringUtils.isNotBlank(dto.getContent())) {
			DeviceStateChangeDTO changeDTO = JSONUtil.toBean(dto.getContent(), DeviceStateChangeDTO.class);
			SmtDevice smtDevice = new SmtDevice();
			smtDevice.setEnableStatus(changeDTO.getEnableStatus());
			smtDevice.setConnectStatus(changeDTO.getDeviceStatus());

			boolean res = smtDeviceService.update(smtDevice, new LambdaUpdateWrapper<SmtDevice>().eq(SmtDevice::getDeviceCode, changeDTO.getDeviceCode()));

			return success(res);
		}

		return fail("更新设备状态失败");
	}
}
