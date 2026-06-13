package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtVisitJcheLimit;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2020-08-06 15:30:50
 */
public interface SmtVisitJcheLimitService extends IService<SmtVisitJcheLimit> {

	/**
	 * 根据园区id获得限制级层列表
	 * @param parkId
	 * @return
	 */
	List<SmtVisitJcheLimit> listByParkId(Integer parkId, Integer type);

	/**
	 * 根据级层id获取限制级层列表
	 * @param jcheId
	 * @return
	 */
	List<SmtVisitJcheLimit> listByJcheId(Integer parkId, String jcheId, Integer type);

	/**
	 * 根据园区Id获得限制级层列表
	 * @param parkId
	 * @return
	 */
	List<String> getJcheIds(Integer parkId, Integer type);

	/**
	 * 根据园区id移除所有级层
	 * @param parkId
	 * @return
	 */
	Boolean removeByParkId(Integer parkId, Integer type);

	/**
	 * 新增级层限制
	 * @param parkId
	 * @param jcheList
	 * @return
	 */
	Boolean saveList(Integer parkId, List<String> jcheList, Integer type);

	IPage getList(Page page, Integer type);

}
