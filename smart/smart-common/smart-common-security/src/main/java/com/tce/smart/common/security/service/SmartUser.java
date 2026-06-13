package com.tce.smart.common.security.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

/**
 * 扩展用户信息
 */
public class SmartUser extends User {

	private static final long serialVersionUID = -6612040381029878798L;

	/**
	 * 用户ID
	 */
	@Getter
	private Integer id;
	/**
	 * 部门ID
	 */
	@Getter
	private Integer deptId;

	/**
	 * 用户关联园区ID集合
	 */
	@Getter
	@Setter
	private List<Integer> parkIdList;

	/**
	 * 是否强密码
	 */
	@Getter
	@Setter
	private Boolean isStrongPwd;

	/**
	 * 薪资计算类型
	 */
	@Getter
	private String salaryTypeName;

	public SmartUser(Integer id, Integer deptId, String username, List<Integer> parkIdList, String password, boolean enabled, boolean accountNonExpired,
					 boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
		this(id,deptId,username,parkIdList,password,enabled,accountNonExpired,credentialsNonExpired,accountNonLocked,authorities,true,"");
	}

	/**
	 * Construct the <code>User</code> with the details required by
	 * {@link DaoAuthenticationProvider}.
	 *
	 * @param id                    用户ID
	 * @param deptId                部门ID
	 * @param username              the username presented to the
	 *                              <code>DaoAuthenticationProvider</code>
	 * @param password              the password that should be presented to the
	 *                              <code>DaoAuthenticationProvider</code>
	 * @param enabled               set to <code>true</code> if the user is enabled
	 * @param accountNonExpired     set to <code>true</code> if the account has not expired
	 * @param credentialsNonExpired set to <code>true</code> if the credentials have not
	 *                              expired
	 * @param accountNonLocked      set to <code>true</code> if the account is not locked
	 * @param authorities           the authorities that should be granted to the caller if they
	 *                              presented the correct username and password and the user is enabled. Not null.
	 * @throws IllegalArgumentException if a <code>null</code> value was passed either as
	 *                                  a parameter or as an element in the <code>GrantedAuthority</code> collection
	 */
	public SmartUser(Integer id, Integer deptId, String username, List<Integer> parkIdList, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities,boolean isStrongPwd,String salaryTypeName) {
		super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
		this.id = id;
		this.deptId = deptId;
		this.parkIdList = parkIdList;
		this.isStrongPwd = isStrongPwd;
		this.salaryTypeName = salaryTypeName;
	}
}
