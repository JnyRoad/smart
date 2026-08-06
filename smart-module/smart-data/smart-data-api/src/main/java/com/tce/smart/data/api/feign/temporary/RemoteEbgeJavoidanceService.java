package com.tce.smart.data.api.feign.temporary;


import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.temporary.req.EbgeJavoidanceRegisterReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 *
 * @author QIPEI
 *
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEbgeJavoidanceService {
	/**
	 * 保存亲属关系
	 * @param ebgeJavoidanceRegisterReqDTO
	 * @return Result
	 */
    @PostMapping("/ebgJavoidanceRegister/internal/save")
	Result<Boolean> save(@RequestBody EbgeJavoidanceRegisterReqDTO ebgeJavoidanceRegisterReqDTO,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);


}
