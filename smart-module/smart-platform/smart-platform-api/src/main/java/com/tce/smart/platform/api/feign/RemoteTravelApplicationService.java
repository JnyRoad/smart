package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 出差申请管理
 * @author 梁园
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteTravelApplicationService {

	/**
	 * 查看出差列表
	 * @param current
	 * @param size
	 * @param staffBadge
	 * @param from
	 * @return
	 */
	@GetMapping("/application/travel/page")
	Result getSmtTravelApplicationPage(@RequestParam("current") final long current, @RequestParam("size") final long size,@RequestParam("staffBadge") final String staffBadge,@RequestHeader(SecurityConstants.FROM) String from);


	/**
	 * 查看出差详情
	 * @param id
	 * @return
	 */
	@GetMapping("/application/travel/detail/{id}")
	 Result getById(@PathVariable("id") Integer id,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查看出差日程
	 * @param id
	 * @return
	 */
	@GetMapping("/application/travel/infoDay/{id}")
	Result getInfoDay(@PathVariable("id") Integer id,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查看出差报告
	 * @param id
	 * @return
	 */
	@GetMapping("/application/travel/infoReport/{id}")
	Result getInfoReport(@PathVariable("id") Integer id,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查看出差流程
	 * @param id
	 * @return
	 */
	@GetMapping("/application/travel/infoFlow/{id}")
	Result getInfoFlow(@PathVariable("id") Integer id,@RequestHeader(SecurityConstants.FROM) String from);

 }
