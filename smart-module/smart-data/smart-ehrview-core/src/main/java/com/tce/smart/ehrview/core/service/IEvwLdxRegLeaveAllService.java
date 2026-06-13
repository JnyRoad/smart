package com.tce.smart.ehrview.core.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.ehrview.core.entity.EvwLdxRegLeaveAll;

import java.util.List;

/**
 * @Auther: guohongtai
 * @Date: 2020-07-22 11:37
 */

public interface IEvwLdxRegLeaveAllService extends IService<EvwLdxRegLeaveAll> {

	List<EvwLdxRegLeaveAll> list(String badge, String queryMonth);

	/**
	 * 根据日期查询调休记录
	 * @param badge
	 * @param queryMonth
	 * @return
	 */
	List<EvwLdxRegLeaveAll> listByDay(String badge, String queryMonth);
}
