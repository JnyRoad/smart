package com.tce.smart.data.api.feign.temporary;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.temporary.req.EbgFamilyRegisterReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEbgFamilyRegisterService {
	/**
	 * 保存家庭背景
	 * @param ebgFamilyRegisterReqDTO
	 * @return Result
	 */
    @PostMapping("/ebgFamilyRegister/internal/save")
	Result<Boolean> save(@RequestBody EbgFamilyRegisterReqDTO ebgFamilyRegisterReqDTO,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);


}
