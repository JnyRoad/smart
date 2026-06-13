package com.tce.smart.platform.service.admittance;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.platform.core.entity.admittance.SmtOaAreaType;

import java.util.List;

/**
 *
 *
 * @author fushiping
 * @date 2021-08-17 17:45:30
 */
public interface SmtOaAreaTypeService extends IService<SmtOaAreaType> {

	/**
	 * 根据oaCode获得区域类型
	 * @param code
	 * @return
	 */
	SmtOaAreaType getByCode(String code);


	/**
	 * oa手动区域同步
	 * @return
	 */
	Boolean syncArea(Integer type);

	/**
	 * oa区域定时同步任务
	 * @return
	 */
	Boolean syncOaTask();

	/**
	 * 获得所有区域类型
	 * @return
	 */
	List<SmtOaAreaType> getAreaType(Integer type);

	/**
	 * 根据value获得区域类型
	 * @param typeValue
	 * @return
	 */
	SmtOaAreaType getByValue(String typeValue, Integer type);


}
