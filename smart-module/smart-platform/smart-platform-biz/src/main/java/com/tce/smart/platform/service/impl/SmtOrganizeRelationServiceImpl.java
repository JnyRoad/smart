package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.dto.UserDTO;
import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.entity.SysRole;
import com.tce.smart.admin.api.feign.RemoteRoleService;
import com.tce.smart.admin.api.feign.RemoteUserService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.util.BeanUtils;
import com.tce.smart.common.core.util.CollectionUtils;
import com.tce.smart.common.core.util.StringUtils;
import com.tce.smart.data.api.dto.ehrview.resp.OvwYsConComanyRespDTO;
import com.tce.smart.data.api.feign.ehrview.RemoteOvwYsConComanyService;
import com.tce.smart.platform.api.dto.req.OrganizeRelationReqDTO;
import com.tce.smart.platform.core.entity.SmtExternalDept;
import com.tce.smart.platform.core.entity.SmtOrganizeAccess;
import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtExternalDeptMapper;
import com.tce.smart.platform.core.mapper.SmtOrganizeRelationMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.service.SmtOrganizeAccessService;
import com.tce.smart.platform.service.SmtOrganizeRelationService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.platform.service.SmtStaffService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 外部bu设置
 *
 * @author
 * @date 2019-04-15 11:34:58
 */
@Slf4j
@Service
public class SmtOrganizeRelationServiceImpl extends ServiceImpl<SmtOrganizeRelationMapper, SmtOrganizeRelation> implements SmtOrganizeRelationService {

	@Autowired
	private RemoteUserService remoteUserService;
	@Autowired
	private RemoteRoleService remoteRoleService;
	@Autowired
	private RemoteOvwYsConComanyService remoteOvwYsConComanyService;
/*	@Autowired
	private SmtParkBuService smtParkBuService;*/
	@Autowired
	private SmtExternalDeptMapper smtExternalDeptMapper;
	@Autowired
	private SmtStaffMapper smtStaffMapper;
	@Autowired
	private SmtOrganizeAccessService organizeAccessService;
	@Autowired
	private SmtStaffDeviceAuthService staffDeviceAuthService;
	@Autowired
	private SmtStaffService staffService;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveBu(OrganizeRelationReqDTO relationReqDTO) {
		saveUser(relationReqDTO);
		LocalDateTime now = LocalDateTime.now();
		SmtOrganizeRelation relation = BeanUtils.transform(SmtOrganizeRelation.class, relationReqDTO);
		try {
			Integer userId = remoteUserService.info(relationReqDTO.getUserName(), SecurityConstants.FROM_IN).getData().getSysUser().getUserId();
			relation.setUserId(userId);
			relation.setCreateTime(now);
		} catch (Exception e) {
			throw new SmartException("无管理权限，请联系管理员分配账号");
		}
		//relation.setSource(OrganizeSourceEnum.MANUAL.getCode());
		this.save(relation);
		return authAccess(relationReqDTO.getDeviceAuthId(), relation);
//		SmtParkBu bu = new SmtParkBu();
//		bu.setCompId(Long.toString(relation.getId()));
//		bu.setCompName(relation.getCompName());
//		bu.setParkId(relation.getParkId());
//		bu.setCreateTime(now);
//		return smtParkBuService.save(bu);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateBu(OrganizeRelationReqDTO relationReqDTO) {
		SmtOrganizeRelation relation = BeanUtils.transform(SmtOrganizeRelation.class, relationReqDTO);
		//若userId为空，则该数据为ehr同步数据且未关联用户
		if (Objects.isNull(relationReqDTO.getUserName())) {
			this.saveUser(relationReqDTO);
			Integer userId = remoteUserService.info(relationReqDTO.getUserName(), SecurityConstants.FROM_IN).getData().getSysUser().getUserId();
			relation.setUserId(userId);
			this.updateById(relation);
			return authAccess(relationReqDTO.getDeviceAuthId(), relation);
		}
//		SmtParkBu bu = new SmtParkBu();
//		bu.setCompId(Long.toString(relation.getId()));
//		bu.setCompName(relation.getCompName());
//		bu.setParkId(relation.getParkId());
//		smtParkBuService.update(bu,Wrappers.<SmtParkBu>query().lambda().eq(SmtParkBu::getCompId, relation.getId()));
		//修改smtStaff表中相应字段
		SmtStaff smtStaff = new SmtStaff();
		smtStaff.setCompName(relation.getCompName());
		smtStaffMapper.update(smtStaff, Wrappers.<SmtStaff>query().lambda().eq(SmtStaff::getCompId,Long.toString(relation.getId())));
		this.updateUser(relationReqDTO);
		this.updateById(relation);
		return authAccess(relationReqDTO.getDeviceAuthId(), relation);
	}

	/**
	 * 更新门禁权限，并按差量同步给下级员工：
	 * 只给员工新增本次新加的权限、删除本次去掉的权限，员工已有的其他权限（含单独分配的）保持不动
	 * @param deviceAuthIds
	 * @param relation
	 * @return
	 */
	private Boolean authAccess(List<Integer> deviceAuthIds, SmtOrganizeRelation relation) {
		if (Objects.isNull(deviceAuthIds)) {
			// 请求未携带门禁权限字段（如组织管理页），不调整权限
			return Boolean.TRUE;
		}
		List<Integer> oldAuthIds = organizeAccessService.list(Wrappers.<SmtOrganizeAccess>lambdaQuery()
				.eq(SmtOrganizeAccess::getOrganizeId, relation.getId())).stream()
				.map(SmtOrganizeAccess::getDeviceAuthId).collect(Collectors.toList());
		List<Integer> addedAuthIds = deviceAuthIds.stream().distinct()
				.filter(authId -> !oldAuthIds.contains(authId)).collect(Collectors.toList());
		List<Integer> removedAuthIds = oldAuthIds.stream().distinct()
				.filter(authId -> !deviceAuthIds.contains(authId)).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(addedAuthIds) && CollectionUtils.isEmpty(removedAuthIds)) {
			// 权限未变化，不动员工权限
			return Boolean.TRUE;
		}
		// 更新门禁权限关联 先删后插（传空列表表示清空单位全部权限）
		List<SmtOrganizeAccess> organizeAccessList = new ArrayList<>();
		deviceAuthIds.forEach(deviceAuthId -> organizeAccessList.add(SmtOrganizeAccess.builder()
				.organizeId(relation.getId())
				.deviceAuthId(deviceAuthId).build()));
		organizeAccessService.delByOrgId(relation.getId());
		if (CollectionUtils.isNotEmpty(organizeAccessList)) {
			organizeAccessService.saveBatch(organizeAccessList);
		}
		// 按差量更新员工门禁权限
		List<SmtStaff> staffList = staffService.list(Wrappers.<SmtStaff>lambdaQuery()
				.eq(Objects.nonNull(relation.getId()), SmtStaff::getCompId, relation.getId())
				.eq(Objects.nonNull(relation.getCompName()), SmtStaff::getCompName, relation.getCompName()));
		if(CollectionUtils.isNotEmpty(staffList)) {
			staffDeviceAuthService.applyAuthDiff(staffList.stream().map(SmtStaff::getId).collect(Collectors.toList()),
					addedAuthIds, removedAuthIds);
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean deleteBu(Long id) {
		//检查是否仍有部门关联该组织
		Integer count = smtExternalDeptMapper.selectCount(Wrappers.<SmtExternalDept>query().lambda().eq(SmtExternalDept::getCompId, id));
		if(count > 0) {
			throw new SmartException("请先移除该组织下的部门与人员");
		}
		SmtOrganizeRelation relation = this.getById(id);
		remoteUserService.delUserForPlatform(relation.getUserName(), SecurityConstants.FROM_IN);
		return this.removeById(id);
	}

	@Override
	public Boolean saveSyncBu(List<String> buList, Integer parkId) {
		List<SmtOrganizeRelation> relation = new ArrayList<>();
		this.remove(Wrappers.<SmtOrganizeRelation>query().lambda().in(SmtOrganizeRelation::getCompId, buList));
		buList.forEach(bu -> {
			SmtOrganizeRelation smtOrganizeRelation = new SmtOrganizeRelation();
			Result<OvwYsConComanyRespDTO> buInfo = remoteOvwYsConComanyService.getByCompId(Integer.parseInt(bu), SecurityConstants.FROM_IN);
			if (Objects.nonNull(buInfo.getData())) {
				smtOrganizeRelation.setCompName(buInfo.getData().getTitle());
				smtOrganizeRelation.setCompId(bu);
			}
			//smtOrganizeRelation.setSource(OrganizeSourceEnum.EHR_SYNC.getCode());
			smtOrganizeRelation.setParkId(parkId);
			smtOrganizeRelation.setCreateTime(LocalDateTime.now());
			relation.add(smtOrganizeRelation);
		});
		return this.saveBatch(relation);
	}

	@Override
	public SmtOrganizeRelation getByUserId(Integer userId) {
		SmtOrganizeRelation relation = this.getOne(Wrappers.<SmtOrganizeRelation>query().lambda().eq(SmtOrganizeRelation::getUserId, userId));
		return relation;
	}

	@Override
	public List<SmtOrganizeRelation> getByParkId(List<Integer> parkIds) {
		return this.list(Wrappers.<SmtOrganizeRelation>lambdaQuery().in(SmtOrganizeRelation::getParkId, parkIds));
	}

	@Override
	public SmtOrganizeRelation getByUserName(String userName) {
		return this.getOne(Wrappers.<SmtOrganizeRelation>query().lambda().eq(SmtOrganizeRelation::getUserName, userName));
	}

	@Override
	public SmtOrganizeRelation getByBu(Long id) {
		return this.getById(id);
	}


	@Override
	public Boolean wasExcludeOrg(Long compId) {
		List<Long> orgList = wasExcludeOrgIds();
		return orgList.contains(compId);
	}
	@Override
	public List<Long> wasExcludeOrgIds() {
		List<Long> tmpOrgIds = new ArrayList<>();
		final List<String> tmpOrg = new ArrayList<String>(){{
			add("美深威");
			add("阜昌");
			add("裕同集团石岩园区临时人员管理处");
		}};
		List<SmtOrganizeRelation> orgList = this.list(new LambdaQueryWrapper<SmtOrganizeRelation>().in(SmtOrganizeRelation::getCompName, tmpOrg));
		if(CollectionUtils.isNotEmpty(orgList)){
			tmpOrgIds = orgList.stream().map(SmtOrganizeRelation::getId).collect(Collectors.toList());
		}
		return tmpOrgIds;
	}


	/**
	 * 新增用户
	 *
	 * @param relationReqDTO
	 * @return
	 */
	private Boolean saveUser(OrganizeRelationReqDTO relationReqDTO) {
		UserDTO userDTO = new UserDTO();
		SmtOrganizeRelation smtOrganizeRelation = this.getByUserName(relationReqDTO.getUserName());
		if (Objects.nonNull(smtOrganizeRelation)) {
			throw new SmartException("该用户已为其他企业管理员");
		}
		UserInfo user = remoteUserService.info(relationReqDTO.getUserName(), SecurityConstants.FROM_IN).getData();
		if (Objects.nonNull(user)) {
			throw new SmartException("该用户已存在");
		}
		Result<List<SysRole>> resultRole = remoteRoleService.listRoles(SecurityConstants.FROM_IN);
		if (CollectionUtils.isEmpty(resultRole.getData())) {
			throw new SmartException("获取角色列表失败");
		}
		List<SysRole> roleList = resultRole.getData();
		List<SysRole> role = roleList.stream().filter(r -> r.getRoleDesc().equals("企业管理员")).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(role)) {
			List<Integer> roleIds = role.stream().map(SysRole::getRoleId).collect(Collectors.toList());
			userDTO.setRole(roleIds);
		} else {
			throw new SmartException("企业管理员角色不存在，请联系管理员新增该角色");
		}
		userDTO.setPark(Collections.singletonList(relationReqDTO.getParkId()));
		userDTO.setPassword(relationReqDTO.getPassword());
		userDTO.setUsername(relationReqDTO.getUserName());
		Result<Boolean> b = remoteUserService.saveUser(userDTO, SecurityConstants.FROM_IN);
		if (Objects.isNull(b)) {
			throw new SmartException("新增用户信息失败");
		}
		return b.getData();
	}

	/**
	 * 修改用户信息
	 *
	 * @param relationReqDTO
	 * @return
	 */
	private Boolean updateUser(OrganizeRelationReqDTO relationReqDTO) {
		SmtOrganizeRelation relation = this.getById(relationReqDTO.getId());
		Result<UserInfo> userResult = remoteUserService.info(relationReqDTO.getUserName(), SecurityConstants.FROM_IN);
		if(Objects.isNull(userResult.getData())) {
			throw new SmartException("获取用户信息异常");
		}
		UserDTO userDTO = new UserDTO();
		userDTO.setUserId(userResult.getData().getSysUser().getUserId());
		if (StringUtils.isNotEmpty(relationReqDTO.getPassword())) {
			userDTO.setPassword(relationReqDTO.getPassword());
		}
		if (!relationReqDTO.getParkId().equals(relation.getParkId())) {
			userDTO.setPark(Collections.singletonList(relationReqDTO.getParkId()));
		}
		userDTO.setUsername(relationReqDTO.getUserName());
		if (Objects.nonNull(userDTO)) {
			Result<Boolean> b = remoteUserService.updateUser(userDTO, SecurityConstants.FROM_IN);
			if (Objects.isNull(b)) {
				throw new SmartException("修改用户信息失败");
			}
			return b.getData();
		}
		return Boolean.TRUE;
	}
}
