package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.EvwCcdFlstandardDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEvwCcdFlstandardService {


	@GetMapping("/flstandard/get")
    Result<EvwCcdFlstandardDTO> getById(@RequestParam("id") String id, @RequestParam(value = "Pzid",required = false) Integer Pzid,
                                        @RequestHeader(SecurityConstants.FROM) String from,
                                        @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
