package com.tce.smart.platform.core.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.core.dto.FeedBackQueryDTO;
import com.tce.smart.platform.core.entity.SmtFeedBack;

public interface SmtFeedBackMapper  extends BaseMapper<SmtFeedBack> {

	IPage<SmtFeedBack> selectPage(Page page,  @Param("query") FeedBackQueryDTO feedBackQueryDTO);

}
