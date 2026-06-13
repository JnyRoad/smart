package com.tce.smart.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.ao.AppealAreaAo;
import com.tce.smart.app.ao.EmployeeNoteAo;
import com.tce.smart.app.ao.fore.AppSubjectAO;
import com.tce.smart.app.ao.fore.AppSubjectDetailAO;
import com.tce.smart.app.entity.AppSubject;

import java.io.IOException;

/**
 * @description: 申诉专区
 * @date: 2020-07-28 11:56
 * @author: wuling
 * @version: 1.0
 */
public interface AppealAreaService{

	/**
	 * 通过主题类型与主题发布状态获取当前登录用户关联的园区的主题
	 * @param page
	 * @param appealAreaAo
	 * @return
	 */
	IPage<AppSubject> getAppSubjectPage(Page page, AppealAreaAo appealAreaAo);


	/**
	 * app端登录用户获取关联的园区的主题
	 * @param page
	 * @param appealAreaAo
	 * @return
	 */
	IPage<AppSubjectAO> getAppSubjectPageByApp(Page page, AppealAreaAo appealAreaAo);

	/**
	 * 添加申诉区文章
	 * 1. 添加subject记录
	 * 2. 添加content记录
	 * 3. 添加subject与content关系记录
	 * @param addAppSubjectAo
	 * @return
	 */
	boolean addAppealAreaArticle(AddAppSubjectAo addAppSubjectAo);


	/**
	 * 更新内容
	 * @param employeeNoteAo
	 */
	boolean updateAppealArticle(AddAppSubjectAo addAppSubjectAo);

	/**
	 * 获取申诉文章详情
	 * @param id
	 * @return
	 */
	AppSubject getAppealArticleDetail(Integer id);

	/**
	 * 删除申诉文章记录
	 * @param id
	 * @return
	 */
	boolean delAppealArticleRecord(Integer id);

	/**
	 * APP端获取文章详情
	 * @param id
	 * @return
	 */
	AppSubjectDetailAO noteDetailByApp(Integer id) throws IOException;
}
