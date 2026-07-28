package com.tce.smart.app.service.fore.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.fore.AllApplicationAo;
import com.tce.smart.app.ao.fore.AttendanceAo;
import com.tce.smart.app.service.fore.AttendanceService;
import com.tce.smart.app.vo.fore.AttendanceListVo;
import com.tce.smart.app.vo.fore.PatchApplicationVo;
import com.tce.smart.app.vo.fore.PatchResonVo;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.PaginationConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.attendance.resp.KQCardDetailsRespDTO;
import com.tce.smart.data.api.feign.attendance.RemoteKQCardDetailsService;
import com.tce.smart.data.api.feign.ehrview.RemoteAvaGetskyPayService;
import com.tce.smart.platform.api.dto.req.AddReplaceApplicationReqDTO;
import com.tce.smart.platform.api.dto.req.SearchAttendanceReqDTO;
import com.tce.smart.platform.api.dto.req.SearchPatchReqDTO;
import com.tce.smart.platform.api.dto.resp.*;
import com.tce.smart.platform.api.feign.RemoteAttendanceApplicationService;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 考勤补卡接口实现
 *
 * @author ly
 */
@Service
@AllArgsConstructor
@Slf4j
public class AttendanceServiceImpl implements AttendanceService {

	private final RemoteAttendanceApplicationService remoteAttendanceApplicationService;

	private final RemoteKQCardDetailsService remoteKQCardDetailsService;

	private final RemoteAvaGetskyPayService remoteAvaGetskyPayService;

	/**
	 * 获取补卡原因
	 */
	@Override
	public PatchResonVo getPatchReason() {
		PatchResonVo patchResonVo = new PatchResonVo();
		Result<List<SearchPatchCardReasonRespDTO>> result = remoteAttendanceApplicationService.getPatchCardReason(SecurityConstants.FROM_IN);
		//转换接过来的值
		if (CommonConstants.SUCCESS.equals(result.getCode())) {
			List<SearchPatchCardReasonRespDTO> list = result.getData();
			patchResonVo.setRecords(list);
			patchResonVo.setTotal(list.size());
		}
		return patchResonVo;
	}

	/**
	 * 获取考勤的信息
	 */
	@Override
	public AttendanceListVo getAttendanceList(AttendanceAo attendanceAo) {
		AttendanceListVo attendanceListVo = new AttendanceListVo();
		SearchAttendanceReqDTO searchAttendanceDto = new SearchAttendanceReqDTO();
		searchAttendanceDto.setQueryDay(attendanceAo.getQueryDay());
		searchAttendanceDto.setStaffBadge(SecurityUtils.getUser().getUsername());
		//获取考勤信息
		List<SearchAttendanceRespDTO> list = remoteAttendanceApplicationService.getAttendance(searchAttendanceDto, SecurityConstants.FROM_IN).getData();
		attendanceListVo.setRecords(list);
		attendanceListVo.setTotal(list.size());
		return attendanceListVo;
	}

	/**
	 * 获取考勤异常的详情
	 */
	@Override
	public SearchAttendanceDetailRespDTO getAttendanceDetail(AttendanceAo attendanceAo) {
		SearchAttendanceReqDTO searchAttendanceDto = new SearchAttendanceReqDTO();
		searchAttendanceDto.setQueryDay(attendanceAo.getQueryDay());
		searchAttendanceDto.setStaffBadge(SecurityUtils.getUser().getUsername());
		Result<SearchAttendanceDetailRespDTO> result = remoteAttendanceApplicationService.getAttendanceDetail(searchAttendanceDto, SecurityConstants.FROM_IN);
		return result.getData();
	}

	/**
	 * 获取考勤正常的详情
	 */
	@Override
	public AttendanceSuccessDetailRespDTO getAttendanceSuccessDeatil(AttendanceAo attendanceAo) {
		SearchAttendanceReqDTO searchAttendanceDto = new SearchAttendanceReqDTO();
		searchAttendanceDto.setQueryDay(attendanceAo.getQueryDay());
		searchAttendanceDto.setStaffBadge(SecurityUtils.getUser().getUsername());
		AttendanceSuccessDetailRespDTO attendanceSuccessDetailRespDTO = remoteAttendanceApplicationService.getAttendanceSuccessDeatil(searchAttendanceDto, SecurityConstants.FROM_IN).getData();
		return attendanceSuccessDetailRespDTO;
	}

	/**
	 * 获取补卡的信息
	 */
	@Override
	public SearchPatchRespDTO getPatchList(SearchPatchReqDTO searchPatchDTO) {
		searchPatchDTO.setStaffBadge(SecurityUtils.getUser().getUsername());
		Result<SearchPatchRespDTO> result = remoteAttendanceApplicationService.getPatchApplication(searchPatchDTO, SecurityConstants.FROM_IN);
		return result.getData();
	}

	/**
	 * 获取补卡记录
	 */
	@Override
	@SuppressWarnings({"rawtypes", "unchecked"})
	public Page<?> getPatchList(Map<String, Object> params) {
		//获取补卡记录信息
		Page<SearchReplaceApplicationRespDTO> pageInfo = remoteAttendanceApplicationService.getSmtReplaceApplicationPage(MapUtil.getInt(params, PaginationConstants.CURRENT), MapUtil.getInt(params, PaginationConstants.SIZE),
				SecurityUtils.getUser().getUsername(), SecurityConstants.FROM_IN).getData();
		//判斷值是否為空
		if (CollectionUtils.isNotEmpty(pageInfo.getRecords())) {
			List patchList = new ArrayList();
			PatchApplicationVo patchApplicationVo = null;
			SearchReplaceApplicationRespDTO searchReplaceApplicationVO = null;
			for (int i = 0; i < pageInfo.getRecords().size(); i++) {
				patchApplicationVo = new PatchApplicationVo();
				searchReplaceApplicationVO = pageInfo.getRecords().get(i);
				patchApplicationVo.setRecordId(String.valueOf(searchReplaceApplicationVO.getRecordId()));
				patchApplicationVo.setRecordTitle(searchReplaceApplicationVO.getStaffName() + "的补卡申请");
				patchApplicationVo.setRecordDesc(searchReplaceApplicationVO.getRecordDesc());
				patchApplicationVo.setPatchDate(searchReplaceApplicationVO.getPatchDate());
				patchApplicationVo.setPatchReasonDesc(searchReplaceApplicationVO.getPatchReasonDesc());
				patchApplicationVo.setBuName(searchReplaceApplicationVO.getBuName());
				patchApplicationVo.setClassDesc(searchReplaceApplicationVO.getClassDesc());
				patchApplicationVo.setRecordDate(searchReplaceApplicationVO.getRecordDate());
				patchApplicationVo.setMissPatchCount(String.valueOf(searchReplaceApplicationVO.getMissPatchCount()));
				patchList.add(patchApplicationVo);
			}
			pageInfo.setRecords(patchList);
		}
		return pageInfo;
	}

	/**
	 * 添加补卡申请
	 */
	@Override
	public void addPatch(AddReplaceApplicationReqDTO addReplaceApplicationDTO) {

		log.info("补卡申请参数："+ addReplaceApplicationDTO);
		addReplaceApplicationDTO.setStaffBadge(SecurityUtils.getUser().getUsername());
		Result<?> result = remoteAttendanceApplicationService.save(addReplaceApplicationDTO, SecurityConstants.FROM_IN);
		if (CommonConstants.SUCCESS != result.getCode()) {
			throw new TCEException(result.getCode(), result.getMsg());
		}
	}

	/**
	 * 获取补卡次数
	 */
	@Override
	public PatchCountRespDTO getPatchCount(SearchPatchReqDTO searchPatchDTO) {
		searchPatchDTO.setStaffBadge(SecurityUtils.getUser().getUsername());
		Result<PatchCountRespDTO> result = remoteAttendanceApplicationService.getPatchCount(searchPatchDTO, SecurityConstants.FROM_IN);
		if (CommonConstants.SUCCESS.equals(result.getCode())) {
			return result.getData();
		} else {
			throw new TCEException(result.getCode(), result.getMsg());
		}
	}

	@Override
	public Result getPatchInfoFlow(AllApplicationAo allApplicationAoId) {
		return remoteAttendanceApplicationService.getInfoFlow(Integer.parseInt(allApplicationAoId.getRecordId()), SecurityConstants.FROM_IN);

	}

	/**
	 * 查询补卡记录详情
	 */
	@Override
	public Result getSmtReplaceApplicationDetail(Integer recordId) {
		// TODO Auto-generated method stub
		Result<ReplaceApplicationDetailRespDTO> smtReplaceApplicationDetail = remoteAttendanceApplicationService.getSmtReplaceApplicationDetail(recordId, SecurityConstants.FROM_IN);
		return smtReplaceApplicationDetail;
	}


	@Override
	public List getAttendanceMothList(SearchPatchReqDTO attendanceAo) {
		// TODO Auto-generated method stub

		String badge = SecurityUtils.getUser().getUsername();
		String kqDate = attendanceAo.getPatchDate();
		log.info("remoteKQCardDetailsService.mothInfo param-badge:" + badge + "-kqDate:" + kqDate);
		Result<List<KQCardDetailsRespDTO>> mothInfo = remoteKQCardDetailsService.mothInfo(badge, kqDate, SecurityConstants.FROM_IN);
		log.info("remoteKQCardDetailsService.mothInfo.result:" + mothInfo);
		return mothInfo.getData();
	}


	/**
	 * 考勤统计
	 * *
	 */
	@Override
	public Object getAttendanceGetSkyPay(SearchPatchReqDTO attendanceAo) {
		// TODO Auto-generated method stub
		String badge = SecurityUtils.getUser().getUsername();
		String kqDate = attendanceAo.getPatchDate();
		log.info("remoteAvaGetskyPayService.info param-badge:" + badge + "-kqDate:" + kqDate);
		Result info = remoteAvaGetskyPayService.info(badge, kqDate, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		log.info("remoteKQCardDetailsService.info.result:" + info);
		return info.getData();

	}

}
