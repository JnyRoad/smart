package com.tce.smart.app.service.fore;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.ao.fore.AttendanceAo;
import com.tce.smart.app.vo.fore.AttendanceListVo;
import com.tce.smart.app.vo.fore.PatchResonVo;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.req.AddReplaceApplicationReqDTO;
import com.tce.smart.platform.api.dto.req.SearchPatchReqDTO;
import com.tce.smart.platform.api.dto.resp.AttendanceSuccessDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.PatchCountRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchAttendanceDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.SearchPatchRespDTO;

import java.util.List;
import java.util.Map;

/**
 * 考勤不卡申请接口
 * @author 梁圆
 *
 */
public interface AttendanceService {

	/**
	 * 获取补卡原因
	 * @return
	 */
	PatchResonVo getPatchReason();
	/**
	 * 获取出勤列表
	 * @param attendanceAo
	 * @return
	 */
	AttendanceListVo getAttendanceList(AttendanceAo attendanceAo);
	/**
	 * 获取出勤的详情
	 * @param attendanceAo
	 * @return
	 */
	SearchAttendanceDetailRespDTO getAttendanceDetail(AttendanceAo attendanceAo);

	/**
	 * 获取补卡的信息
	 * @param searchPatchDTO
	 * @return
	 */
	SearchPatchRespDTO getPatchList(SearchPatchReqDTO searchPatchDTO);

	/**
	 * 获取补卡记录
	 * @param params
	 * @return
	 */
	Page<?> getPatchList(Map<String, Object> params);

	/**
	 * 添加补卡申请
	 * @param addReplaceApplicationDTO
	 * @return
	 */
	void addPatch(AddReplaceApplicationReqDTO addReplaceApplicationDTO);

	/**
	 * 获取补卡次数
	 * @param searchPatchDTO
	 * @return
	 */
	PatchCountRespDTO getPatchCount(SearchPatchReqDTO searchPatchDTO);
	/**
	 * 获取考勤正常的数据
	 * @param attendanceAo
	 * @return
	 */
	AttendanceSuccessDetailRespDTO getAttendanceSuccessDeatil(AttendanceAo attendanceAo);
	Result getPatchInfoFlow(AllApplicationAo allApplicationAoId);

	/**
	 * 获取补卡记录详情
	 * @param recordId
	 * @return
	 */
	Result getSmtReplaceApplicationDetail(Integer recordId);

	List getAttendanceMothList(SearchPatchReqDTO attendanceAo);

	Object getAttendanceGetSkyPay(SearchPatchReqDTO attendanceAo);

}
