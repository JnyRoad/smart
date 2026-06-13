package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.dto.TaskDownRecordDTO;
import com.tce.smart.platform.core.service.SmtTaskDownRecordService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 警报推送人信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:34:43
 */
@RestController
@AllArgsConstructor
@RequestMapping("/task/down")
public class SmtTaskDownRecordController {

	private final SmtTaskDownRecordService smtTaskDownRecordService;

	/**
	 * 分页查询
	 *
	 * @param page              分页对象
	 * @param taskDownRecordDTO 车辆参数查询
	 * @return return
	 */
	@GetMapping("/vehicle/page")
	public Result getVehicle(Page page, TaskDownRecordDTO taskDownRecordDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		taskDownRecordDTO.setParkIds(parkIds);
		return new Result<>(smtTaskDownRecordService.getVehicle(page, taskDownRecordDTO));
	}

	/**
	 * 分页查询
	 *
	 * @param page              分页对象
	 * @param taskDownRecordDTO 车辆参数查询
	 * @return return
	 */
	@GetMapping("/person/page")
	public Result getPerson(Page page, TaskDownRecordDTO taskDownRecordDTO) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		taskDownRecordDTO.setParkIds(parkIds);
		return new Result<>(smtTaskDownRecordService.getPerson(page, taskDownRecordDTO));
	}

	@GetMapping("/park/{type}")
	public Result getPark(@PathVariable("type") Integer type) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return new Result<>(smtTaskDownRecordService.getTree(parkIds, type));
	}

}
