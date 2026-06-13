package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.DormitoryStatisticsDTO;
import com.tce.smart.platform.core.dto.StaffInDormitoryHistoryDTO;
import com.tce.smart.platform.core.dto.UpdateDormitoryStaffDTO;
import com.tce.smart.platform.service.SmtDormitoryStaffHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;


/**
 * 员工宿舍信息表
 *
 * @author 齐佩
 * @date 2019-04-18 14:32:40
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dormitory/staff/history")
@Api(tags = "platform-入住退宿")
public class SmtDormitoryStaffHistoryController extends BaseController {

	private final SmtDormitoryStaffHistoryService service;

	/**
	 * 分页查询员工退宿记录
	 *
	 * @param page 分页对象
	 * @param
	 * @return
	 */
	@ApiOperation("分页查询")
	@GetMapping("/page")
	public Result getSmtDormitoryStaffHistoryPage(Page page, StaffInDormitoryHistoryDTO dto, @RequestParam(value = "rangTimeIn", required = false) String rangTimeIn, @RequestParam(value = "rangTimeOut", required = false) String rangTimeOut) {
		return service.getSmtDormitoryStaffHistory(page, dto, rangTimeIn, rangTimeOut);
	}


	@SysLog("修改 ")
	@PostMapping("update")
	public Result updateById(@RequestBody UpdateDormitoryStaffDTO updateDormitoryStaffDTO) {
		return service.updateById(updateDormitoryStaffDTO);
	}

	@SysLog("删除 ")
	@ApiOperation("根据id删除")
	@PostMapping("/delete/{id}")
	public Result deleteById(@PathVariable("id") Integer id) {
		return success(service.deleteDor(id));
	}

	@SysLog("入住率统计 ")
	@GetMapping("statistics")
	public Result statistics(DormitoryStatisticsDTO dormitoryStatisticsDTO) {
		return new Result<>(service.statistics(dormitoryStatisticsDTO));
	}


	@SysLog("入住率详细 ")
	@GetMapping("statistics/detail")
	public Result statisticsDetial(Page page, DormitoryStatisticsDTO dormitoryStatisticsDTO) {
		return new Result<>(service.statisticsDetial(page, dormitoryStatisticsDTO));
	}


}
