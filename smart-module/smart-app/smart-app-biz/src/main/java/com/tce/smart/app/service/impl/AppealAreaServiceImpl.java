package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.ao.AppealAreaAo;
import com.tce.smart.app.ao.fore.AppSubjectAO;
import com.tce.smart.app.ao.fore.AppSubjectDetailAO;
import com.tce.smart.app.emun.AppContentType;
import com.tce.smart.app.emun.DeleteState;
import com.tce.smart.app.emun.PublishState;
import com.tce.smart.app.emun.SubjectCatalog;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppParkSubject;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.entity.AppSubjectContentText;
import com.tce.smart.app.mapper.AppContentTextMapper;
import com.tce.smart.app.mapper.AppParkSubjectMapper;
import com.tce.smart.app.mapper.AppSubjectMapper;
import com.tce.smart.app.service.*;
import com.tce.smart.common.security.util.SecurityUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @description: 申诉专区文章
 * @date: 2020-07-28 12:00
 * @author: wuling
 * @version: 1.0
 */
@Service
public class AppealAreaServiceImpl implements AppealAreaService {

	@Value("${spring.file.text-pdf-enclosure}")
	private String enclosureUrl;

	@Value("${spring.file.pdf-preview}")
	private String previewUrl;

	@Autowired
	private  AppSubjectBasicService appSubjectBasicService;

	@Autowired
	private  AppContentTextService appContentTextService;

	@Autowired
	private  AppSubjectContentTextService appSubjectContentTextService;

	@Autowired
	private  AppSubjectMapper appSubjectMapper;

	@Autowired
	private  AppParkSubjectMapper appParkSubjectMapper;

	@Autowired
	private  AppContentTextMapper appContentTextMapper;

	@Autowired
	private  AppCommService appCommService;


	@Override
	public IPage<AppSubject> getAppSubjectPage(Page page, AppealAreaAo appealAreaAo) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		return appSubjectMapper.getAppSubjectPageByParkId(page,appealAreaAo,parkIdList);
	}

	@Override
	public IPage<AppSubjectAO> getAppSubjectPageByApp(Page page, AppealAreaAo appealAreaAo) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		IPage<AppSubjectAO> appSubjectListByApp = appSubjectMapper.getAppSubjectListByApp(page, appealAreaAo, parkIdList);
		for(AppSubjectAO appSubjectAO : appSubjectListByApp.getRecords()){
			//图片访问地址
			String imageUrl = appCommService.buildConentTextImageUrl(appSubjectAO.getContentTextId());
			appSubjectAO.setFrontImg(imageUrl);
		}
		return appSubjectListByApp;
	}

	@Transactional
	@Override
	public boolean addAppealAreaArticle(AddAppSubjectAo addAppSubjectAo) {
		AppSubject appSubject = new AppSubject();
		BeanUtils.copyProperties(addAppSubjectAo,appSubject);
		appSubject.setDelFlag(DeleteState.NORMOL.getCode());
		appSubject.setCatalogCode(SubjectCatalog.ARREAL_AREA.type());
		appSubject.setCreateTime(LocalDateTime.now());
		appSubject.setUpdateTime(LocalDateTime.now());
		appSubject.setPublishFlag(PublishState.ONLINE.getCode());
		appSubject.insert();		//添加subject记录

		AppParkSubject appParkSubject = new AppParkSubject();
		appParkSubject.setParkId(addAppSubjectAo.getParkId());
		appParkSubject.setSubjectId(appSubject.getId());
		appParkSubject.insert();		//添加园区与主题关联记录

		//添加主题记录
		Integer textId = appContentTextService.insertTextContent(addAppSubjectAo);

		//添加主题与内容关联记录
		appSubjectContentTextService.insertTextInSubject(textId, appSubject.getId());
		return true;
	}

	/**
	 *  更新操作
	 * @param employeeNoteAo
	 * @return
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateAppealArticle(AddAppSubjectAo addAppSubjectAo) {

		appSubjectBasicService.updateSubjectContent(addAppSubjectAo);
		//将base64替换成URL
		String textDesc = appSubjectBasicService.replaceSaveBase64ContentImg(addAppSubjectAo.getId(), addAppSubjectAo.getSubjectName(), addAppSubjectAo.getTextDesc());
		//重新覆盖文本内容
		addAppSubjectAo.setTextDesc(textDesc);

		appContentTextService.updateTextContent(addAppSubjectAo);
		if(addAppSubjectAo.getParkId() != null ) {
			AppParkSubject appParkSubject = appParkSubjectMapper.selectOne(new QueryWrapper<AppParkSubject>().lambda().eq(AppParkSubject::getSubjectId, addAppSubjectAo.getId()));
			if (appParkSubject != null ){
				appParkSubject.setParkId(addAppSubjectAo.getParkId());
				appParkSubject.updateById();
			}
			if(appParkSubject == null ){
				AppParkSubject appParkSubjectInsert = new AppParkSubject();
				appParkSubjectInsert.setParkId(addAppSubjectAo.getParkId());
				appParkSubjectInsert.setSubjectId(addAppSubjectAo.getId());
				appParkSubjectInsert.insert();
			}
		}
		return true;
	}

	@Override
	public AppSubject getAppealArticleDetail(Integer id) {
		return appSubjectMapper.selectById(id);
	}

	@Override
	public boolean delAppealArticleRecord(Integer id) {

		AppSubject appSubject = appSubjectMapper.selectById(id);
		appSubject.setDelFlag(DeleteState.DELETE.getCode());
		appSubject.setUpdateTime(LocalDateTime.now());
		appSubject.updateById();		//逻辑删除主题数据

		AppSubjectContentText appSubjectContentText =
				appSubjectContentTextService.getOne(Wrappers.<AppSubjectContentText>query().lambda()
						.eq(AppSubjectContentText::getSubjectId, id));
		Integer contentTextId = appSubjectContentText.getContentTextId();
		if(null != contentTextId) {
			AppContentText appContentText = appContentTextMapper.selectById(contentTextId);
			appContentText.setDelFlag(DeleteState.DELETE.getCode());
			appContentText.setUpdateTime(LocalDateTime.now());
			appContentText.updateById();	//逻辑删除内容数据
		}
		return true;
	}

	@Override
	public AppSubjectDetailAO noteDetailByApp(Integer id) throws IOException {
		AppSubject appSubject = getAppealArticleDetail(id);
		AppContentText appContentText = appSubjectMapper.selectText(id);
		AppSubjectDetailAO appSubjectDetailAO = new AppSubjectDetailAO();
		/**
		 * 这里的判断是
		 * 	1.如果enclosureName不为NULL 表示文章的内容是PDF文件
		 * 	2.如果textDesc不为NULL 表示文章内容是文本
		 * 	3.如果subjectUrl不为NULL 表示文章的内容是一个链接
		 */
		if(appContentText != null){
			if(appContentText.getEnclosureName() != null) {
				appSubjectDetailAO.setType(AppContentType.PDF.getType());
				String	enclosureUrl = this.enclosureUrl+appSubject.getId();
				String	previewUrl = this.previewUrl + URLEncoder.encode(enclosureUrl,"UTF-8");
				appSubjectDetailAO.setContentText(previewUrl);
			} else if(appContentText.getTextDesc() != null){
				appSubjectDetailAO.setType(AppContentType.DESC.getType());
				appSubjectDetailAO.setContentText(appContentText.getTextDesc());
			} else if(appSubject.getSubjectUrl() != null){
				appSubjectDetailAO.setType(AppContentType.LINK.getType());
				appSubjectDetailAO.setContentText(appSubject.getSubjectUrl());
			}
		}
		return appSubjectDetailAO;
	}
}
