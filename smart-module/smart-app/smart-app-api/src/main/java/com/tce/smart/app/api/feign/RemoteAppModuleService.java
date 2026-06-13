package com.tce.smart.app.api.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.tce.smart.app.api.vo.AppModuleSimpleInfoVo;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;

/**
 * App模块信息
 *
 * @author mckaywu
 * @date 2019-06-13 15:35:34
 */
@FeignClient(value = ServiceNameConstants.APP_SERVICE)
public interface RemoteAppModuleService {

	/**
	 * 获取业务模块
	 *
	 * @return
	 */
	@GetMapping("/appserve/module/business/simple")
	Result<List<AppModuleSimpleInfoVo>> getSimpleBusModule();

}
