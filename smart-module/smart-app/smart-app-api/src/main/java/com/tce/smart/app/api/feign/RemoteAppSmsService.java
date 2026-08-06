package com.tce.smart.app.api.feign;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Title: RemoteAppSmsService
 * @Auther: guohongtai
 * @Date: 2020-12-13 19:40
 */
@FeignClient(value = ServiceNameConstants.APP_SERVICE)
@Service
public interface RemoteAppSmsService {
	@GetMapping("/sms/verify")
	Result<Boolean> verifySmsCode(@RequestParam("mobile") String mobile, @RequestParam("smsCode") String smsCode);
}
