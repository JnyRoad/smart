package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtArea;
import com.tce.smart.platform.core.vo.SearchAreaVO;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 *
 * @author 齐佩
 * @date 2019-04-13 13:48:18
 */
public interface SmtAreaMapper extends BaseMapper<SmtArea> {

	IPage<SearchAreaVO> getSmtAreaPage(Page page, @Param("query")SmtArea smtArea, @Param("parkIdList") List<Integer> parkIdList);

	List<SmtArea> getSmtAreaAll();

	List<SmtArea> getSmtAreaAll(SmtArea area);
}
