package com.tce.smart.platform.controller;

import com.tce.smart.platform.api.dto.req.SmtDormitoryReqDTO;
import com.tce.smart.platform.api.dto.resp.DormitoryFloorRespDTO;
import com.tce.smart.platform.api.dto.resp.SmtDormitoryRespDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtDormitory;
import com.tce.smart.platform.service.SmtDormitoryService;

import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 园区宿舍楼表
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:25
 */
@Api(tags = "楼栋管理")
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory")
public class SmtDormitoryController {

	private final SmtDormitoryService smtDormitoryService;

	/**
	 * 分页查询
	 *
	 * @param page
	 *            分页对象
	 * @param smtDormitory
	 *            园区宿舍楼表
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtDormitoryPage(Page page, SmtDormitory smtDormitory) {
		return smtDormitoryService.getSmtDormitoryPage(page, smtDormitory);
	}

	/**
	 * 根据条件展示所有的宿舍楼
	 * @param smtDormitory
	 * @return
	 */
	@ApiOperation("根据园区ID查询宿舍信息")
	@PostMapping("/queryDormitory")
	public Result<List<DormitoryFloorRespDTO>> queryDormitory(@RequestBody SmtDormitoryReqDTO smtDormitory) {
		return new Result<>(smtDormitoryService.getByParkId(smtDormitory.getParkId(), smtDormitory.getIsAccount()));
	}

	/**
	 * 根据园区ID查询宿舍信息
	 * @param parkId 园区ID
	 * @return
	 */
	@ApiOperation("根据园区ID查询宿舍信息")
	@GetMapping("/queryDormitoryByParkId/{parkId}")
	public Result<List<SmtDormitoryRespDTO>> queryDormitoryByParkId(@ApiParam(name = "id",value = "园区ID",required = true)@PathVariable("parkId") Integer parkId) {
		return new Result<>(smtDormitoryService.getDormitoryByParkId(parkId));
	}


	/**
	 * 通过id查询园区宿舍楼表
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return new Result<>(smtDormitoryService.getById(id));
	}

	/**
	 * 新增园区宿舍楼表
	 *
	 * @param smtDormitory
	 *            园区宿舍楼表
	 * @return Result
	 */
	@SysLog("新增园区宿舍楼表")
	@PostMapping("/addDormitory")
	public Result addDormitory(@RequestBody SmtDormitory smtDormitory) {
		return smtDormitoryService.addDormitory(smtDormitory);
	}

	/**
	 * 修改园区宿舍楼表
	 *
	 * @param smtDormitory
	 *            园区宿舍楼表
	 * @return Result
	 */
	@SysLog("修改园区宿舍楼表")
	@PostMapping("/updateDormitory")
	public Result updateById(@RequestBody SmtDormitory smtDormitory) {
		return smtDormitoryService.updateDormitoryById(smtDormitory);
	}

	/**
	 * 通过id删除园区宿舍楼表
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@SysLog("删除园区宿舍楼表")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable Integer id) {
		return smtDormitoryService.removeDormitoryById(id);
	}


}
