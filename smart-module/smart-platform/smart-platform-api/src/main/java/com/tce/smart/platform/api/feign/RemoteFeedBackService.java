package com.tce.smart.platform.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.AddFeedBackReqDTO;

/**
 * 员工问题反馈API
 * @author 齐佩
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteFeedBackService {

	/**
	 * 添加问题反馈
	 * @param feedBack
	 * @param from
	 * @return
	 */
	@GetMapping("/feed/back/add")
	Result addSmtFeedBack(@RequestBody AddFeedBackReqDTO feedBack,@RequestHeader(SecurityConstants.FROM) String from);

}
