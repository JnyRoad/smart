package com.tce.smart.platform.controller.energy;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiScopeCatalog;
import com.tce.smart.platform.api.dto.resp.energy.ParkUtilityUsageMonthToDateRespDTO;
import com.tce.smart.platform.service.energy.EnergyProjectionService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 为系统应用提供按 token 园区范围隔离的当月水、电累计只读入口。 */
@RestController
@AllArgsConstructor
@RequestMapping("/open/energy")
public class EnergyUsageOpenController {

	private final EnergyProjectionService energyProjectionService;
	private final OpenApiAuthenticationAdapter openApiAuthenticationAdapter;

	/**
	 * 返回目标园区从本月一日至调用时刻的水、电累计；园区必须为正数、在 Integer 范围内且属于应用授权集合。
	 */
	@OpenApi(OpenApiScopeCatalog.SERVER)
	@GetMapping("/month/{parkId}")
	public Result<ParkUtilityUsageMonthToDateRespDTO> monthToDate(@PathVariable("parkId") Long parkId) {
		if (parkId == null || parkId <= 0 || parkId > Integer.MAX_VALUE) {
			throw new AccessDeniedException("无权访问该园区能耗数据");
		}
		List<Integer> allowedParkIds = openApiAuthenticationAdapter
				.appParkIds(SecurityContextHolder.getContext().getAuthentication());
		if (allowedParkIds == null || !allowedParkIds.contains(parkId.intValue())) {
			throw new AccessDeniedException("无权访问该园区能耗数据");
		}
		return Result.success(energyProjectionService.getCurrentMonthToDate(parkId));
	}
}
