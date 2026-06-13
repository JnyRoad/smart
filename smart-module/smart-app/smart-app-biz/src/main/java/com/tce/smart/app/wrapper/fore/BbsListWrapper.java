package com.tce.smart.app.wrapper.fore;

import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppSubjectBasicService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.fore.BbsListVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BbsListWrapper extends BaseWrapper<AppSubject, BbsListVo> {

	@Autowired
	private AppSubjectService appSubjectService;

	@Autowired
	private AppSubjectBasicService appSubjectBasicService;

	@Autowired
	private AppCommService appCommService;

	@Override
	protected BbsListVo warp(AppSubject appSubject) throws IOException {
		BbsListVo vo = new BbsListVo();
		vo.setBbsId(appSubject.getId());
		vo.setBbsTitle(appSubject.getSubjectName());
		AppContentText appContentText = appSubjectService.selectText(appSubject.getId());
		if (appContentText != null) {
			if (appContentText.getPicBinary() != null) {
				vo.setBbsImg(appCommService.buildConentTextImageUrl(appContentText.getId()));
			}
		}
		String linkUrl = StringUtils.isNotBlank(appSubject.getSubjectUrl()) ? appSubject.getSubjectUrl() : "";
		vo.setBbsUrl(linkUrl);
		// 内容类型
		vo.setContentLinkType(appSubjectBasicService.getContentLinkType(appSubject, appContentText));
		return vo;
	}
}
