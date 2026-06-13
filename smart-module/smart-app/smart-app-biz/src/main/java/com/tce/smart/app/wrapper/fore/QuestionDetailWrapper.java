package com.tce.smart.app.wrapper.fore;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppContentTextService;
import com.tce.smart.app.service.AppSubjectContentTextService;
import com.tce.smart.app.vo.fore.QuestionDetailVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;

/**
 * 常见问题列表详情
 *
 * @author mckaywu
 * @date 2019-06-10 20:31:54
 */
@Component
public class QuestionDetailWrapper extends BaseWrapper<AppSubject, QuestionDetailVo> {
	@Autowired
	private AppSubjectContentTextService appSubjectContentTextService;
	@Autowired
	private AppContentTextService appContentTextService;

	@Override
	protected QuestionDetailVo warp(AppSubject appSubject) throws IOException {
		QuestionDetailVo vo = new QuestionDetailVo();
		vo.setQuestionId(appSubject.getId());
		vo.setQuestionTitle(appSubject.getSubjectName());
		Integer textId = appSubjectContentTextService.getTextById(appSubject.getId());
		if (textId > 0) {
			AppContentText appContentText = appContentTextService.getById(textId);
			String s = appContentText.getTextDesc();
			if (s != null) {
				vo.setAnswerContent(appContentText.getTextDesc());
			}
		}
		return vo;
	}
}
