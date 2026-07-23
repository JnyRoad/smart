package com.tce.smart.bridge.api.feign;

import com.tce.smart.bridge.api.dto.req.BridgeDTO;
import com.tce.smart.bridge.api.dto.req.ImageDTO;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * Feign分发接口
 *
 * @author WangJinbo
 * @date 2019/11/06s
 */
//@FeignClient("SMART-BRIDGE-BIZ-PARK-ID")
public interface RemoteBridgeService {

	@PostMapping("/bridge/dispatch")
	<T> Result<String> dispatch(@RequestBody BridgeDTO<T> bridgeDTO,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@PostMapping("/bridge/image")
	Result<String> getImage(@RequestBody ImageDTO imageDTO,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@PostMapping("/bridge/thumbnail")
	Result<String> getThumbnail(@RequestBody ImageDTO imageDTO,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
//
//	@PostMapping("/bridge/image/save")
//	Result<String> saveImage(@RequestBody ImageDTO imageDTO, @RequestHeader(SecurityConstants.FROM) String from);
//
//	@PostMapping("/bridge/thumbnail/save")
//	Result<String> saveThumbnail(@RequestBody ImageDTO imageDTO,@RequestHeader(SecurityConstants.FROM) String from);
}
