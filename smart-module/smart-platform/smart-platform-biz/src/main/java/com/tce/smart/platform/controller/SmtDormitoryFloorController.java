package com.tce.smart.platform.controller;

import com.tce.smart.platform.api.dto.req.DormitoryFloorReqDTO;
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
import com.tce.smart.platform.core.dto.DormitoryFloorDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryFloor;
import com.tce.smart.platform.service.SmtDormitoryFloorService;

import lombok.AllArgsConstructor;

/**
 * 园区宿舍楼的楼层
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:15
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/floor")
public class SmtDormitoryFloorController {

	private final SmtDormitoryFloorService smtDormitoryFloorService;

	/**
	 * 分页查询
	 *
	 * @param page
	 *            分页对象
	 * @param smtDormitoryFloor
	 *            园区宿舍楼的楼层
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtDormitoryFloorPage(Page page, SmtDormitoryFloor smtDormitoryFloor) {
		return smtDormitoryFloorService.getSmtDormitoryFloorPage(page, smtDormitoryFloor);
	}

	@PostMapping("/queryFloor")
	public Result queryFloor(@RequestBody DormitoryFloorReqDTO smtDormitoryFloor) {
		return smtDormitoryFloorService.queryFloor(smtDormitoryFloor);
	}

	/**
	 * 通过id查询园区宿舍楼的楼层
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return new Result<>(smtDormitoryFloorService.getById(id));
	}

	@GetMapping("/getFloorStartNum/{dormitoryId}")
	public Result getFloorStartNum(@PathVariable("dormitoryId") Integer dormitoryId) {
		return smtDormitoryFloorService.getFloorStartNum(dormitoryId);
	}


	/**
	 * 新增园区宿舍楼的楼层
	 *
	 * @param smtDormitoryFloor
	 *            园区宿舍楼的楼层
	 * @return Result
	 */
	@SysLog("新增园区宿舍楼的楼层")
	@PostMapping("addFloor")
	public Result save(@RequestBody DormitoryFloorDTO dormitoryFloorDTO) {
		return smtDormitoryFloorService.addFloor(dormitoryFloorDTO);
	}

	/**
	 * 修改园区宿舍楼的楼层
	 *
	 * @param smtDormitoryFloor
	 *            园区宿舍楼的楼层
	 * @return Result
	 */
	@SysLog("修改园区宿舍楼的楼层")
	@PostMapping("updateDormitoryFloor")
	public Result updateById(@RequestBody SmtDormitoryFloor smtDormitoryFloor) {
		return smtDormitoryFloorService.updateDormitoryFloorById(smtDormitoryFloor);
	}

	/**
	 * 通过id删除园区宿舍楼的楼层
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@SysLog("删除园区宿舍楼的楼层")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable Integer id) {
		return smtDormitoryFloorService.removeFloorById(id);
	}

}
