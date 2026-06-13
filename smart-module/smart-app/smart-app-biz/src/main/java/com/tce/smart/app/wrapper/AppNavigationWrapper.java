package com.tce.smart.app.wrapper;

import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.app.vo.AppNavigationVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author fushiping
 * @date 2019/6/19 08:42
 **/

@Component
public class AppNavigationWrapper extends BaseWrapper<AppModuleInfo, AppNavigationVo> {
	@Override
	protected AppNavigationVo warp(AppModuleInfo appModuleInfo) throws IOException {
		AppNavigationVo vo = new AppNavigationVo();
		BeanUtils.copyProperties(appModuleInfo, vo);
		if(appModuleInfo.getModuleIcon() != null) {
			String s = new String(appModuleInfo.getModuleIcon());
			vo.setModuleIcon(s);
		}
		return vo;
	}
}
