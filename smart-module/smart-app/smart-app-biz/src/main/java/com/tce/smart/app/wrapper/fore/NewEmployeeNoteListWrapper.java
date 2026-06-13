package com.tce.smart.app.wrapper.fore;

import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.service.AppSubjectBasicService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.fore.NewEmployeeNoteListVo;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 新员工须知列表Response组装类
 *
 * @author mckaywu
 * @date 2019-06-05 14:07:20
 */
@Component
public class NewEmployeeNoteListWrapper extends BaseWrapper<AppSubject, NewEmployeeNoteListVo> {

	@Autowired
	private AppSubjectService appSubjectService;

	@Autowired
	private AppSubjectBasicService appSubjectBasicService;

	@Autowired
	private AppCommService appCommService;

	@Override
	protected NewEmployeeNoteListVo warp(AppSubject appSubject) throws IOException {
		NewEmployeeNoteListVo vo = new NewEmployeeNoteListVo();
		vo.setNoteId(appSubject.getId() + "");
		vo.setNoteName(appSubject.getSubjectName());
		vo.setDate(appSubject.getCreateTime());

		String linkUrl = StringUtils.isNotBlank(appSubject.getSubjectUrl()) ? appSubject.getSubjectUrl() : "";
		vo.setNoteUrl(linkUrl);

		AppContentText appContentText = appSubjectService.selectText(appSubject.getId());
		if (appContentText != null) {
			if (appContentText.getPicBinary() != null) {
				vo.setNoteImage(appCommService.buildConentTextImageUrl(appContentText.getId()));
			}
		}

		// 内容类型
		vo.setContentLinkType(appSubjectBasicService.getContentLinkType(appSubject, appContentText));
		return vo;
	}
}
