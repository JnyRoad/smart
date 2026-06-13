package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.entity.AppSubjectContentPicture;
import com.tce.smart.app.entity.AppSubjectModule;
import com.tce.smart.app.mapper.AppSubjectContentPictureMapper;
import com.tce.smart.app.mapper.AppSubjectModuleMapper;
import com.tce.smart.app.service.AppSubjectContentPictureService;
import com.tce.smart.app.service.AppSubjectModuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppSubjectModuleServiceImpl extends ServiceImpl<AppSubjectModuleMapper, AppSubjectModule> implements AppSubjectModuleService {
	@Autowired
	private AppSubjectModuleMapper appSubjectModuleMapper;
	@Override
	public AppSubjectModule getMoudleById(Integer id) {
		AppSubjectModule appSubjectModule = appSubjectModuleMapper.selectOne(Wrappers.<AppSubjectModule>query().lambda().eq(AppSubjectModule::getSubjectId,id));
		return appSubjectModule;
	}

	@Override
	public void deleteModule(Integer subjectId) {
		appSubjectModuleMapper.deleteModule(subjectId);
	}
}
