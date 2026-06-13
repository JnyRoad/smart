package com.tce.smart.app.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tce.smart.app.entity.AppContentPicture;
import com.tce.smart.app.service.AppLauchService;
import com.tce.smart.app.service.AppSubjectService;

import lombok.AllArgsConstructor;

/**
 * App启动引导服务实现类
 *
 * @author mingkai.wu
 * @date 2019-05-12 18:04:23
 */
@Service
@AllArgsConstructor
public class AppLaunchServiceImpl implements AppLauchService {

//	@Autowired
//	private AppSubjectService subjectService;
//
//	private AppSubjectContentPictureServiceImpl subjectContentPictureService;
//
//	private AppContentPicture contentPicture;

	@Override
	public List<AppContentPicture> getLauchInfo() {
		return null;
	}
}
