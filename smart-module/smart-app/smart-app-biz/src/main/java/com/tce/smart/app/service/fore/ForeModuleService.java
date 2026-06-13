package com.tce.smart.app.service.fore;

import com.tce.smart.app.vo.fore.ModuleListVo;

/**
 * 手机App模块服务接口
 *
 * @author mckaywu
 * @date 2019-06-13 19:20:24
 */
public interface ForeModuleService {

	/**
	 * 获取手机端模块信息
	 *
	 * @return 模块信息集合体
	 */
	ModuleListVo getForeModuleList();

}
