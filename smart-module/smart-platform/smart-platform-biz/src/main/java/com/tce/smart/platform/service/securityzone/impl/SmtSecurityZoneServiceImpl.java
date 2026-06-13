package com.tce.smart.platform.service.securityzone.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.platform.api.dto.req.securityzone.*;
import com.tce.smart.platform.api.dto.resp.securityzone.AuthApplyRemarkRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityStaffRespDTO;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthRelation;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityPersonRelation;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityZone;
import com.tce.smart.platform.core.mapper.SmtSecurityZoneMapper;
import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthRelationService;
import com.tce.smart.platform.service.securityzone.SmtSecurityPersonRelationService;
import com.tce.smart.platform.service.securityzone.SmtSecurityZoneService;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import com.tce.smart.tool.enums.RelationAuthTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2021-07-29 11:12:46
 */
@Service
public class SmtSecurityZoneServiceImpl extends ServiceImpl<SmtSecurityZoneMapper, SmtSecurityZone> implements SmtSecurityZoneService {

	@Autowired
	private SmtSecurityAuthRelationService smtSecurityAuthRelationService;
	@Autowired
	private SmtSecurityPersonRelationService smtSecurityPersonRelationService;
	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtDeviceAuthorityService smtDeviceAuthorityService;
	@Autowired
	private SmtStaffDeviceAuthService smtStaffDeviceAuthService;

	@Override
	public IPage<SmtSecurityZone> getPage(Page page, SecurityZoneQueryReqDTO query) {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return this.page(page, Wrappers.<SmtSecurityZone>query().lambda()
				.like(StringUtils.isNotEmpty(query.getSecurityName()), SmtSecurityZone::getSecurityName, query.getSecurityName())
				.in(CollUtil.isNotEmpty(parkIds), SmtSecurityZone::getParkId, parkIds)
				.eq(Objects.nonNull(query.getParkId()), SmtSecurityZone::getParkId, query.getParkId())
				.orderByDesc(SmtSecurityZone::getCreateTime));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean editZone(SecurityZoneEditReqDTO edit) {
		SmtSecurityZone zone = BeanUtils.transform(SmtSecurityZone.class, edit);
		zone.setUpdateTime(LocalDateTime.now());
		//修改保密区
		this.updateById(zone);
		//查重
		Integer count = this.count(Wrappers.<SmtSecurityZone>query().lambda()
				.eq(SmtSecurityZone::getParkId, edit.getParkId())
				.and(obj -> obj.eq(SmtSecurityZone::getSecurityCode, edit.getSecurityCode())
						.or()
						.eq(SmtSecurityZone::getSecurityName, edit.getSecurityName())));
		if (count > 1) {
			throw new SmartException("已存在同名或同代码保密项目");
		}
		//修改关联权限
		Long zoneId = zone.getId();
		smtSecurityAuthRelationService.deleteAuth(zoneId);
		List<SecurityAuthRelationReqDTO> authList = edit.getAuthIds();
		if (CollUtil.isNotEmpty(authList)) {
			List<SmtSecurityAuthRelation> relations = new ArrayList<>();
			authList.forEach(auth -> {
				relations.add(SmtSecurityAuthRelation.builder().authId(auth.getAuthId())
						.authName(auth.getAuthName()).securityId(zoneId).build());
			});
			smtSecurityAuthRelationService.editAuth(relations);
		}
		return Boolean.TRUE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveZone(SecurityZoneEditReqDTO edit) {
		SmtSecurityZone zone = BeanUtils.transform(SmtSecurityZone.class, edit);
		zone.setCreateTime(LocalDateTime.now());
		this.save(zone);
		List<SecurityAuthRelationReqDTO> authList = edit.getAuthIds();
		if (CollUtil.isNotEmpty(authList)) {
			Long zoneId = zone.getId();
			List<SmtSecurityAuthRelation> relations = new ArrayList<>();
			authList.forEach(auth -> {
				relations.add(SmtSecurityAuthRelation.builder().authId(auth.getAuthId())
						.authName(auth.getAuthName()).securityId(zoneId).build());
			});
			smtSecurityAuthRelationService.editAuth(relations);
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean deleteZone(SecurityZoneQueryReqDTO query) {
		List<Long> ids = this.getHandleIds(query);
		if (CollUtil.isEmpty(ids)) {
			return Boolean.FALSE;
		}
		//删除签署员工
		smtSecurityPersonRelationService.batchDeleteByZoneId(ids);
		//删除关联权限
		smtSecurityAuthRelationService.batchDeleteAuth(ids);
		//删除保密区
		return this.removeByIds(ids);
	}

	/**
	 * 获得待处理保密区ID
	 *
	 * @param query
	 * @return
	 */
	private List<Long> getHandleIds(SecurityZoneQueryReqDTO query) {
		if (CollUtil.isNotEmpty(query.getId())) {
			return query.getId();
		}
		if (StringUtils.isNotEmpty(query.getSecurityName()) || StringUtils.isNotEmpty(query.getSecurityCode())) {
			List<SmtSecurityZone> list = this.list(Wrappers.<SmtSecurityZone>query().lambda()
					.like(StringUtils.isNotEmpty(query.getSecurityName()), SmtSecurityZone::getSecurityName, query.getSecurityName())
					.eq(StringUtils.isNotEmpty(query.getSecurityCode()), SmtSecurityZone::getSecurityCode, query.getSecurityCode())
					.eq(Objects.nonNull(query.getParkId()), SmtSecurityZone::getParkId, query.getParkId()));
			if (CollUtil.isNotEmpty(list)) {
				return list.stream().map(SmtSecurityZone::getId).collect(Collectors.toList());
			}
		}
		return null;
	}

	@Override
	public List<SmtSecurityZone> getSecurityZoneByStaff(Long staffId) {
		List<SmtSecurityPersonRelation> personRelations = smtSecurityPersonRelationService.getByStaffId(staffId);
		if (CollUtil.isEmpty(personRelations)) {
			return null;
		}
		List<Long> zoneId = personRelations.stream().map(SmtSecurityPersonRelation::getSecurityId).collect(Collectors.toList());
		return (List<SmtSecurityZone>) this.listByIds(zoneId);
	}

	@Override
	public List<SecurityStaffRespDTO> getStaffByInfo(SecurityStaffQueryReqDTO reqDTO) {
		List<SmtStaff> staffs = new ArrayList<>();
		List<String> errorBadge = new ArrayList<>();
		List<SecurityStaffRespDTO> respDTOS = new ArrayList<>();
		if (CollUtil.isNotEmpty(reqDTO.getStaffBadges())) {
			staffs = smtStaffService.list(Wrappers.<SmtStaff>query().lambda().in(SmtStaff::getBadge, reqDTO.getStaffBadges()));
			List<String> badges = staffs.stream().map(SmtStaff::getBadge).collect(Collectors.toList());
			reqDTO.getStaffBadges().removeAll(badges);
			errorBadge = reqDTO.getStaffBadges();
		}
		if (CollUtil.isNotEmpty(reqDTO.getStaffIds())) {
			staffs = smtStaffService.list(Wrappers.<SmtStaff>query().lambda().in(SmtStaff::getId, reqDTO.getStaffIds()));
		}
		if (CollUtil.isNotEmpty(staffs)) {
			for (SmtStaff smtStaff : staffs) {
				SecurityStaffRespDTO resp = BeanUtils.transform(SecurityStaffRespDTO.class, smtStaff);
				List<SmtSecurityZone> zone = this.getSecurityZoneByStaff(smtStaff.getId());
				resp.setCreateTime(DateUtil.formatDate(smtStaff.getCreateTime()));
				resp.setIsImg(StringUtils.isEmpty(smtStaff.getFacePicId()) ? OneOrZeroEnum.ZERO.getCode() : OneOrZeroEnum.ONE.getCode());
				resp.setIsSecurity(OneOrZeroEnum.ZERO.getCode());
				resp.setStaffDepId(smtStaff.getDepId());
				resp.setStaffJobId(smtStaff.getJobId());
				if (CollUtil.isNotEmpty(zone)) {
					List<SecurityStaffRespDTO.SecurityZone> list = BeanUtils.batchTransform(SecurityStaffRespDTO.SecurityZone.class, zone);
					resp.setSecurityZones(list);
					resp.setIsSecurity(OneOrZeroEnum.ONE.getCode());
				}
				resp.setErrorBadge(errorBadge);
				respDTOS.add(resp);
			}
		}
		return respDTOS;
	}

	/**
	 * 选定人员后筛选
	 * @param reqDTO
	 * @return
	 */
	@Override
	public List<SecurityStaffRespDTO> getCheckStaff(SecurityStaffCheckReqDTO reqDTO) {
		if(CollUtil.isEmpty(reqDTO.getStaffInfos())) {
			return null;
		}
		List<SecurityStaffInputReqDTO> collect = reqDTO.getStaffInfos().stream().filter(staff ->
				filterData(staff, reqDTO)).collect(Collectors.toList());
		if(CollUtil.isEmpty(collect)) {
			return null;
		}
		return BeanUtils.batchTransform(SecurityStaffRespDTO.class, collect);
	}

	@Override
	public List<AuthApplyRemarkRespDTO> getAuthRemark(List<AuthApplyRemarkReqDTO> req) {
		if(CollUtil.isEmpty(req)) {
			return null;
		}
		List<AuthApplyRemarkRespDTO> respDTOS = new ArrayList<>();
		for(AuthApplyRemarkReqDTO apply : req) {
			//获取申请的权限
			List<Integer> applyAuths = apply.getAuthList();
			//将申请的权限转换为AuthApplyRemarkRespDTO
			AuthApplyRemarkRespDTO resp = BeanUtils.transform(AuthApplyRemarkRespDTO.class, apply);
			//根据员工id查询员工信息
			SmtStaff staff = smtStaffService.getById(apply.getStaffId());
			//查询人脸信息 如果人脸信息为空，则设置备注为“员工未上传人脸”
			if(StringUtils.isEmpty(staff.getFacePicId())) {
				resp.setRemark("员工未上传人脸");
				respDTOS.add(resp);
				continue;
			}
			//查询员工关联的保密区
			Collection<SmtDeviceAuthority> smtDeviceAuthorities = smtDeviceAuthorityService.listByIds(applyAuths);
			if (smtDeviceAuthorities.stream().noneMatch(smtDeviceAuthority -> Integer.valueOf(1).equals(smtDeviceAuthority.getAreaType()))) {
				continue;
			}
			//员工关联保密区
			List<SmtSecurityPersonRelation> relations = smtSecurityPersonRelationService.getByStaffId(apply.getStaffId());
			if(CollUtil.isEmpty(relations)) {
				resp.setRemark("员工未关联保密区");
				respDTOS.add(resp);
				continue;
			}
			List<Long> zoneIds = relations.stream().map(SmtSecurityPersonRelation::getSecurityId).collect(Collectors.toList());
			//保密区关联权限
			List<SmtSecurityAuthRelation> securityAuth = smtSecurityAuthRelationService.getBatchList(zoneIds);
			if(CollUtil.isEmpty(securityAuth)) {
				resp.setRemark("员工关联的保密区未关联权限");
				respDTOS.add(resp);
				continue;
			}

			//查询员工已有的权限
			/*List<Integer> securityAuthIds = securityAuth.stream().map(SmtSecurityAuthRelation::getAuthId).collect(Collectors.toList());
			//查询是否权限已存在
			List<SmtStaffDeviceAuth> staffAuthRelation = smtStaffDeviceAuthService.list(Wrappers.<SmtStaffDeviceAuth>query()
					.lambda().eq(SmtStaffDeviceAuth::getStaffId, staff.getId())
					.eq(SmtStaffDeviceAuth::getAuthType, RelationAuthTypeEnum.SECURITY_AUTH.getCode()));
			List<Integer> alreadyAuth = staffAuthRelation.stream().map(SmtStaffDeviceAuth::getAuthId).collect(Collectors.toList());
			alreadyAuth.retainAll(applyAuths);*/
			//判断申请的权限是否已存在
			/*if(CollUtil.isNotEmpty(alreadyAuth)) {
				Collection<SmtDeviceAuthority> authorities = smtDeviceAuthorityService.listByIds(alreadyAuth);
				String haveAuth = StringUtils.join(SymbolConstants.BRANCH,
						authorities.stream().map(SmtDeviceAuthority::getAuthorityName).collect(Collectors.toList()));
				resp.setRemark("权限" +  haveAuth + "已申请过 ");
				respDTOS.add(resp);
				continue;
			}*/

			//员工可以申请保密区以及非保密区，不再限制仅允许申请保密区权限
			//判断申请权限是否均在保密区关联权限中
/*			applyAuths.removeAll(securityAuthIds);
			if(CollUtil.isNotEmpty(applyAuths)) {
				Collection<SmtDeviceAuthority> authorities = smtDeviceAuthorityService.listByIds(applyAuths);
				String outLimitAuth = StringUtils.join(SymbolConstants.BRANCH,
						authorities.stream()
								.map(SmtDeviceAuthority::getAuthorityName)
								.collect(Collectors.toList()));
				resp.setRemark("权限" +  outLimitAuth + "超出保密区权限范围 ");
			}*/
			respDTOS.add(resp);
			// continue;
		}
		return respDTOS;
	}

	/**
	 * stream.filter筛选方法
	 * @param staff
	 * @param reqDTO
	 * @return
	 */
	private Boolean filterData(SecurityStaffInputReqDTO staff, SecurityStaffCheckReqDTO reqDTO) {
		Boolean rule1 = Boolean.TRUE;
		Boolean rule2 = Boolean.TRUE;
		Boolean rule3 = Boolean.TRUE;
		Boolean rule4 = Boolean.TRUE;
		if (StringUtils.isNotEmpty(reqDTO.getStartDate()) && StringUtils.isNotEmpty(reqDTO.getEndDate())) {
			if(DateUtil.parseDate(staff.getCreateTime()).compareTo(DateUtil.parseDate(reqDTO.getStartDate())) < 0
					|| DateUtil.parseDate(staff.getCreateTime()).compareTo(DateUtil.parseDate(reqDTO.getEndDate())) > 0) {
				rule1 = Boolean.FALSE;
			}
		}
		if (CollUtil.isNotEmpty(reqDTO.getAuthIds())) {
			if(CollUtil.isEmpty(staff.getAuthList())) {
				rule2 = Boolean.FALSE;
			}else {
				List<Integer> authIds = staff.getAuthList().stream().map(SecurityStaffInputReqDTO.RelationAuth::getAuthId)
						.collect(Collectors.toList());
				authIds.retainAll(reqDTO.getAuthIds());
				rule2 = CollUtil.isEmpty(authIds) ? Boolean.FALSE : Boolean.TRUE;
			}
		}
		if (Objects.nonNull(reqDTO.getIsImg())) {
			rule3 = staff.getIsImg().equals(reqDTO.getIsImg());
		}
		if (Objects.nonNull(reqDTO.getIsSecurity())) {
			rule4 = staff.getIsSecurity().equals(reqDTO.getIsSecurity());
		}
		return rule1 && rule2 && rule3 && rule4;
	}
}
