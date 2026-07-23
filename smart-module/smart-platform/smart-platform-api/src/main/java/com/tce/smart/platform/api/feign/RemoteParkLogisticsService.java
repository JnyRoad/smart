package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtParkLogisticsDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 *  园区物流关系表
 *
 * @author mingkai.wu
 * @date 2019-05-09 17:19:50
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteParkLogisticsService {

	/**
	 * 查询园区物流关系表
	 * @param from from
	 * @return
	 */
	@GetMapping("/parklogistics/list")
	Result<List<SmtParkLogisticsDTO>> list(@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@GetMapping("/parklogistics/companyId/{companyId}")
	Result<SmtParkLogisticsDTO> getByCompanyId(@PathVariable("companyId") String companyId,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
