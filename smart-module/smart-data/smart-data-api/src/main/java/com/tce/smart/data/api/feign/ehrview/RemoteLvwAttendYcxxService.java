package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAttendYcxxFullRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.LvwAttendYcxxSimpleRespDTO;
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
public interface RemoteLvwAttendYcxxService {
	/**
	 * 补卡查询
	 *
	 * @param badge
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/attend/ycxx/info")
    Result<LvwAttendYcxxSimpleRespDTO> info(@RequestParam("badge") String badge, @RequestParam("startDate") String startDate,
											@RequestParam("endDate") String endDate,
											@RequestHeader(SecurityConstants.FROM) String from);
	/**
	 * 补卡全部查询
	 *
	 * @param startDate startDate
	 * @param endDate endDate
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/attend/ycxx/infoAll")
	Result<List<LvwAttendYcxxFullRespDTO>> infoAll(@RequestParam("startDate") String startDate,
												   @RequestParam("endDate") String endDate, @RequestHeader(SecurityConstants.FROM) String from);

}
