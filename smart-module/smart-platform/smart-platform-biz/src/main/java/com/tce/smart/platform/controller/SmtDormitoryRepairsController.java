package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.api.dto.req.*;
import com.tce.smart.platform.api.dto.resp.SmtDormitoryRepairsRespVO;
import com.tce.smart.platform.api.dto.resp.dormitoryrepairs.SmtDormitoryRepairsDetailDTO;
import com.tce.smart.platform.service.SmtDormitoryRepairsService;
import com.tce.smart.tool.enums.RangeTypeEnum;
import com.tce.smart.tool.enums.RepairSTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @description: 宿舍报修接口控制器
 * @date: 2020-07-20 14:00
 * @author: wuling
 * @version: 1.0
 */
@Api(tags = "园区报修")
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/repair")
public class SmtDormitoryRepairsController {

	private final SmtDormitoryRepairsService smtDormitoryRepairsService;

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param smtDormitoryRepairsReqDTO 宿舍报修记录
	 * @return
	 */
	@ApiIgnore
	@GetMapping("/page")
	@ApiOperation("宿舍报修记录分页")
	public Result getDormitoryRepairsPage(Page page, SmtDormitoryRepairsReqDTO smtDormitoryRepairsReqDTO) {
		return new Result<>(smtDormitoryRepairsService.getDormitoryRepairsPage(page, smtDormitoryRepairsReqDTO));
	}

	/**
	 * 分页查询
	 * @param page 分页对象
	 * @param dto 园区报修记录--裕同许昌定制
	 * @return
	 */
	@ApiIgnore
	@GetMapping("/page/yuto")
	@ApiOperation("园区报修记录--裕同许昌定制")
	public Result getYutoDormitoryRepairsPage(Page page, SmtDormitoryRepairsReqYutoDTO dto) {
		return new Result<>(smtDormitoryRepairsService.getDormitoryRepairsPage(page, dto));
	}

	/**
	 * 添加报修记录
	 * @param smtDormitoryRepairsAddReqDTO
	 * @return
	 */
	@ApiOperation("添加报修记录")
	@SysLog("新增宿舍报修记录")
	@PostMapping("/add")
	public Result addDormitoryRepairsRecord(@RequestBody SmtDormitoryRepairsAddReqDTO smtDormitoryRepairsAddReqDTO){
		return new Result<>(smtDormitoryRepairsService.addDormitoryRepairs(smtDormitoryRepairsAddReqDTO));
	}

	@GetMapping("/status/update")
	@ApiOperation("园区报修审批")
	public Result status(@RequestParam("id") Long id, @RequestParam("approveBadge") String approveBadge,
						 @RequestParam("status") Integer status, @RequestParam("remark") String remark){
		return new Result<>(smtDormitoryRepairsService.updateStatus(id, approveBadge, status, remark));
	}

	/**
	 * 查询员工报修记录
	 * @param
	 * @return
	 */
	@ApiOperation("查询员工报修记录")
	@SysLog("查询员工报修记录")
	@GetMapping("/query/record")
	public Result<IPage<SmtDormitoryRepairsRespVO>> getStaffReportRecord(@ApiParam(name = "current",value = "当前页",required = true) @RequestParam long current,
																		 @ApiParam(name = "size",value = "大小",required = true) @RequestParam long size){
		return new Result<>(smtDormitoryRepairsService.getDormitoryRepairsPageByStaff(new Page(current,size)));
	}

	/**
	 * 查询报修记录详细
	 * @param id
	 * @return
	 */
	@ApiOperation("查询报修记录详细")
	@SysLog("查询报修记录详细")
	@GetMapping("/query/detail/{id}")
	public Result<SmtDormitoryRepairsDetailDTO> getStaffReportDetail(@ApiParam(name = "id",value = "记录标识",required = true) @PathVariable("id") Long id){
		return new Result<>(smtDormitoryRepairsService.getStaffReportDetail(id));
	}

	/**
	 * 回复报修
	 * @param
	 * @return
	 */
	@ApiIgnore
	@ApiOperation("回复报修")
	@SysLog("回复报修")
	@PostMapping("/reply")
	public Result replyRepair(@RequestBody ReplyRepairReqDTO replyRepairReqDTO){
		return new Result<>(smtDormitoryRepairsService.replyRepair(replyRepairReqDTO));
	}

	@GetMapping("/enum/range")
	@ApiOperation("报修范围枚举")
	public Result<List<Map<String, Object>>> getRange(){
		return new Result<>(RangeTypeEnum.list());
	}

	@GetMapping("/enum/repairs/type")
	@ApiOperation("宿舍维修类型枚举")
	public Result<List<Map<String, Object>>> getRepairSType(){
		return new Result<>(RepairSTypeEnum.list());
	}

}
