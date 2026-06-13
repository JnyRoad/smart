package com.tce.smart.admin.api.dto;

import com.tce.smart.admin.api.entity.SysUser;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @date 2017/11/11
 * <p>
 * commit('SET_ROLES', data)
 * commit('SET_NAME', data)
 * commit('SET_AVATAR', data)
 * commit('SET_INTRODUCTION', data)
 * commit('SET_PERMISSIONS', data)
 */
@Data
public class UserInfo implements Serializable {
	/**
	 * 用户基本信息
	 */
	private SysUser sysUser;
	/**
	 * 权限标识集合
	 */
	private String[] permissions;

	/**
	 * 角色id集合
	 */
	private Integer[] roles;

	/**
	 * 角色信息集合
	 */
	private List<RoleDTO> roleList;

	/**
	 * 薪资计算类型
	 */
	private String salaryTypeName;
}
