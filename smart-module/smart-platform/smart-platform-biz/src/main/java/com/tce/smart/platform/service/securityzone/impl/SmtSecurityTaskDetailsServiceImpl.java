package com.tce.smart.platform.service.securityzone.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityApplyPersonReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.UpdateFaceImgReqDTO;
import com.tce.smart.platform.core.entity.SmtDeviceTask;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityTaskDetails;
import com.tce.smart.platform.core.mapper.SmtSecurityTaskDetailsMapper;
import com.tce.smart.platform.core.service.SmtDeviceTaskService;
import com.tce.smart.platform.core.service.SmtImageService;
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
	private SmtDeviceTaskService smtDeviceTaskService;
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

	/**
	 * 设备权限下发
	 * @param detail 设备信息
	 * @param badge 工号
	 */
	private void down(SmtSecurityTaskDetails detail, String badge) {
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
		//更新任务状态
		detail.setStatus(DeviceDownStatusEnum.IN_WORK.getCode());
		//错误信息处理
		if (StringUtils.isNotEmpty(remark)) {
			detail.setStatus(DeviceDownStatusEnum.FAIL.getCode());
			detail.setRemark(remark);
		}
		//更新任务详情
		this.updateById(detail);
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
		//查询下发中任务
		List<SmtSecurityTaskDetails> details = this.list(Wrappers.<SmtSecurityTaskDetails>query()
				.lambda().eq(SmtSecurityTaskDetails::getStatus, DeviceDownStatusEnum.IN_WORK.getCode())
				.eq(SmtSecurityTaskDetails::getApplyId, applyId));
		if (CollUtil.isEmpty(details)) {
			return Boolean.TRUE;
		}
		details.forEach(detail -> {
			detail.setStatus(DeviceDownStatusEnum.SUCCESS.getCode());
			//查询设备表中下发中任务的下发结果
			List<SmtDeviceTask> tasks = smtDeviceTaskService.list(Wrappers.<SmtDeviceTask>query().lambda()
					.eq(SmtDeviceTask::getCardNo, detail.getStaffId()).eq(SmtDeviceTask::getImageId, detail.getImgCode()));
			for (SmtDeviceTask task : tasks) {
				if (DeviceTaskStatusEnum.FAIL.getCode().equals(task.getStatus())) {
					detail.setRemark(task.getRemark());
					detail.setStatus(DeviceDownStatusEnum.FAIL.getCode());
					continue;
				}
				if (DeviceTaskStatusEnum.INIT.getCode().equals(task.getStatus())) {
					detail.setStatus(DeviceDownStatusEnum.IN_WORK.getCode());
					detail.setRemark(task.getRemark());
					continue;
				}
			}
			this.updateById(detail);
		});
		return Boolean.TRUE;
	}
}
