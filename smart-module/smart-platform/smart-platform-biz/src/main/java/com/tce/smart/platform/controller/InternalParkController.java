package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.platform.api.dto.resp.InternalParkBridgeTargetRespDTO;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.service.SmtParkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 仅向内部受控服务公开的园区最小数据。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/park")
public class InternalParkController extends BaseController {

	private final SmtParkService smtParkService;

	/**
	 * 返回 Dispatcher 创建动态 Bridge 客户端必需的最小字段。
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/bridge-targets")
	public Result<List<InternalParkBridgeTargetRespDTO>> getBridgeTargets() {
		List<SmtPark> parks = smtParkService.getUnStrainedParks();
		if (parks == null) {
			return success(Collections.emptyList());
		}
		List<InternalParkBridgeTargetRespDTO> targets = new ArrayList<>();
		for (SmtPark park : parks) {
			InternalParkBridgeTargetRespDTO target = new InternalParkBridgeTargetRespDTO();
			target.setId(park.getId());
			target.setBridgeUrl(park.getBridgeUrl());
			targets.add(target);
		}
		return success(targets);
	}
}
