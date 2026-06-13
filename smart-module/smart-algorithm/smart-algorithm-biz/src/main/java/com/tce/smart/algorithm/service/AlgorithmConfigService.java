package com.tce.smart.algorithm.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.algorithm.api.dto.req.UpdateAlgorithmConfigDTO;
import com.tce.smart.algorithm.entity.AlgorithmConfig;

import java.util.List;

/**
 * @InterfaceName ConfigService
 * @Description TODD
 * @Author bingeox
 * @Date 2019\8\1 0001 14:27
 * Version 1.0
 **/
public interface AlgorithmConfigService extends IService<AlgorithmConfig> {

	/**
	 * 根据算法类型获取配置类
	 * @param algorithmType
	 * @return
	 */
	AlgorithmConfig getByAlgorithmType(String algorithmType);

	/**
	 * 根据算法类型获取配置信息
	 * @param algorithmType
	 * @param tClass
	 * @param <T>
	 * @return
	 */
	<T> T getAlgorithmProperties(String algorithmType, Class<T> tClass);

	/**
	 * 分页查询
	 * @param page
	 * @param algorithmType
	 * @return
	 */
	IPage<AlgorithmConfig> getPageList(Page<AlgorithmConfig> page, String algorithmType);

	/**
	 * 修改算法配置
	 * @param updateAlgorithmConfigDTO
	 * @return
	 */
	AlgorithmConfig updateConfig(UpdateAlgorithmConfigDTO updateAlgorithmConfigDTO);

	/**
	 * 获取所有算法
	 * @param type
	 * @return
	 */
	List<AlgorithmConfig> getList(String type);
}
