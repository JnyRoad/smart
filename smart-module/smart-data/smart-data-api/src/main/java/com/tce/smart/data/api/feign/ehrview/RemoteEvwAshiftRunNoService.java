package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.EvwAshiftRunNoDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEvwAshiftRunNoService {

	/**
	 * 获取班次数据
	 *
	 * @param badge
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/evwAshiftRunNo/info")
    Result<EvwAshiftRunNoDTO> info(@RequestParam("badge") String badge, @RequestParam("empRunDate") String empRunDate,
								   @RequestHeader(SecurityConstants.FROM) String from);

}
