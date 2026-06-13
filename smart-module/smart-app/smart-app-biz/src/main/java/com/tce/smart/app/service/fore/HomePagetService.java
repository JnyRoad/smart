package com.tce.smart.app.service.fore;

import java.util.List;

import com.tce.smart.app.vo.fore.SubModuleDetailVo;

/**
 * App首页服务接口
 *
 * @author mckaywu
 * @date 2019-06-19 10:28:16
 */
public interface HomePagetService {

	/**
	 * 获取导航菜单
	 *
	 * @param parkId 园区ID
	 * @return 模块信息
	 */
	List<SubModuleDetailVo> getNavigateModule(String parkId);

}
