package com.tce.smart.platform.core.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.SearchBreakOffDTO;
import com.tce.smart.platform.core.entity.SmtBreakoffApplication;
import com.tce.smart.platform.core.vo.SearchBreakoffApplicationVO;

/**
 * 职工调休申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:30:08
 */
public interface SmtBreakoffApplicationMapper extends BaseMapper<SmtBreakoffApplication> {

	Page<SearchBreakoffApplicationVO> getSmtBreakoffApplicationPage(Page page, @Param("query") SmtBreakoffApplication smtBreakoffApplication);

	Page<SearchBreakoffApplicationVO> getSmtBreakoffApplicationPageList(Page page,@Param("query") SearchBreakOffDTO searchBreakOffDTO);

}
