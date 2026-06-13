package com.tce.smart.admin.controller;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.admin.api.entity.SysLog;
import com.tce.smart.admin.api.vo.PreLogVO;
import com.tce.smart.admin.service.SysLogService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.annotation.Inner;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <p>
 * 日志表 前端控制器
 * </p>
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/log")
@Api(value = "log", description = "日志管理模块")
public class LogController {
	private final SysLogService sysLogService;
	/**
	 * 简单分页查询
	 *
	 * @param page   分页对象
	 * @param sysLog 系统日志
	 * @return
	 */
	@GetMapping("/page")
	public Result getLogPage(Page page, SysLog sysLog) {
		//避免前端传空串查询不到数据
		if(StringUtils.isBlank(sysLog.getType())){
			sysLog.setType(null);
		}
		return Result.success(sysLogService.page(page, Wrappers.query(sysLog)));
	}

	/**
	 * 删除日志
	 *
	 * @param id ID
	 * @return success/false
	 */
	@PostMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('sys_log_del')")
	public Result removeById(@PathVariable Long id) {
		return Result.success(sysLogService.removeById(id));
	}

	/**
	 * 插入日志
	 *
	 * @param sysLog 日志实体
	 * @return success/false
	 */
	@Inner
	@PostMapping("/save")
	public Result save(@Valid @RequestBody SysLog sysLog) {
		return Result.success(sysLogService.save(sysLog));
	}

	/**
	 * 批量插入前端异常日志
	 *
	 * @param preLogVOList 日志实体
	 * @return success/false
	 */
	@PostMapping("/logs")
	public Result saveBatchLogs(@RequestBody List<PreLogVO> preLogVOList) {
		return Result.success(sysLogService.saveBatchLogs(preLogVOList));
	}
}
