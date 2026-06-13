package com.tce.smart.app.controller.fore;

import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.ao.fore.AttendanceAo;
import com.tce.smart.app.service.fore.AttendanceService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.platform.api.dto.req.AddReplaceApplicationReqDTO;
import com.tce.smart.platform.api.dto.req.SearchPatchReqDTO;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 考勤和补卡
 * @author 梁圆
 */

@RestController
@AllArgsConstructor
@RequestMapping("")
public class AttendanceController extends BaseController{

	private AttendanceService attendanceService;

	/**
	 * 获取补卡原因
	 * @return
	 */
	@GetMapping("/application/attendance/patch/reason")
	public Result getPatchReason() {
		return new Result<>(attendanceService.getPatchReason());
	}
	/**
	 * 获取指定年-月份的考勤信息
	 * @param attendanceAo
	 */
	@PostMapping("/application/attendance/list")
	public Result getAttendanceList(@RequestBody AttendanceAo attendanceAo) {
		return success(attendanceService.getAttendanceList(attendanceAo));
	}


	/**
	 * 查询当前选择月份得打卡列表
	 * @param searchPatchReqDTO
	 * @return
	 */
	@PostMapping("/application/attendance/month/list")
	public Result getAttendanceMothList(@RequestBody SearchPatchReqDTO searchPatchReqDTO) {
		return success(attendanceService.getAttendanceMothList(searchPatchReqDTO));
	}


	/**
	 * 查看考勤的详情
	 * @param attendanceAo
	 */
	@PostMapping("/application/attendanceError/detail")
	public Result getAttendanceDeatil(@RequestBody AttendanceAo attendanceAo) {
		return success(attendanceService.getAttendanceDetail(attendanceAo));
	}
	/**
	 * 查看考勤正常的详情
	 * @param attendanceAo
	 */
	@PostMapping("/application/attendanceSuccess/detail")
	public Result getAttendanceSuccessDeatil(@RequestBody AttendanceAo attendanceAo) {
		return success(attendanceService.getAttendanceSuccessDeatil(attendanceAo));
	}


	/**
	 * 获取指定年-月-日份的补卡 月次数信息
	 * @param searchPatchDTO
	 */
	@PostMapping("/application/attendance/patch/patchCount")
	public Result getPatchCount(@RequestBody SearchPatchReqDTO searchPatchDTO) {
		return success(attendanceService.getPatchCount(searchPatchDTO));
	}

	/**
	 * 获取指定年-月-日份的补卡信息
	 * @param searchPatchDTO
	 */
	@PostMapping("/application/attendance/patch/query")
	public Result getPatchList(@RequestBody SearchPatchReqDTO searchPatchDTO) {
		return success(attendanceService.getPatchList(searchPatchDTO));
	}
	/**
	 * 获取补卡记录
	 */
	@GetMapping("/process/attendance/patch/record/list")
	public Result getPatchList(@RequestParam Map<String, Object> params) {
		return new Result<>(attendanceService.getPatchList(params));
	}
	/**
	 * 获取补卡记录详情
	 * @param recordId
	 * @return
	 */
	@GetMapping("/process/attendance/patch/record/detail")
	public Result getSmtReplaceApplicationDetail(@RequestParam Integer recordId ) {
		return new Result<>(attendanceService.getSmtReplaceApplicationDetail(recordId));
	}



	/**
	 * 发起补卡申请
	 * @param addReplaceApplicationDTO
	 */
	@PostMapping("/application/attendance/patch")
	public Result addPatch(@RequestBody AddReplaceApplicationReqDTO addReplaceApplicationDTO) {
		attendanceService.addPatch(addReplaceApplicationDTO);
		return success();
	}
	/**
	 * 查看补卡的流程
	 * @param allApplicationAoId
	 * @return
	 */
	@PostMapping("/process/attendance/record/infoFlow")
	public Result getPatchInfoFlow(@RequestBody AllApplicationAo allApplicationAoId) {
		return attendanceService.getPatchInfoFlow(allApplicationAoId);
	}


	@PostMapping("/application/attendance/getSkyPay")
	public Result getAttendanceGetSkyPay(@RequestBody SearchPatchReqDTO attendanceAo) {
		return success(attendanceService.getAttendanceGetSkyPay(attendanceAo));
	}




}
