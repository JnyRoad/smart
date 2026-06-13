package com.tce.smart.app.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.entity.AppSubject;

import java.util.List;

/**
 * Banner管理
 * @author fushiping
 * @date 2019/5/22 13:52
 **/
public interface AppBannerService  extends IService<AppSubject> {

	/**
	 * 获取Banner列表
	 * @param page
	 * @return
	 */
	IPage<AppSubject> getBannerList(Page page);

	/**
	 * 获取已上线Banner列表
	 * @return
	 */
	List<AppSubject> getOnlineBannerList();

	/**
	 * 添加主题
	 * @param addAppSubjectAo
	 * @return
	 */
	Integer addSubject(AddAppSubjectAo addAppSubjectAo);

	/**
	 * 新增主题
	 * @param addAppSubjectAo
	 */
	void updateSubject(AddAppSubjectAo addAppSubjectAo);

	/**
	 * 上线
	 * @param id
	 * @return
	 */
	void onlineById(Integer id);

	/**
	 * 取消上线
	 * @param id
	 * @return
	 */
	void waitById(Integer id);

	/**
	 * 删除主题
	 * @param id
	 * @return
	 */
	void deleteById(Integer id);


}
