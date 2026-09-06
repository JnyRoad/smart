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
import java.util.Map;

/**
 * @program: smart
 * @description:
 * @author: Wuling
 * @create: 2021-07-27 17:48
 **/
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteStaffService {

	/** App 统一登录的人员认证来源，由平台主数据裁定，不允许调用方自行声明。 */
	@GetMapping("/internal/v1/personnel/{staffNo}/auth-source")
	Result<Map<String, String>> getAppAuthSource(@PathVariable("staffNo") String staffNo,
			@RequestHeader(SecurityConstants.FROM) String from);

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
