package com.tce.smart.platform.api.feign;


import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtEhrToStaffSettingDTO;

/**
 * EHR同步设置
 * @author QIPEI
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
@Service
public interface RemoteEhrToStaffSettingService {
	@GetMapping("/ehr/to/staff/set/list")
	Result<List<SmtEhrToStaffSettingDTO>> getList(@RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/ehr/to/staff/set/list-dhr")
	Result<List<SmtEhrToStaffSettingDTO>> getDhrList(@RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/ehr/to/staff/set/list-ehr")
	Result<List<SmtEhrToStaffSettingDTO>> getEhrList(@RequestHeader(SecurityConstants.FROM) String from);
}
