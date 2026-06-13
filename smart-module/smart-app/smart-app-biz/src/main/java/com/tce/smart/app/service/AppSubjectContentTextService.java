package com.tce.smart.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.entity.AppSubjectContentText;

/**
 * 主题文本内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:24
 */
public interface AppSubjectContentTextService extends IService<AppSubjectContentText> {
    Integer getTextById(Integer subjectId);

	/**
	 * 添加主题与文本内容映射
	 * @param textId
	 * @param subjectId
	 * @return
	 */
    void insertTextInSubject(Integer textId, Integer subjectId);

	/**
	 *  根据主题ID级联删除主题-文本内容，文本内容，
	 * @param subjectId 主题ID
	 * @return 成功-true,失败-false
	 */
	Boolean cascadeDelete(Integer subjectId);
}
