package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtAlarmRecordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 水电日结算
 * @author
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteDailySettlementService {

	/**
	 * 水电日结算
	 * @return Result
	 */
	@PostMapping("/dormitory/meterread/daily/gen")
	Result<Boolean> genDailyRecord(@RequestHeader(SecurityConstants.FROM) String from);

}
