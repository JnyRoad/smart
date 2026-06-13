package com.tce.smart.admin.api.feign;

import com.tce.smart.admin.api.dto.SmtParkDTO;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @program: smart
 * @description:
 * @author: Wuling
 * @create: 2021-07-27 17:48
 **/
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteParkService {
	/**
	 * 通过id查询园区表
	 *
	 * @return
	 */
	@GetMapping("/park/app/{id}")
	Result<SmtParkDTO> getPakrById(@RequestParam("id") final Integer parkId, @RequestHeader(SecurityConstants.FROM) String from);
}
