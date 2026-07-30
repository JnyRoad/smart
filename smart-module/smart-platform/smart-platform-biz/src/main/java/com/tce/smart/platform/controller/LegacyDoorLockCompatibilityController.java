package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.DormitoryRoomDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.LegacyDoorLockStaffRespDTO;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.security.LegacyDoorLockCallerContext;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 门锁平台不可升级期间的同路径兼容控制器。
 *
 * <p>路由名称保持不变，但每一条请求都依赖过滤器写入的 Gateway 签名证明和调用方园区
 * 范围；不得在常规员工、园区或宿舍控制器恢复这些匿名入口。</p>
 */
@RestController
@RequiredArgsConstructor
public class LegacyDoorLockCompatibilityController {

	private final SmtParkService smtParkService;
	private final SmtDormitoryStaffService smtDormitoryStaffService;
	private final SmtStaffService smtStaffService;

	@GetMapping("/dormitory/staff/remote/to/lock")
	public Result remoteDormitoryStaff(@RequestParam("parkId") Integer parkId,
			@RequestParam(value = "createTime", required = false) String createTime, HttpServletRequest request) {
		assertAllowedPark(parkId, request);
		return new Result<>(smtDormitoryStaffService.getSmtDormitoryStaffToLock(parkId, createTime));
	}

	@GetMapping("/park/tolock/dormitory/allList")
	public Result dormitoryTree(@RequestParam(value = "parkId", required = false) Integer parkId,
			HttpServletRequest request) {
		LegacyDoorLockCallerContext caller = LegacyDoorLockCallerContext.require(request);
		if (parkId != null) {
			assertAllowedPark(parkId, caller);
			return smtParkService.dormitoryAllListToLock(smtDormitoryStaffService, parkId);
		}
		// 未传 parkId 时只能查询调用方配置的园区，绝不能回退为所有园区。
		return smtParkService.dormitoryAllListToLock(smtDormitoryStaffService, caller.getParkIds());
	}

	@GetMapping("/staff/define/badge")
	public Result<LegacyDoorLockStaffRespDTO> staffByBadge(@RequestParam("badge") String badge,
			HttpServletRequest request) {
		LegacyDoorLockCallerContext caller = LegacyDoorLockCallerContext.require(request);
		SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(badge);
		// 旧接口将不存在的工号视为正常查询未命中，必须保持成功空结果的协议语义。
		if (staff == null) {
			return Result.success(null);
		}
		// 只有已存在的员工才需要依据住宿记录裁决园区范围，避免跨园区泄露真实人员资料。
		List<DormitoryRoomDetailRespDTO> rooms = smtDormitoryStaffService.getStaffRoomInfoList(badge);
		if (rooms == null || rooms.isEmpty() || rooms.stream().anyMatch(room -> room == null || room.getParkId() == null
				|| !caller.getParkIds().contains(room.getParkId()))) {
			throw new AccessDeniedException("无权查询该园区入住员工");
		}
		LegacyDoorLockStaffRespDTO response = new LegacyDoorLockStaffRespDTO();
		response.setBadge(staff.getBadge());
		response.setName(staff.getName());
		response.setPhone(staff.getPhone());
		return new Result<>(response);
	}

	private void assertAllowedPark(Integer parkId, HttpServletRequest request) {
		assertAllowedPark(parkId, LegacyDoorLockCallerContext.require(request));
	}

	private void assertAllowedPark(Integer parkId, LegacyDoorLockCallerContext caller) {
		List<Integer> parkIds = caller.getParkIds();
		if (parkId == null || !parkIds.contains(parkId)) {
			throw new AccessDeniedException("无权访问该园区门锁数据");
		}
	}
}
