package com.tce.smart.platform.controller;

import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.EditDormitoryOutRemarkReqDTO;
import com.tce.smart.platform.api.dto.resp.DormitoryOutRemarkRespDTO;
import com.tce.smart.platform.service.SmtDormitoryOutRemarkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.Date;


/**
 * 住宿备注表
 *
 * @author fushiping
 * @date 2019-04-15 14:43:28
 */
@RestController
@AllArgsConstructor
@Api(tags = "住宿备注表")
@RequestMapping("/dormitory/out/remark")
public class SmtDormitoryOutRemarkController extends BaseController {

	private final SmtDormitoryOutRemarkService smtDormitoryOutRemarkService;

	@ApiOperation("查询列表")
	@GetMapping("/list/{dorStaffId}")
	public Result getRemarkList(@PathVariable("dorStaffId") Integer dorStaffId) {
		return success(smtDormitoryOutRemarkService.getList(dorStaffId), DormitoryOutRemarkRespDTO.class);
	}


	@ApiOperation("删除")
	@GetMapping("/delete/{id}")
	public Result getById(@PathVariable("id") Long id) {
		return success(smtDormitoryOutRemarkService.removeById(id));
	}


	@ApiOperation("编辑备注")
	@PostMapping
	public Result save(@RequestBody EditDormitoryOutRemarkReqDTO dormitory) {
		return success(smtDormitoryOutRemarkService.editRemark(dormitory));
	}

}
