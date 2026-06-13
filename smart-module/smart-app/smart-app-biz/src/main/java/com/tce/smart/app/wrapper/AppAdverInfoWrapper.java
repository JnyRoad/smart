package com.tce.smart.app.wrapper;

import com.tce.smart.app.emun.AdverPositionEnum;
import com.tce.smart.app.entity.AppAdverInfo;
import com.tce.smart.app.service.AppCommService;
import com.tce.smart.app.vo.AppAdverInfoListVo;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.core.wrapper.BaseWrapper;
import io.netty.util.internal.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AppAdverInfoWrapper extends BaseWrapper<AppAdverInfo, AppAdverInfoListVo> {

	@Autowired
	private AppCommService appCommService;

	@Override
	protected AppAdverInfoListVo warp(AppAdverInfo appAdverInfo) throws IOException {
		AppAdverInfoListVo vo = new AppAdverInfoListVo();
		vo.setId(appAdverInfo.getId());
		vo.setImage(appCommService.buildAdverImageUrl(appAdverInfo.getId()));
		vo.setImageLink(appAdverInfo.getImageLink());

		//广告位置
		if (!StringUtil.isNullOrEmpty(appAdverInfo.getImagePosition())) {
			vo.setImagePosition(AdverPositionEnum.code(appAdverInfo.getImagePosition()).getDesc());
		}

//		//发布状态
//		if (!StringUtil.isNullOrEmpty(appAdverInfo.getPublishFlag())) {
//			vo.setPublishFlag(PublishState.code(appAdverInfo.getPublishFlag()).getDesc());
//		}
		vo.setPublishFlag(appAdverInfo.getPublishFlag());

		// 内容类型
		String imageUrl = appAdverInfo.getImageLink();
		if(StringUtils.isNotBlank(imageUrl)
				&& !(imageUrl.startsWith("http") || imageUrl.startsWith("HTTP"))){
			vo.setLinkType("外部链接");
		}

		return vo;
	}

}
