package com.tce.smart.platform.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SearchLeaveDTO;
import com.tce.smart.platform.core.entity.SmtAskLeaveApplication;
import com.tce.smart.platform.core.entity.SmtBreakoffApplication;
import com.tce.smart.platform.core.vo.SearchAskLeaveApplicationVO;

/**
 * 职工请假申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:26:36
 */
public interface SmtAskLeaveApplicationMapper extends BaseMapper<SmtAskLeaveApplication> {

	Page<SearchAskLeaveApplicationVO> getAskLeavePage(Page page, @Param("query") SmtAskLeaveApplication smtAskLeaveApplication);

	Page<SearchAskLeaveApplicationVO> getAskLeavePageList(Page page, @Param("query") SearchLeaveDTO searchLeaveDTO);

	List<SmtBreakoffApplication> selectBreakoffApplication( @Param("query") SmtBreakoffApplication breakOff);


}
