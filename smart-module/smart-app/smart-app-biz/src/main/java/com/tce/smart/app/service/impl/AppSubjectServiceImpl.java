package com.tce.smart.app.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.app.ao.AddAppSubjectAo;
import com.tce.smart.app.dto.AppQuestionDto;
import com.tce.smart.app.emun.DeleteState;
import com.tce.smart.app.emun.PublishState;
import com.tce.smart.app.emun.SubjectCatalog;
import com.tce.smart.app.entity.AppContentText;
import com.tce.smart.app.entity.AppParkSubject;
import com.tce.smart.app.entity.AppSubject;
import com.tce.smart.app.entity.AppSubjectContentText;
import com.tce.smart.app.mapper.AppContentTextMapper;
import com.tce.smart.app.mapper.AppSubjectContentTextMapper;
import com.tce.smart.app.mapper.AppSubjectMapper;
import com.tce.smart.app.service.*;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.tool.enums.ExceptionTypeEnum;
import com.tce.smart.tool.exception.TCEException;
import io.netty.util.internal.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Array;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 主题信息
 *
 * @author fushiping
 * @date 2019-04-25 09:44:43
 */
@Service
@Slf4j
public class AppSubjectServiceImpl extends ServiceImpl<AppSubjectMapper, AppSubject> implements AppSubjectService {

	@Autowired
	private AppSubjectBasicService appSubjectBasicService;

	@Autowired
	private AppSubjectContentTextService subjectContentTextService;

	@Autowired
	private AppSubjectContentPictureService subjectContentPictureService;

	@Autowired
	private AppParkSubjectService appParkSubjectService;

	@Autowired
	private AppSubjectContentTextMapper appSubjectContentTextMapper;

    @Autowired
	private AppContentTextMapper appContentTextMapper;

	@Autowired
    private AppSubjectMapper mapper;



	/**
	 * 通过主题类型与主题发布状态获取主题
	 * @param page
	 * @param catalogCode
	 * @return
	 */
	@Override
	public IPage<AppSubject> getAppSubjectPage(Page page, String publishFlag, String catalogCode, Integer parkId) {
		return mapper.getAppSubjectPage(page, publishFlag, catalogCode, parkId);
	}

	/**
	 * 通过主题类型与主题发布状态获取主题，过滤园区
	 * @param page 分页信息
	 * @param catalogCode 主题分类
	 * @return 分页返回
	 */
	@Override
	public IPage<AppSubject> getAppSubjectPageFilterByPark(Page page, String publishFlag, String catalogCode, Integer parkId) {
		IPage<AppSubject> pageInfo = mapper.getAppSubjectPage(page, publishFlag, catalogCode, parkId);
		//根据园区过滤
		filterSubjectByPark(pageInfo);
		return pageInfo;
	}

	/**
	 * 根据园区过滤
	 *
	 * @param pageInfo 分页内容
	 */
	@Override
	public void filterSubjectByPark(IPage<AppSubject> pageInfo) {
		if (Objects.nonNull(pageInfo) && CollectionUtils.isNotEmpty(pageInfo.getRecords())) {
			//当前用户可见的主题
			List<Integer> userSubjectIds = getCurrUserSubject(pageInfo.getRecords());
			Iterator<AppSubject> iterator = pageInfo.getRecords().iterator();
			while (iterator.hasNext()) {
				if (!userSubjectIds.contains(iterator.next().getId())) {
					iterator.remove();
				}
			}
		}
	}

	/**
	 * 过滤掉当前用户不可见的主题
	 *
	 * @param subjectList 主题列表
	 * @return 当前用户可见主题集合
	 */
	@Override
	public List<Integer> getCurrUserSubject(List<AppSubject> subjectList) {
		List<Integer> userSubjectIds = null;
		if (Objects.nonNull(SecurityUtils.getUser()) && CollectionUtils.isNotEmpty(SecurityUtils.getUser().getParkIdList())) {
			userSubjectIds = new ArrayList<>();
			//当前用户所属园区
			List<Integer> userParkIds = SecurityUtils.getUser().getParkIdList();
			//抽出主题id
			List<Integer> subjectIds = subjectList.stream().map(AppSubject::getId).collect(Collectors.toList());
			//查询主题所属园区
			List<AppParkSubject> subjectIdList = appParkSubjectService.getBySubjectIds(subjectIds);

			if (CollectionUtils.isNotEmpty(subjectIdList)) {
				for (AppParkSubject tempAppParkSubject : subjectIdList) {
					//只保留用户园区主题
					if (userParkIds.contains(tempAppParkSubject.getParkId())) {
						userSubjectIds.add(tempAppParkSubject.getSubjectId());
					}
				}
			}
		}
		return userSubjectIds;
	}

		/**
		 * 主题上移
		 * @param id
		 * @return
		 */
		@Override
		@Transactional(rollbackFor = Exception.class)
		public void moveUpwardById (Integer id){
			this.checkId(id);
			AppSubject appSubject = mapper.selectById(id);
			this.checkFlag(appSubject);
			Integer thisOrder = mapper.selectOrder(id);
			if (thisOrder == 0) {
				throw new TCEException(ExceptionTypeEnum.APP_SBUJECT_UPPER_ERROR);
			}
			if (thisOrder == 1) {
				throw new TCEException(ExceptionTypeEnum.APP_SBUJECT_TOP_ERROR);
			}
			if (thisOrder > 1) {
				Integer previousOrder = thisOrder - 1;
				Integer previousId = mapper.selectId(previousOrder, appSubject.getCatalogCode());
				mapper.updateOrder(id, previousOrder);
				mapper.updateOrder(previousId, thisOrder);
            }

		}

		/**
		 * 主题下移
		 * @param id
		 * @return
		 */
		@Override
		@Transactional(rollbackFor = Exception.class)
		public void moveDownById (Integer id){
			this.checkId(id);
			AppSubject appSubject = mapper.selectById(id);
			this.checkFlag(appSubject);
			Integer thisOrder = mapper.selectOrder(id);
			if (thisOrder == 0) {
				throw new TCEException(ExceptionTypeEnum.APP_SBUJECT_UPPER_ERROR);
			}
			Integer nextOrder = thisOrder + 1;
			Integer nextId = mapper.selectId(nextOrder, appSubject.getCatalogCode());
			if (nextId != null) {
				mapper.updateOrder(id, nextOrder);
				mapper.updateOrder(nextId, thisOrder);
				return;
			}
			throw new TCEException(ExceptionTypeEnum.APP_ORDER_LAST_ERROR);
		}

		/**
		 * 置顶（排序序号为0为置顶主题）
		 * @param id
		 * @return
		 */
		@Override
		@Transactional(rollbackFor = Exception.class)
		public void letTopById (Integer id){
			this.checkId(id);
			AppSubject appSubject = mapper.selectById(id);
			this.checkFlag(appSubject);
			Integer thisOrder = mapper.selectOrder(id);
			Integer count = selectOrderCount(appSubject.getCatalogCode());
			List<AppSubject> appSubjectList = mapper.selectList(Wrappers.<AppSubject>query().lambda()
					.eq(AppSubject::getCatalogCode, appSubject.getCatalogCode()).eq(AppSubject::getSubjectOrder, 0));
			if (appSubjectList.size() == 1) {
				Integer previousTopId = appSubjectList.get(0).getId();
				mapper.updateBatchOrder("+1", appSubject.getCatalogCode(), 1, thisOrder - 1);
				mapper.updateOrder(previousTopId, 1);
				mapper.updateOrder(id, 0);
				return;
			}
			if (appSubjectList.size() > 1) {
				throw new TCEException(ExceptionTypeEnum.APP_TOP_NUMBER_ERROR);
			}
			mapper.updateOrder(id, 0);
			mapper.updateBatchOrder("-1", appSubject.getCatalogCode(), thisOrder, count);
		}

		/**
		 * 取消置顶
		 * @param id
		 * @return
		 */
		@Override
		@Transactional(rollbackFor = Exception.class)
		public void cancleTopById (Integer id){
			this.checkId(id);
			AppSubject appSubject = mapper.selectById(id);
			this.checkFlag(appSubject);
			Integer count = mapper.selectCount(Wrappers.<AppSubject>query().lambda()
					.eq(AppSubject::getCatalogCode, appSubject.getCatalogCode())
					.eq(AppSubject::getPublishFlag, PublishState.ONLINE.getCode())
					.eq(AppSubject::getDelFlag, DeleteState.NORMOL.getCode()));
			mapper.updateBatchOrder("+1", appSubject.getCatalogCode(), 1, count - 1);
			mapper.updateOrder(id, 1);
		}

		/**
		 * 主题下线
		 * @param id
		 * @return
		 */
		@Override
		@Transactional(rollbackFor = Exception.class)
		public void offlineById (Integer id){
			checkId(id);
			AppSubject appSubjectCode = mapper.selectById(id);
			this.checkFlag(appSubjectCode);
			AppSubject appSubject = new AppSubject();
			appSubject.setId(id);
			Integer order = mapper.selectOrder(id);
			Integer count = selectOrderCount(appSubjectCode.getCatalogCode());
			appSubject.setPublishFlag(PublishState.OFFLINE.getCode());
			appSubject.setUpdateTime(LocalDateTime.now());
			if (appSubjectCode.getSubjectOrder() != 0) {
				mapper.updateBatchOrder("-1", appSubjectCode.getCatalogCode(), order + 1, count + 1);
				appSubject.setSubjectOrder(-1);
				mapper.updateById(appSubject);
				return;
			}
			appSubject.setSubjectOrder(-1);
			mapper.updateById(appSubject);
		}

		/**
		 * 批量待发布
		 * @param ids
		 * @return
		 */
		@Override
		@Transactional(rollbackFor = Exception.class)
		public void batchWait ( int[] ids){
			checkArray(ids);
			AppSubject appSubject = new AppSubject();
			for (int i = 0; i < ids.length; i++) {
				appSubject.setId(ids[i]);
				appSubject.setPublishFlag(PublishState.INIT.getCode());
				appSubject.setUpdateTime(LocalDateTime.now());
				appSubject.updateById();
			}
		}

		/**
		 * 批量上线
		 * @param ids
		 * @return
		 */
		@Override
		@Transactional(rollbackFor = Exception.class)
		public void batchOnline ( int[] ids){
			checkArray(ids);
			Integer size = ids.length;
			Integer count = selectOrderCount(mapper.selectById(ids[0]).getCatalogCode());
			AppSubject appSubject = new AppSubject();
			for (int i = 0; i < size; i++) {
				AppSubject appSubjectCode = mapper.selectById(ids[i]);
				appSubject.setId(ids[i]);
				appSubject.setPublishFlag(PublishState.ONLINE.getCode());
				mapper.updateBatchOrder("+1", appSubjectCode.getCatalogCode(), 1, count + i + 1);
				appSubject.setSubjectOrder(1);
				appSubject.setUpdateTime(LocalDateTime.now());
				appSubject.updateById();

			}
		}

		/**
		 *  删除主题
		 * @param id
		 * @return
		 */
		@Override
		@Transactional(rollbackFor = Exception.class)
		public void deleteById (Integer id){
			checkId(id);
			AppSubject appSubject = new AppSubject();
			appSubject.setId(id);
			appSubject.setDelFlag(DeleteState.DELETE.getCode());
			appSubject.setUpdateTime(LocalDateTime.now());
			mapper.updateById(appSubject);
			Integer textId = mapper.selectTextId(id);
			if (textId != null) {
				subjectContentTextService.cascadeDelete(id);
//				appContentTextService.deleteTextContent(textId);
				subjectContentPictureService.cascadeDelete(id);
			}
		}

		/**
		 * 批量删除
		 * @param ids
		 */
		@Override
		public void batchDelete ( int[] ids){
			checkArray(ids);
			AppSubject appSubject = new AppSubject();
			for (int i = 0; i < ids.length; i++) {
				appSubject.setId(ids[i]);
				appSubject.setDelFlag(DeleteState.DELETE.getCode());
				appSubject.setUpdateTime(LocalDateTime.now());
				mapper.updateById(appSubject);
				Integer textId = mapper.selectTextId(ids[i]);
				if (textId != null) {
//					appContentTextService.deleteTextContent(textId);
					subjectContentTextService.cascadeDelete(appSubject.getId());
					subjectContentPictureService.cascadeDelete(appSubject.getId());
				}
			}
		}

		/**
		 * 修改主题
		 * @param addAppSubjectAo
		 * @return
		 */
		@Override
		public void subjectUpdate (AddAppSubjectAo addAppSubjectAo){
			String catalog = this.getById(addAppSubjectAo.getId()).getCatalogCode();
			checkAddSubject(addAppSubjectAo, catalog);
			appSubjectBasicService.updateSubject(addAppSubjectAo);
		}

		/**
		 * 新增主题
		 * @param addAppSubjectAo
		 * @param catalogCode
		 */
		@Override
		public Integer subjectInsert (AddAppSubjectAo addAppSubjectAo, String catalogCode){
			checkAddSubject(addAppSubjectAo, catalogCode);
			return appSubjectBasicService.addSubject(addAppSubjectAo, catalogCode);
		}

		/**
		 * 查询主题内容
		 * @param id
		 * @return
		 */
		@Override
		public AppSubject subjectDetails (Integer id){
			checkId(id);
			return mapper.selectById(id);
		}

		/**
		 * 根据主题ID查找主题图片
		 * @param id
		 * @return
		 */
		@Override
		public AppContentText selectText (Integer id){
			checkId(id);
			AppContentText appContentText = mapper.selectText(id);
			return appContentText;
		}

	@Override
	public AppContentText selectTextNew(Integer id) {
		checkId(id);
		AppContentText appContentText = mapper.selectTextNew(id);
		return appContentText;
	}

	/**
		 * 根据ID显示问题详情
		 * @param id
		 * @return
		 */
		@Override
		public AppSubject detailQuestionById (Integer id){
			AppSubject appSubject = this.getById(id);
			return appSubject;
		}
		/**
		 * 分页显示所有问题
		 * @param page
		 * @param appQuestionDto
		 * @return
		 */
		@Override
		public IPage<AppSubject> getAppQuestionPage (Page page, AppQuestionDto appQuestionDto){
			if (appQuestionDto.getSubjectName() != null) {
				String s = '%' + appQuestionDto.getSubjectName() + '%';
				appQuestionDto.setSubjectName(s);
			}
		/*if(appQuestionDto.getStartTime() != null) {
			String time = appQuestionDto.getStartTime().substring(0, 10);
			appQuestionDto.setStartTime(time);
		}*/
			appQuestionDto.setCatalogCode(SubjectCatalog.QUESTION.type());
			return mapper.getAppQuestionPage(page, appQuestionDto);
		}
		/**
		 * 删除主题表中问题
		 * @param id
		 * @return
		 */
		@Override
		@Transactional(rollbackFor = Exception.class)
		public void deleteQuestion (Integer id){
			AppSubject appSubject = this.getById(id);
			appSubject.setUpdateTime(LocalDateTime.now());
			appSubject.setDelFlag(DeleteState.DELETE.getCode());
			this.updateById(appSubject);
			Integer textId = this.getQuestionTextId(id);
			if (textId != null) {
				this.deleteQuestionText(textId);
			}
		}
		/**
		 * 根据传入参数添加新的主题
		 * @param appSubject
		 * @return
		 */
		@Override
		public Integer insertSubject (AppSubject appSubject){
			appSubject.setParentSubject(0);
			appSubject.setCreateTime(LocalDateTime.now());
			appSubject.setDelFlag(DeleteState.NORMOL.getCode());
			appSubject.setPublishFlag(PublishState.INIT.getCode());
			appSubject.setSubjectOrder(1);
			appSubject.setSubjectUrl("");
			appSubject.setUpdateTime(LocalDateTime.now());
			this.save(appSubject);
			return appSubject.getId();
		}
		/**
		 * 根据关联表取出文本ID
		 * @param id
		 * @return
		 */
		public Integer getQuestionTextId (Integer id){
			AppSubjectContentText appSubjectContentText = appSubjectContentTextMapper.selectOne(Wrappers.<AppSubjectContentText>query().lambda().eq(AppSubjectContentText::getSubjectId, id));
			return appSubjectContentText.getContentTextId();
		}
		@Override
		public Result updateQuestion (AppQuestionDto appQuestionDto){
			return null;
		}

		@Override
		public List<AppSubject> selectByCatalogCode (String catalogCode, String publishFlag){
			QueryWrapper<AppSubject> queryWrapper = new QueryWrapper<AppSubject>();
			queryWrapper.lambda()
					.eq(AppSubject::getCatalogCode, catalogCode)
					.eq(AppSubject::getPublishFlag, publishFlag);
			List<AppSubject> appSubjectList = baseMapper.selectList(queryWrapper);
			return appSubjectList;
		}

		/**
		 * 删除问题文本表中的内容
		 * @param id
		 * @return
		 */
		public void deleteQuestionText (Integer id){
			AppContentText appContentText = appContentTextMapper.selectById(id);
			appContentText.setDelFlag(DeleteState.DELETE.getCode());
			appContentText.setUpdateTime(LocalDateTime.now());
			appContentTextMapper.updateById(appContentText);
		}


		public Integer selectOrderCount (String catalogCode){
			Integer count = mapper.selectCount(Wrappers.<AppSubject>query().lambda()
					.ne(AppSubject::getSubjectOrder, 0)
					.eq(AppSubject::getCatalogCode, catalogCode)
					.eq(AppSubject::getPublishFlag, PublishState.ONLINE.getCode())
					.eq(AppSubject::getDelFlag, DeleteState.NORMOL.getCode()));
			return count;
		}
		public void checkId (Integer id){
			if (id == null) {
				throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_ID_NULL);
			}
			Integer count = mapper.selectCount(Wrappers.<AppSubject>query().lambda().eq(AppSubject::getId, id));
			if (count == 0) {
				throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_ID_ERROR);
			}
		}

		public void checkArray ( int[] ids){
			if (Array.getLength(ids) == 0) {
				throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_BATCH_NULL);
			}
		}

		public void checkFlag (AppSubject appSubject){
			if (!PublishState.ONLINE.getCode().equals(appSubject.getPublishFlag()) || !DeleteState.NORMOL.getCode().equals(appSubject.getDelFlag())) {
				throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_FLAG_ERROR);
			}
		}

		public void checkAddSubject (AddAppSubjectAo addAppSubjectAo, String catalog){
			Integer num = appSubjectBasicService.num(addAppSubjectAo.getTextDesc());
			if (StringUtils.isBlank(addAppSubjectAo.getSubjectName())) {
				throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_NAME_NULL);
			}
			if ((num == 0 || StringUtils.isBlank(addAppSubjectAo.getTextDesc()))
					&& StringUtils.isBlank(addAppSubjectAo.getSubjectUrl()) && StringUtils.isBlank(addAppSubjectAo.getEnclosureName())) {
				throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_CONTENT_NULL);
			}
			if (StringUtils.isNotBlank(addAppSubjectAo.getSubjectUrl())) {
				if (!appSubjectBasicService.isURL(addAppSubjectAo.getSubjectUrl())) {
					throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_URL_ERROR);
				}
			}
			if ((!Objects.nonNull(addAppSubjectAo.getParkId())
					&& !SubjectCatalog.PARK_NOTICE.type().equals(catalog))) {
				throw new TCEException(ExceptionTypeEnum.APP_PARK_NULL);
			}
			if (StringUtil.isNullOrEmpty(addAppSubjectAo.getPicBinary())) {
				throw new TCEException(ExceptionTypeEnum.APP_SUBJECT_PIC_NULL);
			}
		}
	}
