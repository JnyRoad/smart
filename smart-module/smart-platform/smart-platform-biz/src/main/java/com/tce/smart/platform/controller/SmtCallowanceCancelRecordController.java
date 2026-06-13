package com.tce.smart.platform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.service.SmtCallowanceCancelRecordService;
import lombok.AllArgsConstructor;

/**
 * 员工外宿补贴撤销表
 *
 * @author 齐佩
 * @date 2019-12-10 16:14:20
 */
@RestController
@AllArgsConstructor
@RequestMapping("/callowance/cancel")
public class SmtCallowanceCancelRecordController {
	private final SmtCallowanceCancelRecordService  cancelRecordService;

	/**
	 * 员工外宿补贴撤销录入
	 * @param badge 员工号
	 * @return
	 */
	@SysLog("员工外宿补贴撤销录入")
	@GetMapping("/add")
	public Result save(@RequestParam("badge") String badge,@RequestParam("backDate") String backDate, @RequestParam("type") Integer type) {
		return cancelRecordService.save(badge,backDate, type);
	}

	@SysLog("员工外宿补贴撤销详情查看")
	@GetMapping("/get/detail")
	public Result detail(@RequestParam("id") Integer id) {
		return cancelRecordService.get(id);
	}

	@SysLog("员工外宿补贴撤销查看")
	@GetMapping("/get")
	public Result get(@RequestParam("badge") String badge) {
		return cancelRecordService.getInfo(badge);
	}

	@SysLog("查询外宿补贴信息")
	@GetMapping("/get/out/dormitory")
	public Result getOutDormitory(@RequestParam("badge") String badge, @RequestParam("type") Integer type) {
		return cancelRecordService.getOutDormitory(badge, type);
	}

	@SysLog("查询补贴详情")
	@GetMapping("/get/callowance/detail")
	public Result getCallowanceDetail(@RequestParam("badge") String badge, @RequestParam("type") Integer type ) {
		return cancelRecordService.getCallowanceDetail(badge, type);
	}
}
