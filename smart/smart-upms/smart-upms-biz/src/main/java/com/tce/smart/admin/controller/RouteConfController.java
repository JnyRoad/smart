package com.tce.smart.admin.controller;

import cn.hutool.json.JSONArray;
import com.tce.smart.admin.service.SysRouteConfService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 路由
 *
 */
@RestController
@AllArgsConstructor
@RequestMapping("/route")
@Api(value = "route",description = "动态路由管理模块")
public class RouteConfController {
	private final SysRouteConfService sysRouteConfService;


	/**
	 * 获取当前定义的路由信息
	 *
	 * @return
	 */
	@GetMapping
	public Result listRoutes() {
		return Result.success(sysRouteConfService.list());
	}

	/**
	 * 修改路由
	 *
	 * @param routes 路由定义
	 * @return
	 */
	@SysLog("修改路由")
	@PostMapping
	public Result updateRoutes(@RequestBody JSONArray routes) {
		return Result.success(sysRouteConfService.updateRoutes(routes));
	}

}
