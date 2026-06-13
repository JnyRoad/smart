package com.tce.smart.platform.controller;

import com.tce.smart.platform.api.dto.req.DormitoryBedReqDTO;
import com.tce.smart.platform.api.dto.req.DormitoryFloorReqDTO;
import com.tce.smart.platform.api.dto.req.dormitorymange.BedReqDTO;
import com.tce.smart.platform.service.SmtDormitoryRoomService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.platform.core.dto.DormitoryBedPageQueryDTO;
import com.tce.smart.platform.core.entity.SmtDormitoryBed;
import com.tce.smart.platform.core.entity.SmtDormitoryStaff;
import com.tce.smart.platform.service.SmtDormitoryBedService;

import lombok.AllArgsConstructor;

/**
 * 园区宿舍楼l楼层中房间的床位数
 *
 * @author 齐佩
 * @date 2019-04-13 18:17:21
 */
@RestController
@AllArgsConstructor
@Api(tags = "床位管理")
@RequestMapping("/dormitory/bed")
public class SmtDormitoryBedController {

	private final SmtDormitoryBedService smtDormitoryBedService;

	private final SmtDormitoryRoomService smtDormitoryRoomService;

	/**
	 * 分页查询
	 *
	 * @param page
	 * @param bed
	 *            园区宿舍楼l楼层中房间的床位信息和占用情况
	 * @return
	 */
	@GetMapping("/page")
	public Result getSmtDormitoryBedPage(Page page, DormitoryBedPageQueryDTO bed) {
		return new Result<>(smtDormitoryBedService.getDormitoryBedOfStaff(page, bed));
	}

	@GetMapping("/family/list")
	public Result getStaffFamily(DormitoryBedPageQueryDTO bed) {
		return new Result<>(smtDormitoryBedService.getDormitoryStaffFamily(bed));
	}

	/**
	 * 新增园区宿舍楼l楼层中房间的床位数
	 *
	 * @param smtDormitoryStaff
	 *            园区宿舍楼l楼层中房间的床位的占用人员
	 * @return Result
	 */
	@SysLog("新增园区宿舍楼l楼层中房间的床位的占用人员")
	@PostMapping
	public Result save(@RequestBody SmtDormitoryStaff smtDormitoryStaff) {
		return smtDormitoryBedService.addDormitoryBedOfStaff(smtDormitoryStaff);
	}

	/**
	 * 通过id删除园区宿舍楼l楼层中房间的床位的占用人员
	 *
	 * @param smtDormitoryStaff
	 *            id 员工宿舍表的id
	 * @return Result
	 */
	@SysLog("删除园区宿舍楼l楼层中房间的床位的占用人员")
	@PostMapping("/{id}")
	public Result removeById(@PathVariable SmtDormitoryStaff smtDormitoryStaff) {
		return new Result<>(smtDormitoryBedService.deleteStaffBed(smtDormitoryStaff));
	}


	@SysLog("修改园区宿舍楼楼层中房间的床位的占用人员")
	@PostMapping("/update")
	public Result updateDormitoryBedOfStaff(@RequestBody SmtDormitoryStaff smtDormitoryStaff) {
		return smtDormitoryBedService.updateDormitoryBedOfStaff(smtDormitoryStaff);
	}

	/**
	 * 查询离职并在宿的人员个数
	 * @param bed
	 * @return
	 */
	@GetMapping("/getLeaveStaff")
	public Result getLeaveStaff(DormitoryBedPageQueryDTO bed) {
		return new Result<>(smtDormitoryBedService.getLeaveStaff(bed));
	}

	@PostMapping("/queryBed")
	public Result queryBed(@RequestBody DormitoryBedReqDTO bed) {
		return smtDormitoryBedService.queryBed(bed);
	}

	@ApiOperation("修改床位名称")
	@PostMapping("/updatename")
	public Result<Boolean> updateBedName(@RequestBody BedReqDTO bedReqDTO) {
		return new Result<>(smtDormitoryBedService.updateBedName(bedReqDTO));
	}

	@ApiOperation("禁用或启用床位")
	@PostMapping("/switchDelFlg")
	public Result<Boolean> switchDelFlg(@RequestBody BedReqDTO bedReqDTO) {
		return new Result<>(smtDormitoryBedService.switchDelFlg(bedReqDTO,smtDormitoryRoomService));
	}
}
