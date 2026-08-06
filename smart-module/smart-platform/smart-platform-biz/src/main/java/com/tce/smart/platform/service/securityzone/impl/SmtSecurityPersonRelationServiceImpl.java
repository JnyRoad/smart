package com.tce.smart.platform.service.securityzone.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYscompRespDTO;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsdepRespDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYscompService;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsdepService;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityPersonAddReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityPersonExcelAddReqDTO;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityPersonQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.SecurityPersonAddRespDTO;
import com.tce.smart.platform.api.dto.resp.securityzone.StaffTreeRespDTO;
import com.tce.smart.platform.core.dto.SecurityAllStaffListDTO;
import com.tce.smart.platform.core.dto.SecurityPersonRelationDTO;
import com.tce.smart.platform.core.entity.*;
import com.tce.smart.platform.core.entity.ext.SecurityPersonRelationExt;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthRelation;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityPersonRelation;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityTaskDetails;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityZone;
import com.tce.smart.platform.core.mapper.SmtSecurityPersonRelationMapper;
import com.tce.smart.platform.core.mapper.SmtSecurityZoneMapper;
import com.tce.smart.platform.service.*;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtParkService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthRelationService;
import com.tce.smart.platform.service.securityzone.SmtSecurityPersonRelationService;
import com.tce.smart.platform.service.securityzone.SmtSecurityTaskDetailsService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import com.tce.smart.tool.constant.SymbolConstants;
import com.tce.smart.tool.enums.OneOrZeroEnum;
import com.tce.smart.tool.enums.RelationAuthTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author fushiping
 * @date 2021-07-29 11:13:00
 */
@Service
@Slf4j
public class SmtSecurityPersonRelationServiceImpl extends ServiceImpl<SmtSecurityPersonRelationMapper, SmtSecurityPersonRelation> implements SmtSecurityPersonRelationService {

	@Autowired
	private SmtStaffService smtStaffService;
	@Autowired
	private SmtParkService smtParkService;
	@Autowired
	private SmtParkBuService smtParkBuService;
	@Autowired
	private SmtStaffDeviceAuthService smtStaffDeviceAuthService;
	@Autowired
	private SmtDeviceAuthorityRelationService smtDeviceAuthorityRelationService;
	@Autowired
	private SmtSecurityTaskDetailsService smtSecurityTaskDetailsService;
	@Autowired
	private SmtSecurityAuthRelationService smtSecurityAuthRelationService;
	@Autowired
	private RemoteOvwYsdepService remoteOvwYsdepService;
	@Autowired
	private RemoteOvwYscompService remoteOvwYscompService;
	@Autowired
	private SmtSecurityZoneMapper smtSecurityZoneMapper;

	@Override
	public IPage<SecurityPersonRelationDTO> getPage(Page page, SecurityPersonQueryReqDTO reqDTO) {
		SecurityPersonRelationExt ext = new SecurityPersonRelationExt();
		if (Objects.nonNull(reqDTO)) {
			if (CollUtil.isNotEmpty(reqDTO.getRelationId())) {
				return this.baseMapper.getPageById(page, ext);
			}
			ext = BeanUtils.transform(SecurityPersonRelationExt.class, reqDTO);
		}
		IPage<SecurityPersonRelationDTO> pageResp = this.baseMapper.getPage(page, ext);
		return pageResp;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveRelation(List<SecurityPersonAddReqDTO> reqDTO) {
		for (SecurityPersonAddReqDTO req : reqDTO) {
			SmtSecurityPersonRelation relation = BeanUtils.transform(SmtSecurityPersonRelation.class, req);
			for (Long securityId : req.getSecurityId()) {
				//避免循环调用，使用mapper
				SmtSecurityZone zone = smtSecurityZoneMapper.selectById(securityId);
				relation.setId(null);
				relation.setParkId(zone.getParkId());
				relation.setSecurityId(securityId);
				if (this.getByStaffId(securityId, req.getStaffId())) {
					this.save(relation);
				}
			}
		}
		return Boolean.TRUE;
	}

	/**
	 * 判断员工是否已存在
	 *
	 * @param securityId
	 * @param staffId
	 * @return true 存在 false 不存在
	 */
	private Boolean getByStaffId(Long securityId, Long staffId) {
		Integer count = this.count(Wrappers.<SmtSecurityPersonRelation>query().lambda()
				.eq(SmtSecurityPersonRelation::getSecurityId, securityId).eq(SmtSecurityPersonRelation::getStaffId, staffId));
		return count == 0 ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public String saveExportRelation(List<SecurityPersonExcelAddReqDTO> reqDTO) {
		if (CollUtil.isEmpty(reqDTO)) {
			throw new SmartException("列表为空");
		}
		List<String> errorList = new ArrayList<>();
		for (SecurityPersonExcelAddReqDTO req : reqDTO) {
			SmtStaff staff = smtStaffService.getOne(Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getBadge, req.getStaffBadge()));
			if (Objects.isNull(staff) || !staff.getName().equals(req.getStaffName())) {
				errorList.add(req.getStaffBadge());
			} else {
				SmtSecurityPersonRelation relation = BeanUtils.transform(SmtSecurityPersonRelation.class, req);
				relation.setStaffId(staff.getId());
				relation.setCreateTime(LocalDateTime.now());
				//查重
				if (this.getByStaffId(relation.getSecurityId(), staff.getId())) {
					this.save(relation);
				}
			}
		}
		if (CollUtil.isNotEmpty(errorList)) {
			String error = StrUtil.join(SymbolConstants.COMMA, errorList);
			throw new SmartException("工号为：" + error + "的员工不存在或工号与姓名不匹配");
		}
		return null;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean batchDelete(SecurityPersonQueryReqDTO reqDTO) {
		if(Objects.isNull(reqDTO)) {
			return Boolean.FALSE;
		}
		//查询关联人员
		List<SmtSecurityPersonRelation> relations;
		if (CollUtil.isNotEmpty(reqDTO.getRelationId())) {
			List<Long> ids = reqDTO.getRelationId().stream().map(id -> {
				return Long.parseLong(id);
			}).collect(Collectors.toList());
			relations = this.baseMapper.selectBatchIds(ids);
		} else {
			SecurityPersonRelationExt ext = BeanUtils.transform(SecurityPersonRelationExt.class, reqDTO);
			relations = this.baseMapper.getList(ext);
		}
		if (CollUtil.isEmpty(relations)) {
			return Boolean.FALSE;
		}
		//删除保密区人员权限
		this.deleteAuth(relations);
		//删除保密区人员关联
		List<Long> personIds = relations.stream().map(SmtSecurityPersonRelation::getId).collect(Collectors.toList());
		return this.removeByIds(personIds);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean batchDeleteByZoneId(List<Long> securityZoneIds) {
		List<SmtSecurityPersonRelation> relations = this.list(Wrappers.<SmtSecurityPersonRelation>query().lambda()
				.in(SmtSecurityPersonRelation::getSecurityId, securityZoneIds));
		if (CollUtil.isEmpty(relations)) {
			return Boolean.FALSE;
		}
		this.deleteAuth(relations);
		this.remove(Wrappers.<SmtSecurityPersonRelation>query().lambda()
				.in(SmtSecurityPersonRelation::getSecurityId, securityZoneIds));
		return Boolean.TRUE;
	}

	/**
	 * 删除人员下发权限关联与权限
	 *
	 * @param relations
	 */
	public void deleteAuth(List<SmtSecurityPersonRelation> relations) {
		for (SmtSecurityPersonRelation relation : relations) {
			//当人员权限关联状态为已下发时，需要删除权限
			List<SmtSecurityTaskDetails> taskDetails = smtSecurityTaskDetailsService.list(Wrappers.<SmtSecurityTaskDetails>query().lambda()
					.eq(SmtSecurityTaskDetails::getStaffId, relation.getStaffId()));
			if (CollUtil.isEmpty(taskDetails)) {
				return;
			}
			List<SmtSecurityAuthRelation> authRelations = smtSecurityAuthRelationService.getList(relation.getSecurityId());
			List<Integer> authId = authRelations.stream().map(SmtSecurityAuthRelation::getAuthId).collect(Collectors.toList());
			//删除人员权限关联 仅删除保密区类型权限且当前保密区关联权限
			List<SmtStaffDeviceAuth> authRelation = smtStaffDeviceAuthService.list(Wrappers.<SmtStaffDeviceAuth>query()
					.lambda().eq(SmtStaffDeviceAuth::getStaffId, relation.getStaffId())
					.eq(SmtStaffDeviceAuth::getAuthType, RelationAuthTypeEnum.SECURITY_AUTH.getCode())
					.in(SmtStaffDeviceAuth::getAuthId, authId));
			if (CollUtil.isEmpty(authRelation)) {
				return;
			}
			SmtStaff staff = new SmtStaff();
			staff.setId(relation.getStaffId());
			staff.setName(relation.getStaffName());
			staff.setBadge(relation.getStaffBadge());
			//删除人员下发权限
			for (SmtStaffDeviceAuth auth : authRelation) {
				//设备权限表
				List<SmtDeviceAuthorityRelation> deviceAuthList = smtDeviceAuthorityRelationService
						.list(new LambdaQueryWrapper<SmtDeviceAuthorityRelation>()
								.eq(SmtDeviceAuthorityRelation::getAuthorityId, auth.getAuthId()));
				if (CollUtil.isEmpty(deviceAuthList)) {
					continue;
				}
				//删除人员已下发权限
				smtStaffService.savePersonCardTask(DeviceTaskConstants.DEL,
						DateUtil.currentSeconds(), DateUtil.currentSeconds(), staff, deviceAuthList);
			}
			List<Integer> authIds = authRelation.stream().map(SmtStaffDeviceAuth::getId).collect(Collectors.toList());
			smtStaffDeviceAuthService.removeByIds(authIds);
		}
	}

	@Override
	public List<StaffTreeRespDTO> getStaffTree() {
		List<Integer> parkIds = SecurityUtils.getUser().getParkIdList();
		return this.getParkTree(parkIds);
	}

	private List<StaffTreeRespDTO> getParkTree(List<Integer> parkIds) {
		List<StaffTreeRespDTO> depTreeList = new ArrayList<>();
		List<SmtPark> parks = smtParkService.list(Wrappers.<SmtPark>query()
				.lambda().in(CollUtil.isNotEmpty(parkIds), SmtPark::getId, parkIds));
		if (CollUtil.isNotEmpty(parks)) {
			for (SmtPark park : parks) {
				StaffTreeRespDTO depTree = new StaffTreeRespDTO();
				depTree.setLabel(park.getParkName());
				depTree.setValue(park.getId());
				depTree.setChildren(this.getComp(park.getId(), true));
				depTreeList.add(depTree);
			}
		}
		return depTreeList;
	}

	@Override
	public List<StaffTreeRespDTO> getComp(Integer parkId, boolean isDept) {
		List<SmtParkBu> listBu = smtParkBuService.list(Wrappers.<SmtParkBu>query().lambda()
				.eq(SmtParkBu::getParkId, parkId));
		List<StaffTreeRespDTO> listTree = new ArrayList<>();
		// 查询裕同视图中的全部bu
		if (CollUtil.isNotEmpty(listBu)) {
			Result<List<OvwYscompRespDTO>> ysList = remoteOvwYscompService.getList(SecurityConstants.FROM_IN);
			if (CollUtil.isEmpty(ysList.getData())) {
				return listTree;
			}
			Map<Integer, List<OvwYscompRespDTO>> ovmYsMap = ysList.getData().stream().collect(Collectors.groupingBy(OvwYscompRespDTO::getCompid));
			listBu.forEach(bu -> {
				Integer compId = Integer.parseInt(bu.getCompId());
				if (ovmYsMap.containsKey(compId)) {
					StaffTreeRespDTO depTree = new StaffTreeRespDTO();
					depTree.setLabel(ovmYsMap.get(compId).get(0).getTitle());
					depTree.setValue(ovmYsMap.get(compId).get(0).getCompid());
					if (isDept) {
						depTree.setChildren(getDepTree(compId));
					}
					listTree.add(depTree);
				}
			});
		}
		return listTree;
	}

	private List<StaffTreeRespDTO> getDepTree(Integer id) {
		List<StaffTreeRespDTO> depTreeList = new ArrayList<>();
		StaffTreeRespDTO depTree;
		Result<List<OvwYsdepRespDTO>> result = remoteOvwYsdepService.getByCompId(id, SecurityConstants.FROM_IN);
		List<OvwYsdepRespDTO> list = result.getData();
		if (CollectionUtil.isNotEmpty(list)) {
			for (OvwYsdepRespDTO ovwYsdepVO : list) {
				depTree = new StaffTreeRespDTO();
				depTree.setLabel(ovwYsdepVO.getDepname());
				depTree.setValue(ovwYsdepVO.getDepid());
				depTreeList.add(depTree);
			}
			return depTreeList;
		}
		return null;
	}

	@Override
	public List<SecurityPersonAddRespDTO> getStaffByDepId(String depId) {
		List<SmtStaff> staffs = smtStaffService.list(Wrappers.<SmtStaff>query().lambda()
				.eq(SmtStaff::getDepId, depId));
		if (CollUtil.isEmpty(staffs)) {
			return null;
		}
		List<SecurityPersonAddRespDTO> staffList = new ArrayList<>();
		staffs.forEach(staff -> {
			staffList.add(SecurityPersonAddRespDTO.builder().staffId(staff.getId())
					.staffBadge(staff.getBadge()).staffName(staff.getName())
					.staffInfo(staff.getBadge() + SymbolConstants.BLANK + staff.getName()).build());
		});
		return staffList;
	}

	@Override
	public IPage<SecurityAllStaffListDTO> getAllStaffPage(Page page, SecurityPersonQueryReqDTO reqDTO) {
		List<Integer> parkIdList = SecurityUtils.getUser().getParkIdList();
		SecurityPersonRelationExt ext = new SecurityPersonRelationExt();
		if (Objects.nonNull(reqDTO)) {
			ext = BeanUtils.transform(SecurityPersonRelationExt.class, reqDTO);
		}
		ext.setParkIds(parkIdList);
		IPage<SecurityAllStaffListDTO> result = smtStaffService.getStaffPage(page, ext);
		return result;
	}

	@Override
	public List<SmtSecurityPersonRelation> getByStaffId(Long staffId) {
		return this.list(Wrappers.<SmtSecurityPersonRelation>query().lambda().eq(SmtSecurityPersonRelation::getStaffId, staffId));
	}

}
