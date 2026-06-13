package com.tce.smart.platform.controller;

import com.tce.smart.platform.api.dto.resp.DormitoryTypeRespDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.DormitoryTypeDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryType;
import com.tce.smart.platform.service.SmtDormitoryTypeService;

import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 园区宿舍类型
 *
 * @author 齐佩
 * @date 2019-04-13 18:16:57
 */
@Api(tags = "宿舍类型管理")
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/type")
public class SmtDormitoryTypeController {

	private final SmtDormitoryTypeService smtDormitoryTypeService;

	/**
	 * 分页查询
	 *
	 * @param page
	 *            分页对象
	 * @param smtDormitoryType
	 *            园区宿舍类型
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtDormitoryTypePage(Page page, SmtDormitoryType smtDormitoryType) {
		return new Result<>(smtDormitoryTypeService.getSmtDormitoryTypePage(page,smtDormitoryType ));
	}

	@GetMapping("/all")
	public Result getSmtDormitoryTypeAll() {
		return smtDormitoryTypeService.getSmtDormitoryTypeAll( );
	}

	/**
	 * 通过园区ID 查询宿舍分类信息
	 * @param parkId
	 * @return
	 */
	@ApiOperation("通过园区ID查询宿舍分类信息")
	@GetMapping("/by/park")
	public Result<List<DormitoryTypeRespDTO>> getSmtDormitoryTypeByPark(@ApiParam(name = "parkId",value = "园区ID")@RequestParam(value = "parkId", required = false) Integer parkId) {
		return new Result<>(smtDormitoryTypeService.getSmtDormitoryTypeByPark(parkId));
	}

	/**
	 * 通过园区ID和宿舍ID 查询宿舍分类信息
	 * @param parkId
	 * @return
	 */
	@ApiOperation("通过园区ID和宿舍ID查询宿舍分类信息")
	@GetMapping("/by/park-and-dormitory")
	public Result<List<DormitoryTypeRespDTO>> getSmtDormitoryTypeByParkAndDormitory(@ApiParam(name = "parkId",value = "园区ID")@RequestParam(value = "parkId") Integer parkId,
																					@ApiParam(name = "dormitoryId",value = "宿舍ID")@RequestParam(value = "dormitoryId") Integer dormitoryId) {
		return new Result<>(smtDormitoryTypeService.getSmtDormitoryTypeByParkAndDormitory(parkId,dormitoryId));
	}


	/**
	 * 通过id查询园区宿舍类型
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return new Result<>(smtDormitoryTypeService.getById(id));
	}

	/**
	 * 新增园区宿舍类型
	 *
	 * @param smtDormitoryType
	 *            园区宿舍类型
	 * @return Result
	 */
	@SysLog("新增园区宿舍类型")
	@PostMapping("/addDormitoryType")
	public Result save(@RequestBody DormitoryTypeDTO smtDormitoryType) {
		return smtDormitoryTypeService.addDormitoryType(smtDormitoryType);
	}

	/**
	 * 修改园区宿舍类型
	 *
	 * @param smtDormitoryType
	 *            园区宿舍类型
	 * @return Result
	 */
	@SysLog("修改园区宿舍类型")
	@PostMapping("/updateDormitoryType")
	public Result updateById(@RequestBody DormitoryTypeDTO smtDormitoryType) {
		return smtDormitoryTypeService.updateDormitoryTypeById(smtDormitoryType);
	}

	/**
	 * 通过id删除园区宿舍类型
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@SysLog("删除园区宿舍类型")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable Integer id) {
		return smtDormitoryTypeService.removeDormitoryTypeById(id);
	}



}
