package com.tce.smart.app.wrapper;

import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.AppBannerVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author fushiping
 * @date 2019/5/22 14:25
 **/

@Component
public class AppBannerWrapper extends BaseWrapper<AppSubject, AppBannerVo> {

	@Autowired
	private AppSubjectService appSubjectService;

	@Override
	protected AppBannerVo warp(AppSubject appSubject) throws IOException {
		AppBannerVo vo = new AppBannerVo();
		BeanUtils.copyProperties(appSubject, vo);
		AppContentText appContentText = appSubjectService.selectText(appSubject.getId());
		if (appContentText != null) {
			if (appContentText.getPicBinary() != null) {
				vo.setPicBinary(new String(appContentText.getPicBinary()));
			}
			vo.setTextName(appContentText.getTextName());
		}
		return vo;
	}
}
