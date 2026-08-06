package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.EvwJjitemRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteEvwJjitemService {
	/**
	 * 根据人事区域获取工作交接项信息
	 *
	 * @param ezid
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/jjitem/{ezid}")
	Result<List<EvwJjitemRespDTO>>  info(@PathVariable("ezid") Integer ezid,
										 @RequestHeader(SecurityConstants.FROM) String from);

}
