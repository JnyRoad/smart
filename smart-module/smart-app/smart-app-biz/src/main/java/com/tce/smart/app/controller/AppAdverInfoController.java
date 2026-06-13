package com.tce.smart.app.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.emun.AdverPositionEnum;
import com.tce.smart.app.entity.AppAdverInfo;
import com.tce.smart.app.service.AppAdverInfoService;
import com.tce.smart.app.vo.AppAdverInfoListVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/adverinfo")
public class AppAdverInfoController extends BaseController {

	private final AppAdverInfoService appAdverInfoService;

	/**
	 * 分页查询
	 *
	 * @param page         分页对象
	 * @param appAdverInfo 查询条件
	 * @return
	 */
	@GetMapping("/page")
	public Result getAppAdverPage(Page page, AppAdverInfo appAdverInfo) {
		return success(appAdverInfoService.listByPage(page, appAdverInfo), AppAdverInfoListVo.class);
	}

	/**
	 * 通过id查询
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result<AppAdverInfo> getById(@PathVariable("id") Integer id) {
		return success(appAdverInfoService.getById(id));
	}

	/**
	 * 新增
	 *
	 * @param saveAo 新增信息
	 * @return Result
	 */
	@SysLog("新增App广告")
	@PostMapping("/save")
	public Result<Boolean> save(@RequestBody AppAdverInfo saveAo) {
		return success(appAdverInfoService.saveAdver(saveAo));
	}

	/**
	 * 修改主题分类
	 *
	 * @param saveAo 修改信息
	 * @return Result
	 */
	@SysLog("修改App广告")
	@PostMapping("/update")
	public Result<Boolean> updateById(@RequestBody AppAdverInfo saveAo) {
		return success(appAdverInfoService.updateAdverById(saveAo));
	}

	/**
	 * 发布App广告
	 *
	 * @param id 修改ID
	 * @return Result
	 */
	@SysLog("发布App广告")
	@PostMapping("/publish/{id}")
	public Result<Boolean> publish(@PathVariable("id") Integer id) {
		return success(appAdverInfoService.publish(id));
	}

	/**
	 * 取消发布App广告
	 *
	 * @param id 修改ID
	 * @return Result
	 */
	@SysLog("取消发布App广告")
	@PostMapping("/publish/cancel/{id}")
	public Result unPublish(@PathVariable("id") Integer id) {
		return success(appAdverInfoService.unpPublish(id));
	}

	/**
	 * 通过id删除(逻辑删除)
	 *
	 * @param id id
	 * @return Result
	 */
	@SysLog("删除App广告")
	@PostMapping("/{id}")
	public Result<Boolean> removeById(@PathVariable("id") Integer id) {
		return success(appAdverInfoService.deleteAdver(id));
	}

	/**
	 * 通过id查询
	 *
	 * @return Result
	 */
	@GetMapping("/position/list")
	public Result<List<Map<String,Object>>> listPosition() {
		return success(AdverPositionEnum.list());
	}
}
