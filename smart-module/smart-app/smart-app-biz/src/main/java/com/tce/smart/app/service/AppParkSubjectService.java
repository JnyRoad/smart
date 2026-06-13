package com.tce.smart.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.entity.AppParkSubject;

import java.util.List;

/**
 * 园区主题
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:44:25
 */
public interface AppParkSubjectService extends IService<AppParkSubject> {
	void deletePark(Integer id);

	/**
	 * 获取园区主题配置信息
	 *
	 * @param parkId    园区ID
	 * @param subjectId 主题ID
	 * @return
	 */
	List<AppParkSubject> getByUnionId(Integer parkId, Integer subjectId);

	/**
	 * 根据主题查询指定所属园区
	 *
	 * @param subjectIds 主题ID集合
	 * @return 园区园区集合
	 */
	List<AppParkSubject> getBySubjectIds(List<Integer> subjectIds);
}
