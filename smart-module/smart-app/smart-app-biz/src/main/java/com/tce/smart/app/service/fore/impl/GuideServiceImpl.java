package com.tce.smart.app.service.fore.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.emun.PublishState;
import com.tce.smart.app.emun.SubjectCatalog;
import com.tce.smart.app.entity.AppContentPicture;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.entity.AppSubjectContentPicture;
import com.tce.smart.app.service.AppContentPictureService;
import com.tce.smart.app.service.AppSubjectContentPictureService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.service.fore.GuideService;
import com.tce.smart.app.vo.fore.WelcomeVo;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.tool.util.ImageUtils;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 引导帮助服务实现类
 *
 * @ClassName GuideServiceImpl.java
 * @Author mingkai.wu
 * @Date 2019-05-07 18:20
 * @Description
 */
@Service
@AllArgsConstructor
@Slf4j
public class GuideServiceImpl implements GuideService {
	private AppSubjectService subjectService;

	private AppSubjectContentPictureService subjectContentPictureService;

	private AppContentPictureService contentPictureService;

	@Override
	public Page<WelcomeVo> getWelcome(Map<String, Object> params) {
		Page<WelcomeVo> page = null;

		// 查询引导信息列表
		List<AppSubject> appSubjectList = subjectService.selectByCatalogCode(SubjectCatalog.APP_LAUNCH.type(),
				PublishState.ONLINE.getCode());
		if (CollectionUtils.isNotEmpty(appSubjectList)) {

			List<WelcomeVo> welcomeVoList = new ArrayList<WelcomeVo>();
			List<AppSubjectContentPicture> subjectContentPictureList = null;

			WelcomeVo welcomeVo = null;
			AppContentPicture appContentPicture = null;

			for (AppSubject appSubject : appSubjectList) {

				// 查询引导信息图片内容列表
				subjectContentPictureList = subjectContentPictureService.selectBySubjectId(appSubject.getId());
				if (CollectionUtils.isNotEmpty(subjectContentPictureList)) {

//					for (AppSubjectContentPicture subjectContentPicture : subjectContentPictureList) {
					// 查询引导图片
					appContentPicture = contentPictureService
							.getById(subjectContentPictureList.get(0).getContentPictureId());
					if (Objects.nonNull(appContentPicture)) {
						welcomeVo = new WelcomeVo();
						welcomeVo.setWelcomeId(String.valueOf(appSubject.getId()));
						welcomeVo.setWelcomeTitle(appSubject.getSubjectName());
						welcomeVo
								.setPictureUrl(ImageUtils.changeFullBase64(appContentPicture.getPicBinary()));

						welcomeVoList.add(welcomeVo);
					}
				}

//				}
			}

			page = new Page<WelcomeVo>();
			page.setRecords(welcomeVoList);
		}

		return page;
	}

}
