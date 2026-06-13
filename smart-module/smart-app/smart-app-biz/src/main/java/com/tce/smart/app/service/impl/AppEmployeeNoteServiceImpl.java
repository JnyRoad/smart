package com.tce.smart.app.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.ao.EmployeeNoteAo;
import com.tce.smart.app.dto.AppSubjectDto;
import com.tce.smart.app.emun.DeleteState;
import com.tce.smart.app.emun.SubjectCatalog;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppParkSubject;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.entity.AppSubjectContentText;
import com.tce.smart.app.mapper.AppContentTextMapper;
import com.tce.smart.app.mapper.AppEmployeeNoteMapper;
import com.tce.smart.app.mapper.AppParkSubjectMapper;
import com.tce.smart.app.mapper.AppSubjectMapper;
import com.tce.smart.app.service.*;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.SmtParkDTO;
import com.tce.smart.platform.api.feign.RemoteParkService;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.exception.TCEException;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @version 2.0
 * @date 2019/5/16 10:22
 **/
@Slf4j
@Service
public class AppEmployeeNoteServiceImpl extends ServiceImpl<AppEmployeeNoteMapper, AppSubject> implements AppEmployeeNoteService {
	@Autowired
	AppSubjectService apSubjectService;

	@Autowired
	private AppSubjectContentTextService appSubjectContentTextService;

	@Autowired
	private AppSubjectBasicService appSubjectBasicService;

	@Autowired
	private AppContentTextService appContentTextService;

	@Autowired
    private AppEmployeeNoteMapper appEmployeeNoteMapper;

    @Autowired
    private AppContentTextMapper appContentTextMapper;

    @Autowired
	private AppSubjectMapper appSubjectMapper;

    @Autowired
	private AppParkSubjectMapper appParkSubjectMapper;

    @Autowired
	private RemoteParkService remoteParkService;

    /**
     *  条件分页查询
     * @param page
     * @param employeeNoteAo
     * @return
     */
    @Override
    public IPage<AppSubject> getPageList(Page page, EmployeeNoteAo employeeNoteAo) {
		return appEmployeeNoteMapper.getPageList(page, employeeNoteAo);
	}

	/**
	 *  条件分页查询,根据园区过滤
	 * @param page
	 * @param employeeNoteAo
	 * @return
	 */
	@Override
	public IPage<AppSubject> getPageListFilterByPark(Page page, EmployeeNoteAo employeeNoteAo) {
		IPage<AppSubject> pageInfo = appEmployeeNoteMapper.getPageList(page, employeeNoteAo);
		//根据园区过滤
		apSubjectService.filterSubjectByPark(pageInfo);
		return pageInfo;
	}

    /**
     *  新员工须知详情
     * @param id
     * @return
     */
    @Override
    public AppSubject noteDetail(Integer id) {
		checkId(id);
		return appSubjectMapper.selectById(id);
    }

    /**
     *  删除操作
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void noteDel(Integer id) {
		this.checkId(id);
		AppSubject appSubject = appSubjectMapper.selectById(id);
        appSubject.setDelFlag(DeleteState.DELETE.getCode());
        appSubject.setUpdateTime(LocalDateTime.now());
        appSubject.updateById();
        AppSubjectContentText appSubjectContentText =
                appSubjectContentTextService.getOne(Wrappers.<AppSubjectContentText>query().lambda()
						.eq(AppSubjectContentText::getSubjectId, id));
        Integer contentTextId = appSubjectContentText.getContentTextId();
        if(null != contentTextId) {
			AppContentText appContentText = appContentTextMapper.selectById(contentTextId);
			appContentText.setDelFlag(DeleteState.DELETE.getCode());
			appContentText.setUpdateTime(LocalDateTime.now());
			appContentText.updateById();
		}
    }

    /**
     *  更新操作
     * @param employeeNoteAo
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void noteUpdate(EmployeeNoteAo employeeNoteAo) {
        this.checkAo(employeeNoteAo);
		AppSubjectDto appSubjectDto = new AppSubjectDto();
		BeanUtils.copyProperties(employeeNoteAo, appSubjectDto);
		appSubjectDto.setUpdateTime(LocalDateTime.now());
		appSubjectDto.updateById();
		AddAppSubjectAo addAppSubjectAo = new AddAppSubjectAo();
		BeanUtils.copyProperties(employeeNoteAo, addAppSubjectAo);
		appSubjectBasicService.updateSubjectContent(addAppSubjectAo);

		//将base64替换成URL
		String textDesc = appSubjectBasicService.replaceSaveBase64ContentImg(appSubjectDto.getId(), addAppSubjectAo.getSubjectName(), addAppSubjectAo.getTextDesc());
		//重新覆盖文本内容
		addAppSubjectAo.setTextDesc(textDesc);

		appContentTextService.updateTextContent(addAppSubjectAo);
		if(employeeNoteAo.getParkId() != null ) {
			AppParkSubject appParkSubject =
					appParkSubjectMapper.selectOne(new QueryWrapper<AppParkSubject>().eq("subject_id", employeeNoteAo.getId()));
			if (appParkSubject != null ){
				appParkSubject.setParkId(employeeNoteAo.getParkId());
				appParkSubject.updateById();
			}
			if(appParkSubject == null ){
				AppParkSubject appParkSubjectInsert = new AppParkSubject();
				appParkSubjectInsert.setParkId(employeeNoteAo.getParkId());
				appParkSubjectInsert.setSubjectId(employeeNoteAo.getId());
				appParkSubjectInsert.insert();
			}
		}
    }

    /**
     *  添加操作
     * @param employeeNoteAo
	 *
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer noteAdd(EmployeeNoteAo employeeNoteAo) {
        this.checkAo(employeeNoteAo);
        AppSubject appSubject = new AppSubject();
		BeanUtils.copyProperties(employeeNoteAo, appSubject);
        appSubject.setDelFlag(DeleteState.NORMOL.getCode());
        appSubject.setCatalogCode(SubjectCatalog.EMPLOYEE_NOTICE.type());
        appSubject.setCreateTime(LocalDateTime.now());
		appSubject.setUpdateTime(LocalDateTime.now());
        appSubject.insert();
        int subjectId = appSubject.getId();
        if(employeeNoteAo.getParkId() != null ){
			AppParkSubject appParkSubject = new AppParkSubject();
			appParkSubject.setParkId(employeeNoteAo.getParkId());
			appParkSubject.setSubjectId(subjectId);
			appParkSubject.insert();
		}
		AddAppSubjectAo addAppSubjectAo = new AddAppSubjectAo();
		BeanUtils.copyProperties(employeeNoteAo, addAppSubjectAo);
        Integer textId = appContentTextService.insertTextContent(addAppSubjectAo);
		appSubjectContentTextService.insertTextInSubject(textId, appSubject.getId());
        return appSubject.getId();
    }

	/**
	 * 获取所有园区
	 * @return
	 */
	@Override
	public List<AppParkSubject> prakIdArray() {
		Result<?> result = remoteParkService.getParkList(SecurityConstants.FROM_IN);
		List smtParklist = (List) result.getData();
		return smtParklist;
	}

	@Override
	public List<SmtParkDTO> getParkList() {
		Result<List<SmtParkDTO>> result = remoteParkService.getParkList(SecurityConstants.FROM_IN);
		List<SmtParkDTO> smtParklist = result.getData();
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		//返回当前登录用户关联的园区信息
		List<SmtParkDTO> collect = smtParklist.stream().filter(a -> (parkIdList.contains(a.getId()))).collect(Collectors.toList());
		return collect;
	}

	@Override
	public List<SmtParkDTO> getUserPark() {
		// 调用远程	获取园区列表
		Result<List<SmtParkDTO>>  result = remoteParkService.getParks(SecurityConstants.FROM_IN);
		List<SmtParkDTO> parks = result.getData();
		return parks;
	}

	public void checkAo(EmployeeNoteAo employeeNoteAo) {
		Integer num = appSubjectBasicService.num(employeeNoteAo.getTextDesc());
		if (StringUtils.isBlank(employeeNoteAo.getSubjectName()) ) {
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_NAME_NULL);
		}
		if (Objects.isNull(employeeNoteAo.getParkId())) {
			throw new TCEException(ExceptionTypeEnum.APP_PARK_NULL);
		}
        if ((0 == num || StringUtil.isNullOrEmpty(employeeNoteAo.getTextDesc()))
				&& (StringUtils.isBlank(employeeNoteAo.getSubjectUrl()))
				&& (StringUtils.isBlank(employeeNoteAo.getEnclosureName()))){
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_CONTENT_NULL);
		}
		if(StringUtils.isNotBlank(employeeNoteAo.getSubjectUrl()) ){
			if(!appSubjectBasicService.isURL(employeeNoteAo.getSubjectUrl())){
				throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_URL_ERROR);
			}
		}
		if(StringUtils.isBlank(employeeNoteAo.getPicBinary())){
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_PIC_NULL);
		}
	}



	public void checkId(Integer id) {
		if (id == null) {
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_ID_NULL);
		}
		Integer count = appSubjectMapper.selectCount(Wrappers.<AppSubject>query().lambda().eq(AppSubject::getId, id));
		if(count == 0){
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_PIC_NULL);
		}
	}

}
