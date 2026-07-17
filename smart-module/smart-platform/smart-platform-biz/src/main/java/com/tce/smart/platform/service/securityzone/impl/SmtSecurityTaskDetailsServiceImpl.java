package com.tce.smart.platform.service.securityzone.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityApplyPersonReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.UpdateFaceImgReqDTO;
import com.tce.smart.platform.core.dto.SecurityAuthDispatchContext;
import com.tce.smart.platform.core.entity.SmtIscDeviceTask;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityTaskDetails;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthApplyMapper;
import com.tce.smart.platform.core.mapper.SmtSecurityTaskDetailsMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.service.SmtIscDeviceTaskService;
import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.securityzone.SmtSecurityTaskDetailsService;
import com.tce.smart.tool.enums.DeviceDownStatusEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import com.tce.smart.tool.enums.RelationAuthTypeEnum;
import com.tce.smart.tool.enums.SmtImageEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author fushiping
 * @date 2021-07-29 11:13:17
 */
@Service
@Slf4j
public class SmtSecurityTaskDetailsServiceImpl extends ServiceImpl<SmtSecurityTaskDetailsMapper, SmtSecurityTaskDetails> implements SmtSecurityTaskDetailsService {

	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtImageService smtImageService;
	@Autowired
	private SmtIscDeviceTaskService smtIscDeviceTaskService;
	@Autowired
	private SmtSecurityAuthApplyMapper smtSecurityAuthApplyMapper;
	@Autowired
	private SmtDeviceAuthorityService smtDeviceAuthorityService;
	@Autowired
	private SmtStaffDeviceAuthService smtStaffDeviceAuthService;

	@Override
	public Boolean initTask(List<SecurityApplyPersonReqDTO> personReq, Long applyId) {
		//审批通过后初始化任务
		if (CollUtil.isEmpty(personReq)) {
			return Boolean.FALSE;
		}
		List<SmtSecurityTaskDetails> taskDetails = new ArrayList<>();
		LocalDateTime now = LocalDateTime.now();
		for (SecurityApplyPersonReqDTO person : personReq) {
			if (CollUtil.isEmpty(person.getApplyAuths())) {
				continue;
			}
			SmtStaff staff = smtStaffService.getSimpleSttaffByBadge(person.getBadge());
			for (SecurityApplyPersonReqDTO.ApplyAuth auth : person.getApplyAuths()) {
				//添加任务详情
				SmtSecurityTaskDetails details = SmtSecurityTaskDetails.builder()
						.staffId(staff.getId())
						.applyId(applyId).areaName(person.getAreaName())
						.authId(auth.getAuthId()).authName(auth.getAuthName())
						.createTime(now).imgCode(staff.getFacePicId())
						.staffBadge(person.getBadge()).staffName(person.getStaffName())
						.status(DeviceDownStatusEnum.WAIT.getCode()).build();
				taskDetails.add(details);
			}
		}
		return this.saveBatch(taskDetails);
	}

	@Override
	public List<SmtSecurityTaskDetails> getList(Long taskId) {
		//更新设备下发状态
		this.syncTaskStatus(taskId);
		return this.list(Wrappers.<SmtSecurityTaskDetails>query().lambda()
				.eq(SmtSecurityTaskDetails::getApplyId, taskId));
	}

	/**
	 * 更新员工人脸图片
	 * @param req 任务详情
	 * @return Boolean 更新结果
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean reloadImg(UpdateFaceImgReqDTO req) {
		// 获取人脸图片的base64编码
		String base64 = req.getFaceBase64();
		// 如果base64编码为空，则返回false
		if (StringUtils.isEmpty(base64)) {
			return Boolean.FALSE;
		}
		// 根据任务详情id获取任务详情
		SmtSecurityTaskDetails detail = this.getById(req.getDetailId());
		// 根据员工id获取员工信息
		SmtStaff staff = smtStaffService.getById(detail.getStaffId());
		try {
			// 将人脸图片保存到图片服务器
			String facePicId = smtImageService.saveImage(0, base64, SmtImageEnum.TYPE_STAFF_FACE.getCode());
			// 更新员工的人脸图片id
			staff.setFacePicId(facePicId);
			// 上传人脸库
			smtStaffService.createStaffPhotoUploadRecord(staff);
			// 更新人脸图片
			smtStaffService.updateById(staff);
			// 根据认证id获取员工设备认证信息
			List<SmtStaffDeviceAuth> authList = smtStaffDeviceAuthService.list(Wrappers.<SmtStaffDeviceAuth>query()
					.lambda().eq(SmtStaffDeviceAuth::getAuthId, detail.getAuthId()));
			// 获得下发策略
			String remark = smtStaffService.updatePersonCard(staff, base64, facePicId, authList, null, null);
			// 修改设备下发任务
			detail.setStatus(DeviceDownStatusEnum.IN_WORK.getCode());
			if (StringUtils.isNotEmpty(remark)) {
				// 如果获得下发策略失败，则修改任务状态为失败
				detail.setStatus(DeviceDownStatusEnum.FAIL.getCode());
				// 记录失败原因
				detail.setRemark(remark);
			}
			// 记录更新时间
			detail.setUpdateTime(LocalDateTime.now());
			// 记录图片编码
			detail.setImgCode(facePicId);
			// 更新任务详情
			this.updateById(detail);
		} catch (Exception e) {
			log.error("更改图片下发设备失败 ,badge={}", staff.getBadge(), e);
		}
		return Boolean.TRUE;
	}

	/**
	 * 下发权限
	 * @param applyId 申请ID
	 * @param badge 工号
	 * @return Boolean
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean downDevice(Long applyId, String badge) {
		// 根据申请ID查询设备信息
		List<SmtSecurityTaskDetails> details = this.list(Wrappers.<SmtSecurityTaskDetails>query()
				.lambda().eq(SmtSecurityTaskDetails::getStatus, DeviceDownStatusEnum.WAIT.getCode())
				.eq(SmtSecurityTaskDetails::getApplyId, applyId));
		// 如果设备信息为空，返回false
		if (CollUtil.isEmpty(details)) {
			return Boolean.FALSE;
		}
		// 遍历设备信息，下发权限
		for (SmtSecurityTaskDetails detail : details) {
			this.down(detail, badge);
		}
		return Boolean.TRUE;
	}

	@Override
	public int rebindDispatchBatch(Long applyId, Long dispatchBatchId) {
		return this.baseMapper.update(null, Wrappers.<SmtSecurityTaskDetails>lambdaUpdate()
				.eq(SmtSecurityTaskDetails::getApplyId, applyId)
				.and(wrapper -> wrapper.ne(SmtSecurityTaskDetails::getStatus, DeviceDownStatusEnum.SUCCESS.getCode())
						.or().isNull(SmtSecurityTaskDetails::getStatus))
				.set(SmtSecurityTaskDetails::getStatus, DeviceDownStatusEnum.WAIT.getCode())
				.set(SmtSecurityTaskDetails::getDispatchBatchId, dispatchBatchId));
	}

	@Override
	public int countDispatchPeople(Long applyId, Long dispatchBatchId) {
		return this.baseMapper.countDispatchPeople(applyId, dispatchBatchId);
	}

	@Override
	public boolean claimDispatchDetail(Long detailId, Long dispatchBatchId) {
		return this.baseMapper.update(null, Wrappers.<SmtSecurityTaskDetails>lambdaUpdate()
				.eq(SmtSecurityTaskDetails::getId, detailId)
				.eq(SmtSecurityTaskDetails::getStatus, DeviceDownStatusEnum.WAIT.getCode())
				.eq(SmtSecurityTaskDetails::getDispatchBatchId, dispatchBatchId)
				.set(SmtSecurityTaskDetails::getStatus, DeviceDownStatusEnum.IN_WORK.getCode())) == 1;
	}

	@Override
	public List<SmtSecurityTaskDetails> listPendingDispatchDetails(int limit) {
		if (limit <= 0) {
			throw new IllegalArgumentException("下发候选上限必须大于零");
		}
		return this.baseMapper.listPendingCurrentDispatchDetails(DeviceDownStatusEnum.WAIT.getCode(), limit);
	}

	@Override
	public int dispatchCurrentBatchDetails(Long applyId, Long dispatchBatchId, String applyBadge,
			List<Long> staffIds) {
		if (CollUtil.isEmpty(staffIds)) {
			return 0;
		}
		List<SmtSecurityTaskDetails> details = this.list(Wrappers.<SmtSecurityTaskDetails>lambdaQuery()
				.eq(SmtSecurityTaskDetails::getApplyId, applyId)
				.eq(SmtSecurityTaskDetails::getDispatchBatchId, dispatchBatchId)
				.eq(SmtSecurityTaskDetails::getStatus, DeviceDownStatusEnum.WAIT.getCode())
				.in(SmtSecurityTaskDetails::getStaffId, staffIds));
		int processed = 0;
		for (SmtSecurityTaskDetails detail : details) {
			if (!claimDispatchDetail(detail.getId(), dispatchBatchId)) {
				continue;
			}
			SmtStaff staff = smtStaffService.getById(detail.getStaffId());
			if (staff == null) {
				throw new IllegalStateException("保密区下发员工不存在：" + detail.getStaffId());
			}
			List<SmtStaffDeviceAuth> existingAuths = checkAuth(detail.getAuthId(), staff.getId());
			List<SmtStaffDeviceAuth> staffAuthList = CollUtil.isEmpty(existingAuths)
					? new ArrayList<>() : new ArrayList<>(existingAuths);
			if (CollUtil.isEmpty(staffAuthList)) {
				SmtStaffDeviceAuth staffAuth = new SmtStaffDeviceAuth();
				staffAuth.setAuthId(detail.getAuthId());
				staffAuth.setCreateTime(new Date());
				staffAuth.setStaffId(staff.getId());
				staffAuth.setAuthType(RelationAuthTypeEnum.SECURITY_AUTH.getCode());
				smtStaffDeviceAuthService.save(staffAuth);
				staffAuthList.add(staffAuth);
			}
			String imgBase64 = smtImageService.getImageBase64ByCode(staff.getFacePicId());
			SecurityAuthDispatchContext context = SecurityAuthDispatchContext.of(applyId, detail.getId(),
					dispatchBatchId, staff.getId(), detail.getAuthId());
			String remark = smtStaffService.updatePersonCardForSecurityDispatch(staff, imgBase64,
					staff.getFacePicId(), staffAuthList, null, applyBadge, context);
			if (StringUtils.isNotEmpty(remark)) {
				this.update(Wrappers.<SmtSecurityTaskDetails>lambdaUpdate()
						.eq(SmtSecurityTaskDetails::getId, detail.getId())
						.eq(SmtSecurityTaskDetails::getDispatchBatchId, dispatchBatchId)
						.set(SmtSecurityTaskDetails::getStatus, DeviceDownStatusEnum.FAIL.getCode())
						.set(SmtSecurityTaskDetails::getRemark, remark)
						.set(SmtSecurityTaskDetails::getUpdateTime, LocalDateTime.now()));
			}
			processed++;
		}
		return processed;
	}

	/**
	 * 设备权限下发
	 * @param detail 设备信息
	 * @param badge 工号
	 */
	private void down(SmtSecurityTaskDetails detail, String badge) {
		// 明细级原子抢占：status WAIT(0)->IN_WORK(3)，抢不到说明并发方（重复下发/对账任务）已处理，直接跳过（spec §3.1.1）
		boolean claimed = this.update(Wrappers.<SmtSecurityTaskDetails>lambdaUpdate()
				.eq(SmtSecurityTaskDetails::getId, detail.getId())
				.eq(SmtSecurityTaskDetails::getStatus, DeviceDownStatusEnum.WAIT.getCode())
				.set(SmtSecurityTaskDetails::getStatus, DeviceDownStatusEnum.IN_WORK.getCode()));
		if (!claimed) {
			return;
		}
		//根据员工编号获取员工信息
		SmtStaff staff = smtStaffService.getById(detail.getStaffId());
		//根据权限编号和员工id，检查员工权限
		List<SmtStaffDeviceAuth> staffAuthList = this.checkAuth(detail.getAuthId(),staff.getId());
		//添加人员权限关联
		if(CollUtil.isEmpty(staffAuthList)) {
			SmtStaffDeviceAuth staffAuth = new SmtStaffDeviceAuth();
			staffAuth.setAuthId(detail.getAuthId());
			staffAuth.setCreateTime(new Date());
			staffAuth.setStaffId(staff.getId());
			staffAuth.setAuthType(RelationAuthTypeEnum.SECURITY_AUTH.getCode());
			smtStaffDeviceAuthService.save(staffAuth);
			staffAuthList.add(staffAuth);
		}
		//获取员工人脸图片的base64编码
		String imgBase64 = smtImageService.getImageBase64ByCode(staff.getFacePicId());
		// 下发或更新权限
		String remark = smtStaffService.updatePersonCard(staff, imgBase64, staff.getFacePicId(), staffAuthList, null, badge);
		// 任务状态已由开头的 CAS 抢占置为 IN_WORK，此处仅需在失败时覆盖为 FAIL 并记录原因
		if (StringUtils.isNotEmpty(remark)) {
			detail.setStatus(DeviceDownStatusEnum.FAIL.getCode());
			detail.setRemark(remark);
			//更新任务详情（失败场景需要落库覆盖状态与备注）
			this.updateById(detail);
		}
	}

	/**
	 * 判断权限策略是否已存在
	 * @param authId
	 * @return
	 */
	private List<SmtStaffDeviceAuth> checkAuth(Integer authId, Long staffId) {
		List<SmtStaffDeviceAuth> authList = smtStaffDeviceAuthService.list(Wrappers.<SmtStaffDeviceAuth>query().lambda()
				.eq(SmtStaffDeviceAuth::getStaffId, staffId).eq(SmtStaffDeviceAuth::getAuthId, authId)
				.eq(SmtStaffDeviceAuth::getAuthType,RelationAuthTypeEnum.SECURITY_AUTH.getCode()));
		return authList;
	}

	@Override
	public Integer getCount(Long applyId, Integer status) {
		return this.count(Wrappers.<SmtSecurityTaskDetails>query()
				.lambda().eq(SmtSecurityTaskDetails::getStatus, status)
				.eq(SmtSecurityTaskDetails::getApplyId, applyId));
	}

	/**
	 * 同步任务状态
	 * @param applyId
	 * @return
	 */
	@Override
	public Boolean syncTaskStatus(Long applyId) {
		SmtSecurityAuthApply authApply = smtSecurityAuthApplyMapper.selectById(applyId);
		if (authApply == null || authApply.getCurrentDispatchBatchId() == null) {
			return Boolean.TRUE;
		}
		Long currentBatchId = authApply.getCurrentDispatchBatchId();
		// 只聚合申请单当前批次；旧批次的迟到结果只能更新旧 ISC 任务本身。
		List<SmtSecurityTaskDetails> details = this.list(Wrappers.<SmtSecurityTaskDetails>query()
				.lambda().eq(SmtSecurityTaskDetails::getApplyId, applyId)
				.eq(SmtSecurityTaskDetails::getDispatchBatchId, currentBatchId));
		if (CollUtil.isEmpty(details)) {
			return Boolean.TRUE;
		}
		for (SmtSecurityTaskDetails detail : details) {
			if (!DeviceDownStatusEnum.IN_WORK.getCode().equals(detail.getStatus())) {
				continue;
			}
			List<SmtIscDeviceTask> tasks = smtIscDeviceTaskService.list(Wrappers.<SmtIscDeviceTask>lambdaQuery()
					.eq(SmtIscDeviceTask::getSourceType, SecurityAuthDispatchContext.SOURCE_TYPE)
					.eq(SmtIscDeviceTask::getSourceId, applyId)
					.eq(SmtIscDeviceTask::getSourceDetailId, detail.getId())
					.eq(SmtIscDeviceTask::getBatchId, currentBatchId));
			Integer targetStatus = aggregateDetailStatus(tasks);
			String targetRemark = detailFailureReason(tasks);
			detail.setStatus(targetStatus);
			detail.setRemark(targetRemark);
			detail.setUpdateTime(LocalDateTime.now());
			this.update(Wrappers.<SmtSecurityTaskDetails>lambdaUpdate()
					.eq(SmtSecurityTaskDetails::getId, detail.getId())
					.eq(SmtSecurityTaskDetails::getDispatchBatchId, currentBatchId)
					.eq(SmtSecurityTaskDetails::getStatus, DeviceDownStatusEnum.IN_WORK.getCode())
					.set(SmtSecurityTaskDetails::getStatus, targetStatus)
					.set(SmtSecurityTaskDetails::getRemark, targetRemark)
					.set(SmtSecurityTaskDetails::getUpdateTime, detail.getUpdateTime()));
		}
		// 明细 CAS 可能因并发终态回调未命中；聚合主单前必须重读，禁止用旧快照回退状态。
		List<SmtSecurityTaskDetails> currentDetails = this.list(Wrappers.<SmtSecurityTaskDetails>lambdaQuery()
				.eq(SmtSecurityTaskDetails::getApplyId, applyId)
				.eq(SmtSecurityTaskDetails::getDispatchBatchId, currentBatchId));
		if (CollUtil.isEmpty(currentDetails)) {
			return Boolean.TRUE;
		}
		Integer mainStatus = aggregateApplyStatus(currentDetails);
		smtSecurityAuthApplyMapper.update(null, Wrappers.<SmtSecurityAuthApply>lambdaUpdate()
				.eq(SmtSecurityAuthApply::getId, applyId)
				.eq(SmtSecurityAuthApply::getCurrentDispatchBatchId, currentBatchId)
				.in(SmtSecurityAuthApply::getDeviceStatus, DeviceDownStatusEnum.WAIT.getCode(),
						DeviceDownStatusEnum.IN_WORK.getCode())
				.set(SmtSecurityAuthApply::getDeviceStatus, mainStatus));
		return Boolean.TRUE;
	}

	private Integer aggregateDetailStatus(List<SmtIscDeviceTask> tasks) {
		if (CollUtil.isEmpty(tasks)) {
			return DeviceDownStatusEnum.IN_WORK.getCode();
		}
		boolean failed = tasks.stream().anyMatch(task -> DeviceTaskStatusEnum.FAIL.getCode().equals(task.getStatus())
				|| DeviceTaskStatusEnum.CANCEL.getCode().equals(task.getStatus())
				|| DeviceTaskStatusEnum.EXPIRED.getCode().equals(task.getStatus()));
		if (failed) {
			return DeviceDownStatusEnum.FAIL.getCode();
		}
		boolean allSucceeded = tasks.stream()
				.allMatch(task -> DeviceTaskStatusEnum.SUCCESS.getCode().equals(task.getStatus()));
		return allSucceeded ? DeviceDownStatusEnum.SUCCESS.getCode() : DeviceDownStatusEnum.IN_WORK.getCode();
	}

	private String detailFailureReason(List<SmtIscDeviceTask> tasks) {
		if (CollUtil.isEmpty(tasks)) {
			return null;
		}
		return tasks.stream()
				.filter(task -> DeviceTaskStatusEnum.FAIL.getCode().equals(task.getStatus())
						|| DeviceTaskStatusEnum.CANCEL.getCode().equals(task.getStatus())
						|| DeviceTaskStatusEnum.EXPIRED.getCode().equals(task.getStatus()))
				.map(task -> StringUtils.isNotEmpty(task.getRemark())
						? task.getRemark() : DeviceTaskStatusEnum.desc(task.getStatus()))
				.findFirst().orElse(null);
	}

	private Integer aggregateApplyStatus(List<SmtSecurityTaskDetails> details) {
		if (details.stream().allMatch(detail -> DeviceDownStatusEnum.SUCCESS.getCode().equals(detail.getStatus()))) {
			return DeviceDownStatusEnum.SUCCESS.getCode();
		}
		if (details.stream().anyMatch(detail -> DeviceDownStatusEnum.FAIL.getCode().equals(detail.getStatus()))) {
			return DeviceDownStatusEnum.FAIL.getCode();
		}
		return DeviceDownStatusEnum.IN_WORK.getCode();
	}
}
