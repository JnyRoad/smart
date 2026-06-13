package com.tce.smart.app.wrapper.fore;

import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.fore.NewsDetailVo;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;

@Component
public class NewsDetailWrapper extends BaseWrapper<AppSubject, NewsDetailVo> {


	@Value("${spring.file.text-pdf-enclosure}")
	private String enclosureUrl;

	@Value("${spring.file.pdf-preview}")
	private String previewUrl;

	@Autowired
	private AppSubjectService appSubjectService;

	@Autowired
	private AppCommService appCommService;

	@Override
	protected NewsDetailVo warp(AppSubject appSubject) throws IOException {
		NewsDetailVo vo = new NewsDetailVo();
		vo.setNewsId(appSubject.getId());
		vo.setNewsTitle(appSubject.getSubjectName());
		vo.setDate(appSubject.getCreateTime());
		AppContentText appContentText = appSubjectService.selectText(appSubject.getId());
		if(appContentText != null){
			if( appContentText.getPicBinary() != null ) {
				vo.setTitleImage(appCommService.buildConentTextImageUrl(appContentText.getId()));
			}
			vo.setNewsContent(appContentText.getTextDesc());

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
