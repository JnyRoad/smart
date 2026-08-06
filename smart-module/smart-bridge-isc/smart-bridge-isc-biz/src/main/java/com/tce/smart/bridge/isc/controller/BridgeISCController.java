package com.tce.smart.bridge.isc.controller;

import com.tce.smart.bridge.isc.api.dto.req.BridgeDTO;
import com.tce.smart.bridge.isc.api.dto.req.ImageDTO;
import com.tce.smart.bridge.isc.service.BridgeISCService;
import com.tce.smart.bridge.isc.service.HBaseFileService;
import com.tce.smart.bridge.isc.service.HandleService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 园区分发模块
 *
 * @author WangJinbo
 * @date 2019-11-06 10:00:35
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/bridge")
@Api(value = "园区分发模块")
public class BridgeISCController extends BaseController {

	private final BridgeISCService bridgeISCService;

	private final HandleService handleService;

	private final HBaseFileService hBaseFileService;

	@Inner
	@ApiOperation("业务分发")
	@PostMapping("/dispatch")
	public <T> Result dispatch(@RequestBody BridgeDTO<T> bridgeDTO) {
		return success(bridgeISCService.dispatch(bridgeDTO));
	}

	@ApiOperation("ISC事件处理")
	@PostMapping("/handle")
	public <T> Result handle(@RequestBody String eventData) {
		return success(handleService.eventHandle(eventData));
	}

	@Inner
	@ApiOperation("查询图片")
	@PostMapping("/image")
	public Result getImage(@RequestBody ImageDTO imageDTO){
		return success(hBaseFileService.getById(imageDTO.getId()));
	}

	@Inner
	@ApiOperation("查询缩略图")
	@PostMapping("/thumbnail")
	public Result getThumbnail(@RequestBody ImageDTO imageDTO){
		return success(hBaseFileService.getThumbnailById(imageDTO.getId()));
	}
}
