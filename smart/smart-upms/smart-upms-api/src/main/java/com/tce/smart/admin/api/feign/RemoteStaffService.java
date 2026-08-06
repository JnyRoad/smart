package com.tce.smart.admin.api.feign;

import com.tce.smart.admin.api.dto.SmtStaffDTO;
import com.tce.smart.admin.api.dto.StaffPerfectReqDTO;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: smart
 * @description:
 * @author: Wuling
 * @create: 2021-07-27 17:48
 **/
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteStaffService {

	@GetMapping("/staff/simple/get/badge")
	Result<SmtStaffDTO> getSimpleSttaffByBadge(@RequestParam("badge") String badge);

	/**
	 * 登陆初始化员工权限
	 * @return
	 */
	@PostMapping("/staff/auth/login/init")
	Result<Boolean> inintLoginAuth(@RequestParam("badge") String badge);

	@GetMapping("/staff/query/{mobile}")
	Result<List<SmtStaffDTO>> queryMobile(@PathVariable("mobile") String mobile, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 人脸登陆-人脸搜索
	 *
	 * @return
	 */
	@PostMapping("/staff/face/search/login")
	Result<SmtStaffDTO> faceSearchForLogin(@RequestBody StaffPerfectReqDTO staffPerfectDTO);
}
