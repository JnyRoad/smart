package com.tce.smart.platform.core.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SearchOverTimeDTO;
import com.tce.smart.platform.core.entity.SmtOvertimeApplication;
import com.tce.smart.platform.core.vo.SearchOverTimeApplicationVO;

/**
 * 职工加班申请表

 *
 * @author 梁圆
 * @date 2019-04-13 18:20:11
 */
public interface SmtOvertimeApplicationMapper extends BaseMapper<SmtOvertimeApplication> {

	Page<SearchOverTimeApplicationVO> getOvertimeApplicationPage(Page page, @Param("query") SmtOvertimeApplication smtOvertimeApplication);

	Page<SearchOverTimeApplicationVO> getOvertimeApplicationPageList(Page page, @Param("query") SearchOverTimeDTO searchLeaveDTO);

}
