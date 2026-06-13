package com.tce.smart.app.wrapper;

import com.tce.smart.app.entity.AppModuleInfo;
import com.tce.smart.app.service.AppServeService;
import com.tce.smart.app.vo.AppServeVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class AppServeWrapper extends BaseWrapper<AppModuleInfo, AppServeVo> {
	@Autowired
	private AppServeService appServeService;
	@Override
	protected AppServeVo warp(AppModuleInfo appModuleInfo) throws IOException {
		AppServeVo vo = new AppServeVo();
		BeanUtils.copyProperties(appModuleInfo, vo);
        Integer id = appModuleInfo.getId();
		vo.setModule(appServeService.getAllMenu(id));
		return vo;
	}
}
