package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.emun.DeleteState;
import com.tce.smart.app.emun.PublishState;
import com.tce.smart.app.emun.SubjectCatalog;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.mapper.AppBannerMapper;
import com.tce.smart.app.mapper.AppSubjectMapper;
import com.tce.smart.app.service.*;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.exception.TCEException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Banner管理
 * @author fushiping
 * @date 2019/5/22 13:52
 **/
@Service
public class AppBannerServiceImpl extends ServiceImpl<AppBannerMapper, AppSubject> implements AppBannerService {

	@Autowired
	private AppSubjectService appSubjectService;

	@Autowired
	private AppSubjectBasicService appSubjectBasicService;

	@Autowired
	private AppSubjectMapper appSubjectMapper;

	@Autowired
	private AppBannerMapper appBannerMapper;

	@Autowired
	private AppContentTextService appContentTextService;

	/**
	 * 获取Banner列表
	 * @param page
	 * @return
	 */
	@Override
	public IPage<AppSubject> getBannerList(Page page) {
		return appBannerMapper.getBannerList(page);
	}

	/**
	 * 获取Banner列表
	 * @return
	 */
	@Override
	public List<AppSubject> getOnlineBannerList() {
		return appBannerMapper.getOnlineBannerList();
	}

	/**
	 * 新增
	 * @param addAppSubjectAo
	 * @return
	 */
	@Override
	public Integer addSubject(AddAppSubjectAo addAppSubjectAo) {
		this.checkAddSubject(addAppSubjectAo);
		return appSubjectBasicService.addSubject(addAppSubjectAo, SubjectCatalog.APP_BANNER.type());
	}

	/**
	 * 修改主题
	 * @param addAppSubjectAo
	 */
	@Override
	public void updateSubject(AddAppSubjectAo addAppSubjectAo) {
		this.checkAddSubject(addAppSubjectAo);
		appSubjectBasicService.updateSubject(addAppSubjectAo);
	}

	/**
	 * 上线
	 * @param id
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void onlineById(Integer id) {
		checkId(id);
		Integer count = appSubjectService.selectOrderCount(SubjectCatalog.APP_BANNER.type());
		if(count<6) {
			AppSubject appSubject = new AppSubject();
			appSubject.setId(id);
			appSubject.setPublishFlag(PublishState.ONLINE.getCode());
			appSubject.setSubjectOrder(count + 1);
			appSubject.setUpdateTime(LocalDateTime.now());
			appSubject.updateById();
			return;
		}
		throw new TCEException(ExceptionTypeEnum.APP_BANNER_ERROR);
	}

	/**
	 * 取消上线
	 * @param id
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void waitById(Integer id) {
		checkId(id);
		AppSubject appSubject = new AppSubject();
		appSubject.setId(id);
		Integer order = appSubjectMapper.selectOrder(id);
		Integer count = appSubjectService.selectOrderCount(SubjectCatalog.APP_BANNER.type());
		appSubject.setPublishFlag(PublishState.INIT.getCode());
		appSubject.setUpdateTime(LocalDateTime.now());
		appSubject.setSubjectOrder(-1);
		appSubjectMapper.updateBatchOrder("-1", SubjectCatalog.APP_BANNER.type(), order + 1, count + 1);
		appBannerMapper.updateById(appSubject);
	}

	/**
	 * 删除主题
	 * @param id
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteById(Integer id) {
		this.checkId(id);
		AppSubject appSubject = new AppSubject();
		appSubject.setId(id);
		appSubject.setDelFlag(DeleteState.DELETE.getCode());
		appSubject.setUpdateTime(LocalDateTime.now());
		String publishFlag = this.checkState(id);
		Integer textId = appSubjectMapper.selectTextId(id);
		if ( textId != null) {
			appContentTextService.deleteTextContent(textId);
		}
		appSubject.setSubjectOrder(-1);
		if ("1".equals(publishFlag)) {
			Integer order = appSubjectMapper.selectOrder(id);
			Integer count = appSubjectService.selectOrderCount(SubjectCatalog.APP_BANNER.type());
			//批量修改序号
			appSubjectMapper.updateBatchOrder("-1", SubjectCatalog.APP_BANNER.type(),order + 1, count + 1);
			appBannerMapper.updateById(appSubject);
			return;
		}
		if ("0".equals(publishFlag)) {
			appBannerMapper.updateById(appSubject);
        }

	}

	public void checkId(Integer id) {
		if (id == null) {
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_ID_NULL);
		}
		Integer count = appBannerMapper.selectCount(Wrappers.<AppSubject>query().lambda().eq(AppSubject::getId, id));
		if(count == 0){
			throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_ID_ERROR);
		}
	}

	public String checkState (Integer id) {
		AppSubject appSubject = appBannerMapper.selectById(id);
		return appSubject.getPublishFlag();
	}

	public  void checkAddSubject(AddAppSubjectAo addAppSubjectAo) {
		if (StringUtils.isBlank(addAppSubjectAo.getSubjectUrl())) {
			throw new TCEException(ExceptionTypeEnum.APP_MODULE_URL_NULL);
		}
		if (StringUtils.isBlank(addAppSubjectAo.getPicBinary())) {
			throw new TCEException(ExceptionTypeEnum.APP_PICTURE_NULL);
		}
	}
}
