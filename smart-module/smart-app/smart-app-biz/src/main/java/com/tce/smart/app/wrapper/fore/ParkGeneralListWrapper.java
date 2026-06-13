package com.tce.smart.app.wrapper.fore;

import com.tce.smart.app.emun.AppContentType;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppSubjectBasicService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.fore.ParkGeneralListVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 园区概况列表Wrapper
 *
 * @author mckaywu
 * @date 2019-06-18 11:25:23
 */
@Component
public class ParkGeneralListWrapper extends BaseWrapper<AppSubject, ParkGeneralListVo> {

	@Autowired
	private AppSubjectService appSubjectService;

	@Autowired
	private AppCommService appCommService;

	@Autowired
	private AppSubjectBasicService appSubjectBasicService;

	@Override
	protected ParkGeneralListVo warp(AppSubject appSubject) throws IOException {
		ParkGeneralListVo vo = new ParkGeneralListVo();
		vo.setGeneralId(appSubject.getId());
		vo.setGeneralTitle(appSubject.getSubjectName());
		vo.setDate(appSubject.getCreateTime());

		String linkUrl = StringUtils.isNotBlank(appSubject.getSubjectUrl()) ? appSubject.getSubjectUrl() : "";
		vo.setGeneralUrl(linkUrl);

		AppContentText appContentText = appSubjectService.selectText(appSubject.getId());
		if (appContentText != null) {
			if (appContentText.getPicBinary() != null) {
				vo.setTitleImage(appCommService.buildConentTextImageUrl(appContentText.getId()));
			}
		}

		// 内容类型
		vo.setContentLinkType(appSubjectBasicService.getContentLinkType(appSubject, appContentText));

		return vo;
	}
}
