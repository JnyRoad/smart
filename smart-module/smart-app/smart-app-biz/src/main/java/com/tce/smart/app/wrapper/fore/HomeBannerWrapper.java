package com.tce.smart.app.wrapper.fore;

import com.tce.smart.app.emun.AppContentType;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.fore.HomeBannerVo;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author fushiping
 * @date 2019/5/22 14:25
 **/

@Component
public class HomeBannerWrapper extends BaseWrapper<AppSubject, HomeBannerVo> {

	@Autowired
	private AppSubjectService appSubjectService;

	@Autowired
	private AppCommService appCommService;

	@Override
	protected HomeBannerVo warp(AppSubject appSubject) throws IOException {
		HomeBannerVo vo = new HomeBannerVo();
		vo.setPictureId(appSubject.getId());
		vo.setLinkUrl(appSubject.getSubjectUrl());

		// 内容类型
		Integer contentLinkType = AppContentType.LINK.getType();
		if(StringUtils.isNotBlank(appSubject.getSubjectUrl())
				&& !(appSubject.getSubjectUrl().startsWith("http") || appSubject.getSubjectUrl().startsWith("HTTP"))){
			contentLinkType = AppContentType.MODULE.getType();
		}
		vo.setContentLinkType(contentLinkType);

		String pictureName = StringUtils.isNotBlank(appSubject.getSubjectName()) ? appSubject.getSubjectName() : "";
		vo.setPictureName(pictureName);

		String pictureUrl = "";
		AppContentText appContentText = appSubjectService.selectTextNew(appSubject.getId());
		if (appContentText != null) {
			if (null != appContentText.getPicLength() && appContentText.getPicLength() > 0) {
				pictureUrl = appCommService.buildConentTextImageUrl(appContentText.getId());
			}
		}
		vo.setPictureUrl(pictureUrl);
		return vo;
	}
}
