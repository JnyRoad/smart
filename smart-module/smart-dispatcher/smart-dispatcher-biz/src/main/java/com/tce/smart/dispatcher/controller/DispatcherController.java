package com.tce.smart.dispatcher.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.dto.req.ImageDTO;
import com.tce.smart.dispatcher.api.dto.resp.BridgeDTO;
import com.tce.smart.dispatcher.service.DispatcherService;
import com.tce.smart.dispatcher.service.HandleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 业务分发模块
 *
 * @author WangJinbo
 * @date 2019-11-06 10:00:35
 */
@Slf4j
@RestController
@RequestMapping("/dispatcher")
@Api(value = "业务分发模块")
public class DispatcherController extends BaseController {

	@Resource
	private DispatcherService dispatcherService;

	@Resource
	private HandleService handleService;

	/**
	 * 业务分发
	 *
	 * @param
	 * @param
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@ApiOperation("业务分发")
	@PostMapping("/dispatch")
	public <T> Result dispatch(@RequestBody DispatcherDTO<T> dispatcherDTO) {
		return success(dispatcherService.dispatch(dispatcherDTO));
	}

	/**
	 * 接收园区服务的请求，转发给集团服务并处理
	 *
	 * @param bridgeDTO
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@ApiOperation("园区请求转发")
	@PostMapping("/handle")
	public Result handle(@RequestBody BridgeDTO<String> bridgeDTO) {
		return success(handleService.handle(bridgeDTO));
	}

	/**
	 * 获取园区图片
	 *
	 * @param imageDTO
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@ApiOperation("获取园区图片")
	@PostMapping("/image")
	public Result getImage(@RequestBody ImageDTO imageDTO) {
		return success(dispatcherService.getImage(imageDTO.getParkId(), imageDTO.getId()));
	}


	@Inner
	@OpenApi("server")
	@ApiOperation("获取园区缩略图")
	@PostMapping("/thumbnail")
	public Result getThumbnail(@RequestBody ImageDTO imageDTO){
		return success(dispatcherService.getThumbnail(imageDTO.getParkId(),imageDTO.getId()));
	}
//
//	@Inner
//	@ApiOperation("存储园区图片")
//	@PostMapping("/image/save")
//	public Result saveImage(@RequestBody ImageDTO imageDTO) {
//		return success(dispatcherService.saveImage(imageDTO.getParkId(), imageDTO.getBase64Image()));
//	}
//
//
//	@Inner
//	@ApiOperation("存储园区缩略图")
//	@PostMapping("/thumbnail/save")
//	public Result saveThumbnail(@RequestBody ImageDTO imageDTO){
//		return success(dispatcherService.saveThumbnail(imageDTO.getParkId(),imageDTO.getBase64Image()));
//	}
}
