package com.tce.smart.platform.controller.energy;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiScopeCatalog;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.resp.energy.ParkUtilityUsageMonthToDateRespDTO;
import com.tce.smart.platform.service.energy.EnergyProjectionService;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/** 园区能耗投影内部入口及仅查询的当月累计接口。 */
@Api(tags = "platform-园区能耗")
@RestController
@AllArgsConstructor
public class EnergyProjectionController {
	private final EnergyProjectionService energyProjectionService;

	/** 返回调用日所在月份从 1 日到当前时刻的水、电分项累计。 */
	@GetMapping("/sd/statistics/month/{parkId}")
	@PreAuthorize("@pms.hasPermission('platform_energy_usage_view')")
	public Result<ParkUtilityUsageMonthToDateRespDTO> monthToDate(@PathVariable("parkId") Long parkId) {
		assertCurrentUserCanReadPark(parkId);
		return Result.success(energyProjectionService.getCurrentMonthToDate(parkId));
	}

	/** 处理有限批量的待投影请求，仅允许具备能耗投影能力的调度服务调用。 */
	@Inner
	@OpenApi(value = OpenApiScopeCatalog.ENERGY_PROJECTION_RUN,
			compatibilityScopes = {OpenApiScopeCatalog.LEGACY_SERVER})
	@PostMapping("/inner/energy/projection/process-pending")
	public Result<Boolean> processPending() {
		energyProjectionService.processPending();
		return Result.success(Boolean.TRUE);
	}

	/** 回算指定业务日，仅允许定时服务内部调用。 */
	@Inner
	@OpenApi(value = OpenApiScopeCatalog.ENERGY_PROJECTION_RUN,
			compatibilityScopes = {OpenApiScopeCatalog.LEGACY_SERVER})
	@PostMapping("/inner/energy/projection/reconcile/{businessDate}")
	public Result<Boolean> reconcile(@PathVariable("businessDate") String businessDate) {
		energyProjectionService.reconcile(LocalDate.parse(businessDate));
		return Result.success(Boolean.TRUE);
	}

	/** 受上限保护的当月回填入口，不接受调用方指定日期。 */
	@Inner
	@OpenApi(value = OpenApiScopeCatalog.ENERGY_PROJECTION_RUN,
			compatibilityScopes = {OpenApiScopeCatalog.LEGACY_SERVER})
	@PostMapping("/inner/energy/projection/backfill-month-to-date")
	public Result<Boolean> backfillMonthToDate() {
		energyProjectionService.backfillCurrentMonthToDate();
		return Result.success(Boolean.TRUE);
	}

	/** 将同一业务日的回算和受控回填置于一次内部远程调用，避免调度端两次 Feign 间失锁。 */
	@Inner
	@OpenApi(value = OpenApiScopeCatalog.ENERGY_PROJECTION_RUN,
			compatibilityScopes = {OpenApiScopeCatalog.LEGACY_SERVER})
	@PostMapping("/inner/energy/projection/daily/{businessDate}")
	public Result<Boolean> daily(@PathVariable("businessDate") String businessDate, @RequestParam("reconcile") boolean reconcile,
			@RequestParam("backfill") boolean backfill) {
		if (!reconcile && !backfill) throw new IllegalArgumentException("至少需要启用回算或回填");
		LocalDate parsedBusinessDate = LocalDate.parse(businessDate);
		if (reconcile) energyProjectionService.reconcile(parsedBusinessDate);
		if (backfill) energyProjectionService.backfillCurrentMonthToDate();
		return Result.success(Boolean.TRUE);
	}

	/** 空认证、空园区授权集和不匹配园区均拒绝，不能将空集合解释为全园区。 */
	static void assertParkAuthorized(SmartUser user, Long parkId) {
		List<Integer> parkIds = user == null ? null : user.getParkIdList();
		if (parkId == null || parkIds == null || parkIds.isEmpty() || !parkIds.contains(parkId.intValue())) {
			throw new AccessDeniedException("无权访问该园区能耗数据");
		}
	}

	static void assertAuthenticationCanReadPark(Authentication authentication, Long parkId) {
		if (authentication == null || !authentication.isAuthenticated()) throw new AccessDeniedException("无权访问该园区能耗数据");
		assertParkAuthorized(SecurityUtils.getUser(authentication), parkId);
	}

	private static void assertCurrentUserCanReadPark(Long parkId) {
		assertAuthenticationCanReadPark(SecurityContextHolder.getContext().getAuthentication(), parkId);
	}
}
