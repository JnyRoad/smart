package com.tce.smart.app.wrapper.fore;

import com.tce.smart.app.emun.AppContentType;
import com.tce.smart.app.entity.AppAdverInfo;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.vo.fore.AdverVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AdverListWrapper extends BaseWrapper<AppAdverInfo, AdverVo> {

	@Autowired
	private AppCommService appCommService;

	@Override
	protected AdverVo warp(AppAdverInfo appAdverInfo) throws IOException {
		AdverVo vo = new AdverVo();
		String linkUrl = StringUtils.isNotBlank(appAdverInfo.getImageLink()) ? appAdverInfo.getImageLink() : "";
		vo.setImageUrl(appCommService.buildAdverImageUrl(appAdverInfo.getId()));
		vo.setImageLink(linkUrl);
		// 内容类型
		Integer contentLinkType = AppContentType.LINK.getType();
		if (com.tce.smart.common.core.util.StringUtils.isNotBlank(appAdverInfo.getImageLink())
				&& !(appAdverInfo.getImageLink().startsWith("http") || appAdverInfo.getImageLink().startsWith("HTTP"))) {
			contentLinkType = AppContentType.MODULE.getType();
		}
		vo.setContentLinkType(contentLinkType);
		return vo;
	}
}
