package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.dto.AppQuestionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.entity.AppSuggestInfo;
import com.tce.smart.app.mapper.AppSuggestInfoMapper;
import com.tce.smart.app.service.AppSuggestInfoService;

import java.util.List;

/**
 * 意见反馈
 *
 * @author mingkai.wu
 * @date 2019-04-25 11:32:25
 */
@Service
public class AppSuggestInfoServiceImpl extends ServiceImpl<AppSuggestInfoMapper, AppSuggestInfo> implements AppSuggestInfoService {
    @Autowired
	private  AppSuggestInfoMapper appSuggestInfoMapper;

	@Override
	public IPage<List<AppSuggestInfo>> getAppSuggestInfoPage(Page page, AppQuestionDto appQuestionDto) {
		IPage<List<AppSuggestInfo>> test = appSuggestInfoMapper.getAppSuggestInfoPage(page,appQuestionDto);
		List<List<AppSuggestInfo>> l= test.getRecords();
		int i = l.size();
		System.out.println(i);
		return appSuggestInfoMapper.getAppSuggestInfoPage(page,appQuestionDto);

	}
}
