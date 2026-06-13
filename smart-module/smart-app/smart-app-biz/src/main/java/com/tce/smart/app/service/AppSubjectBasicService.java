package com.tce.smart.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;



/**
 * App公共功能基础实现
 * @author fushiping
 * @date 2019/5/29 09:20
 **/
public interface AppSubjectBasicService extends IService<AppSubject> {

	/**
	 * 修改主题
	 * @param addAppSubjectAo
	 */
	void updateSubject(AddAppSubjectAo addAppSubjectAo);

	/**
	 * 新增主题
	 * @param addAppSubjectAo
	 * @param CatalogCode
	 * @return
	 */
	Integer addSubject(AddAppSubjectAo addAppSubjectAo, String CatalogCode);

	/**
	 * 添加主题内容
	 * @param addAppSubjectAo
	 * @param calalogCode
	 * @return
	 */
	Integer insertSubjectContent(AddAppSubjectAo addAppSubjectAo, String calalogCode);

	/**
	 * 修改主题内容
	 * @param addAppSubjectAo
	 * @return
	 */
	void updateSubjectContent(AddAppSubjectAo addAppSubjectAo);


	/**
	 * 测试链接是否符合规范
	 * @param url
	 * @return
	 */
	boolean isURL(String url);

	Integer num(String str);

	/**
	 * 判断内容类型
	 *
	 * @param appSubject     主题
	 * @param appContentText 文本内容
	 * @return 1 自定义URL链接跳转,2 内容详情页跳转,3 APP模块跳转，4 PDF附件
	 */
	Integer getContentLinkType(AppSubject appSubject, AppContentText appContentText);

	/**
	 * 将文本中的base64字符串保存到图片表
	 *
	 * @param subjectId 主题id
	 * @param picName 图片名称
	 * @param contentText 图文并茂内容
	 * @return
	 */
	String replaceSaveBase64ContentImg(Integer subjectId, String picName, String contentText);
}
