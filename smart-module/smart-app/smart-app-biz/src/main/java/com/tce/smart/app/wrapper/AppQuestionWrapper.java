package com.tce.smart.app.wrapper;

import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppContentTextService;
import com.tce.smart.app.service.AppSubjectContentTextService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.AppQuestionVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class AppQuestionWrapper extends BaseWrapper<AppSubject, AppQuestionVo> {
	@Autowired
	private AppSubjectContentTextService appSubjectContentTextService;
	@Autowired
	private AppContentTextService appContentTextService;
	@Override
	protected AppQuestionVo warp(AppSubject appSubject) throws IOException {
		AppQuestionVo vo = new AppQuestionVo();
		BeanUtils.copyProperties(appSubject, vo);
		Integer textId = appSubjectContentTextService.getTextById(appSubject.getId());
		if(textId > 0) {
			vo.setContentTextId(textId);
			AppContentText appContentText = appContentTextService.getById(textId);
			String s = appContentText.getTextDesc();
			if(s != null) {
				vo.setTextDesc(appContentText.getTextDesc());
			}
		}
		return vo;
	}
}
