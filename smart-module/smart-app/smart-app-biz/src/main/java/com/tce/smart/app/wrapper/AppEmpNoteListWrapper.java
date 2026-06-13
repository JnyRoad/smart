package com.tce.smart.app.wrapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppParkSubject;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppParkSubjectService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.AppEmployeeNoteListVo;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.feign.RemoteParkService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 新员工须知列表
 * @author fushiping
 * @date 2019/10/15 14:01
 **/
@Component
public class AppEmpNoteListWrapper extends BaseWrapper<AppSubject, AppEmployeeNoteListVo> {

	@Autowired
	AppSubjectService appSubjectService;
	@Autowired
	private RemoteParkService remoteParkService;
	@Autowired
	private AppParkSubjectService appParkSubjectService;
	@Autowired
	AppCommService appCommService;

	@Override
	protected AppEmployeeNoteListVo warp(AppSubject appSubject) throws IOException {
		AppEmployeeNoteListVo vo=new AppEmployeeNoteListVo();
		BeanUtils.copyProperties(appSubject,vo);
		AppContentText appContentText=appSubjectService.selectText(appSubject.getId());
		if(appContentText!=null){
			if(appContentText.getPicBinary()!=null){
				vo.setPicBinary(appCommService.buildConentTextImageUrl(appContentText.getId()));
			}
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
