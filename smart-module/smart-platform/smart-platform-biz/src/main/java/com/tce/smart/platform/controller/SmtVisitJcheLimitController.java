package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.VisitJcheLimitReqDTO;
import com.tce.smart.platform.api.dto.resp.VisitJcheLimitDTO;
import com.tce.smart.platform.service.SmtVisitJcheLimitService;
import com.tce.smart.tool.enums.ConfigBusinessEnum;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

/**
 * @author fushiping
 * @date 2020-08-06 15:30:50
 */
@RestController
@AllArgsConstructor
@RequestMapping("/visit/limit")
public class SmtVisitJcheLimitController extends BaseController {

	private final SmtVisitJcheLimitService smtVisitJcheLimitService;

	/**
	 * 分页查询
	 *
	 * @param page 分页对象
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtVisitJcheLimitPage(Page page) {
		return success(smtVisitJcheLimitService.getList(page, ConfigBusinessEnum.VISITOR.getCode()), VisitJcheLimitDTO.class);
	}

	/**
	 * 列表查询
	 *
	 * @param parkId 园区id
	 * @param
	 * @return
	 */
	@GetMapping("/list")
	public Result getSmtVisitJcheLimitList(@RequestParam("parkId") Integer parkId) {
		return success(smtVisitJcheLimitService.listByParkId(parkId, ConfigBusinessEnum.VISITOR.getCode()));
	}

	/**
	 * 列表查询
	 *
	 * @param parkId 园区id
	 * @param
	 * @return
	 */
	@GetMapping("/list/jche")
	public Result getSmtVisitJcheList(@RequestParam("parkId") Integer parkId) {
		return success(smtVisitJcheLimitService.getJcheIds(parkId, ConfigBusinessEnum.VISITOR.getCode()));
	}


	/**
	 * 通过id查询
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return success(smtVisitJcheLimitService.getById(id));
	}

	/**
	 * 新增
	 *
	 * @param
	 * @return Result
	 */
	@SysLog("新增或编辑")
	@PostMapping("/edit")
	public Result save(@RequestBody VisitJcheLimitReqDTO reqDTO) {
		return success(smtVisitJcheLimitService.saveList(reqDTO.getParkId(), reqDTO.getJcheList(), ConfigBusinessEnum.VISITOR.getCode()));
	}

	/**
	 * 分页查询
	 *
	 * @param page 分页对象
	 * @return
	 */
	@GetMapping("/admittance/page")
	public Result getAdmittancePage(Page page) {
		return success(smtVisitJcheLimitService.getList(page, ConfigBusinessEnum.ADMITTANCE.getCode()), VisitJcheLimitDTO.class);
	}

	/**
	 * 列表查询
	 *
	 * @param parkId 园区id
	 * @param
	 * @return
	 */
	@GetMapping("/admittance/list")
	public Result getAdmittanceList(@RequestParam("parkId") Integer parkId) {
		return success(smtVisitJcheLimitService.listByParkId(parkId, ConfigBusinessEnum.ADMITTANCE.getCode()));
	}

	/**
	 * 列表查询
	 *
	 * @param parkId 园区id
	 * @param
	 * @return
	 */
	@GetMapping("/admittance/list/jche")
	public Result getAdmittanveJcheList(@RequestParam("parkId") Integer parkId) {
		return success(smtVisitJcheLimitService.getJcheIds(parkId, ConfigBusinessEnum.ADMITTANCE.getCode()));
	}
	/**
	 * 新增
	 *
	 * @param
	 * @return Result
	 */
	@SysLog("新增或编辑")
	@PostMapping("/admittance/edit")
	public Result saveAdmittance(@RequestBody VisitJcheLimitReqDTO reqDTO) {
		return success(smtVisitJcheLimitService.saveList(reqDTO.getParkId(), reqDTO.getJcheList(), ConfigBusinessEnum.ADMITTANCE.getCode()));
	}


}
