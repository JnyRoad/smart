package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.dto.AddAskLeavelApplicationDTO;
import com.tce.smart.platform.core.dto.SearchLeaveDTO;
import com.tce.smart.platform.core.entity.SmtAskLeaveApplication;
import com.tce.smart.platform.core.vo.SearchAskLeaveApplicationDetailVO;
import com.tce.smart.platform.core.vo.SearchAskLeaveApplicationVO;
import com.tce.smart.platform.core.vo.SearchAskLeaveTypeVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 请假申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
public interface SmtAskLeaveApplicationService extends IService<SmtAskLeaveApplication> {

	Page<SearchAskLeaveApplicationVO> getAskLeavePage(Page page, @Param("query") SmtAskLeaveApplication smtAskLeaveApplication);

	void add(AddAskLeavelApplicationDTO addAskLeavelApplicationDTO);

	List<SearchAskLeaveTypeVO> getAskTypeList();

	SearchAskLeaveApplicationDetailVO getAskLeaveById(Integer id);

	/**
	 * OA 审批结束修改状态
	 * @param badge badge
	 * @param code code
	 * @param id id
	 */
	void approvalNotice(String badge, String code, Integer id);

	Page<SearchAskLeaveApplicationVO> getAskLeavePageList(Page page, SearchLeaveDTO searchLeaveDTO);

	SearchAskLeaveApplicationDetailVO getAskLeaveByListId(Integer id);
}
