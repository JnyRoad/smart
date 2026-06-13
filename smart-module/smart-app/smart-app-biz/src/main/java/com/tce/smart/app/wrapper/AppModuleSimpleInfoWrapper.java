package com.tce.smart.app.wrapper;

import java.io.IOException;

import org.springframework.stereotype.Component;

import com.tce.smart.app.api.vo.AppModuleSimpleInfoVo;
import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.common.core.wrapper.BaseWrapper;

@Component
public class AppModuleSimpleInfoWrapper extends BaseWrapper<AppModuleInfo, AppModuleSimpleInfoVo> {

	@Override
	protected AppModuleSimpleInfoVo warp(AppModuleInfo appModuleInfo) throws IOException {
		AppModuleSimpleInfoVo vo = new AppModuleSimpleInfoVo();
		vo.setId(appModuleInfo.getId());
		vo.setModuleName(appModuleInfo.getModuleName());
		return vo;
	}
}
