package com.tce.smart.platform.core.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.entity.SmtWhiteJob;
import com.tce.smart.platform.core.vo.WhiteJobVO;

public interface SmtWhiteJobMapper extends BaseMapper<SmtWhiteJob> {

	IPage<WhiteJobVO> page(Page page, @Param("query") SmtWhiteJob smtWhiteJob,@Param("park") List<Integer> parkIdList);

}
