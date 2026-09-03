package com.tce.smart.platform.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.enums.SmtVisitorEnum;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.api.dto.CardDTO;
import com.tce.smart.platform.api.dto.SmtStaffDTO;
import com.tce.smart.platform.core.dto.DeviceTaskVO;
import com.tce.smart.platform.core.dto.UpdateDeviceAuthDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtStaffDeviceAuthMapper;
import com.tce.smart.platform.core.service.*;
import com.tce.smart.platform.core.util.PermissionValidityWindow;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.*;
import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@AllArgsConstructor
@Service
@Slf4j
public class SmtStaffDeviceAuthServiceImpl extends ServiceImpl<SmtStaffDeviceAuthMapper, SmtStaffDeviceAuth> implements SmtStaffDeviceAuthService {

	private final SmtStaffDeviceAuthMapper mapper;

	private final SmtDeviceTaskService smtDeviceTaskService;

	private final SmtIscDeviceTaskService smtIscDeviceTaskService;

	private final SmtParkBuService smtParkBuService;

	private final SmtDeviceAuthorityRelationService deviceAuthorityRelationService;

	private final SmtStaffService staffService;

	private final SmtDeviceTaskDetailService smtDeviceTaskDetailService;

	@Lazy
	private final SmtTaskDownRecordService smtTaskDownRecordService;

	@Lazy
	private final SmtIscDownRecordService iscDownRecordService;

	private final RemoteDispatcherService remoteDispatcherService;

	@Override
	public Integer removeAuthById(Integer id) {
		// TODO Auto-generated method stub
		return mapper.deleteById(id);
	}

	@Override
	public Boolean removeByAuthId(Integer authId) {
		return remove(Wrappers.<SmtStaffDeviceAuth>lambdaQuery().eq(SmtStaffDeviceAuth::getAuthId, authId));
	}

	@Override
	public Integer addAuth(SmtStaffDeviceAuth auth) {
		// TODO Auto-generated method stub
		return mapper.insert(auth);
	}


	@Transactional
	@Override
	public Boolean updateAuth(UpdateDeviceAuthDTO auth) {
		List<String> staffIds = auth.getIds();
		for (String staffId : staffIds) {
			//这里以后要做多园区的话 要在SmtStaffDeviceAuth表添加园区字段 并在这里区分
			List<SmtStaffDeviceAuth> reStaffDevAuths = this.list(new LambdaQueryWrapper<SmtStaffDeviceAuth>()
					.eq(SmtStaffDeviceAuth::getStaffId, Long.parseLong(staffId))
			);
			List<Integer> oldAuthIds;
			if (CollectionUtils.isNotEmpty(reStaffDevAuths)) {
				oldAuthIds = reStaffDevAuths.stream().map(SmtStaffDeviceAuth::getAuthId).collect(Collectors.toList());
			} else {
				oldAuthIds = new ArrayList<>();
			}
			// 删除旧权限
			this.remove(Wrappers.<SmtStaffDeviceAuth>lambdaQuery().eq(SmtStaffDeviceAuth::getStaffId, Long.parseLong(staffId)));
			// 添加新权限
			List<SmtStaffDeviceAuth> deviceAuthList = new ArrayList<>();
			for (Integer newAuthId : auth.getDeviceAuthIds()) {
				SmtStaffDeviceAuth staffDeviceAuth = new SmtStaffDeviceAuth();
				staffDeviceAuth.setStaffId(Long.parseLong(staffId));
				staffDeviceAuth.setAuthId(newAuthId);
				staffDeviceAuth.setCreateTime(new Date());
				deviceAuthList.add(staffDeviceAuth);
			}
			saveBatch(deviceAuthList);
			//更新员工设备权限
			SmtStaff staff = staffService.getById(Long.parseLong(staffId));
			if (!staff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode())) {
				// 若员工非离职状态，则更新通关权限
				smtDeviceTaskService.updateStaffAuth(staff, oldAuthIds, auth.getDeviceAuthIds(), DeviceTaskConstants.CARD_STAFF_IMPORT);
			}
//			SmtStaffDeviceAuth reStaffDevAuth = reStaffDevAuths.get(0);
//
//			if(!reStaffDevAuth.getAuthId().equals(auth.getDeviceAuthId())){
//				//员工权限已修改
//				SmtStaffDeviceAuth up = new SmtStaffDeviceAuth();
//				up.setStaffId(Long.parseLong(long1));
//				up.setAuthId(auth.getDeviceAuthId());
//				up.setCreateTime(DateUtils.date());
//				up.setId(reStaffDevAuth.getId());
//				mapper.updateById(up);
//
//				//更新员工权限
//				SmtStaff staff = staffService.getById(Long.parseLong(long1));
//
//				if(!staff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode())) {
//					//如果员工是离职状态 不生成设备权限任务
//					smtDeviceTaskService.updateStaffAuth(staff, auth.getDeviceAuthId(), reStaffDevAuth.getAuthId(), DeviceTaskConstants.CARD_STAFF_IMPORT);
//				}
//			}
		}
		return true;
	}


	/**
	 * 获得权限下发进度
	 *
	 * @return
	 */
	@Override
	public List<SmtDeviceTaskDetail> getTaskResult(String taskListId) {
		List<SmtDeviceTaskDetail> list = smtDeviceTaskDetailService.list(Wrappers.<SmtDeviceTaskDetail>query().lambda().eq(SmtDeviceTaskDetail::getTaskListId, taskListId));
		for (SmtDeviceTaskDetail detail : list) {
			boolean b = DeviceTaskStatusEnum.FAIL.getCode().equals(detail.getStatus())
					|| DeviceTaskStatusEnum.CANCEL.getCode().equals(detail.getStatus());
			if (b && StringUtils.isEmpty(detail.getRemark())) {
				detail.setStatus(DeviceTaskStatusEnum.FAIL.getCode());
				SmtIscDeviceTask iscTask = smtIscDeviceTaskService.getById(Long.parseLong(detail.getTaskId()));
				if (Objects.nonNull(iscTask) && StringUtils.isNotEmpty(iscTask.getRemark())) {
					detail.setRemark(iscTask.getRemark());
					continue;
				}
				SmtDeviceTask task = smtDeviceTaskService.getById(Integer.parseInt(detail.getTaskId()));
				if (Objects.nonNull(task) && StringUtils.isNotEmpty(task.getRemark())) {
					detail.setRemark(task.getRemark());
					continue;
				}
			}
		}
		return list;

/*		List<SmtDeviceAuthorityRelation> newDeviceAuthorityRelations = smtDeviceAuthorityRelationMapper
				.selectList(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>().in(SmtDeviceAuthorityRelation::getAuthorityId, auth.getDeviceAuthIds()));
		List<String> newDev = newDeviceAuthorityRelations.stream().map(SmtDeviceAuthorityRelation::getDeviceId).collect(Collectors.toList());
		List<SmtIscDeviceTask> iscTask =  smtIscDeviceTaskService.getNewTask(auth.getIds(), newDev);
		List<SmtDeviceTask> tasks = smtDeviceTaskService.getNewTask(auth.getIds(), newDev);
		if(CollUtil.isNotEmpty(iscTask)) {
			List<SmtDeviceTask> iscList = BeanUtils.batchTransform(SmtDeviceTask.class, iscTask);
			tasks.addAll(iscList);
		}
		return tasks;*/
	}

	@Transactional
	@Override
	public String updateAuthNew(Integer type, UpdateDeviceAuthDTO auth) {
		// 在任何关联或任务写入前校验日期，非法请求必须零写入。
		PermissionValidityWindow validityWindow = PermissionValidityWindow.resolve(auth.getStartTime(), auth.getEndTime());
		List<String> staffIds = auth.getIds();
		String taskRecordNum = UUID.randomUUID().toString();
		for (String staffId : staffIds) {

			//这里以后要做多园区的话 要在SmtStaffDeviceAuth表添加园区字段 并在这里区分
			// 查询该员工关联的权限列表
			List<SmtStaffDeviceAuth> reStaffDevAuths = this.list(new LambdaQueryWrapper<SmtStaffDeviceAuth>()
					.eq(SmtStaffDeviceAuth::getStaffId, Long.parseLong(staffId))
			);
			List<Integer> oldAuthIds;
			if (CollectionUtils.isNotEmpty(reStaffDevAuths)) {
				oldAuthIds = reStaffDevAuths.stream().map(SmtStaffDeviceAuth::getAuthId).collect(Collectors.toList());
			} else {
				oldAuthIds = new ArrayList<>();
			}
			// 覆盖权限，删除旧权限
			List<Integer> newAuthIds = new ArrayList<>();

			if (type == 2) {
				this.remove(Wrappers.<SmtStaffDeviceAuth>lambdaQuery().eq(SmtStaffDeviceAuth::getStaffId, Long.parseLong(staffId)));
			}else if(type == 3) {
				//删除下发记录
				smtTaskDownRecordService.remove(buildStaffDownRecordCleanupQuery(staffId));
				iscDownRecordService.remove(buildStaffIscDownRecordCleanupQuery(staffId));
				smtDeviceTaskService.remove(buildStaffDeviceTaskCleanupQuery(staffId));
				//重新下发不删除权限
				newAuthIds.addAll(oldAuthIds);
				oldAuthIds.clear();
			}else{
				// 追加权限，保留旧权限
				newAuthIds.addAll(oldAuthIds);
			}
			// 添加新权限
			List<SmtStaffDeviceAuth> deviceAuthList = new ArrayList<>();
			if(CollUtil.isNotEmpty(auth.getDeviceAuthIds())) {
				for (Integer newAuthId : auth.getDeviceAuthIds()) {
					// 若为追加权限，过滤已存在的权限
					if (type.equals(1) && oldAuthIds.contains(newAuthId)) {
						continue;
					}
					SmtStaffDeviceAuth staffDeviceAuth = new SmtStaffDeviceAuth();
					staffDeviceAuth.setStaffId(Long.parseLong(staffId));
					staffDeviceAuth.setAuthId(newAuthId);
					staffDeviceAuth.setCreateTime(new Date());
					staffDeviceAuth.setStartTime(validityWindow.getStartDateTime());
					staffDeviceAuth.setEndTime(validityWindow.getEndDateTime());
					deviceAuthList.add(staffDeviceAuth);
					newAuthIds.add(newAuthId);
				}
			}
			saveBatch(deviceAuthList);
			//更新员工设备权限
			SmtStaff staff = staffService.getById(Long.parseLong(staffId));

			// 若员工非离职状态，则更新通关权限
			if (!staff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode())) {
				smtIscDeviceTaskService.cancelSupersededStaffAuthTasks(staffId);
				smtDeviceTaskService.updateStaffAuthNew(staff, oldAuthIds, newAuthIds,
						DeviceTaskConstants.CARD_STAFF_IMPORT, taskRecordNum, type,
						validityWindow.getStartTime(), validityWindow.getOverTime());
			} else {
				SmtDeviceTaskDetail deviceTaskDetail = SmtDeviceTaskDetail.builder()
						.action(DeviceTaskActionEnum.DOWN.getCode())
						.status(DeviceTaskStatusEnum.CANCEL.getCode()).badge(staff.getBadge()).name(staff.getName())
						.createTime(LocalDateTime.now()).taskListId(taskRecordNum).remark("员工已离职").build();
				deviceTaskDetail.insert();
			}

		}
		return taskRecordNum;
	}

	private LambdaQueryWrapper<SmtTaskDownRecord> buildStaffDownRecordCleanupQuery(String staffId) {
		return new LambdaQueryWrapper<SmtTaskDownRecord>()
				.eq(SmtTaskDownRecord::getCardNo, staffId)
				.eq(SmtTaskDownRecord::getDeviceType, DeviceTaskConstants.CARD)
				.in(SmtTaskDownRecord::getServiceType, DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskConstants.UPDATE_FACE);
	}

	private LambdaQueryWrapper<SmtIscDownRecord> buildStaffIscDownRecordCleanupQuery(String staffId) {
		return new LambdaQueryWrapper<SmtIscDownRecord>()
				.eq(SmtIscDownRecord::getCardNo, staffId)
				.eq(SmtIscDownRecord::getDeviceType, DeviceTaskConstants.CARD)
				.in(SmtIscDownRecord::getServiceType, DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskConstants.UPDATE_FACE);
	}

	private LambdaQueryWrapper<SmtDeviceTask> buildStaffDeviceTaskCleanupQuery(String staffId) {
		// 在途任务（DOING）不删除：避免进度查询悬挂、并发回写丢失，让其自然走完状态机
		return new LambdaQueryWrapper<SmtDeviceTask>()
				.eq(SmtDeviceTask::getCardNo, staffId)
				.eq(SmtDeviceTask::getDeviceType, DeviceTaskConstants.CARD)
				.in(SmtDeviceTask::getServiceType, DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskConstants.UPDATE_FACE)
				.and(wrapper -> wrapper.ne(SmtDeviceTask::getStatus, DeviceTaskStatusEnum.DOING.getCode())
						.or().isNull(SmtDeviceTask::getStatus));
	}

	@Transactional
	@Override
	public Boolean updateAuth(List<Long> staffIds, List<Integer> deviceAuthIds) {
		// TODO Auto-generated method stub
		for (Long staffId : staffIds) {
			//这里以后要做多园区的话 要在SmtStaffDeviceAuth表添加园区字段 并在这里区分
			List<SmtStaffDeviceAuth> reStaffDevAuths = this.list(Wrappers.<SmtStaffDeviceAuth>lambdaQuery()
					.eq(SmtStaffDeviceAuth::getStaffId, staffId));
			List<Integer> oldDeviceAuthIds;
			if (CollectionUtils.isNotEmpty(reStaffDevAuths)) {
				oldDeviceAuthIds = reStaffDevAuths.stream().map(SmtStaffDeviceAuth::getAuthId).collect(Collectors.toList());
			} else {
				oldDeviceAuthIds = new ArrayList<>();
			}
			if (CollectionUtils.isNotEmpty(deviceAuthIds)) {
				// 先删后插入
				List<SmtStaffDeviceAuth> staffDeviceAuthList = new ArrayList<>();
				deviceAuthIds.forEach(deviceAuthId -> {
					SmtStaffDeviceAuth staffDeviceAuth = new SmtStaffDeviceAuth();
					staffDeviceAuth.setStaffId(staffId);
					staffDeviceAuth.setAuthId(deviceAuthId);
					staffDeviceAuth.setCreateTime(DateUtils.date());
					staffDeviceAuthList.add(staffDeviceAuth);
				});
				this.remove(Wrappers.<SmtStaffDeviceAuth>lambdaQuery().eq(SmtStaffDeviceAuth::getStaffId, staffId));
				this.saveBatch(staffDeviceAuthList);
				//更新员工权限
				SmtStaff staff = staffService.getById(staffId);
				if (!staff.getStatus().equals(StaffStatusEnum.STAFF_STATUS_QUIT.getCode())) {
					//如果员工是离职状态 不生成设备权限任务
					smtIscDeviceTaskService.cancelSupersededStaffAuthTasks(staffId.toString());
					smtDeviceTaskService.updateStaffAuth(staff, oldDeviceAuthIds, deviceAuthIds, DeviceTaskConstants.CARD_STAFF_IMPORT);
				}
			}
		}
		return true;
	}


	@Transactional
	@Override
	public Boolean applyAuthDiff(List<Long> staffIds, List<Integer> addedAuthIds, List<Integer> removedAuthIds) {
		if (CollectionUtils.isEmpty(addedAuthIds) && CollectionUtils.isEmpty(removedAuthIds)) {
			return true;
		}
		for (Long staffId : staffIds) {
			List<SmtStaffDeviceAuth> currentAuths = this.list(Wrappers.<SmtStaffDeviceAuth>lambdaQuery()
					.eq(SmtStaffDeviceAuth::getStaffId, staffId));
			List<Integer> oldAuthIds = currentAuths.stream().map(SmtStaffDeviceAuth::getAuthId).collect(Collectors.toList());
			// 目标权限 = (现有权限 - 被删权限) + 新增权限，员工已有的其他权限保持不动
			List<Integer> newAuthIds = oldAuthIds.stream().filter(authId -> !removedAuthIds.contains(authId)).collect(Collectors.toList());
			List<Integer> toAddAuthIds = addedAuthIds.stream().distinct().filter(authId -> !newAuthIds.contains(authId)).collect(Collectors.toList());
			newAuthIds.addAll(toAddAuthIds);
			List<Integer> toRemoveIds = currentAuths.stream()
					.filter(auth -> removedAuthIds.contains(auth.getAuthId()))
					.map(SmtStaffDeviceAuth::getId).collect(Collectors.toList());
			if (CollectionUtils.isEmpty(toAddAuthIds) && CollectionUtils.isEmpty(toRemoveIds)) {
				// 该员工权限无变化，不重复下发
				continue;
			}
			SmtStaff staff = staffService.getById(staffId);
			if (Objects.isNull(staff)) {
				log.warn("applyAuthDiff: staff not found, staffId={}", staffId);
				continue;
			}
			if (CollectionUtils.isNotEmpty(toRemoveIds)) {
				this.removeByIds(toRemoveIds);
			}
			if (CollectionUtils.isNotEmpty(toAddAuthIds)) {
				List<SmtStaffDeviceAuth> staffDeviceAuthList = new ArrayList<>();
				for (Integer authId : toAddAuthIds) {
					SmtStaffDeviceAuth staffDeviceAuth = new SmtStaffDeviceAuth();
					staffDeviceAuth.setStaffId(staffId);
					staffDeviceAuth.setAuthId(authId);
					staffDeviceAuth.setCreateTime(DateUtils.date());
					staffDeviceAuthList.add(staffDeviceAuth);
				}
				this.saveBatch(staffDeviceAuthList);
			}
			if (!StaffStatusEnum.STAFF_STATUS_QUIT.getCode().equals(staff.getStatus())) {
				//如果员工是离职状态 不生成设备权限任务
				smtIscDeviceTaskService.cancelSupersededStaffAuthTasks(staffId.toString());
				smtDeviceTaskService.updateStaffAuth(staff, oldAuthIds, newAuthIds, DeviceTaskConstants.CARD_STAFF_IMPORT);
			}
		}
		return true;
	}


	private void addDeviceTask(SmtStaff smtStaff, Integer authType) {
		List<SmtParkBu> list = smtParkBuService.list(Wrappers.<SmtParkBu>query().lambda().eq(SmtParkBu::getCompId, smtStaff.getCompId()));
		if (CollUtil.isNotEmpty(list)) {
			list.forEach(bu -> {

				//设备权限表
				List<SmtDeviceAuthorityRelation> selectList = deviceAuthorityRelationService
						.getRelationAuth(bu.getParkId(), BusinessAuthorityEnum.STAFF_FACE.getCode(), DeviceAuthorityEnum.STAFF);
				log.info("DeviceAuthorityListCount:" + selectList.size());
				DeviceTaskVO deviceTaskVO = null;
				CardDTO cardInfo = null;

				log.info("=============卡片删除开始==============");
				int count = smtDeviceTaskService.count(Wrappers.<SmtDeviceTask>query().lambda().eq(SmtDeviceTask::getCardNo, smtStaff.getId().toString())
						.eq(SmtDeviceTask::getDeviceType, DeviceTaskConstants.CARD));
				if (count > 0) {
					smtDeviceTaskService.remove(Wrappers.<SmtDeviceTask>query().lambda().eq(SmtDeviceTask::getCardNo, smtStaff.getId().toString())
							.eq(SmtDeviceTask::getDeviceType, DeviceTaskConstants.CARD));
//					for (int i = 0; i < selectList.size(); i++) {
//						CardDelDTO cardDelDTO = new CardDelDTO();
//						cardDelDTO.setCardNo(smtStaff.getId().toString());
//						cardDelDTO.setDeviceCode(selectList.get(i).getDeviceId());
//						Result<?> result = null;
					//Result<?> result = remoteCardService.delete(cardDelDTO, SecurityConstants.FROM_IN);

//						log.info("remoteCardService delete result：{}", result);
//						if (result.getCode() == ExceptionTypeEnum.CHECK_SUCCESS.getCode()) {
//							log.info("卡片删除成功，删除信息：{}", cardDelDTO);
//						} else {
//							log.info("卡片删除失败，删除信息：{}", cardDelDTO);
//						}
//					}
				}


				//推迟1分钟下发
				Long addStartSecond = DateUtil.currentSeconds() + 60;
				for (int i = 0; i < selectList.size(); i++) {
					deviceTaskVO = new DeviceTaskVO();
					deviceTaskVO.setAction(DeviceTaskConstants.DOWN);
					deviceTaskVO.setCardNo(smtStaff.getId().toString());
					deviceTaskVO.setDeviceCode(selectList.get(i).getDeviceId());
					deviceTaskVO.setServiceType(DeviceTaskConstants.CARD_STAFF_IMPORT);
					deviceTaskVO.setImageId(smtStaff.getFacePicId());
					deviceTaskVO.setCardType(SmtVisitorEnum.CARD_TYPE_1.getType());
					deviceTaskVO.setGeneral(smtStaff.getBadge() + SymbolConstants.MINUS + smtStaff.getName());
					deviceTaskVO.setDeviceType(DeviceTaskConstants.CARD);
					deviceTaskVO.setStartTime(addStartSecond);
					deviceTaskVO.setOverTime(DeviceTaskConstants.maxTime);

					//添加下发设备任务-添加卡片
					smtDeviceTaskService.saveTask(deviceTaskVO);
				}
			});
		}
	}


	/**
	 * 人员卡片删除
	 *
	 * @param staffId  员工工号
	 * @param deviceId 设备编号
	 * @return
	 */
	private CardDTO delCardInfo(String staffId, String deviceId) {
		CardDTO cardDTO = new CardDTO();
		cardDTO.setCardNo(staffId);
		cardDTO.setDeviceCode(deviceId);
		return cardDTO;
	}


	@Override
	public List<SmtStaffDeviceAuth> queryList(Long staffId) {
		// TODO Auto-generated method stub
		List<SmtStaffDeviceAuth> selectList = mapper.selectList(Wrappers.<SmtStaffDeviceAuth>query().lambda().eq(SmtStaffDeviceAuth::getStaffId, staffId));
		return selectList;
	}

	@Override
	public List<SmtStaffDeviceAuth> querySecurityAuth(Integer parkId) {
		return this.mapper.getSecurityAuth(parkId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeAuthToDevice(List<Integer> ids, List<String> deviceIds) {
		if (CollectionUtil.isEmpty(deviceIds)) {
			return this.removeByIds(ids);
		}
		for (Integer id : ids) {
			SmtStaffDeviceAuth staffDeviceAuth = getById(id);
			SmtStaff staff = staffService.getById(staffDeviceAuth.getStaffId());
			if (StrUtil.isBlank(staff.getFacePicId())) {
				log.info("员工[{}]人脸图片ID为空", staff.getBadge());
				continue;
			}
			smtDeviceTaskService.addDeviceDelTaskImmed(deviceIds, staff.getId().toString(),
					staff.getName(), DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskActionEnum.DELAY_DEL.getCode(),
					SmtVisitorEnum.CARD_TYPE_1, DeviceTaskConstants.CARD, staff.getFacePicId());
		}
		this.removeByIds(ids);
		return true;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeAuthToDevice(SmtStaffDeviceAuth staffAuth, List<String> deviceIds) {
		SmtStaff staff = staffService.getById(staffAuth.getStaffId());
		if (StrUtil.isBlank(staff.getFacePicId())) {
			log.info("员工[{}]人脸图片ID为空", staff.getBadge());
			return false;
		}
		smtDeviceTaskService.addDeviceDelTaskImmed(deviceIds, staff.getId().toString(),
				staff.getName(), DeviceTaskConstants.CARD_STAFF_IMPORT, DeviceTaskActionEnum.DELAY_DEL.getCode(),
				SmtVisitorEnum.CARD_TYPE_1, DeviceTaskConstants.CARD, staff.getFacePicId());

		return this.removeById(staffAuth.getId());
	}
}
