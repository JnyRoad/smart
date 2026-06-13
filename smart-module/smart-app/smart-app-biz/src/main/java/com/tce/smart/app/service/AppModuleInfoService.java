package com.tce.smart.app.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.entity.AppModuleInfo;

/**
 * @author fushiping
 * @date 2019/5/21 13:32
 **/
public interface AppModuleInfoService extends IService<AppModuleInfo> {
	Integer getIdByName(String name);

	/**
	 * 获取所有顶级模块
	 *
	 * @return 所有可用顶级模块
	 */
	List<AppModuleInfo> getTopModule();

	/**
	 * 获取指定分类顶级模块
	 *
	 * @param catalogCode 业务分类模块
	 * @return 所有可用顶级模块
	 */
	List<AppModuleInfo> getTopModule(Integer catalogCode);

	/**
	 * 获取子模块模块列表
	 *
	 * @param parentId 父级模块ID
	 * @return 所有可用顶级模块
	 */
	List<AppModuleInfo> getSubModuleByPid(Integer parentId);

	/**
	 * 获取子模块模块列表
	 *
	 * @param moduleIdList 模块ID列表
	 * @return 模块信息
	 */
	List<AppModuleInfo> getSubModuleByIds(List<Integer> moduleIdList);
}
