package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.BridgeListenerDTO;
import com.tce.smart.tool.exception.TCEException;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: IntergrationCameraController
 * @date: 2020-07-02 17:02
 * @author: wuling
 * @version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/inner/camera")
public class IntergrationCameraController extends BaseController {

	/**
	 * 接收越界报警通知
	 * @param bridgeListenerDTO
	 * @return
	 */
	@Inner
	@ApiOperation("接收越界报警通知")
	@PostMapping("/cross/border/reply")
	public Result<Boolean> replyOfCrossBorder(@RequestBody BridgeListenerDTO bridgeListenerDTO){
		log.info("接收越界报警{}",bridgeListenerDTO.getContent());
		if(StringUtils.isBlank(bridgeListenerDTO.getContent())){
			throw new TCEException("越界报警收到数据为空");
		}
		//return success(alarmLogBizService.handBorderAlarmLog(bridgeListenerDTO.getContent()));
		return success();
	}

	@Inner
	@ApiOperation("根据区域ID获取设备列表")
	@PostMapping("/log/reply")
	public Result<Boolean> replyOfCamera(@RequestBody BridgeListenerDTO bridgeListenerDTO) {
		return success();
	}
}
