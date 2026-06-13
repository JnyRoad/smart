package com.tce.smart.platform.controller;

import javax.validation.Valid;

import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.dto.LogisticsAppointmentDTO;
import com.tce.smart.platform.core.vo.LogisticsAppointmentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtLogisticsAppointment;
import com.tce.smart.platform.service.SmtLogisticsAppointmentService;

import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 物流车预约信息表
 *
 * @author 王艳勇
 * @date 2019-04-15 11:33:27
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/logistics/appointment")
public class SmtLogisticsAppointmentController extends BaseController {

	private final SmtLogisticsAppointmentService smtLogisticsAppointmentService;

	/**
	 * 分页查询,同时获取统计数据
	 *
	 * @param page
	 *            分页对象
	 * @param smtLogisticsAppointment
	 *            物流车预约信息表
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtLogisticsAppointmentPage(Page page, SmtLogisticsAppointment smtLogisticsAppointment) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return new Result<>(smtLogisticsAppointmentService.getLogisticsAppointment(page, smtLogisticsAppointment,parkIds));
	}

	/**
	 * 通过id查询物流车预约信息表
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getById(@PathVariable("id") Long id) {
		SmtLogisticsAppointment smtLogisticsAppointment = smtLogisticsAppointmentService.getById(id);
		return success(smtLogisticsAppointment, LogisticsAppointmentVO.class);
	}

	/**
	 * 新增物流车预约信息表
	 *
	 * @param logisticsAppointmentDTO
	 *            物流车预约信息表
	 * @return Result
	 */
	@SysLog("新增物流车预约信息表")
	@PostMapping("/save")
	public Result save(@Valid @RequestBody LogisticsAppointmentDTO logisticsAppointmentDTO) {
		return new Result<>(smtLogisticsAppointmentService.saveLogisticsAppointment(logisticsAppointmentDTO));
	}

	/**
	 * 修改物流车预约信息表
	 *
	 * @param smtLogisticsAppointment
	 *            物流车预约信息表
	 * @return Result
	 */
	@SysLog("修改物流车预约信息表")
	@PostMapping("/update")
	public Result updateById(@RequestBody SmtLogisticsAppointment smtLogisticsAppointment) {
		return new Result<>(smtLogisticsAppointmentService.updateById(smtLogisticsAppointment));
	}

	/**
	 * 手动进厂
	 *
	 * @param id
	 *            物流车预约信息
	 * @return
	 */
	@GetMapping("/manualEnter/{id}")
	public Result manualEnter(@PathVariable("id") Long id) {
		return new Result<>(smtLogisticsAppointmentService.manualEnter(id));
	}

	/**
	 * 返回预约
	 *
	 * @param id
	 *            物流车预约信息
	 * @return
	 */
	@GetMapping("/goOrder/{id}")
	public Result goOrder(@PathVariable("id") Long id) {
		return new Result<>(smtLogisticsAppointmentService.goOrder(id));
	}

	/**
	 * 手动离厂
	 *
	 * @param id
	 *            物流车预约信息
	 * @return
	 */
	@GetMapping("/manualLeave/{id}")
	public Result manualLeave(@PathVariable("id") Long id) {
		return new Result<>(smtLogisticsAppointmentService.manualLeave(id));
	}

	/**
	 * 取消预约
	 *
	 * @param id
	 *            物流车预约信息
	 * @return
	 */
	@GetMapping("/cancelOrder/{id}")
	public Result cancelOrder(@PathVariable("id") Long id) {
		return new Result<>(smtLogisticsAppointmentService.cancelOrder(id));
	}

	/**
	 * 返回在厂
	 *
	 * @param id
	 *            物流车预约信息
	 * @return
	 */
	@GetMapping("/goIn/{id}")
	public Result goIn(@PathVariable("id") Long id) {
		return new Result<>(smtLogisticsAppointmentService.goIn(id));
	}

	/**
	 * 通过id删除物流车预约信息表
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@SysLog("删除物流车预约信息表")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable Long id) {
		return new Result<>(smtLogisticsAppointmentService.removeById(id));
	}

	/**
	 * 更新超时状态
	 * @return 返回结果
	 */
	@SysLog("更新物流车超时信息表")
	@GetMapping("/update/status")
	public Result updateStatus() {
		return new Result<>(smtLogisticsAppointmentService.updateStatus());
	}
}
