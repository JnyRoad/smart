package com.tce.smart.app.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.dto.AppQuestionDto;
import com.tce.smart.app.entity.AppSuggestInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 意见反馈
 *
 * @author mingkai.wu
 * @date 2019-04-25 11:32:25
 */
@Mapper
public interface AppSuggestInfoMapper extends BaseMapper<AppSuggestInfo> {
	IPage<List<AppSuggestInfo>> getAppSuggestInfoPage(Page page, @Param("query") AppQuestionDto appQuestionDto);
}
