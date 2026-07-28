package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAdjustbasicFullRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAdjustbasicRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *
 * @date 2019/5/6
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteLvwAdjustbasicService {
	/**
	 * 获取员工剩余可调休天数
	 *
	 * @param badge
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/adjust/basic/info")
	Result<LvwAdjustbasicRespDTO> info(@RequestParam("badge") String badge, @RequestParam("term") String term,
									   @RequestHeader(SecurityConstants.FROM) String from,
									   @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
	/**
	 * 获取员工出勤和可调修天数
	 *
	 * @param badge
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/adjust/basic/getByBadge")
	Result<List<LvwAdjustbasicFullRespDTO>> getByBadge(@RequestParam("badge") String badge,
													   @RequestHeader(SecurityConstants.FROM) String from,
													   @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
