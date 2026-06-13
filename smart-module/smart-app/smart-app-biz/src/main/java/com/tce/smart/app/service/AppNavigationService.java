package com.tce.smart.app.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.app.vo.AppNavigationVo;

import java.util.List;

/**
 * @author fushiping
 * @date 2019/6/18 16:51
 * APP导航菜单获取
 **/
public interface AppNavigationService extends IService<AppModuleInfo> {
	/**
	 * @return
	 * 获取导航菜单
	 */
	List<AppNavigationVo> getNavigationMenu();
}
