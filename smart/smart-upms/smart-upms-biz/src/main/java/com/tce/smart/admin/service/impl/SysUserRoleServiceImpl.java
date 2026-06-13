package com.tce.smart.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tce.smart.admin.api.entity.SysUserRole;
import com.tce.smart.admin.mapper.SysUserRoleMapper;
import com.tce.smart.admin.service.SysUserRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户角色表 服务实现类
 * </p>
 */
@Service
public class SysUserRoleServiceImpl extends ServiceImpl<SysUserRoleMapper, SysUserRole> implements SysUserRoleService {

	/**
	 * 根据用户Id删除该用户的角色关系
	 *
	 * @param userId 用户ID
	 * @return boolean
	 * @author 寻欢·李
	 * @date 2017年12月7日 16:31:38
	 */
	@Override
	public boolean deleteByUserId(Integer userId) {
		return baseMapper.deleteByUserId(userId);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public boolean updateUserRole(Integer userId, List<Integer> roleIdList) {
		if (CollectionUtils.isNotEmpty(roleIdList)) {
			if (this.deleteByUserId(userId)) {
				return this.saveUserRoleBatch(userId, roleIdList);
			}
		}
		return Boolean.FALSE;
	}

	@Override
	public boolean saveUserRoleBatch(Integer userId, List<Integer> roleIdList) {
		if (Objects.nonNull(userId) && CollectionUtils.isNotEmpty(roleIdList)) {
			List<SysUserRole> userParkList = roleIdList.stream().map(roleId -> {
				SysUserRole userPark = new SysUserRole();
				userPark.setUserId(userId);
				userPark.setRoleId(roleId);
				return userPark;
			}).collect(Collectors.toList());

			return this.saveBatch(userParkList);
		}

		return Boolean.FALSE;
	}
}
