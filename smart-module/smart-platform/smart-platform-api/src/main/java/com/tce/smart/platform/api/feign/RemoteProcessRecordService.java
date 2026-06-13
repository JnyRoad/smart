package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtProcessRecordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 流程数据管理
 * @author ly
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteProcessRecordService {

	/**
	 * 添加流程的信息
	 * @param smtProcessRecordDTO smtProcessRecordDTO
	 * @param from from
	 * @return
	 */
	@PostMapping("/processRecord/add")
	Result<?> save(@RequestBody SmtProcessRecordDTO smtProcessRecordDTO, @RequestHeader(SecurityConstants.FROM) String from);

}
