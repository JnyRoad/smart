package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.LvwAcardlostDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteLvwAcardlostService {
	/**
	 * 根据工号和出勤日期查询  补卡信息
	 * @param badge
	 * @param startDate
	 * @return
	 */
	@GetMapping("/lvw/acardlost/info")
	Result<LvwAcardlostDTO> getByBadge(@RequestParam("badge") String badge, @RequestParam("startDate") String startDate,
										   @RequestHeader(SecurityConstants.FROM) String from,
										   @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

}
