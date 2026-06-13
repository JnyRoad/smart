package com.tce.smart.app.wrapper.fore;

import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.fore.NewEmployeeNoteDetailVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;

/**
 * 新员工须知详情Response组装类
 *
 * @author mckaywu
 * @date 2019-06-05 14:05:03
 */
@Component
public class NewEmployeeNoteDetaiWrapper extends BaseWrapper<AppSubject, NewEmployeeNoteDetailVo> {

	@Value("${spring.file.text-pdf-enclosure}")
	private String enclosureUrl;

	@Value("${spring.file.pdf-preview}")
	private String previewUrl;

	@Autowired
	private AppSubjectService appSubjectService;

	@Autowired
	private AppCommService appCommService;

	@Override
	protected NewEmployeeNoteDetailVo warp(AppSubject appSubject) throws IOException {
		NewEmployeeNoteDetailVo vo = new NewEmployeeNoteDetailVo();
		vo.setNoteId(appSubject.getId() + "");
		vo.setNoteName(appSubject.getSubjectName());
		vo.setDate(appSubject.getCreateTime());
		AppContentText appContentText = appSubjectService.selectText(appSubject.getId());
		if (appContentText != null) {
			String enclosureName = appContentText.getEnclosureName();
			if (appContentText.getPicBinary() != null) {
				vo.setNoteImage(appCommService.buildConentTextImageUrl(appContentText.getId()));
			}
			if (appContentText.getTextDesc() != null) {
				vo.setNoteContent(appContentText.getTextDesc());
			} else {
				vo.setNoteContent("");
			}
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
