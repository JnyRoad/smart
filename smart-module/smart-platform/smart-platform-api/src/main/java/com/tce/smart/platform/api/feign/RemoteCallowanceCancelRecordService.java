package com.tce.smart.platform.api.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;

/**
 * 外宿补贴撤销
 * @author QIPEI
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteCallowanceCancelRecordService {


	/**
	 * 外宿补贴撤销申请添加
	 * @param badge
	 * @param backDate
	 * @param from
	 * @return
	 */
	@GetMapping("/callowance/cancel/add")
	Result save(@RequestParam("badge") String badge,@RequestParam("backDate") String backDate, @RequestParam("type")Integer type, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 外宿补贴撤销记录详情
	 * @param id
	 * @param from
	 * @return
	 */
	@GetMapping("/callowance/cancel/get/detail")
	Result detail(@RequestParam("id") Integer id,@RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 外宿补贴撤销列表查询
	 * @param badge
	 * @param from
	 * @return
	 */
	@GetMapping("/callowance/cancel/get")
	Result get(@RequestParam("badge") String badge, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查询员工外宿补贴信息
	 * @param badge
	 * @return
	 */
	@GetMapping("/callowance/cancel/get/out/dormitory")
	Result getOutDormitory(@RequestParam("badge") String badge, @RequestParam("type")Integer type, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查询外宿补贴详情
	 * @param badge
	 * @param from
	 * @return
	 */
	@GetMapping("/callowance/cancel/get/callowance/detail")
	Result callowanceDetail(@RequestParam("badge") String badge, @RequestParam("type")Integer type, @RequestHeader(SecurityConstants.FROM) String from);
}
