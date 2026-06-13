package com.tce.smart.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tce.smart.admin.api.entity.SysMoveDataTask;
import com.tce.smart.admin.service.SysMoveDataTaskService;
import com.tce.smart.common.core.model.Result;

import lombok.AllArgsConstructor;

/**
 * <p>
 * 数据转移任务表配置管理 前端控制器
 * </p>
 */
@RestController
@AllArgsConstructor
@RequestMapping("/movetask")
public class MoveDataTaskController {

	private final SysMoveDataTaskService sysMoveDataTaskService;

	/**
	 * 查询模块表配置信息
	 *
	 * @param moduleType 模块类型
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@GetMapping("/list")
	public Result<List<SysMoveDataTask>> getTaskTableList(@RequestParam("moduleType") Integer moduleType) {
		return Result.success(sysMoveDataTaskService.getModuleTaskList(moduleType));
	}

}
