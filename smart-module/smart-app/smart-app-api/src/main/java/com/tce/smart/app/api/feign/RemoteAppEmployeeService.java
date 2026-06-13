package com.tce.smart.app.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;

/**
 * App员工服务接口
 *
 * @author mckaywu
 * @date 2019-06-13 15:35:34
 */
@FeignClient(value = ServiceNameConstants.APP_SERVICE)
public interface RemoteAppEmployeeService {

	/**
	 * 同步员工照片信息到C6、EHR
	 *
	 * @param from 调用标识
	 * @return true-成功
	 */
	@GetMapping("/employee/sync/photo/c6")
	Result<Boolean> syncPhotoToC6(@RequestHeader(SecurityConstants.FROM) String from);
}
