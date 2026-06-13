package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.dto.AppQuestionDto;
import com.tce.smart.app.emun.DeleteState;
import com.tce.smart.app.emun.PublishState;
import com.tce.smart.app.emun.SubjectCatalog;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.entity.AppSubjectContentText;
import com.tce.smart.app.mapper.AppContentTextMapper;
import com.tce.smart.app.mapper.AppSubjectContentTextMapper;
import com.tce.smart.app.mapper.AppSubjectMapper;
import com.tce.smart.app.service.AppContentTextService;
import com.tce.smart.app.service.AppQuestionService;
import com.tce.smart.app.service.AppSubjectBasicService;
import com.tce.smart.app.service.AppSubjectContentTextService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.exception.TCEException;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class AppQuestionServiceImpl extends ServiceImpl<AppSubjectMapper, AppSubject> implements AppQuestionService {
	@Autowired
	private final AppSubjectContentTextMapper appSubjectContentTextMapper;

	@Autowired
	private final AppContentTextMapper appContentTextMapper;

	@Autowired
	private final AppSubjectMapper mapper;

	@Autowired
	private AppSubjectContentTextService appSubjectContentTextService;

	@Autowired
	private AppContentTextService appContentTextService;

	@Autowired
	private AppSubjectBasicService appSubjectBasicService;

	/**
	 * 分页显示所有问题
	 * @param page
	 * @param appQuestionDto
	 * @return
	 */
	@Override
	public IPage<AppSubject> getAppQuestionPage(Page page, AppQuestionDto appQuestionDto) {
		if(appQuestionDto.getSubjectName() != null){
			String s = '%'+appQuestionDto.getSubjectName()+'%';
			appQuestionDto.setSubjectName(s);
		}
		appQuestionDto.setCatalogCode(SubjectCatalog.QUESTION.type());
		return mapper.getAppQuestionPage(page,appQuestionDto);
	}
	/**
	 * 删除主题表中问题
	 * @param id
	 * @return
	 */
	@Override
	@Transactional(rollbackFor=Exception.class)
	public void deleteQuestion(Integer id) {
		AppSubject appSubject = this.getById(id);
		appSubject.setUpdateTime(LocalDateTime.now());
		appSubject.setDelFlag(DeleteState.DELETE.getCode());
		this.updateById(appSubject);
		Integer textId = this.getQuestionTextId(id);
		if(textId != null){
			this.deleteQuestionText(textId);
		}
	}

	/**
	 * 根据ID显示问题详情
	 * @param id
	 * @return
	 */
	@Override
	public AppSubject detailQuestionById(Integer id) {
		AppSubject appSubject = this.getById(id);
		return appSubject;
	}
	/**
	 * 根据传入参数添加新的主题
	 * @param appSubject
	 * @return
	 */
	@Override
	public Integer insertSubject(AppSubject appSubject) {
		//appSubject.setParentSubject(0);
		appSubject.setCreateTime(LocalDateTime.now());
		appSubject.setDelFlag(DeleteState.NORMOL.getCode());
		appSubject.setPublishFlag(PublishState.INIT.getCode());
		//appSubject.setSubjectOrder(1);
		appSubject.setSubjectUrl("");
		appSubject.setUpdateTime(LocalDateTime.now());
		this.save(appSubject);
		return appSubject.getId();
	}
	/**
	 * 根据关联表取出文本ID
	 * @param id
	 * @return
	 */
	public Integer getQuestionTextId(Integer id){
		AppSubjectContentText appSubjectContentText = appSubjectContentTextMapper.selectOne(Wrappers.<AppSubjectContentText>query().lambda().eq(AppSubjectContentText::getSubjectId,id));
		return appSubjectContentText.getContentTextId();

	}

	/**
	 * 删除问题文本表中的内容
	 * @param id
	 * @return
	 */
	public Result deleteQuestionText(Integer id){
		AppContentText appContentText = appContentTextMapper.selectById(id);
		appContentText.setDelFlag(DeleteState.DELETE.getCode());
		appContentText.setUpdateTime(LocalDateTime.now());
		appContentTextMapper.updateById(appContentText);
		return new Result<>(Boolean.TRUE, "删除问题答案成功");
	}

	/**
	 * 添加问题（三个表，主题表，主题配置表，内容表）
	 * @param appQuestionDto
	 * @return
	 */
	@Override
	@Transactional(rollbackFor=Exception.class)
	public Integer addQuestion(AppQuestionDto appQuestionDto) {
		this.checkQuestion(appQuestionDto);
		Integer subjectId = this.addQuestionSubject(appQuestionDto.getSubjectName());
		Integer textId = this.addQuestionText(appQuestionDto.getTextDesc());
		if(subjectId != null&&textId != null) {
			this.addQuestionContent(subjectId,textId);
		}
		return subjectId;
	}
	/**
	 * 更新问题信息，修改两个表（主题表和文本表）
	 * @param appQuestionDto
	 * @return
	 */
	@Override
	@Transactional(rollbackFor=Exception.class)
	public void updateQuestion(AppQuestionDto appQuestionDto) {
		this.checkQuestion(appQuestionDto);
		AppSubject appSubject = this.getById(appQuestionDto.getId());
		appSubject.setSubjectName(appQuestionDto.getSubjectName());
		appSubject.setUpdateTime(LocalDateTime.now());
		this.updateById(appSubject);
		AppContentText appContentText = appContentTextService.getById(appQuestionDto.getContentTextId());
		appContentText.setTextDesc(appQuestionDto.getTextDesc());
		appContentTextService.updateById(appContentText);
	}
	/**
	 * 添加主题表
	 * @param question
	 * @return
	 */
	private Integer addQuestionSubject(String question){
		AppSubject appSubject=new AppSubject();
		appSubject.setSubjectName(question);
		appSubject.setCatalogCode(SubjectCatalog.QUESTION.type());
		return this.insertSubject(appSubject);
	}
	/**
	 * 添加问题内容表
	 * @param answer
	 * @return
	 */
	@Override
	public Integer addQuestionText(String answer){
		AppContentText appContentText = new AppContentText();
		appContentText.setTextOrder(null);
		appContentText.setPicBinary(null);
		appContentText.setDelFlag(DeleteState.NORMOL.getCode());
		appContentText.setTextDesc(answer);
		appContentText.setTextName("null");
		appContentText.setCreateTime(LocalDateTime.now());
		appContentText.setUpdateTime(LocalDateTime.now());
		appContentTextService.save(appContentText);
		return appContentText.getId();
	}
	/**
	 * 添加问题答案关联表
	 * @param subjectId
	 * @param textId
	 * @return
	 */
	private Result addQuestionContent(Integer subjectId,Integer textId){
		AppSubjectContentText appSubjectContentText = new AppSubjectContentText();
		appSubjectContentText.setSubjectId(subjectId);
		appSubjectContentText.setContentTextId(textId);
		return new Result<>(appSubjectContentTextService.save(appSubjectContentText));
	}

	private void checkQuestion(AppQuestionDto appQuestionDto){
		String regex = "[ ]|[\\u4E00-\\u9FA5a-zA-Z\\d\\s]{0,30}";
		if(!appQuestionDto.getSubjectName().matches(regex) || appQuestionDto.getSubjectName().trim().length() < 3 || appQuestionDto.getSubjectName().trim().length() > 30
		) {
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_NAME_ERROR);
		}
		Integer l = appSubjectBasicService.num(appQuestionDto.getTextDesc());
		if(StringUtils.isEmpty(appQuestionDto.getTextDesc()) || l == 0) {
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_TEXT_NULL);
		}
	}
}
