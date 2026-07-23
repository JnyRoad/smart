package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.CvwCcdAllowRuleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 计算规则
 * @author tce
 *
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteCvwCcdAllowRuleService {

	/**
	 * 根据id获取补贴计算规则
	 * @param id
	 * @return
	 */
	@GetMapping("/cd/allow/rule/get")
    Result<CvwCcdAllowRuleDTO> getById(@RequestParam("id") String id, @RequestHeader(SecurityConstants.FROM) String from,
    @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@GetMapping("/cd/allow/rule/get/byTitle")
    Result<CvwCcdAllowRuleDTO> getByTitle(@RequestParam("title") String title,
									   @RequestHeader(SecurityConstants.FROM) String from,
									   @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
