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
 * 警报记录
 * @author Lenovo
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteAlarmRecordService {

	/**
	 * 添加警报记录
	 * @param smtAlarmRecord 警报记录信息
	 * @return Result
	 */
	@PostMapping("/alarm/record/save")
	Result<Boolean> save(@RequestBody SmtAlarmRecordDTO smtAlarmRecord, @RequestHeader(SecurityConstants.FROM) String from);

}
