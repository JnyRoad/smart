package com.tce.smart.app.wrapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppParkSubject;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppParkSubjectService;
import com.tce.smart.app.service.AppSubjectBasicService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.AppSubjectListVo;
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
 * @author fushiping
 * @date 2019/10/15 10:17
 **/
@Component
public class AppSubjectListWrapper extends BaseWrapper<AppSubject, AppSubjectListVo> {

	@Autowired
	AppSubjectService appSubjectService;
	@Autowired
	private RemoteParkService remoteParkService;
	@Autowired
	private AppParkSubjectService appParkSubjectService;
	@Autowired
	private AppCommService appCommService;
	@Autowired
	private AppSubjectBasicService appSubjectBasicService;

	@Override
	protected AppSubjectListVo warp(AppSubject appSubject) throws IOException {
		AppSubjectListVo vo = new AppSubjectListVo();
		BeanUtils.copyProperties(appSubject, vo);
		AppContentText appContentText = appSubjectService.selectTextNew(appSubject.getId());
		if(appContentText != null){
			if(null != appContentText.getPicLength() && appContentText.getPicLength() > 0){
				vo.setPicBinary(appCommService.buildConentTextImageUrl(appContentText.getId()));
			}
			//vo.setType(appSubjectBasicService.getContentLinkType(appSubject,appContentText));
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
