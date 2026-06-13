package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.beust.jcommander.Parameter;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtArea;
import com.tce.smart.platform.service.SmtAreaService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *
 * 地点表
 *
 * @author 齐佩
 * @date 2019-04-13 13:48:18
 */
@RestController
@AllArgsConstructor
@RequestMapping("/area")
public class SmtAreaController {

	private final SmtAreaService smtAreaService;

	/**
	 * 分页查询地点表
	 *
	 * @param page
	 *            分页对象
	 * @param smtArea
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtAreaPage(Page page, SmtArea smtArea) {
		return new Result<>(smtAreaService.getSmtAreaPage(page, smtArea));
	}

	@GetMapping("/all")
	public Result getSmtParkAll() {
		return new Result<>(smtAreaService.getSmtAreaAll());
	}
	/**
	 * 通过id查询地点表
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return new Result<>(smtAreaService.getById(id));
	}

	/**
	 * 新增地点表
	 *
	 * @param smtArea
	 * @return Result
	 */
	@SysLog("新增")
	@PostMapping("/save")
	public Result save(@RequestBody SmtArea smtArea) {
		return smtAreaService.addArea(smtArea);
	}

	/**
	 * 修改地点表
	 *
	 * @param smtArea
	 *
	 * @return Result
	 */
	@SysLog("修改")
	@PostMapping("/update")
	public Result updateById(@RequestBody SmtArea smtArea) {
		return smtAreaService.updateAreaById(smtArea);
	}

	/**
	 * 通过id删除地点表
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@SysLog("删除")
	@GetMapping("/delete/{id}")
	public Result removeById(@PathVariable Integer id) {
		return smtAreaService.removeAreaById(id);
	}

}
