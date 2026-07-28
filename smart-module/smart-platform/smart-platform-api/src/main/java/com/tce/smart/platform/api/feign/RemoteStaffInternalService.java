package com.tce.smart.platform.api.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.InternalStaffBindingRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffIdentityRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffLoginRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffModuleRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffPasswordRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffPhoneRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffSelfProfileRespDTO;
import com.tce.smart.platform.api.dto.req.InternalStaffPhoneUpdateReqDTO;
import com.tce.smart.platform.api.dto.req.InternalStaffFaceLoginReqDTO;
import com.tce.smart.platform.api.dto.resp.InternalStaffFaceLoginRespDTO;
import com.tce.smart.platform.api.dto.resp.MyDormitoryRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * 员工内部最小资料 Feign 契约。
 *
 * 每个方法都要求显式内部来源与服务令牌标记，调用方不得复用面向客户端的员工实体接口。
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteStaffInternalService {

	@GetMapping("/internal/staff/binding/{badge}")
	Result<InternalStaffBindingRespDTO> getBindingStaff(
			@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	@GetMapping("/internal/staff/module/{badge}")
	Result<InternalStaffModuleRespDTO> getModuleStaff(
			@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	@GetMapping("/internal/staff/password/{badge}")
	Result<InternalStaffPasswordRespDTO> getPasswordStaff(
			@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	@GetMapping("/internal/staff/ocr/{badge}")
	Result<InternalStaffIdentityRespDTO> getIdentityStaff(
			@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	@GetMapping("/internal/staff/login/mobile/{mobile}")
	Result<List<InternalStaffLoginRespDTO>> getLoginStaffByMobile(
			@PathVariable("mobile") String mobile,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	@GetMapping("/internal/staff/password-phone/{badge}")
	Result<InternalStaffPhoneRespDTO> getPasswordPhone(
			@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	@GetMapping("/internal/staff/self-profile/{badge}")
	Result<InternalStaffSelfProfileRespDTO> getSelfProfile(
			@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	@PostMapping("/internal/staff/phone")
	Result<Boolean> updatePhone(@RequestBody InternalStaffPhoneUpdateReqDTO request,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	@PostMapping("/internal/staff/face-login")
	Result<InternalStaffFaceLoginRespDTO> faceLogin(@RequestBody InternalStaffFaceLoginReqDTO request,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

	@GetMapping("/internal/staff/dormitory/{badge}")
	Result<MyDormitoryRespDTO> getMyDormitory(@PathVariable("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
			@RequestHeader("X-Smart-Internal-Purpose") String purpose);

}
