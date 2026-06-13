package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.TempStaffEditReqDTO;
import com.tce.smart.platform.api.dto.req.outsrcapply.SmtOutSrcApplyReqDTO;
import com.tce.smart.platform.api.dto.req.outsrcapply.SmtOutSrcApplyUpdDTO;
import com.tce.smart.platform.api.dto.resp.outsrcapply.SmtOutSrcApplyDetailListDTO;
import com.tce.smart.platform.api.dto.resp.outsrcapply.SmtOutSrcApplyDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.outsrcapply.SmtOutSrcApplyRespDTO;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import com.tce.smart.platform.service.SmtOutSrcApplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/3 10:31
 */
@RestController
@AllArgsConstructor
@RequestMapping("/out/src/apply")
@Api(tags = "外包单位管理")
public class SmtOutSrcApplyController extends BaseController {

	private final SmtOutSrcApplyService smtOutSrcApplyService;

	private final SmtDormitoryStaffService dormitoryStaffService;

	/**
	 * 导入外包人员申请
	 * @param  smtStaff 员工表
	 * @return Result
	 */
	@SysLog("外包申请导入")
	@ApiOperation("外包申请导入")
	@PostMapping("/excel/import")
	public Result saveBatchRec(@RequestBody List<TempStaffEditReqDTO> smtStaff){
		return success(smtOutSrcApplyService.saveBatchRec(smtStaff));
	}

	@SysLog("外包申请单列表")
	@ApiOperation("外包申请单列表")
	@GetMapping("/page")
	public Result<IPage<SmtOutSrcApplyRespDTO>> getPage(Page page, SmtOutSrcApplyReqDTO applyReqDTO) {
		return success(smtOutSrcApplyService.getPage(page, applyReqDTO), SmtOutSrcApplyRespDTO.class);
	}

	@SysLog("外包申请单详情")
	@ApiOperation("外包申请单详情")
	@PostMapping("/detail/{applyId}")
	public Result<SmtOutSrcApplyDetailRespDTO> getDetail(@PathVariable("applyId") Long applyId) {
		return success(smtOutSrcApplyService.getDetail(applyId));
	}

	@SysLog("外包申请单详情列表")
	@ApiOperation("外包申请单详情列表")
	@GetMapping("/detail/page/{applyId}")
	public Result<IPage<SmtOutSrcApplyDetailListDTO>> getDetailPage(Page page, @PathVariable("applyId") Long applyId) {
		return success(smtOutSrcApplyService.getDetailPage(page, applyId));
	}

	@SysLog("通过/拒绝申请单操作")
	@ApiOperation("通过/拒绝申请单操作")
	@PostMapping("/passOrRefuse")
	public Result passOrRefuse(@RequestBody SmtOutSrcApplyUpdDTO dto) {
		return success(smtOutSrcApplyService.passOrRefuse(dto,dormitoryStaffService));
	}
}
