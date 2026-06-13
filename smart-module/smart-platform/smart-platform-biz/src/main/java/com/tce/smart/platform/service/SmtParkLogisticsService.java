package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.SmtParkLogistics;

/**
 * 园区物流关系表
 *
 * @author mckaywu
 * @date 2019-11-20 10:36:33
 */
public interface SmtParkLogisticsService extends IService<SmtParkLogistics> {

	/**
	 * 根据园查询关联关系
	 *
	 * @param parkId 园区ID
	 * @return 园区物流关系表
	 */
	SmtParkLogistics getByParkId(Integer parkId);

	/**
	 * 根据园区删除关联关系
	 *
	 * @param parkId 园区ID
	 * @return Boolean true-成功，fasle-失败
	 */
	Boolean removeByParkId(Integer parkId);

	/**
	 * 修改园区关系(先删除再新增)
	 *
	 * @param parkId     园区ID
	 * @param logisticId 物流中心编号
	 * @return Boolean true-成功，fasle-失败
	 */
	Boolean saveParkLogistics(Integer parkId, String logisticId);

}
