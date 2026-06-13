package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.core.entity.SmtSdTemplates;
import com.tce.smart.platform.service.settlement.SmtSDTemplatesService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @description: 水电模板控制器
 * @date: 2020-07-07 11:38
 * @author: wuling
 * @version: 1.0
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/sd")
public class SmtSDTemplatesController {

	private final SmtSDTemplatesService smtSDTemplatesService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param smtSdTemplates 水电模板数据
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtParkingCountPage(Page page, SmtSdTemplates smtSdTemplates) {
		return new Result<>(smtSDTemplatesService.getSmtDormitorySDTemplatePage(page, smtSdTemplates));
	}

	/**
	 * 新增水电模板记录
	 * @param smtSdTemplates 水电模板记录
	 * @return Result
	 */
	@PostMapping("/add")
	public Result save(@RequestBody SmtSdTemplates smtSdTemplates){
		smtSdTemplates.setCreateTime(new Date());
		return new Result<>(smtSDTemplatesService.save(smtSdTemplates));
	}

	/**
	 * 修改水电模板记录
	 * @param smtSdTemplates 水电模板记录
	 * @return Result
	 */
	@PostMapping("/update")
	public Result updateById(@RequestBody SmtSdTemplates smtSdTemplates) {
		return new Result<>(smtSDTemplatesService.update(SmtSdTemplates.builder().templateName(smtSdTemplates.getTemplateName()).build(),new LambdaQueryWrapper<SmtSdTemplates>()
				.eq(SmtSdTemplates::getId,smtSdTemplates.getId())
				.eq(SmtSdTemplates::getParkId,smtSdTemplates.getParkId())));
	}


	/**
	 * 通过id查询水电模板记录
	 * @param tempId
	 * @return Result
	 */
	@GetMapping("/{tempId}")
	public Result getById(@PathVariable("tempId") Long tempId){
		return new Result<>(smtSDTemplatesService.getById(tempId));
	}

	/**
	 * 通过id删除水电模板记录
	 * @param tempId
	 * @return Result
	 */
	@PostMapping("/{tempId}")
	public Result removeById(@PathVariable("tempId") Long tempId){
		return new Result<>(smtSDTemplatesService.deleteSDTemplateData(tempId));
	}

	/**
	 * 通过parkid查询水电模板记录
	 * @param parkid
	 * @return Result
	 */
	@GetMapping("/parkId/{parkid}")
	public Result getByParkId(@PathVariable("parkid") Integer parkid){
		return new Result<>(smtSDTemplatesService.getSDTempByParkId(parkid));
	}

	/**
	 * 获取岗位级层列表
	 * @return Result
	 */
	@GetMapping("/jchenList")
	public Result getJChenList(){
		return new Result<>(smtSDTemplatesService.getJChenList());
	}
}
