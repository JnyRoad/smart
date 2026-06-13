package com.tce.smart.platform.service;
import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.api.dto.req.PatchStatisticsReqDTO;
import com.tce.smart.platform.core.dto.AddReplaceApplicationDTO;
import com.tce.smart.platform.core.dto.SearchAttendanceDTO;
import com.tce.smart.platform.core.dto.SearchPatchDTO;
import com.tce.smart.platform.core.dto.SearchReplaceDTO;
import com.tce.smart.platform.core.entity.SmtReplaceApplication;
import com.tce.smart.platform.core.vo.*;

/**
 * 补卡申请表

 *
 * @author 梁圆
 * @date 2019-04-13 18:19:37
 */
public interface SmtReplaceApplicationService extends IService<SmtReplaceApplication> {

	/**
	 * 获取补卡的原因
	 * @return
	 */
	List<SearchPatchCardReasonVO> getPatchCardReason();

	/**
	 * 添加补卡申请
	 * @param addReplaceApplicationDTO
	 */
	void add(AddReplaceApplicationDTO addReplaceApplicationDTO);

	/**
	 * 获取补卡的记录
	 * @param page
	 * @param smtReplaceApplication
	 * @return
	 */
	Page<SearchReplaceApplicationVO> getSmtReplaceApplicationPage(Page page, SmtReplaceApplication smtReplaceApplication);

	/**
	 * 获取补卡信息
	 * @param searchPatchDTO
	 * @return
	 */
	SearchPatchVO getPatchApplication(SearchPatchDTO searchPatchDTO);

	/**
	 * 获取出勤信息
	 * @param searchAttendanceDTO
	 * @return
	 */
	List<SearchAttendanceVO> getAttendance(SearchAttendanceDTO searchAttendanceDTO);

	SearchAttendanceDetailVO getAttendanceDetail(SearchAttendanceDTO searchAttendanceDTO);

	PatchCountVO getPatchCount(SearchPatchDTO searchPatchDTO);

	/**
	 * 考勤正常详情
	 * @param searchAttendanceDTO
	 * @return
	 */
	AttendanceSuccessDetailVO getAttendanceSuccessDetail(SearchAttendanceDTO searchAttendanceDTO);

	List<FlowVO>  getInfoFlow(Integer id);

	void patchErrorPushMsg();

	ReplaceApplicationDetailVO getSmtReplaceApplicationDetail(Integer id);

	Page<SearchReplaceApplicationVO> getSmtReplaceApplicationPageList(Page page, SearchReplaceDTO searchReplaceDTO);

	/**
	 * 根据补卡原因统计
	 * @param page
	 * @param reqDTO
	 * @return
	 */
	IPage<PatchStatisticsVo> patchCountStatistics(Page page, PatchStatisticsReqDTO reqDTO);

}
