package com.tce.smart.platform.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysDict;
import com.tce.smart.admin.api.feign.RemoteDictService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.TempStaffEditReqDTO;
import com.tce.smart.platform.api.dto.req.outsrcapply.SmtOutSrcApplyReqDTO;
import com.tce.smart.platform.api.dto.req.outsrcapply.SmtOutSrcApplyUpdDTO;
import com.tce.smart.platform.api.dto.resp.outsrcapply.SmtOutSrcApplyDetailListDTO;
import com.tce.smart.platform.api.dto.resp.outsrcapply.SmtOutSrcApplyDetailRespDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.mapper.SmtOutSrcApplyMapper;
import com.tce.smart.platform.emun.OutSrcApplyStatusEnum;
import com.tce.smart.platform.service.*;
import com.tce.smart.tool.constant.DictConstants;
import com.tce.smart.tool.constant.NumberConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: Li.JiaJun
 * @since: 2021/8/2 16:57
 */
@Service
@Slf4j
public class SmtOutSrcApplyServiceImpl extends ServiceImpl<SmtOutSrcApplyMapper, SmtOutSrcApply> implements SmtOutSrcApplyService {

	@Autowired
	private SmtOutSrcApplyDetailsService smtOutSrcApplyDetailsService;
	@Autowired
	private SmtOrganizeRelationService smtOrganizeRelationService;
	@Resource
	private RemoteDictService remoteDictService;
	@Autowired
	private SmtExternalDeptService smtExternalDeptService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtStaffExtService smtStaffExtService;

	private static final String START_POSTFEX = " 00:00:00";
	private static final String END_POSTFEX = " 23:59:59";

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveBatchRec(List<TempStaffEditReqDTO> tempStaffs) {
		if (CollectionUtil.isEmpty(tempStaffs)) {
			throw new SmartException("员工信息为空，请重新选择");
		}
		Integer userId = SecurityUtils.getUser().getId();
		SmtOrganizeRelation organizeRelation = smtOrganizeRelationService.getByUserId(userId);
		if (Objects.isNull(organizeRelation)) {
			throw new SmartException("您尚未登记企业");
		}
		List<SysDict> dict = remoteDictService.findByType(DictConstants.JOB_LEVEL, SecurityConstants.FROM_IN).data();
		if (CollUtil.isEmpty(dict)) {
			throw new SmartException("获取级层字典表失败");
		}
		validData(dict, organizeRelation, tempStaffs);
		SmtOutSrcApply smtOutSrcApply = SmtOutSrcApply.builder()
				.applyNum(tempStaffs.size())
				.applyUserId(userId)
				.applyUserName(organizeRelation.getUserName())
				.compId(String.valueOf(organizeRelation.getId()))
				.compName(organizeRelation.getCompName())
				.createTime(new Date())
				.status(OutSrcApplyStatusEnum.PENDING.getCode())
				.build();
		this.save(smtOutSrcApply);
		List<SmtOutSrcApplyDetails> smtOutSrcApplyDetails = new ArrayList<>();
		for (TempStaffEditReqDTO tempStaff : tempStaffs) {
			SmtExternalDept dept = smtExternalDeptService.getByName(tempStaff.getDepName(), organizeRelation.getId());
			SysDict jche = dict.stream().filter(d -> d.getLabel().equals(tempStaff.getJcheName())).collect(Collectors.toList()).get(0);
			SmtOutSrcApplyDetails smtOutSrcApplyDetail = SmtOutSrcApplyDetails.builder()
					.applyId(smtOutSrcApply.getId())
					.badge(tempStaff.getBadge())
					.certno(tempStaff.getCertno())
					.depId(String.valueOf(dept.getId()))
					.depName(tempStaff.getDepName())
					.dispatchChannel(tempStaff.getDispatch())
					.entryDate(DateUtils.parseDateTime(tempStaff.getEntryTime()))
					.jobName(tempStaff.getJobName())
					.jcheId(jche.getValue())
					.jcheName(jche.getLabel())
					.name(tempStaff.getName())
					.phone(tempStaff.getPhone())
					.build();
			smtOutSrcApplyDetails.add(smtOutSrcApplyDetail);
			if(smtOutSrcApplyDetails.size() == NumberConstants.maxSize) {
				smtOutSrcApplyDetailsService.saveBatch(smtOutSrcApplyDetails);
				smtOutSrcApplyDetails.clear();
			}
		}
		if(smtOutSrcApplyDetails.size() > 0) {
			smtOutSrcApplyDetailsService.saveBatch(smtOutSrcApplyDetails);
		}
		return Boolean.TRUE;
	}

	private void validData(List<SysDict> dict, SmtOrganizeRelation organizeRelation, List<TempStaffEditReqDTO> tempStaffs) {
		List<String> dictStr = dict.stream().map(SysDict::getLabel).collect(Collectors.toList());
		List<String> depts = smtExternalDeptService.getList().stream().map(SmtExternalDept::getDeptName).collect(Collectors.toList());
		if (CollUtil.isEmpty(depts)) {
			throw new SmartException("企业：" + organizeRelation.getCompName() + " 尚未登记部门，无法导入");
		}
		StringBuilder errorDept = new StringBuilder();
		StringBuilder errorJche = new StringBuilder();
		StringBuilder errorBadge = new StringBuilder();
		List<String> existBadge = new ArrayList<>();
		for (TempStaffEditReqDTO staff : tempStaffs) {
			SmtStaff emp = smtStaffService.getStaffByBadge(staff.getBadge());
			if(existBadge.contains(staff.getBadge())) {
				throw new SmartException("工号" + staff.getBadge() + "重复");
			}
			existBadge.add(staff.getBadge());
			if(Objects.nonNull(emp)) {
				errorBadge.append(staff.getBadge()).append(",");
			}
			if (!depts.contains(staff.getDepName())) {
				errorDept.append(staff.getDepName()).append(",");
			}
			if (!dictStr.contains(staff.getJcheName())) {
				errorJche.append(staff.getJcheName()).append(",");
			}
		}
		if (errorBadge.length() > 0) {
			throw new SmartException("工号" + errorBadge.substring(0, errorBadge.length() - 1) + "已存在");
		}
		if (errorDept.length() > 0) {
			throw new SmartException("部门‘" + errorDept.substring(0, errorDept.length() - 1) + " '尚未登记在" + organizeRelation.getCompName() + "企业下");
		}
		if (errorJche.length() > 0) {
			throw new SmartException("级层‘" + errorJche.substring(0, errorJche.length() - 1) + "不存在");
		}
	}

	@Override
	public IPage<SmtOutSrcApply> getPage(Page page, SmtOutSrcApplyReqDTO applyReqDTO) {
		if(applyReqDTO.getIsApprove()) {
			SmartUser smartUser = SecurityUtils.getUser();
			List<SmtOrganizeRelation> organizeRelations = smtOrganizeRelationService.getByParkId(smartUser.getParkIdList());
			if(CollectionUtils.isEmpty(organizeRelations)) {
				return page;
			}
			return this.page(page, Wrappers.<SmtOutSrcApply>lambdaQuery()
					.ge(Objects.nonNull(applyReqDTO.getApplyStartTime()), SmtOutSrcApply::getCreateTime, convertDate(applyReqDTO.getApplyStartTime(), START_POSTFEX))
					.le(Objects.nonNull(applyReqDTO.getApplyEndTime()), SmtOutSrcApply::getCreateTime, convertDate(applyReqDTO.getApplyEndTime(), END_POSTFEX))
					.eq(Objects.nonNull(applyReqDTO.getStatus()), SmtOutSrcApply::getStatus, applyReqDTO.getStatus())
					.like(Objects.nonNull(applyReqDTO.getCompName()), SmtOutSrcApply::getCompName, applyReqDTO.getCompName())
					.in(SmtOutSrcApply::getCompId, organizeRelations.stream().map(SmtOrganizeRelation::getId).collect(Collectors.toList()))
					.orderByDesc(SmtOutSrcApply::getCreateTime)
			);
		} else {
			Integer userId = SecurityUtils.getUser().getId();
			return this.page(page, Wrappers.<SmtOutSrcApply>lambdaQuery()
					.ge(Objects.nonNull(applyReqDTO.getApplyStartTime()), SmtOutSrcApply::getCreateTime, convertDate(applyReqDTO.getApplyStartTime(), START_POSTFEX))
					.le(Objects.nonNull(applyReqDTO.getApplyEndTime()), SmtOutSrcApply::getCreateTime, convertDate(applyReqDTO.getApplyEndTime(), END_POSTFEX))
					.eq(Objects.nonNull(applyReqDTO.getStatus()), SmtOutSrcApply::getStatus, applyReqDTO.getStatus())
					.like(Objects.nonNull(applyReqDTO.getCompName()), SmtOutSrcApply::getCompName, applyReqDTO.getCompName())
					.eq(SmtOutSrcApply::getApplyUserId, userId)
					.orderByDesc(SmtOutSrcApply::getCreateTime)
			);
		}
	}

	private Date convertDate(String date, String startOrEnd) {
		if(Objects.nonNull(date)) {
			return DateUtil.parseDateTime(date + startOrEnd);
		} else {
			return null;
		}
	}

	@Override
	public SmtOutSrcApplyDetailRespDTO getDetail(Long applyId) {
		SmtOutSrcApply smtOutSrcApply = this.getById(applyId);
		return SmtOutSrcApplyDetailRespDTO.builder()
				.applyId(smtOutSrcApply.getId())
				.applyTime(DateUtils.format(smtOutSrcApply.getCreateTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT))
				.statusDesc(OutSrcApplyStatusEnum.desc(smtOutSrcApply.getStatus()))
				.reason(smtOutSrcApply.getReason())
				.approver(smtOutSrcApply.getApprover())
				.approverTime(DateUtils.format(smtOutSrcApply.getApproverTime(), DateUtils.DEFAULT_DATE_TIME_FORMAT))
				.build();
	}

	@Override
	public IPage<SmtOutSrcApplyDetailListDTO> getDetailPage(Page page, Long applyId) {
		IPage<SmtOutSrcApplyDetails> details = smtOutSrcApplyDetailsService.getByApplyId(page, applyId);
		List<SmtOutSrcApplyDetailListDTO> dtoList = new ArrayList<>();
		IPage<SmtOutSrcApplyDetailListDTO> iPage = new Page<>();
		for (SmtOutSrcApplyDetails detail : details.getRecords()) {
			SmtOutSrcApplyDetailListDTO dto = SmtOutSrcApplyDetailListDTO.builder()
					.badge(detail.getBadge())
					.certno(detail.getCertno())
					.depName(detail.getDepName())
					.dispatchChannel(detail.getDispatchChannel())
					.entryDate(DateUtils.format(detail.getEntryDate(), DateUtils.DEFAULT_DATE_TIME_FORMAT))
					.jcheName(detail.getJcheName())
					.jobName(detail.getJobName())
					.name(detail.getName())
					.phone(detail.getPhone())
					.build();
			dtoList.add(dto);
		}
		iPage.setCurrent(details.getCurrent());
		iPage.setSize(details.getSize());
		iPage.setTotal(details.getTotal());
		iPage.setRecords(dtoList);
		return iPage;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean passOrRefuse(SmtOutSrcApplyUpdDTO dto, SmtDormitoryStaffService smtDormitoryStaffService) {
		SmartUser smartUser = SecurityUtils.getUser();
		SmtOutSrcApply smtOutSrcApply = this.getById(dto.getApplyId());
		smtOutSrcApply.setStatus(dto.getStatus());
		// 通过
		if(OutSrcApplyStatusEnum.AGREE.getCode().equals(dto.getStatus())) {
			SmtOrganizeRelation organizeRelation = smtOrganizeRelationService.getById(smtOutSrcApply.getCompId());
			List<TempStaffEditReqDTO> tempStaffs = new ArrayList<>();
			List<SmtOutSrcApplyDetails> details = smtOutSrcApplyDetailsService.getByApplyId(dto.getApplyId());
			for (SmtOutSrcApplyDetails detail : details) {
				TempStaffEditReqDTO reqDTO = new TempStaffEditReqDTO();
				reqDTO.setBadge(detail.getBadge());
				reqDTO.setCertno(detail.getCertno());
				reqDTO.setDepName(detail.getDepName());
				reqDTO.setJcheName(detail.getJcheName());
				reqDTO.setJobName(detail.getJobName());
				reqDTO.setName(detail.getName());
				reqDTO.setPhone(detail.getPhone());
				reqDTO.setEntryTime(DateUtils.format(detail.getEntryDate(), DateUtils.DEFAULT_DATE_TIME_FORMAT));
				reqDTO.setDispatch(detail.getDispatchChannel());
				tempStaffs.add(reqDTO);
			}
			smtStaffExtService.saveBatchTemporaryStaff(tempStaffs, organizeRelation,smtDormitoryStaffService);
		// 拒绝
		} else {
			smtOutSrcApply.setReason(dto.getReason());
		}
		smtOutSrcApply.setApprover(smartUser.getUsername());
		smtOutSrcApply.setApproverId(smartUser.getId());
		smtOutSrcApply.setApproverTime(new Date());
		return this.updateById(smtOutSrcApply);
	}
}
