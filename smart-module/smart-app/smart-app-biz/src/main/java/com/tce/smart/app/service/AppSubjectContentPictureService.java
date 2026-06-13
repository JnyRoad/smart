package com.tce.smart.app.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.entity.AppSubjectContentPicture;

/**
 * 主题图片内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:02
 */
public interface AppSubjectContentPictureService extends IService<AppSubjectContentPicture> {

	/**
	 * 根据主题Id查询
	 *
	 * @param subjectId
	 *
	 * @return List<AppSubject>
	 */
	List<AppSubjectContentPicture> selectBySubjectId(Integer subjectId);

	/**
	 * 根基主题ID查询单个
	 * @param subjectId
	 * @return
	 */
	Integer  getBySubjectId(Integer subjectId);

	/**
	 *  根据主题ID级联删除主题-图片内容，图片内容，
	 * @param subjectId 主题ID
	 * @return 成功-true,失败-false
	 */
	Boolean cascadeDelete(Integer subjectId);
}
