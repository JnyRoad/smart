package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryApplyFailBackDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryApplyReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.DormitoryApplySearchReqDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.DormitoryApplySearchRespDTO;
import com.tce.smart.platform.api.dto.resp.dormitorymange.DormitoryDistRespDTO;
import com.tce.smart.platform.service.SmtDormitoryApplyService;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @description: 内宿申请管理
 * @date: 2020/12/29 15:41
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "内宿申请管理")
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/apply")
public class SmtDormitoryApplyController {

	private final SmtDormitoryApplyService smtDormitoryApplyService;

	private final SmtDormitoryStaffService dormitoryStaffService;

	@ApiOperation("内宿申请记录分页查询")
	@GetMapping("/page")
	public Result<IPage<DormitoryApplySearchRespDTO>> getApplyRecord(DormitoryApplySearchReqDTO searchReqDTO) {
		return new Result<>(smtDormitoryApplyService.getApplyRecord(searchReqDTO));
	}


	@ApiOperation("内宿申请")
	@PostMapping("/save")
	public Result<Boolean> saveApply(@RequestBody DormitoryApplyReqDTO applyReqDTO) {
		return new Result<>(smtDormitoryApplyService.saveApply(applyReqDTO));
	}

	@ApiOperation("内宿申请撤销")
	@PostMapping("/cancel")
	public Result<Boolean> cancelApply() {
		return new Result<>(smtDormitoryApplyService.cancelApply());
	}

	@ApiOperation("内宿申请退回")
	@PostMapping("/failback")
	public Result<Boolean> failbackApply(@RequestBody DormitoryApplyFailBackDTO failBackDTO) {
		return new Result<>(smtDormitoryApplyService.failbackApply(failBackDTO));
	}

	@ApiOperation("手动分配宿舍")
	@PostMapping("/manual")
	public Result<Boolean> manualDis(@ApiParam(name = "applyId",value = "申请记录Id",required = true) @RequestParam Long applyId,
									 @ApiParam(name = "bedId",value = "床位记录Id",required = true) @RequestParam Integer bedId) {
		return new Result<>(smtDormitoryApplyService.manualDis(applyId,bedId, dormitoryStaffService));
	}

	@ApiOperation("自动推荐宿舍")
	@PostMapping("/recommend")
	public Result<DormitoryDistRespDTO> recommendDis(@ApiParam(name = "applyId",value = "申请记录Id",required = true) @RequestParam String applyId) {
		return new Result<>(smtDormitoryApplyService.recommendDis(Long.parseLong(applyId)));
	}
}
