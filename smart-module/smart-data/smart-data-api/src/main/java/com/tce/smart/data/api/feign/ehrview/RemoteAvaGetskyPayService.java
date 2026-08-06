package com.tce.smart.data.api.feign.ehrview;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.dto.ehrview.AvaGetskyPayYSHRDTO;
import com.tce.smart.data.api.dto.ehrview.req.AvaGetskyPayYSHRReqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 考勤统计
 * @author qipei
 *
 */

@FeignClient(value = ServiceNameConstants.SMART_DATA)
public interface RemoteAvaGetskyPayService {


	/**
	 * 查询考勤统计
	 * @param badge  补贴类型名称
	 * @param kqDate
	 * @return
	 */
	  @GetMapping("/ava/getskypay/info")
	  Result<AvaGetskyPayYSHRDTO> info(@RequestParam("badge") String badge, @RequestParam("kqDate") String kqDate,
									   @RequestHeader(SecurityConstants.FROM) String from);
	/**
	 * 获得范围时间内考勤数据
	 * @param startTime
	 * @return
	 */
	@GetMapping("/ava/getskypay/list")
	Result<List<AvaGetskyPayYSHRDTO>> monthList(@RequestParam("startTime") LocalDateTime startTime,
												@RequestParam("buIds") List<String> buIds,
												@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 获得范围时间内考勤数据
	 * @param dto
	 * @return
	 */
	@GetMapping("/ava/getskypay/byBadge")
	Result<List<AvaGetskyPayYSHRDTO>> monthListByBadge(@RequestBody AvaGetskyPayYSHRReqDTO dto,
												@RequestHeader(SecurityConstants.FROM) String from);

}
