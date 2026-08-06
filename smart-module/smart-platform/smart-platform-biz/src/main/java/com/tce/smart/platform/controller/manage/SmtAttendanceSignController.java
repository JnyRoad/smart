package com.tce.smart.platform.controller.manage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.manage.AttendanceSignReqDTO;
import com.tce.smart.platform.api.dto.req.manage.QueryAttendanceSignReqDTO;
import com.tce.smart.platform.api.dto.resp.manage.AttendanceSignDetailRespDTO;
import com.tce.smart.platform.service.manage.SmtAttendanceSignService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;

import java.util.List;

/**
 * @author fushiping
 * @date 2020-07-27 10:45:43
 */
@RestController
@AllArgsConstructor
@Api(tags = "考勤签单")
@RequestMapping("/attendance/sign")
public class SmtAttendanceSignController extends BaseController {

	private final SmtAttendanceSignService smtAttendanceSignService;

	/**
	 * 提交签名
	 *
	 * @param reqDto 签名信息
	 * @return Result
	 */
	@PostMapping("/save")
	@ApiOperation("提交签名确认")
	public Result<Boolean> updateStatusById(@RequestBody AttendanceSignReqDTO reqDto) {
		return success(smtAttendanceSignService.updateToSign(reqDto));
	}

	/**
	 * 根据月份查询签收状态
	 * @return
	 */
	@GetMapping("/getStatus")
	@ApiOperation("根据月份查询签收状态")
	public Result getSignStatus(@RequestParam("checkDate") String checkDate) {
		return success(smtAttendanceSignService.getSignStatus(checkDate));
	}

	/**
	 * 计算当前登录人员有几次未签单数据
	 * @return
	 */
	@GetMapping("/not/count")
	@ApiOperation("未签单次数")
	public Result countNotSign() {
		return success(smtAttendanceSignService.countNotSign());
	}

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param reqDTO 工资签单
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperation("分页查询")
	public Result getSmtWageSignPage(Page page, QueryAttendanceSignReqDTO reqDTO) {
		return success(smtAttendanceSignService.getPage(page, reqDTO));
	}

	/**
	 * 通过id查询
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/{id}")
	@ApiOperation("通过id查询")
	public Result getById(@PathVariable("id") Long id) {
		return success(smtAttendanceSignService.getById(id), AttendanceSignDetailRespDTO.class);
	}

	/**
	 * 通过工号和月份查询
	 *
	 * @param checkDate id
	 * @return Result
	 */
	@GetMapping("/byBadge")
	@ApiOperation("通过工号和月份查询详情")
	public Result getByBadge(@RequestParam("checkDate") String checkDate) {
		String badge = SecurityUtils.getUser().getUsername();
		return success(smtAttendanceSignService.getByBadge(badge, checkDate));
	}


	@SysLog("每月同步员工定时任务")
	@ApiOperation("每月同步员工定时任务")
	@GetMapping("/sync/task")
	public Result syncStaff() {
		return success(smtAttendanceSignService.syncStaff());
	}


	@SysLog("短信提醒发送")
	@ApiOperation("短信提醒发送")
	@PostMapping("/msg")
	public Result msg(@RequestBody(required = false) QueryAttendanceSignReqDTO reqDTO) {
		return success(smtAttendanceSignService.sendMessage(reqDTO));
	}

	@SysLog("短信提醒条数")
	@ApiOperation("短信提醒条数")
	@PostMapping("/msg/count")
	public Result msgCount(@RequestBody(required = false) QueryAttendanceSignReqDTO reqDTO) {
		return success(smtAttendanceSignService.countMessage(reqDTO));
	}

}
