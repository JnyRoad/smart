package com.tce.smart.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.dto.AppQuestionDto;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.common.core.model.Result;

import java.util.List;

/**
 * 主题信息
 *
 * @author fushiping
 * @date 2019-04-25 09:44:43
 */
public interface AppSubjectService extends IService<AppSubject> {

	/**
	 * 通过主题类型与主题发布状态获取主题
	 * @param page
	 * @param publishFlag
	 * @param catalogCode
	 * @return
	 */
	IPage<AppSubject> getAppSubjectPage(Page page, String publishFlag, String catalogCode, Integer parkId);

	/**
	 * 通过主题类型与主题发布状态获取主题，过滤园区
	 * @param page 分页信息
	 * @param catalogCode 主题分类
	 * @return 分页返回
	 */
	IPage<AppSubject> getAppSubjectPageFilterByPark(Page page, String publishFlag, String catalogCode, Integer parkId);

	void filterSubjectByPark(IPage<AppSubject> pageInfo);

	/**
	 * 过滤当前用户不可见的主题
	 *
	 * @param subjectList 主题列表
	 * @return 当前用户可见主题集合
	 */
	List<Integer> getCurrUserSubject(List<AppSubject> subjectList);

	/**
	 * 主题上移
	 * @param id
	 */
	void moveUpwardById(Integer id);

	/**
	 * 主题下移
	 * @param id
	 * @return
	 */
	void moveDownById(Integer id);

	/**
	 * 主题置顶
	 * @param id
	 * @return
	 */
	void letTopById(Integer id);

	/**
	 * 取消置顶
	 * @param id
	 * @return
	 */
	void cancleTopById(Integer id);

	/**
	 * 批量上线
	 * @param ids
	 * @return
	 */
	void batchOnline(int[] ids);

	/**
	 * 批量待发布
	 * 下线
	 * @param id
	 * @return
	 */
	void offlineById(Integer id);

	/**
	 * @param ids
	 * @return
	 */
	void batchWait(int[] ids);

	/**
	 * 删除主题
	 * @param id
	 * @return
	 */
	void deleteById(Integer id);

	/**
	 * 批量删除
	 * @param ids
	 */
	void batchDelete(int[] ids);


	/**
	 * 查看主题内容
	 * @param id
	 * @return
	 */
	AppSubject subjectDetails(Integer id);

	/**
	 * 根据主题ID查找文本
	 * @param id
	 * @return
	 */
	AppContentText selectText(Integer id);

	/**
	 * 根据主题ID查找文本
	 * @param id
	 * @return
	 */
	AppContentText selectTextNew(Integer id);

	/**
	 * 获取已上线且已发布的主题总数
	 * @param catalogCode
	 * @return
	 */
	Integer selectOrderCount(String catalogCode);

	/**
	 * 修改主题
	 * @param addAppSubjectAo
	 */
	void subjectUpdate(AddAppSubjectAo addAppSubjectAo);

	/**
	 * 新增主题
	 * @param addAppSubjectAo
	 * @param calalogCode
	 * @return
	 */
	Integer subjectInsert(AddAppSubjectAo addAppSubjectAo, String calalogCode);

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
	 * 更新问题
	 * @param appQuestionDto
	 * @return
	 */
    Result updateQuestion(AppQuestionDto appQuestionDto);

	/**
	 * 显示问题详情
	 * @param id
	 * @return
	 */
	AppSubject detailQuestionById(Integer id);


	/**
	 * 根据分类编码查询主题信息
	 *
	 * @param catalogCode 主题分类
	 * @param publishFlag 发布状态
	 * @return List<AppSubject>
	 */
	List<AppSubject> selectByCatalogCode(String catalogCode,String publishFlag);

}
