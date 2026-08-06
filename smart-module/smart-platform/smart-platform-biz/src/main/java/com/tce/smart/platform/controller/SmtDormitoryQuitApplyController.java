package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.platform.api.dto.req.*;
import com.tce.smart.platform.api.dto.resp.*;
import com.tce.smart.platform.service.SmtDormitoryQuitApplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 退宿申请与审批
 *
 * @Auther: fushiping
 * @Date: 2020-07-23 16:56
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dor/quit")
@Api(value = "退宿申请与审批", tags = "退宿申请与审批")
public class SmtDormitoryQuitApplyController extends BaseController {

	private final SmtDormitoryQuitApplyService smtDormitoryQuitApplyService;

	@GetMapping("/page")
	@ApiOperation(value = "退宿申请分页查询")
	public Result<IPage<DormitoryQuitApplyRespDTO>> getPage(Page page, DormitoryQuitApplyQueryDTO reqDTO) {
		return success(smtDormitoryQuitApplyService.getPage(page, reqDTO), DormitoryQuitApplyRespDTO.class);
	}


	@PostMapping("/list/approval")
	@ApiOperation(value = "查询审批列表")
	public Result<IPage<DormitoryQuitApplyRespDTO>> getApprovalList(Page page,@RequestBody QuitDorApplyQueryReqDTO query) {
		return success(smtDormitoryQuitApplyService.getApprovalList(page, query), DormitoryQuitApplyRespDTO.class);
	}

	@GetMapping("/list/check")
	@ApiOperation(value = "查询确认列表")
	public Result<IPage<DormitoryQuitApplyRespDTO>> getCheckList(Page page, @RequestParam("parkId") Integer parkId) {
		return success(smtDormitoryQuitApplyService.getCheckList(page, parkId), DormitoryQuitApplyRespDTO.class);
	}


	@GetMapping("/dealy/quit")
	@ApiOperation(value = "延迟退宿")
	public Result<Boolean> dealyQuit() {
		return success(smtDormitoryQuitApplyService.dealyQuit());
	}

	@GetMapping("/list/check/{smsCode}")
	@ApiOperation(value = "根据二维码查询确认列表")
	public Result<DormitoryQuitApplyDetailRespDTO> getCheckByCode(@PathVariable("smsCode") String smsCode) {
		return success(smtDormitoryQuitApplyService.getCheckByCode(smsCode), DormitoryQuitApplyDetailRespDTO.class);
	}


	@Inner
	@GetMapping("/detail/{id}")
	@ApiOperation(value = "根据ID查询申请详情")
	public Result<DormitoryQuitApplyDetailRespDTO> getById(@PathVariable("id") String id) {
		return success(smtDormitoryQuitApplyService.getById(Long.parseLong(id)), DormitoryQuitApplyDetailRespDTO.class);
	}

	@Inner
	@GetMapping("/status/update")
	@ApiOperation(value = "审批状态更改")
	public Result<Boolean> status(@RequestParam("id") Long id, @RequestParam("approveBadge") String approveBadge,
								  @RequestParam("status") Integer status, @RequestParam(value = "remark", required = false) String remark) {
		return success(smtDormitoryQuitApplyService.status(id, approveBadge, status, remark));
	}

	/**
	 * 退宿申请
	 * @param reqDTO
	 * @return
	 */
	@PostMapping("/apply")
	@ApiOperation(value = "退宿申请")
	public Result<Boolean> quitDroApply(@RequestBody DormitoryQuitApplyEditReqDTO reqDTO) {
		return success(smtDormitoryQuitApplyService.saveApply(reqDTO));
	}

}
