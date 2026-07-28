package com.tce.smart.dispatcher.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.dto.req.ImageDTO;
import com.tce.smart.dispatcher.api.dto.resp.BridgeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Feign分发接口
 *
 * @author WangJinbo
 * @date 2019/11/06s
 */
@FeignClient(value = ServiceNameConstants.SMART_DISPATCHER)
public interface RemoteDispatcherService {
	/**
	 * 接收集团服务的请求，统一往园区分发
	 *
	 * @param dispatcherDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/dispatcher/dispatch")
	<T> Result<String> dispatch(@RequestBody DispatcherDTO<T> dispatcherDTO, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 接收园区服务的请求，转发给集团服务并处理
	 *
	 * @param bridgeDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/dispatcher/handle")
	Result<Boolean> handle(@RequestBody BridgeDTO<String> bridgeDTO, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 获取园区图片
	 *
	 * @param imageDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/dispatcher/image")
	Result<String> getImage(@RequestBody ImageDTO imageDTO, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 获取园区缩略图
	 * @param imageDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/dispatcher/thumbnail")
	Result<String> getThumbnail(@RequestBody ImageDTO imageDTO, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 存储园区图片
	 *
	 * @param imageDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/dispatcher/image/save")
	Result<String> saveImage(@RequestBody ImageDTO imageDTO, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 存储园区缩略图
	 * @param imageDTO
	 * @param from
	 * @return
	 */
	@PostMapping("/dispatcher/thumbnail/save")
	Result<String> saveThumbnail(@RequestBody ImageDTO imageDTO, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/**
	 * 接收园区服务的请求，转发给集团服务并处理
	 *
	 * @param eventData
	 * @param from
	 * @return
	 */
	@PostMapping("/dispatcher/event/handle")
	Result<Boolean> eventHandle(@RequestBody String eventData, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
