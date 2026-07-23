package com.tce.smart.platform.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.SmtStaffDTO;
import com.tce.smart.platform.api.dto.req.*;
import com.tce.smart.platform.api.dto.resp.InternalScheduleIscPersonRespDTO;
import com.tce.smart.platform.api.dto.resp.InternalScheduleStaffIdentityRespDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 员工服务间调用契约。 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
@Service
public interface RemoteStaffService {
	@GetMapping("/internal/staff/schedule/isc-person/{staffId}")
	Result<InternalScheduleIscPersonRespDTO> getScheduleIscPersonStaff(@PathVariable("staffId") String staffId,
			@RequestHeader(SecurityConstants.FROM) String from, @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@GetMapping("/internal/staff/schedule/identity/{staffId}")
	Result<InternalScheduleStaffIdentityRespDTO> getScheduleIdentityStaff(@PathVariable("staffId") String staffId,
			@RequestHeader(SecurityConstants.FROM) String from, @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@PostMapping("/staff/sync")
	Result syncStaff(@RequestBody EmpHrReqDTO empHr, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@PostMapping("/staff/sync/img")
	Result syncStaffImg(@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@PostMapping("/staff/emergency/updateByBadge")
	Result updateByBadge(@RequestBody StaffEmergencyReqDTO emergencyDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 旧 /staff/myDormitory 已删除：该路由曾允许服务端调用方提交任意工号。
	 * App 本人宿舍查询必须使用 /internal/staff/dormitory 的专属用途契约。
	 */

	@GetMapping("/staff/qrcode")
	Result getQrcode(@RequestParam("badge") String badge, @RequestHeader(SecurityConstants.FROM) String from);

	@PostMapping("/dormitory/staff/addInDormitory")
	Result addInDormitory(@RequestBody InDormitoryReqDTO inDormitoryReqDTO, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/staff/perfect/check")
	Result<Boolean> checkPerfectInfo(@RequestParam("badge") String badge);

	@PostMapping("/staff/perfect/info/face")
	Result<Boolean> perfectFace(@RequestBody StaffPerfectReqDTO staffPerfectReqDTO);

	@GetMapping("/staff/auth/app/module/list")
	Result<List<String>> getStaffAppModule(@RequestParam("badge") String badge);

	@PostMapping("/staff/auth/login/init")
	Result<Boolean> inintLoginAuth(@RequestParam("badge") String badge);

	@PostMapping("/staff/sync/face/login/{badge}")
	Result<Boolean> synStaffFaceImage(@PathVariable("badge") String badge);

	@PostMapping("/staff/isc/person/face/sync")
	Result<Boolean> syncIscPersonFace(@RequestParam("badge") String badge, @RequestParam("parkId") Integer parkId,
			@RequestParam(value = "imageId", required = false) String imageId, @RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@PostMapping("/staff/isc/person/face/retry")
	Result<Boolean> retryFailedIscPersonFaceSync(@RequestHeader(SecurityConstants.FROM) String from,
			@RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@GetMapping("/dormitory/room/getJche/freeBed")
	Result getJcheFreeBed(@RequestParam("parkId") Integer parkId, @RequestParam("badge") String badge,
			@RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/staff/getStaffPark/{staffBadge}")
	Result getStaffPark(@PathVariable("staffBadge") String staffBadge, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/staff/sync/list")
	Result<Page<EmpHrReqDTO>> getStaffList(@RequestParam("current") Long current, @RequestParam("size") Long size,
			@RequestHeader(SecurityConstants.FROM) String from, @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

	@PostMapping("/staff/device/task/{action}")
	Result<Boolean> addDeviceTask(@RequestBody SmtStaffDTO smtStaff, @PathVariable("action") Integer action);
}
