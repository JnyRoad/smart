package com.tce.smart.admin.api.feign;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @program: smart
 * @description:
 * @author: Wuling
 * @create: 2021-07-27 17:55
 **/
@FeignClient(value = ServiceNameConstants.APP_SERVICE)
public interface RemoteAppSmsService {
	@GetMapping("/sms/verify")
	Result<Boolean> verifySmsCode(@RequestParam("mobile") String mobile, @RequestParam("smsCode") String smsCode);

	@PostMapping("/wechat/getBadge")
	Result<String> getBadge(@RequestParam("code") String code);

	@GetMapping("/wechat/getBadge/by-code")
	Result<String> getBadgeByCode(@RequestParam("code") String code);

	@GetMapping("/yht/user/badge")
	Result<String> getUserBadge(@RequestParam("code") String code);
}
