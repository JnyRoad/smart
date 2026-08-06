package com.tce.smart.app.api.feign;

import com.tce.smart.app.api.dto.InternalSmsVerifyReqDTO;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @Title: RemoteAppSmsService
 * @Auther: guohongtai
 * @Date: 2020-12-13 19:40
 */
@FeignClient(value = ServiceNameConstants.APP_SERVICE)
@Service
public interface RemoteAppSmsService {
	@PostMapping("/sms/internal/verify")
	Result<Boolean> verifySmsCode(@RequestBody InternalSmsVerifyReqDTO request,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	/** 匿名访客专用 OTP 校验，服务端会限制失败次数并在成功后原子消费验证码。 */
	@PostMapping("/sms/internal/visitor/verify")
	Result<Boolean> verifyVisitorSmsCode(@RequestBody InternalSmsVerifyReqDTO request,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
