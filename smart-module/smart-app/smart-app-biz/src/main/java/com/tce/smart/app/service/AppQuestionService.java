package com.tce.smart.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.dto.AppQuestionDto;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.common.core.model.Result;

public interface AppQuestionService {
	/**
	 * 查询所有问题
	 * @param page
	 * @param appQuestionDto
	 * @return
	 */
	IPage<AppSubject> getAppQuestionPage(Page page, AppQuestionDto appQuestionDto);

	/**
	 * 删除问题
	 * @param id
	 * @return
	 */
	void deleteQuestion(Integer id);

	/**
	 * 添加问题
	 * @param appSubject
	 * @return
	 */
	Integer insertSubject(AppSubject appSubject);
	/**
	 * 显示问题详情
	 * @param id
	 * @return
	 */
	AppSubject detailQuestionById(Integer id);


	Integer addQuestion(AppQuestionDto appQuestionDto);

	void updateQuestion(AppQuestionDto appQuestionDto);

	Integer addQuestionText(String answer);
}
