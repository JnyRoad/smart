package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.core.entity.SmtRecruitment;
import com.tce.smart.platform.service.SmtRecruitmentService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 招聘表
 *
 * @author 齐佩
 * @date 2019-04-13 18:18:34
 */
@RestController
@AllArgsConstructor
@RequestMapping("/recruitment")
public class SmtRecruitmentController extends BaseController {

	private final SmtRecruitmentService smtRecruitmentService;

	/**
	 * 分页查询
	 *
	 * @param page
	 *            分页对象
	 * @param smtRecruitment
	 *            招聘表
	 * @return
	 */
	@GetMapping("/page")
	public Result getRecruitmentPage(Page page, SmtRecruitment smtRecruitment) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return success(smtRecruitmentService.getPage(page, smtRecruitment, parkIds));
	}
	/**
	 * 分页查询
	 *公众号接口
	 * @param jobListAO
	 * @return
	 */
	@Inner
	@PostMapping("/job/list")
	public Result getJobList(@RequestBody SmtRecruitment smtRecruitment) {

		return success(smtRecruitmentService.getJobList(smtRecruitment.getParkId()));
	}


	/**
	 * 通过id查询招聘表
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@GetMapping("/getInfo/{id}")
	public Result getInfoById(@PathVariable("id") Integer id) {
		return success(smtRecruitmentService.getById(id));
	}
	/**
	 * 通过id查询招聘表
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@GetMapping("/{id}")
	public Result getRecruitmentById(@PathVariable("id") Integer id) {
		return success(smtRecruitmentService.getRecruitById(id));
	}

	/**
	 * 新增招聘表
	 *
	 * @param smtRecruitment
	 *            招聘表
	 * @return Result
	 */
	@SysLog("新增招聘表")
	@PostMapping("addRecruitment")
	public Result save(@RequestBody SmtRecruitment smtRecruitment) {
		return smtRecruitmentService.addRecruitment(smtRecruitment);
	}
	/**
	 *
	 *
	 * 获取职层列表
	 * @return
	 */
	@GetMapping("getJche")
	public Result getJche() {
		return smtRecruitmentService.getJche();
	}


	/**
	 * 获取bu
	 * @return
	 */
	@GetMapping("/getComp")
	public Result getComp() {
		return smtRecruitmentService.getComp();
	}



	/**
	 * 获取dep
	 * @return
	 */
	@GetMapping("/getDep/{compId}")
	public Result getDep( @PathVariable Integer compId) {
		return smtRecruitmentService.getDep(compId);
	}


	/**
	 * 获取job
	 * @return
	 */
	@GetMapping("/getJob/{depId}")
	public Result getJob( @PathVariable Integer depId) {
		return smtRecruitmentService.getJob(depId);
	}



	/**
	 * 获取job详情
	 * @return
	 */
	@GetMapping("/getJobInfo/{jobId}")
	public Result getJobInfo( @PathVariable Integer jobId) {
		return smtRecruitmentService.getJobInfo(jobId);
	}

	/**
	 * 修改招聘表
	 *
	 * @param smtRecruitment
	 *            招聘表
	 * @return Result
	 */
	@SysLog("修改招聘表")
	@PostMapping("updateRecruitment")
	public Result updateRecruitmentById(@RequestBody SmtRecruitment smtRecruitment) {
		return smtRecruitmentService.updateRecruitmentById(smtRecruitment);
	}


	@SysLog("招聘岗位置顶")
	@GetMapping("updateIsUp")
	public Result updateIsUp(SmtRecruitment smtRecruitment) {
		return smtRecruitmentService.updateIsUp(smtRecruitment);
	}


	/**
	 * 刷新招聘表时间
	 * @param smtRecruitment
	 * @return
	 */
	@SysLog("刷新招聘表时间")
	@GetMapping("refreshRecruitment")
	public Result refreshRecruitmentById() {
		return smtRecruitmentService.refreshRecruitmentById();
	}


	@SysLog("更新组织信息")
	@GetMapping("refreshComp")
	public void refreshComp() {
	  smtRecruitmentService.refreshComp();
	}

	/**
	 * 通过id删除招聘表
	 *
	 * @param id
	 *            id
	 * @return Result
	 */
	@SysLog("删除招聘表")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable Integer id) {
		return smtRecruitmentService.removeRecruitmentById(id);
	}



	@Inner
	@SysLog("app接口，分页查询招聘岗位列表")
	@GetMapping("/app/page")
	public Result getSmtRecruitmentPage(Page page, SmtRecruitment smtRecruitment) {
		return success(smtRecruitmentService.getPage(page, smtRecruitment, null));
	}


	@Inner
	@SysLog("app接口，查询招聘岗位详情")
	@GetMapping("/app/{id}")
	public Result getById(@PathVariable("id") Integer id) {
		return success(smtRecruitmentService.getRecruitById(id));
	}

	@Inner
	@SysLog("app接口，修改招聘表")
	@PostMapping("/app/updateRecruitment")
	public Result updateById(@RequestBody SmtRecruitment smtRecruitment) {
		return smtRecruitmentService.updateRecruitmentById(smtRecruitment);
	}



}
