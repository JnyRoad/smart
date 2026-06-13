package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.AppealReplyReqDTO;
import com.tce.smart.platform.api.dto.req.SmtDormitoryRepairsReqDTO;
import com.tce.smart.platform.api.dto.req.SmtStaffAppealReqDTO;
import com.tce.smart.platform.api.dto.resp.SmtStaffAppealListVO;
import com.tce.smart.platform.api.dto.resp.SmtStaffAppealQueryVO;
import com.tce.smart.platform.core.dto.StaffAppealSearchDTO;
import com.tce.smart.platform.core.entity.SmtSecurityAreaSupplier;
import com.tce.smart.platform.core.entity.SmtStaffAppeal;
import com.tce.smart.platform.core.vo.SmtStaffAppealVO;
import com.tce.smart.platform.service.SmtStaffAppealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @description: SmtStaffAppealController
 * @date: 2020-07-23 14:07
 * @author: wuling
 * @version: 1.0
 */
@RestController
@AllArgsConstructor
@Api(tags = "员工申诉")
@RequestMapping("/staff/appeal")
public class SmtStaffAppealController {

	private final SmtStaffAppealService smtStaffAppealService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param staffAppealSearchDTO 员工申诉查询数据
	 * @return
	 */
	@ApiIgnore
	@ApiOperation("分页查询记录-管理后台使用")
	@GetMapping("/page")
	public Result getStaffAppealPage(Page page, StaffAppealSearchDTO staffAppealSearchDTO) {
		return new Result<>(smtStaffAppealService.getStaffAppealPage(page, staffAppealSearchDTO));
	}

	/**
	 * 分页查询指定员工申诉记录
	 * @param page 分页对象
	 * @param staffId 员工标识
	 * @return
	 */
	@ApiOperation("分页查询员工申诉记录")
	@GetMapping("/record")
	public Result<IPage<SmtStaffAppealListVO>> getStaffAppealRecord(@ApiParam(name = "current",value = "当前页",required = true) @RequestParam long current,
																	@ApiParam(name = "size",value = "大小",required = true) @RequestParam long size) {

		return new Result<>(smtStaffAppealService.getStaffAppealRecord(new Page(current,size)));
	}

	/**
	 * 保存员工申诉记录
	 * @param smtStaffAppealReqDTO 员工申诉信息
	 * @return Result
	 */
	@SysLog("保存员工申诉记录")
	@ApiOperation("添加员工申诉")
	@PostMapping("/save")
	public Result<Boolean> saveStaffAppealRecord(@RequestBody SmtStaffAppealReqDTO smtStaffAppealReqDTO){
		return new Result<>(smtStaffAppealService.saveStaffAppealRecord(smtStaffAppealReqDTO));
	}

	/**
	 * 获取申诉详情
	 * @param id 记录Id
	 * @return Result
	 */
	@ApiOperation("获取申诉详情")
	@GetMapping("/detail/{id}")
	public Result<SmtStaffAppealQueryVO> getAppealDetail(@PathVariable("id") Long id){
		return new Result<>(smtStaffAppealService.getAppealDetail(id));
	}

	/**
	 * 转交审批人员
	 * @param id 申诉记录标识
	 * @param changeBadge 选择的员工工号
	 * @return Result
	 */
	@ApiIgnore
	@SysLog("转交审批人员")
	@ApiOperation("转交审批人员")
	@PostMapping("/changeApprove")
	public Result changeApprove(@RequestParam Long id,@RequestParam String changeBadge){
		return new Result<>(smtStaffAppealService.AddApproveList(id,changeBadge));
	}

	/**
	 * 保存回复信息
	 * @param appealReplyReqDTO 回复内容
	 * @return Result
	 */
	@SysLog("保存回复信息")
	@ApiOperation("回复申诉")
	@PostMapping("/saveReply")
	public Result<Boolean> saveReply(@RequestBody AppealReplyReqDTO appealReplyReqDTO){
		return new Result<>(smtStaffAppealService.saveReplyDesc(appealReplyReqDTO));
	}
}
