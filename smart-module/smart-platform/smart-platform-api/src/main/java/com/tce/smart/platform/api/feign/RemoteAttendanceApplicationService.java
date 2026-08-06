package com.tce.smart.platform.api.feign;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.AddReplaceApplicationReqDTO;
import com.tce.smart.platform.api.dto.req.SearchAttendanceReqDTO;
import com.tce.smart.platform.api.dto.req.SearchPatchReqDTO;
import com.tce.smart.platform.api.dto.resp.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 考勤和补卡申请管理
 * @author 梁园
 *
 */
@FeignClient(value = ServiceNameConstants.PLATFORM_SERVICE)
public interface RemoteAttendanceApplicationService {

	/**
	 *
	 * 查看考勤信息
	 * @param searchAttendanceDTO
	 * @return
	 */
	@PostMapping("/application/attendance/getAttendance")
	Result<List<SearchAttendanceRespDTO>> getAttendance(@RequestBody SearchAttendanceReqDTO searchAttendanceDTO, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 *查询考勤的详情
	 * @param searchAttendanceDTO
	 * @return
	 */
	@PostMapping("/application/attendance/detail")
	 Result<SearchAttendanceDetailRespDTO> getAttendanceDetail(@RequestBody SearchAttendanceReqDTO searchAttendanceDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 *查询考勤正常的详情
	 * @param searchAttendanceDTO
	 * @return
	 */
	@PostMapping("/application/attendance/success/detail")
	Result<AttendanceSuccessDetailRespDTO> getAttendanceSuccessDeatil(@RequestBody SearchAttendanceReqDTO searchAttendanceDTO, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 *
	 * 查看补卡列表
	 * @param current ,size,staffBadge
	 * @return
	 */
	@GetMapping("/application/attendance/replace/page")
	Result<Page<SearchReplaceApplicationRespDTO>> getSmtReplaceApplicationPage(@RequestParam("current") final long current, @RequestParam("size") final long size, @RequestParam("staffBadge") final String staffBadge, @RequestHeader(SecurityConstants.FROM) String from);


	@GetMapping("/application/attendance/replace/detail/{recordId}")
	Result<ReplaceApplicationDetailRespDTO> getSmtReplaceApplicationDetail(@PathVariable("recordId") Integer recordId, @RequestHeader(SecurityConstants.FROM) String from);


	/**
	 *
	 * 补卡信息查询，根据补卡的时间
	 * @param searchPatchDTO
	 * @return
	 */
	@PostMapping("/application/attendance/replace/patch")
	Result<SearchPatchRespDTO> getPatchApplication(@RequestBody SearchPatchReqDTO searchPatchDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 补卡次数查询
	 * @param searchPatchDTO 实体
	 * @return success、false
	 */
	@PostMapping("/application/attendance/replace/patchCount")
    Result<PatchCountRespDTO> getPatchCount(@RequestBody SearchPatchReqDTO searchPatchDTO, @RequestHeader(SecurityConstants.FROM) String from);
	/**
	 * 添加补卡申请
	 * @param addReplaceApplicationDTO 实体
	 * @return success、false
	 */
	@PostMapping("/application/attendance/replace/add")
	Result save(@RequestBody AddReplaceApplicationReqDTO addReplaceApplicationDTO, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 *获取补卡原因
	 * @return
	 */
	@GetMapping("/application/attendance/replace/reason")
	 Result<List<SearchPatchCardReasonRespDTO>> getPatchCardReason(@RequestHeader(SecurityConstants.FROM) String from);
	/**
	 * 查看补卡流程
	 * @param id
	 * @return
	 */
	@GetMapping("/application/attendance/infoFlow/{id}")
	Result getInfoFlow(@PathVariable("id") Integer id,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 考勤消息通知
	 * @param from
	 * @return
	 */
	@GetMapping("/application/attendance/patchErrorPushMsg")
	Result patchErrorPushMsg(@RequestHeader(SecurityConstants.FROM) String from);

 }
