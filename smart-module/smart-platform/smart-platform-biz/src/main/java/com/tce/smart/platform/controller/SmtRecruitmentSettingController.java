package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.ao.RecruitSetSaveAO;
import com.tce.smart.platform.core.ao.RecruitSetSearchAO;
import com.tce.smart.platform.core.entity.SmtRecruitmentSetting;
import com.tce.smart.platform.core.vo.RecruitSetCompListVO;
import com.tce.smart.platform.core.vo.RecruitSetListVO;
import com.tce.smart.platform.core.vo.RecruitSetWorkBaseListVO;
import com.tce.smart.platform.service.SmtRecruitmentSettingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.tce.smart.common.core.model.Result.success;


/**
 * 招聘设置控制器
 *
 * @author mckaywu
 * @date 2019-11-20 10:35:16
 */
@RestController
@AllArgsConstructor
@RequestMapping("/recruitmentsetting")
@Api(tags = "招聘设置")
public class SmtRecruitmentSettingController {

	private final SmtRecruitmentSettingService recruitmentSettingService;

	/**
	 * 分页查询
	 *
	 * @param page                  分页对象
	 * @param smtRecruitmentSetting 招聘设置表
	 * @return
	 */
	@GetMapping("/page")
	@ApiOperation("获取招聘设置列表")
	public Result<Page<SmtRecruitmentSetting>> getSmtRecruitmentSettingPage(Page page, SmtRecruitmentSetting smtRecruitmentSetting) {
		return success(recruitmentSettingService.page(page, Wrappers.query(smtRecruitmentSetting)));
	}

	/**
	 * 分页查询
	 *
	 * @param
	 * @return
	 */
	@GetMapping("/list")
	@ApiOperation("获取招聘设置列表")
	public Result<List<SmtRecruitmentSetting>> getSmtRecruitmentSettingList(SmtRecruitmentSetting smtRecruitmentSetting) {
		RecruitSetListVO vo = recruitmentSettingService.listRecruit(smtRecruitmentSetting.getParkId(),smtRecruitmentSetting.getWorkCompId());
		if(Objects.nonNull(vo)) {
			List<SmtRecruitmentSetting> list = vo.getCompOrgList();
			List<SmtRecruitmentSetting> settings = list.stream().collect(Collectors.collectingAndThen(
					Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(SmtRecruitmentSetting::getWorkBaseCode))), ArrayList::new));
			return success(settings);
		}
		return null;
	}

	/**
	 * 获取招聘设置信息
	 * @param parkId 园区ID
	 * @return
	 */
	@GetMapping("/list/{parkId}")
	@ApiOperation("获取招聘设置列表")
	public Result<RecruitSetListVO> listRecruit(@PathVariable("parkId") Integer parkId) {
		return success(recruitmentSettingService.listRecruit(parkId, null));
	}

	/**
	 * 通过id查询招聘设置
	 *
	 * @param id id
	 * @return Result
	 */
	@GetMapping("/{id}")
	@ApiOperation("通过id查询招聘设置")
	public Result<SmtRecruitmentSetting> getById(@PathVariable("id") Integer id) {
		return success(recruitmentSettingService.getById(id));
	}

	/**
	 * 批量新增招聘设置
	 *
	 * @param recruitSetSaveAO 批量招聘设置表
	 * @return Result
	 */
	@SysLog("批量保存招聘设置")
	@PostMapping("/save")
	@ApiOperation("批量保存招聘设置")
	public Result<Boolean> batchSaveRecruit(@Validated @RequestBody RecruitSetSaveAO recruitSetSaveAO) {
		return success(recruitmentSettingService.batchSaveRecruit(recruitSetSaveAO));
	}

	/**
	 * 修改招聘设置表
	 *
	 * @param smtRecruitmentSetting 招聘设置表
	 * @return Result
	 */
	@SysLog("修改招聘设置")
	@PostMapping("/update")
	@ApiOperation("修改招聘设置")
	public Result<Boolean> updateById(@RequestBody SmtRecruitmentSetting smtRecruitmentSetting) {
		return success(recruitmentSettingService.updateById(smtRecruitmentSetting));
	}

	/**
	 * 通过id删除招聘设置
	 *
	 * @param id id
	 * @return Result
	 */
	@SysLog("删除招聘设置")
	@PostMapping("/{id}")
	@ApiOperation("删除招聘设置")
	public Result<Boolean> removeById(@PathVariable Integer id) {
		return success(recruitmentSettingService.removeById(id));
	}

	/**
	 * 获取工作地点列表
	 *
	 * @param recruitSetSearchAO 工作地点关键字
	 * @return Result
	 */
	@SysLog("获取工作地点列表")
	@PostMapping("/list/workbase")
	@ApiOperation("获取工作地点列表")
	public Result<List<RecruitSetWorkBaseListVO>> getWorkBaseCodeList(@RequestBody RecruitSetSearchAO recruitSetSearchAO) {
		return success(recruitmentSettingService.getWorkBaseCodeList(recruitSetSearchAO.getWorkBaseName()));
	}

	/**
	 * 获取可签约单位列表
	 *
	 * @param recruitSetSearchAO 标题关键字
	 * @return Result
	 */
	@SysLog("获取可签约单位列表")
	@PostMapping("/list/concomany")
	@ApiOperation("获取可签约单位列表")
	public Result<List<RecruitSetCompListVO>> getListByTitle(@RequestBody RecruitSetSearchAO recruitSetSearchAO) {
		return success(recruitmentSettingService.getListByTitle(recruitSetSearchAO.getWorkOrgName()));
	}

	/**
	 * 获取可签约BU列表
	 *
	 * @param recruitSetSearchAO 标题关键字
	 * @return Result
	 */
	@SysLog("获取可签约Bu列表")
	@PostMapping("/list/comp")
	@ApiOperation("获取可签约Bu列表")
	public Result<List<RecruitSetCompListVO>> getListByCompAbbr(@RequestBody RecruitSetSearchAO recruitSetSearchAO) {
		return success(recruitmentSettingService.getCompeList(recruitSetSearchAO.getCompTitle()));
	}
}
