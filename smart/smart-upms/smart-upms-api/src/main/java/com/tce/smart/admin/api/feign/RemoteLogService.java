package com.tce.smart.admin.api.feign;

import com.tce.smart.admin.api.entity.SysLog;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 *
 * @date 2018/6/28
 */
@FeignClient(value = ServiceNameConstants.UMPS_SERVICE)
public interface RemoteLogService {
	/**
	 * 保存日志
	 *
	 * @param sysLog 日志实体
	 * @param from   是否内部调用
	 * @return succes、false
	 */
	@PostMapping("/log/save")
    Result<Boolean> saveLog(@RequestBody SysLog sysLog, @RequestHeader(SecurityConstants.FROM) String from);
}
