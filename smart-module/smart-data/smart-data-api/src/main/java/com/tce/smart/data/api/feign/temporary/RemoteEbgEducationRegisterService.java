package com.tce.smart.data.api.feign.temporary;

import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.temporary.req.EbgEducationRegisterReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEbgEducationRegisterService {
	/**
	 * 保存教育经历
	 * @param ebgEducationRegisterReqDTO
	 * @return Result
	 */
    @PostMapping("/ebgEducationRegister/save")
	Result<Boolean> save(@RequestBody EbgEducationRegisterReqDTO ebgEducationRegisterReqDTO);


}
