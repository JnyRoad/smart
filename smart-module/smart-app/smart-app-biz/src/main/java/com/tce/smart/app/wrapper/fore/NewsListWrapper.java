package com.tce.smart.app.wrapper.fore;

import com.tce.smart.app.emun.AppContentType;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppSubjectBasicService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.fore.NewsListVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class NewsListWrapper extends BaseWrapper<AppSubject, NewsListVo> {

	@Autowired
	private AppSubjectService appSubjectService;

	@Autowired
	private AppCommService appCommService;

	@Autowired
	private AppSubjectBasicService appSubjectBasicService;

	@Override
	protected NewsListVo warp(AppSubject appSubject) throws IOException {
		NewsListVo vo = new NewsListVo();
		vo.setNewsId(appSubject.getId());
		vo.setNewsTitle(appSubject.getSubjectName());
		vo.setDate(appSubject.getCreateTime());

		String linkUrl = StringUtils.isNotBlank(appSubject.getSubjectUrl()) ? appSubject.getSubjectUrl() : "";
		vo.setNewsUrl(linkUrl);

		AppContentText appContentText = appSubjectService.selectTextNew(appSubject.getId());
		if (appContentText != null) {
			if (null != appContentText.getPicLength() && appContentText.getPicLength() > 0) {
				vo.setTitleImage(appCommService.buildConentTextImageUrl(appContentText.getId()));
			}
		}

		// 内容类型
		vo.setContentLinkType(appSubjectBasicService.getContentLinkType(appSubject, appContentText));
		return vo;
	}
}
