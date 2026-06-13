package com.tce.smart.app.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.app.dto.AppPictureDto;
import com.tce.smart.app.emun.DeleteState;
import com.tce.smart.app.emun.SubjectCatalog;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.entity.AppSubjectContentPicture;
import com.tce.smart.app.mapper.AppSubjectContentPictureMapper;
import com.tce.smart.app.mapper.AppSubjectMapper;
import com.tce.smart.app.service.AppSubjectContentPictureService;
import com.tce.smart.app.service.AppSubjectService;
import com.tce.smart.app.vo.AppPictureVo;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.exception.TCEException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.entity.AppContentPicture;
import com.tce.smart.app.mapper.AppContentPictureMapper;
import com.tce.smart.app.service.AppContentPictureService;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片内容
 *
 * @author mingkai.wu
 * @date 2019-04-25 09:45:47
 */
@Service
public class AppContentPictureServiceImpl extends ServiceImpl<AppContentPictureMapper, AppContentPicture> implements AppContentPictureService {
	@Autowired
	private AppSubjectService appSubjectService;

	@Autowired
	private AppSubjectContentPictureService appSubjectContentPictureService;

	@Autowired
	private AppContentPictureMapper appContentPictureMapper;

	@Autowired
	private AppSubjectMapper appSubjectMapper;

	@Autowired
	private AppSubjectContentPictureMapper appSubjectContentPictureMapper;
	/*
	 *添加引导页图片内容表
	 *
	 */
	@Override
	@Transactional(rollbackFor=Exception.class)
	public Integer addBootPage(AppPictureDto appPictureDto) {
		this.checkPicture(appPictureDto);
		AppContentPicture appContentPicture = new AppContentPicture();
		appContentPicture.setPicName(appPictureDto.getPicName());
		appContentPicture.setCreateTime(LocalDateTime.now());
        appContentPicture.setPicBinary(appPictureDto.getPicBinary().getBytes(StandardCharsets.UTF_8));
        appContentPicture.setDelFlag(DeleteState.NORMOL.getCode());
		appContentPicture.setUpdateTime(LocalDateTime.now());
		this.save(appContentPicture);
		Integer subjectId = this.addBootSubject();
		this.addBootPicSubject(appContentPicture.getId(),subjectId);
		return subjectId;
	}

	/**
	 * 添加启动页
	 * @param appPictureDto
	 * @return
	 */
	@Override
	public Integer addStartPage(AppPictureDto appPictureDto) {
		this.checkPicture(appPictureDto);
		AppContentPicture appContentPicture = new AppContentPicture();
		appContentPicture.setPicName(appPictureDto.getPicName());
		appContentPicture.setCreateTime(LocalDateTime.now());
        appContentPicture.setPicBinary(appPictureDto.getPicBinary().getBytes(StandardCharsets.UTF_8));
        appContentPicture.setDelFlag(DeleteState.NORMOL.getCode());
		appContentPicture.setUpdateTime(LocalDateTime.now());
		this.save(appContentPicture);
		Integer subjectId = this.addStartSubject();
		this.addBootPicSubject(appContentPicture.getId(),subjectId);
		return subjectId;
	}

	/**
	 * 显示引导页图片
	 * @return
	 */
	@Override
	public AppPictureVo bootPage() {
		this.checkBoot();
		List <AppSubject> list = appSubjectMapper.selectList(Wrappers.<AppSubject>query().lambda()
				.eq(AppSubject::getSubjectName,SubjectCatalog.BOOT_PAGE.getName())
				.eq(AppSubject::getCatalogCode,SubjectCatalog.APP_LAUNCH.type())
				.eq(AppSubject::getDelFlag,DeleteState.NORMOL.getCode()));
		List<AppPictureDto> listDto = new ArrayList<>();
		if(list != null) {
			list.forEach(AppSubject -> {
				AppPictureDto appPictureDto = new AppPictureDto();
				appPictureDto.setId(AppSubject.getId());
				Integer id = appSubjectContentPictureService.getBySubjectId(AppSubject.getId());
				AppContentPicture appContentPicture = appContentPictureMapper.selectById(id);
				appPictureDto.setPicName(appContentPicture.getPicName());
				if (appContentPicture.getPicBinary() != null) {
					appPictureDto.setPicBinary(new String(appContentPicture.getPicBinary()));
				}
				listDto.add(appPictureDto);
			});
		}
		AppPictureVo appPictureVo = new AppPictureVo();
		appPictureVo.setPicture(listDto);
		return appPictureVo;
	}

	/**
	 * 显示启动页图片
	 * @return
	 */
	@Override
	public AppPictureDto startPage() {
		this.checkStart();
		AppPictureDto appPictureDto = null;
		AppSubject appSubject = appSubjectMapper.selectOne(Wrappers.<AppSubject>query().lambda()
				.eq(AppSubject::getSubjectName,SubjectCatalog.START_PAGE.getName())
				.eq(AppSubject::getCatalogCode,SubjectCatalog.APP_LAUNCH.type())
		        .eq(AppSubject::getDelFlag,DeleteState.NORMOL.getCode()));
		if(appSubject != null) {
			appPictureDto = new AppPictureDto();
			appPictureDto.setId(appSubject.getId());
			Integer id = appSubjectContentPictureService.getBySubjectId(appSubject.getId());
			AppContentPicture appContentPicture = appContentPictureMapper.selectById(id);
			appPictureDto.setPicName(appContentPicture.getPicName());
			if (appContentPicture.getPicBinary() != null) {
				appPictureDto.setPicBinary(new String(appContentPicture.getPicBinary()));
			}
		}
		return appPictureDto;
	}

	/**
	 * 更新图片信息
	 */
	@Override
	public void updatePage(AppPictureDto appPictureDto) {
		AppSubject appSubject = appSubjectService.getById(appPictureDto.getId());
		Integer id = appSubjectContentPictureService.getBySubjectId(appSubject.getId());
		AppContentPicture appContentPicture = appContentPictureMapper.selectById(id);
		appContentPicture.setPicName(appPictureDto.getPicName());
        appContentPicture.setPicBinary(appPictureDto.getPicBinary().getBytes(StandardCharsets.UTF_8));
        appContentPictureMapper.updateById(appContentPicture);
	}

	@Override
	public Boolean deletePicContent(Integer contentPictureId) {
		AppContentPicture appContentPicture = new AppContentPicture();
		appContentPicture.setId(contentPictureId);
		appContentPicture.setDelFlag(DeleteState.DELETE.getCode());
		appContentPicture.setUpdateTime(LocalDateTime.now());
		return  appContentPicture.updateById();
	}

	/*
	 *添加主题图片关系表
	 *
	*/
	private Integer addBootPicSubject(Integer pictureId,Integer subjectId){
		AppSubjectContentPicture appSubjectContentPicture = new AppSubjectContentPicture();
		appSubjectContentPicture.setContentPictureId(pictureId);
		appSubjectContentPicture.setSubjectId(subjectId);
		appSubjectContentPictureMapper.insert(appSubjectContentPicture);
		return appSubjectContentPicture.getId();
	}
	/*
	 *添加引导页主题配置信息表
	 *
	 */
	private Integer addBootSubject(){
		AppSubject appSubject = new AppSubject();
		appSubject.setCatalogCode(SubjectCatalog.APP_LAUNCH.type());
		appSubject.setParentSubject(0);
		appSubject.setCreateTime(LocalDateTime.now());
		appSubject.setDelFlag(DeleteState.NORMOL.getCode());
		appSubject.setPublishFlag("0");
		appSubject.setSubjectName(SubjectCatalog.BOOT_PAGE.getName());
		appSubject.setSubjectOrder(1);
		appSubject.setUpdateTime(LocalDateTime.now());
		appSubjectService.save(appSubject);
		return appSubject.getId();
	}

	/*
	 *添加启动页配置信息表
	 *
	 */
	private Integer addStartSubject(){
		AppSubject appSubject = new AppSubject();
		appSubject.setCatalogCode(SubjectCatalog.APP_LAUNCH.type());
		appSubject.setCreateTime(LocalDateTime.now());
		appSubject.setDelFlag(DeleteState.NORMOL.getCode());
		appSubject.setSubjectName(SubjectCatalog.START_PAGE.getName());
		appSubject.setUpdateTime(LocalDateTime.now());
		appSubjectService.save(appSubject);
		return appSubject.getId();
	}

	private void checkStart(){
		Integer i = appSubjectMapper.selectCount(Wrappers.<AppSubject>query().lambda()
				.eq(AppSubject::getSubjectName,SubjectCatalog.START_PAGE.getName())
				.eq(AppSubject::getCatalogCode,SubjectCatalog.APP_LAUNCH.type())
				.eq(AppSubject::getDelFlag,DeleteState.NORMOL.getCode()));
		if(i > 1){
			List <AppSubject> list = appSubjectMapper.selectList(Wrappers.<AppSubject>query().lambda()
					.eq(AppSubject::getSubjectName,SubjectCatalog.START_PAGE.getName())
					.eq(AppSubject::getCatalogCode,SubjectCatalog.APP_LAUNCH.type())
					.eq(AppSubject::getDelFlag,DeleteState.NORMOL.getCode()));
			list.forEach(AppSubject->{
				appSubjectService.deleteById(AppSubject.getId());
			});
			throw new TCEException("启动页图片不能超过一张，将自动清除记录");
		}
	}

	private void checkBoot(){
		Integer i = appSubjectMapper.selectCount(Wrappers.<AppSubject>query().lambda()
				.eq(AppSubject::getSubjectName,SubjectCatalog.BOOT_PAGE.getName())
				.eq(AppSubject::getCatalogCode,SubjectCatalog.APP_LAUNCH.type())
				.eq(AppSubject::getDelFlag,DeleteState.NORMOL.getCode()));
		if(i > 3){
			List <AppSubject> list = appSubjectMapper.selectList(Wrappers.<AppSubject>query().lambda()
					.eq(AppSubject::getSubjectName,SubjectCatalog.BOOT_PAGE.getName())
					.eq(AppSubject::getCatalogCode,SubjectCatalog.APP_LAUNCH.type())
					.eq(AppSubject::getDelFlag,DeleteState.NORMOL.getCode()));
			list.forEach(AppSubject->{
				appSubjectService.deleteById(AppSubject.getId());
			});
			throw new TCEException("引导页图片不能超过三张，将自动清除记录");
		}
	}

	private void checkPicture(AppPictureDto appPictureDto){
		if(StringUtils.isEmpty(appPictureDto.getPicName())) {
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_NAME_NULL);
		}
		if(StringUtils.isEmpty(appPictureDto.getPicBinary())) {
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_TEXT_NULL);
		}
	}
}
