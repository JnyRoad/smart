package com.tce.smart.admin.api.dto;

import com.tce.smart.admin.api.entity.SysUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 *
 * @date 2017/11/5
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends SysUser {
	/**
	 * 角色ID
	 */
	private List<Integer> role;

	/**
	 * 园区ID
	 */
	private List<Integer> park;

	private Integer deptId;

	/**
	 * 角色名称
	 */
	private String roleName;

	/**
	 * 新密码
	 */
	private String newpassword1;
}
