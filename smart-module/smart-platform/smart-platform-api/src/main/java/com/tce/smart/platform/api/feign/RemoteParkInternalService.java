package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.InternalParkBridgeTargetRespDTO;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * 园区内部最小数据 Feign 契约。
 *
 * 动态 Bridge 地址会接收服务令牌，调用方必须显式声明内部来源和客户端凭据标记。
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteParkInternalService {

	@GetMapping("/internal/park/bridge-targets")
	Result<List<InternalParkBridgeTargetRespDTO>> getBridgeTargets(
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@GetMapping("/internal/park/all")
	Result<List<SmtParkDTO>> getAllParks(
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);
}
