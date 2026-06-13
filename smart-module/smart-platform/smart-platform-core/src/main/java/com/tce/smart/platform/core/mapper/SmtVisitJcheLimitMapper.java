package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtVisitJcheLimit;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2020-08-06 15:30:50
 */
public interface SmtVisitJcheLimitMapper extends BaseMapper<SmtVisitJcheLimit> {

	IPage<SmtVisitJcheLimit> getPage(Page page, @Param("parkId") List<Integer> parkId, @Param("type") Integer type);

}
