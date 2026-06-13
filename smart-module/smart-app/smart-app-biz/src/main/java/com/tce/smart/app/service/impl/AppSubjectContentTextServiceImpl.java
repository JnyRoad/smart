package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.entity.AppContentPicture;
import com.tce.smart.app.entity.AppSubjectContentText;
import com.tce.smart.app.mapper.AppSubjectContentTextMapper;
import com.tce.smart.app.service.AppContentTextService;
import com.tce.smart.app.service.AppSubjectContentTextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 主题文本内容
 *
 * @author fushiping
 * @date 2019-04-25 09:45:24
 */
@Service
public class AppSubjectContentTextServiceImpl extends ServiceImpl<AppSubjectContentTextMapper, AppSubjectContentText> implements AppSubjectContentTextService {
	@Autowired
	private AppSubjectContentTextService subjectContentTextService;

	@Autowired
	private AppContentTextService contentTextService;

	@Override
	public Integer getTextById(Integer subjectId) {
		AppSubjectContentText appSubjectContentText = subjectContentTextService.getBaseMapper().selectOne(Wrappers.<AppSubjectContentText>query().lambda().eq(AppSubjectContentText::getSubjectId,subjectId));
		return appSubjectContentText != null ?appSubjectContentText.getContentTextId():0;
	}

	/**
	 * 添加主题与文本内容映射
	 * @param textId
	 * @param subjectId
	 * @return
	 */
	public void insertTextInSubject(Integer textId, Integer subjectId) {
		AppSubjectContentText appSubjectContentText = new AppSubjectContentText();
		appSubjectContentText.setContentTextId(textId);
		appSubjectContentText.setSubjectId(subjectId);
		appSubjectContentText.insert();
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean cascadeDelete(Integer subjectId) {
		List<AppSubjectContentText> rsList = this.list(Wrappers.<AppSubjectContentText>query().lambda().eq(AppSubjectContentText::getSubjectId,subjectId));
		if(CollectionUtils.isNotEmpty(rsList)){
			for(AppSubjectContentText element : rsList){
//				//删除主题文本内容
//				element.deleteById();
				//删除文本内
				contentTextService.deleteTextContent(element.getContentTextId());
			}
		}

		return Boolean.TRUE;
	}

}
