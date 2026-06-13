package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.entity.SmtNoticeSwitch;
import com.tce.smart.platform.service.SmtNoticeSwitchService;
import com.tce.smart.tool.enums.ParkNoticeTypeEnum;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.tce.smart.common.core.model.Result.success;


/**
 * 园区通知控制开关
 *
 * @author mckaywu
 * @date 2019-11-20 10:37:43
 */
@RestController
@AllArgsConstructor
@RequestMapping("/parknoticeswitch")
public class SmtNoticeSwitchController {

	private final SmtNoticeSwitchService smtNoticeSwitchService;

	/**
	 * 分页查询
	 *
	 * @param page            分页对象
	 * @param smtNoticeSwitch 园区通知控制开关
	 * @return
	 */
	@GetMapping("/page")
	public Result<SmtNoticeSwitch> getSmtNoticeSwitchPage(Page page, SmtNoticeSwitch smtNoticeSwitch) {
		return success(smtNoticeSwitchService.page(page, Wrappers.query(smtNoticeSwitch)));
	}

	/**
	 * 获取指定园区开关
	 *
	 * @param parkId 园区ID
	 * @return 开关列表
	 */
	@GetMapping("/list/switch/{parkId}")
	public Result<List<SmtNoticeSwitch>> getParkSwitch(@PathVariable("parkId") Integer parkId) {
		return success(smtNoticeSwitchService.listInitSwitch(parkId));
	}

	/**
	 * 获取所有开关种类
	 *
	 * @return 开关集合
	 */
	@GetMapping("/list/switch/type")
	public Result getAllSwitchType() {
		return success(ParkNoticeTypeEnum.list());
	}

	/**
	 * 通过id查询园区通知控制开关
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result<Boolean> getById(@PathVariable("id") Integer id) {
		return success(smtNoticeSwitchService.getById(id));
	}

	/**
	 * 新增园区通知控制开关
	 *
	 * @param smtNoticeSwitch 园区通知控制开关
	 * @return Result
	 */
	@SysLog("新增园区通知控制开关")
	@PostMapping("/save")
	public Result<Boolean> save(@RequestBody SmtNoticeSwitch smtNoticeSwitch) {
		return success(smtNoticeSwitchService.save(smtNoticeSwitch));
	}

	/**
	 * 批量新增或修改开关
	 *
	 * @param switchList 园区通知控制开关
	 * @return Result
	 */
	@SysLog("批量新增或修改开关")
	@PostMapping("/batchSave/{parkId}")
	public Result<Boolean> batchSave(@PathVariable Integer parkId, @RequestBody List<SmtNoticeSwitch> switchList) {
		return success(smtNoticeSwitchService.batchSave(parkId, switchList));
	}

	/**
	 * 修改园区通知控制开关
	 *
	 * @param smtNoticeSwitch 园区通知控制开关
	 * @return Result
	 */
	@SysLog("修改园区通知控制开关")
	@PostMapping("/update")
	public Result<Boolean> updateById(@RequestBody SmtNoticeSwitch smtNoticeSwitch) {
		return success(smtNoticeSwitchService.updateById(smtNoticeSwitch));
	}

	/**
	 * 通过id删除园区通知控制开关
	 *
	 * @param id id
	 * @return Result
	 */
	@SysLog("删除园区通知控制开关")
	@PostMapping("/{id}")
	public Result<Boolean> removeById(@PathVariable Integer id) {
		return success(smtNoticeSwitchService.removeById(id));
	}
}
