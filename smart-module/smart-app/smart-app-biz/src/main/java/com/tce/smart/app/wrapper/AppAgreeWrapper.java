package com.tce.smart.app.wrapper;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.app.entity.*;
import com.tce.smart.app.mapper.AppParkSubjectMapper;
import com.tce.smart.app.mapper.AppSubjectMapper;
import com.tce.smart.app.mapper.AppSubjectModuleMapper;
import com.tce.smart.app.service.AppContentTextService;
import com.tce.smart.app.service.AppModuleInfoService;
import com.tce.smart.app.service.AppSubjectContentTextService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.AppAgreeVo;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.feign.RemoteParkService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
@Component
public class AppAgreeWrapper extends BaseWrapper<AppSubject, AppAgreeVo> {
	@Autowired
	private AppSubjectContentTextService appSubjectContentTextService;
	@Autowired
	private AppContentTextService appContentTextService;
	@Autowired
	private AppParkSubjectMapper appParkSubjectMapper;
	@Autowired
	private AppSubjectModuleMapper appSubjectModuleMapper;
	@Autowired
	private AppSubjectService appSubjectService;
	@Autowired
	private AppSubjectMapper appSubjectMapper;
	@Autowired
	private RemoteParkService remoteParkService;
	@Autowired
	private AppModuleInfoService appModuleInfoService;
	@Override
	protected AppAgreeVo warp(AppSubject appSubject) throws IOException {
		AppAgreeVo vo = new AppAgreeVo();
		BeanUtils.copyProperties(appSubject, vo);
		Integer textId = appSubjectContentTextService.getTextById(appSubject.getId());
		vo.setContentTextId(textId);
		AppContentText appContentText = appContentTextService.getById(textId);
		vo.setTextDesc(appContentText.getTextDesc());
/*		List<AppParkSubject> listPark = appParkSubjectMapper.selectList(Wrappers.<AppParkSubject>query().lambda().eq(AppParkSubject::getSubjectId,appSubject.getId()));
		if(CollectionUtils.isNotEmpty(listPark)) {
			vo.setParkId(this.park(listPark));
		}

		AppSubjectModule module = appSubjectModuleMapper.selectOne(Wrappers.<AppSubjectModule>query().lambda().eq(AppSubjectModule::getSubjectId,appSubject.getId()));
		if(Objects.nonNull(module)) {
			AppModuleInfo moduleName = appModuleInfoService.getById(module.getModuleId());
			vo.setModule(moduleName.getModuleName());
		}

		vo.setPark(this.getpark(listPark));*/
		return vo;
	}

	private String park(List<AppParkSubject> listPark){
		String park = "";
		for(int i = 0;i < listPark.size();i ++){
			Result<SmtParkDTO> result = remoteParkService.getPakrById(listPark.get(i).getParkId(), SecurityConstants.FROM_IN);
			SmtParkDTO smtPark = result.getData();
			if(Objects.nonNull(smtPark)) {
				if(i == listPark.size()-1 && StrUtil.isNotEmpty(smtPark.getParkName()))
				{
					park += smtPark.getParkName();
				}
				else if(StrUtil.isNotEmpty(smtPark.getParkName())){
					park += smtPark.getParkName() + "、";
				}
			}
		}
		return park;
	}

	private List<Integer> getpark(List<AppParkSubject> listPark){
		List<Integer> list = new ArrayList<>();
		for(int i = 0;i < listPark.size();i ++){
			list.add(listPark.get(i).getParkId());
		}
		return list;
	}
}
