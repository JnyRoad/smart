package com.tce.smart.data.api.feign.temporary;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.temporary.req.EstaffRegisterReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEstaffRegisterService {

	/**
	 * 保存入职信息
	 * @param estaffRegisterReqDTO
	 * @return Result
	 */
    @PostMapping("/estaffRegister/internal/save")
	Result<Boolean> save(@RequestBody EstaffRegisterReqDTO estaffRegisterReqDTO,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
