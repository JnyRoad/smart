package com.tce.smart.platform.api.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.SearchSocialSecurityRespDTO;

/**
 * 社保公积金api
 * @author 齐佩
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteSocialSecurityService {


	@GetMapping("/social/security/list")
    Result<List<SearchSocialSecurityRespDTO>> getSmtSocialSecurityList(@RequestHeader(SecurityConstants.FROM) String from);
}
