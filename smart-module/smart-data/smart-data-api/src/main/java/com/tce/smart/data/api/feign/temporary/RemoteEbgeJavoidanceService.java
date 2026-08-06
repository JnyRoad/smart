package com.tce.smart.data.api.feign.temporary;


import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.temporary.req.EbgeJavoidanceRegisterReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    @PostMapping("/ebgJavoidanceRegister/save")
	Result<Boolean> save(@RequestBody EbgeJavoidanceRegisterReqDTO ebgeJavoidanceRegisterReqDTO);


}
