package com.tce.smart.app.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.dto.AppAgreeDto;
import com.tce.smart.app.emun.DeleteState;
import com.tce.smart.app.emun.ModuleCatalog;
import com.tce.smart.app.emun.SubjectCatalog;
import com.tce.smart.app.entity.*;
import com.tce.smart.app.mapper.AppAgreeMapper;
import com.tce.smart.app.mapper.AppModuleInfoMapper;
import com.tce.smart.app.mapper.AppSubjectMapper;
import com.tce.smart.app.service.*;
import com.tce.smart.app.vo.AppCheckVo;
import com.tce.smart.common.core.util.RegexUtils;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.exception.TCEException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AppAgreeServiceImpl extends ServiceImpl<AppSubjectMapper, AppSubject>  implements AppAgreeService {
	@Autowired
	private AppSubjectService appSubjectService;

	@Autowired
	private AppParkSubjectService appParkSubjectService;

	@Autowired
	private AppContentTextService appContentTextService;

	@Autowired
	private AppSubjectContentTextService appSubjectContentTextService;

	@Autowired
	private AppSubjectModuleService appSubjectModuleService;

	@Autowired
	private AppQuestionService appQuestionService;

	@Autowired
	private AppSubjectBasicService appSubjectBasicService;

	@Autowired
	private AppSubjectMapper appSubjectMapper;

	@Autowired
	private AppModuleInfoMapper appModuleInfoMapper;

	@Autowired
	private AppAgreeMapper mapper;

	/**
	 * 初始化选择框信息
	 * @return
	 */
	@Override
	public AppCheckVo getInitDate() {
		AppCheckVo appCheckVo = new AppCheckVo();
		List<AppModuleInfo> list = appModuleInfoMapper.selectList(Wrappers.<AppModuleInfo>query().lambda()
				.eq(AppModuleInfo::getParentModule,0)
				.eq(AppModuleInfo::getDelFlag,DeleteState.NORMOL.getCode()));
		appCheckVo.setModuleName(list);
		return appCheckVo;
	}

	/**
	 * 添加协议信息(需要添加主题表，主题园区表，主题内容配置表，文本表,模块协议关联表）
	 * @return
	 */
	@Override
	@Transactional(rollbackFor=Exception.class)
	public Integer addAppAgree(AppAgreeDto appAgreeDto) {
		this.checkAgree(appAgreeDto);
		AppSubject appSubject = new AppSubject();
		appSubject.setSubjectName(appAgreeDto.getSubjectName());
		appSubject.setCatalogCode(SubjectCatalog.DORM_AGREE.type());
		appSubject.setCreateTime(LocalDateTime.now());
		appSubject.setDelFlag(DeleteState.NORMOL.getCode());
		appSubjectService.save(appSubject);
		Integer subjectId = appSubject.getId();
		/*this.addAgreeModule(subjectId,appAgreeDto.getModule());*/
		Integer textId = appQuestionService.addQuestionText(appAgreeDto.getTextDesc());
		this.addAgreeContent(subjectId,textId);
		/*this.addPark(appAgreeDto.getParkId(),subjectId);*/
		return subjectId;
	}
	/**
	 * 修改协议信息,需要更新四个表的数据（园区主题表，主题表，文本表，主题模块关联表)
	 * @param appAgreeDto
	 */
	@Override
	@Transactional(rollbackFor=Exception.class)
	public void updateAppAgree(AppAgreeDto appAgreeDto) {
		this.checkAgree(appAgreeDto);
		Integer subjectId = appAgreeDto.getId();
		AppSubject appSubject = appSubjectService.getById(subjectId);
		appSubject.setSubjectName(appAgreeDto.getSubjectName());
		appSubjectService.updateById(appSubject);
		Integer textId = appSubjectContentTextService.getTextById(subjectId);
		AppContentText appContentText = appContentTextService.getById(textId);
		appContentText.setTextDesc(appAgreeDto.getTextDesc());
		appContentText.setUpdateTime(LocalDateTime.now());
		appContentTextService.updateById(appContentText);
		//前端已经不传parkId和module了,注释掉
//		AppModuleInfo appModuleInfo = appModuleInfoMapper.selectOne(Wrappers.<AppModuleInfo>query().lambda().eq(StrUtil.isNotBlank(appAgreeDto.getModule()),AppModuleInfo::getModuleName,appAgreeDto.getModule())
//				.eq(AppModuleInfo::getDelFlag,DeleteState.NORMOL.getCode())
//				.eq(AppModuleInfo::getParentModule, ModuleCatalog.PARENT.getType()));
//		AppSubjectModule appSubjectModule = appSubjectModuleService.getMoudleById(subjectId);
//		if(appSubjectModule != null) {
//			appSubjectModule.setModuleId(appModuleInfo.getId());
//			appSubjectModuleService.updateById(appSubjectModule);
//		}
//		else{
//			this.addAgreeModule(subjectId,appAgreeDto.getModule());
//		}
//		appParkSubjectService.deletePark(subjectId);
//		this.addPark(appAgreeDto.getParkId(),subjectId);
	}
	/**
	 * 根据id获取协议主题
	 * @param id
	 * @return
	 */
	@Override
	public AppSubject getAppAgree(Integer id) {
		return appSubjectService.getById(id);
	}

	/**
	 * 删除协议(需要清除两个表，主题表和文本类容表）
	 * @param id 协议主题id
	 * @return
	 */
	@Override
	@Transactional(rollbackFor=Exception.class)
	public void deleteAgree(Integer id) {
		appSubjectModuleService.getById(id);
		appSubjectService.deleteQuestion(id);
	}
	/**
	 * 协议管理分页显示
	 * @param page
	 * @param appSubject
	 * @return
	 */
	@Override
	public IPage<AppSubject> getAppQuestionPage(Page page, AppSubject appSubject) {
		return   mapper.getAppAgreePage(page,appSubject);
	}

	/**
	 * 添加协议文本类容关联表
	 * @param subjectId
	 * @param textId
	 * @return
	 */
	private void addAgreeContent(Integer subjectId,Integer textId){
		AppSubjectContentText appSubjectContentText = new AppSubjectContentText();
		appSubjectContentText.setSubjectId(subjectId);
		appSubjectContentText.setContentTextId(textId);
		appSubjectContentTextService.save(appSubjectContentText);
	}

	/**
	 *添加模块关联表
	 * @param subjectId
	 * @param module
	 * @return
	 */
	public void addAgreeModule(Integer subjectId,String module){
		AppModuleInfo appSubject = appModuleInfoMapper.selectOne(Wrappers.<AppModuleInfo>query().lambda().eq(AppModuleInfo::getModuleName,module)
				.eq(AppModuleInfo::getDelFlag,DeleteState.NORMOL.getCode())
		        .eq(AppModuleInfo::getParentModule, ModuleCatalog.PARENT.getType()));
		AppSubjectModule appSubjectModule = new AppSubjectModule();
		appSubjectModule.setSubjectId(subjectId);
		appSubjectModule.setModuleId(appSubject.getId());
		appSubjectModuleService.save(appSubjectModule);
	}

	/**
	 * 根据ID添加园区协议对应表
	 * @param parkId
	 * @param subjectId
	 * @return
	 */
	private void addPark(int[] parkId, int subjectId){
		for(int i=0;i<parkId.length;i++)
		{
			AppParkSubject appParkSubject = new AppParkSubject();
			appParkSubject.setSubjectId(subjectId);
			appParkSubject.setParkId(parkId[i]);
			appParkSubjectService.save(appParkSubject);
		}
	}

	private void checkAgree(AppAgreeDto appAgreeDto){
		Integer l = appSubjectBasicService.num(appAgreeDto.getTextDesc());
		if(!RegexUtils.matchName(appAgreeDto.getSubjectName())) {
			throw new TCEException(ExceptionTypeEnum.APP_AGREE_NAME_ERROR);
		}
		if(StringUtils.isEmpty(appAgreeDto.getSubjectName()) || appAgreeDto.getSubjectName().trim().length() == 0) {
			throw new TCEException(ExceptionTypeEnum.APP_AGREE_NAME_NULL);
		}
		if(StringUtils.isEmpty(appAgreeDto.getTextDesc()) || l == 0) {
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_TEXT_NULL);
		}
/*		if(StringUtils.isEmpty(appAgreeDto.getModule())) {
			throw new TCEException(ExceptionTypeEnum.APP_MODULE_NAME_NULL);
		}
		if(appAgreeDto.getParkId().length == 0) {
			throw new TCEException(ExceptionTypeEnum.APP_PARK_NULL);
		}*/
	}
}
