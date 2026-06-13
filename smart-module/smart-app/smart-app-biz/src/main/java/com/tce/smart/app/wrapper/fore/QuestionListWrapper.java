package com.tce.smart.app.wrapper.fore;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.fore.QuestionListVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;

/**
 * 常见问题列表响应消息包装类
 *
 * @author mckaywu
 * @date 2019-06-10 20:04:17
 */
@Component
public class QuestionListWrapper extends BaseWrapper<AppSubject, QuestionListVo> {

	@Autowired
	AppSubjectService appSubjectService;

	@Override
	protected QuestionListVo warp(AppSubject appSubject) throws IOException {
		QuestionListVo vo = new QuestionListVo();
		vo.setQuestionId(appSubject.getId());
		vo.setQuestionTitle(appSubject.getSubjectName());
		return vo;
	}
}
