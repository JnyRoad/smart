package com.tce.smart.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.dto.AppQuestionDto;
import com.tce.smart.app.entity.AppSuggestInfo;

import java.util.List;

/**
 * 意见反馈
 *
 * @author mingkai.wu
 * @date 2019-04-25 11:32:25
 */
public interface AppSuggestInfoService extends IService<AppSuggestInfo> {

	IPage<List<AppSuggestInfo>> getAppSuggestInfoPage(Page page, AppQuestionDto appQuestionDto);

}
