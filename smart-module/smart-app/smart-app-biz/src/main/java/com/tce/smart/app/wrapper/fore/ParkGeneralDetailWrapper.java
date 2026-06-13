package com.tce.smart.app.wrapper.fore;

import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.fore.ParkGeneralDetailVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * 园区概况详情Wrapper
 *
 * @author mckaywu
 * @date 2019-06-18 11:23:55
 */
@Component
public class ParkGeneralDetailWrapper extends BaseWrapper<AppSubject, ParkGeneralDetailVo> {


	@Value("${spring.file.text-pdf-enclosure}")
	private String enclosureUrl;

	@Value("${spring.file.pdf-preview}")
	private String previewUrl;
	@Autowired
	private AppSubjectService appSubjectService;

	@Autowired
	private AppCommService appCommService;

	@Override
	protected ParkGeneralDetailVo warp(AppSubject appSubject) throws IOException {
		ParkGeneralDetailVo vo = new ParkGeneralDetailVo();
		vo.setGeneralId(appSubject.getId());
		vo.setGeneralTitle(appSubject.getSubjectName());
		vo.setDate(appSubject.getCreateTime());
		AppContentText appContentText = appSubjectService.selectText(appSubject.getId());
		if (appContentText != null) {
			if (appContentText.getPicBinary() != null) {
				vo.setTitleImage(appCommService.buildConentTextImageUrl(appContentText.getId()));
			}
			vo.setGeneralContent(appContentText.getTextDesc());

			String enclosureName = appContentText.getEnclosureName();
			if (enclosureName != null) {
				vo.setEnclosureName(enclosureName);
			} else {
				vo.setEnclosureName("");
			}

			String enclosureUrl = "";
			String previewUrl = "";
			if (StringUtils.isNotBlank(enclosureName)) {
				enclosureUrl = this.enclosureUrl+appSubject.getId();
				previewUrl = this.previewUrl + URLEncoder.encode(enclosureUrl,"UTF-8");
			}
			vo.setEnclosureUrl(enclosureUrl);
			vo.setPreviewUrl(previewUrl);
		}
		return vo;
	}
}
