package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.platform.service.VisitorTaskService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 访客任务
 *
 * @author WangJinbo
 * @date 2019-05-19 02:35:58
 */
@RestController
@AllArgsConstructor
@RequestMapping("/visitorTask")
public class VisitorTaskController extends BaseController {
	private final VisitorTaskService visitorTaskService;
	/**
	 * 已经到达的访客删除闸机
	 * @return
	 */
	@Inner
	@GetMapping("/comeOnTime")
	public Result visitorComeOnTime() {
		visitorTaskService.visitorComeOnTime();
		return success();
	}
	/**
	 * @param
	 * @param
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/overTime")
	public Result visitorOverTime(@RequestParam("parkId") Integer parkId) {
		visitorTaskService.visitorOverTime(parkId);
		return success();
	}
	/**
	 * 超时未离开
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/overTimeNoLeave")
	public Result overTimeNoLeave(@RequestParam("parkId") Integer parkId) {
		visitorTaskService.visitorOverTimeNoLeave(parkId);
		return success();
	}
	/**
	 * @param
	 * @param
	 * @return
	 */
	@Inner
	@OpenApi("server")
	@GetMapping("/remind")
	public Result visitorRemind(@RequestParam("parkId") Integer parkId) {
		visitorTaskService.visitorRemind(parkId);
		return success();

	}

	@Inner
	@OpenApi("server")
	@GetMapping("/toEmail")
	public Result toEmail() {
		visitorTaskService.toEmail();
		return success();
	}

}
