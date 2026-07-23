package com.tce.smart.admin.api.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.tce.smart.admin.api.entity.SysMoveDataTask;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;

/**
 * 数据转移任务远程服务
 *
 * @author mkwu
 * @date 2019-08-08
 */
@FeignClient(value = ServiceNameConstants.UMPS_SERVICE)
public interface RemoteMoveDataTaskService {

	/**
	 * 查询模块表配置信息
	 *
	 * @param moduleType 模块类型
	 * @param from       是否内部调用
	 * @return
	 */
	@GetMapping("/movetask/list")
	Result<List<SysMoveDataTask>> getTaskTableList(@RequestParam("moduleType") Integer moduleType,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
