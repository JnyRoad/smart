package com.tce.smart.data.api.feign.attendance;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.attendance.resp.KQCardDetailsRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteKQCardDetailsService {

	/**
	 * 获取打卡
	 * @param badge
	 * @param from
	 * @return
	 */
	@GetMapping("/kq/card/details/info")
	Result<List<KQCardDetailsRespDTO>> info(@RequestParam("badge") String badge, @RequestParam("kqDate") String kqDate,
											@RequestHeader(SecurityConstants.FROM) String from);



	@GetMapping("/kq/card/details/month/info")
	Result<List<KQCardDetailsRespDTO>> mothInfo(@RequestParam("badge") String badge, @RequestParam("kqDate") String kqDate,
										 @RequestHeader(SecurityConstants.FROM) String from);

}
