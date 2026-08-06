package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.IscTemperatureDTO;
import com.tce.smart.platform.api.dto.req.SaveSnapPersonReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 *梁圆
 * @date 2018/6/28
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteSnapPersonService {

	/**
	 * 添加抓拍
	 *
	 * @param saveSnapPersonReqDTO 抓拍实体
	 * @param from from
	 * @return succes、false
	 */
	@PostMapping("/snap/person/addSnapPerson")
    Result<Boolean> addSnapPerson(@RequestBody SaveSnapPersonReqDTO saveSnapPersonReqDTO, @RequestHeader(SecurityConstants.FROM) String from);

	@PostMapping("/snap/person/check/temp")
	Result<Boolean> checkTemperature(@RequestBody List<IscTemperatureDTO> dto,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
