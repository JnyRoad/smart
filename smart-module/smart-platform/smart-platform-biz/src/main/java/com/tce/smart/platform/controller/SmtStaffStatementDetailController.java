package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.SmtStaffStatementReqDTO;
import com.tce.smart.platform.api.dto.req.visitormanage.StaffStatementWithDorReqDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.StaffStayModifyRespDTO;
import com.tce.smart.platform.api.dto.resp.commonsd.StaffStayRespDTO;
import com.tce.smart.platform.api.dto.resp.sdstatement.SDStatementDetailRespDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDTO;
import com.tce.smart.platform.core.dto.SmtStaffStatementDetailDTO;
import com.tce.smart.platform.service.settlement.SmtSdMeterreadService;
import com.tce.smart.platform.service.settlement.SmtStaffStatementDetailService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * @description: 员工水电结算控制器
 * @date: 2020-07-17 17:13
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "水电扣费明细")
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/staff/statementdetail")
public class SmtStaffStatementDetailController {

	private final SmtSdMeterreadService smtSdMeterreadService;

	private final SmtStaffStatementDetailService smtStaffStatementDetailService;

	/**
	 * 后台分页查询员工水电结算记录 各收费项目合在一起 按月统计
	 * @param page 分页对象
	 * @param smtStaffStatementReqDTO 员工结算查询对象
	 * @return
	 */
	@ApiOperation("分页查询水电明细")
	@GetMapping("/page")
	public Result<IPage<SmtStaffStatementDTO>> getSDMeterreadPage(Page page, SmtStaffStatementReqDTO smtStaffStatementReqDTO) {
		return new Result<>(smtStaffStatementDetailService.getStaffSDStatementDetail(page, smtStaffStatementReqDTO));
	}

	/**
	 * 后台分页查询员工水电结算记录 各收费项目合在一起 按月统计
	 * @param page 分页对象
	 * @param smtStaffStatementReqDTO 员工结算查询对象
	 * @return
	 */
	@ApiOperation("分页查询水电明细-新接口")
	@GetMapping("/new-page")
	public Result<IPage<SmtStaffStatementDTO>> getSDMeterreadPageNew(Page page, SmtStaffStatementReqDTO smtStaffStatementReqDTO) {
		return new Result<>(smtStaffStatementDetailService.getStaffSDStatementDetailNew(page, smtStaffStatementReqDTO, smtSdMeterreadService));
	}

	/**
	 * 按楼栋查询员工水电结算记录 各收费项目合在一起 按月统计
	 * @param staffStatementWithDorReqDTO
	 * @return
	 */
	@ApiOperation("按楼栋查询员工水电结算记录")
	@GetMapping("/by-dor")
	public Result<List<SmtStaffStatementDTO>> getSDMeterreadWithDor(StaffStatementWithDorReqDTO staffStatementWithDorReqDTO) {
		return new Result<>(smtStaffStatementDetailService.getSDMeterreadWithDor(staffStatementWithDorReqDTO, smtSdMeterreadService));
	}

	/**
	 * 按楼栋查询员工水电结算记录 各收费项目合在一起 按月统计
	 * @param staffStatementWithDorReqDTO
	 * @return
	 */
	@ApiOperation("水电结算记录详情导出")
	@GetMapping("/export/detail")
	public Result<List<SmtStaffStatementDetailDTO>> getSDMeterreadDeteilExport(StaffStatementWithDorReqDTO staffStatementWithDorReqDTO) {
		return new Result<>(smtStaffStatementDetailService.getSDMeterreadDeteilExport(staffStatementWithDorReqDTO, smtSdMeterreadService));
	}

	/**
	 * APP端查询当前员工的水电结算详情 各收费项目单独显示  按月分隔
	 * @return
	 */
	@ApiOperation("水电扣费明细")
	@GetMapping("/record")
	public Result<IPage<SDStatementDetailRespDTO>> getStaffSDMeterRecord(@ApiParam(name = "current",value = "当前页",required = true) @RequestParam long current,
																		 @ApiParam(name = "size",value = "大小",required = true) @RequestParam long size,
																		 @ApiParam(name = "statementMonth",value = "结算月份",required = false) @DateTimeFormat(pattern = "yyyy-MM") @RequestParam Date statementMonth) {
		return new Result<>(smtStaffStatementDetailService.getStaffSDMeterRecord(new Page(current,size),statementMonth));
	}

	/**
	 * 查询员工入住天数
	 * @return
	 */
	@ApiOperation("查询员工入住天数")
	@GetMapping("/queryStayInfo")
	public Result<List<StaffStayRespDTO>> queryStaffStayNum(@ApiParam(name = "mrId",value = "抄表记录Id",required = true) @RequestParam Long mrId) {
		return new Result<>(smtStaffStatementDetailService.queryStaffStayNum(mrId));
	}

	/**
	 * 修改员工入住天数
	 * @return
	 */
	@ApiOperation("修改员工入住天数")
	@PostMapping("/updateStayInfo/{mrId}")
	public Result<Boolean> updateStaffStayNum(@ApiParam(name = "mrId",value = "抄表记录Id",required = true) @PathVariable Long mrId, @RequestBody List<StaffStayRespDTO> staffStayRespDTOS) {
		return new Result<>(smtStaffStatementDetailService.updateStaffStayNum(mrId,staffStayRespDTOS,smtSdMeterreadService));
	}

	/**
	 * 查询员工入住天数修改记录
	 * @return
	 */
	@ApiOperation("查询员工入住天数修改记录")
	@GetMapping("/queryStayModify/{mrId}")
	public Result<List<StaffStayModifyRespDTO>> queryStaffStayUpdateRecord(@ApiParam(name = "mrId",value = "抄表记录Id",required = true) @PathVariable Long mrId) {
		return new Result<>(smtStaffStatementDetailService.queryStaffStayUpdateRecord(mrId,smtSdMeterreadService));
	}
}
