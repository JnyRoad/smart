package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtBlackVisitor;
import com.tce.smart.platform.core.vo.BlackVisitorVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SmtBlackVisitorMapper extends BaseMapper<SmtBlackVisitor>  {

	IPage<BlackVisitorVO> page(Page page, @Param("query") SmtBlackVisitor smtBlackVisitor, @Param("park") List<Integer> parkIdList);

}
