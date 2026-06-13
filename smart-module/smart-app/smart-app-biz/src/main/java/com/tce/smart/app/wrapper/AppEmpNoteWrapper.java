package com.tce.smart.app.wrapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppParkSubject;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppParkSubjectService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.AppEmployeeNoteVo;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.feign.RemoteParkService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AppEmpNoteWrapper extends BaseWrapper<AppSubject, AppEmployeeNoteVo>{
	@Autowired
	private AppSubjectService appSubjectService;
	@Autowired
	private RemoteParkService remoteParkService;
	@Autowired
	private AppParkSubjectService appParkSubjectService;
	@Autowired
	private AppCommService appCommService;

	@Override
	protected AppEmployeeNoteVo warp(AppSubject appSubject) throws IOException{
		AppEmployeeNoteVo vo=new AppEmployeeNoteVo();
		BeanUtils.copyProperties(appSubject,vo);
		AppContentText appContentText=appSubjectService.selectText(appSubject.getId());
		if(appContentText!=null){
			if(appContentText.getPicBinary()!=null){
			vo.setPicBinary(appCommService.buildConentTextImageUrl(appContentText.getId()));
			}
			if(appContentText.getEnclosureName() != null) {
				vo.setEnclosureName(appContentText.getEnclosureName());
			}
			if(appContentText.getEnclosure()!=null){
				vo.setEnclosure(new String(appContentText.getEnclosure()));
			}
			vo.setTextDesc(appContentText.getTextDesc());
			vo.setTextName(appContentText.getTextName());
		}

		AppParkSubject appParkSubject =
				appParkSubjectService.getOne(Wrappers.<AppParkSubject>query().lambda()
						.eq(AppParkSubject::getSubjectId, appSubject.getId()));
		if (appParkSubject != null) {
			Result<SmtParkDTO> result = remoteParkService.getPakrById(appParkSubject.getParkId(), SecurityConstants.FROM_IN);
			SmtParkDTO smtPark = result.getData();
			vo.setParkId(smtPark.getId());
			vo.setParkName(smtPark.getParkName());
		}
		return vo;
		}
}
