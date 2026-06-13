package com.tce.smart.platform.core.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtTravelApplication;
import com.tce.smart.platform.core.vo.SearchTravelApplicationVO;

/**
 * 出差申请表
 *
 * @author 梁圆
 * @date 2019-04-13 18:19:00
 */
public interface SmtTravelApplicationMapper extends BaseMapper<SmtTravelApplication> {

	Page<SearchTravelApplicationVO> getSmtTravelApplicationPage(Page page, @Param("query") SmtTravelApplication smtTravelApplication);

}
