package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.CvwCcdAllowanceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 补贴类型
 * @author qipei
 *
 */

@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteCvwCcdAllowanceService {


	/**
	 * 获取补贴类型信息
	 * @param allowanceName  补贴类型名称
	 * @param from
	 * @return
	 */
	@GetMapping("/cd/allowance/get")
	Result<CvwCcdAllowanceDTO> getByName(@RequestParam("allowanceName") String allowanceName,
										 @RequestHeader(SecurityConstants.FROM) String from);

}
