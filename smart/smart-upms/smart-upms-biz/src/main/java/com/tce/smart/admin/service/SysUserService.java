package com.tce.smart.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tce.smart.admin.api.dto.UserDTO;
import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.api.vo.UserVO;
import com.tce.smart.common.core.model.Result;

import java.util.List;

public interface SysUserService extends IService<SysUser> {
	/**
	 * 查询用户信息
	 *
	 * @param sysUser 用户
	 * @return userInfo
	 */
	UserInfo findUserInfo(SysUser sysUser);

	/**
	 * 分页查询用户信息（含有角色信息）
	 *
	 * @param page    分页对象
	 * @param userDTO 参数列表
	 * @return
	 */
	IPage getUsersWithRolePage(Page page, UserDTO userDTO);

	/**
	 * 删除用户
	 *
	 * @param sysUser 用户
	 * @return boolean
	 */
	Result<Boolean> deleteUserById(SysUser sysUser);

	/**
	 * 更新当前用户基本信息
	 *
	 * @param userDto 用户信息
	 * @return Boolean
	 */
	Result<Boolean> updateUserInfo(UserDTO userDto);

	Boolean verifyMobile( String mobile);

	/**
	 * 更新指定用户信息
	 *
	 * @param userDto 用户信息
	 * @return
	 */
	Boolean updateUser(UserDTO userDto);

	/**
	 * 通过ID查询用户信息
	 *
	 * @param id 用户ID
	 * @return 用户信息
	 */
	UserVO selectUserVoById(Integer id);

	/**
	 * 查询上级部门的用户信息
	 *
	 * @param username 用户名
	 * @return Result
	 */
	List<SysUser> listAncestorUsers(String username);

	/**
	 * 保存用户信息
	 *
	 * @param userDto DTO 对象
	 * @return success/fail
	 */
	Boolean saveUser(UserDTO userDto);

	/**
	 * 重置密码
	 * @param id
	 * @return
	 */
	Boolean reset(Integer id);

	/**
	 * 冻结用户
	 * @param id
	 * @return
	 */
	Boolean freeze(Integer id);

	Boolean simpleLogin(String username, String password);

	/**
	 * 显式校验工号密码，不使用旧用户详情缓存作为认证结果。
	 *
	 * @param username 用户名
	 * @param password 密码
	 * @return 是否通过认证
	 */
	Boolean authenticate(String username, String password);

	/**
	 * App 统一登录使用的内部认证：来源由平台主数据决定，正式员工交给 DHR 适配器，
	 * 外包和派遣人员只校验本系统账户。不会改变旧 /simple 和 Web 登录语义。
	 */
	Boolean authenticateAppSession(String username, String password);

	/**
	 * 修改用户密码
	 *
	 * @param username 用户名
	 * @param password 用户新密码
	 * @param updateAuthCode 修改授权密码
	 * @return true-成功
	 */
	Boolean updatePwd(String username,String password,String updateAuthCode);

	/**
	 * 查询用户关联园区
	 *
	 * @param userId 用户Id
	 * @return List<Integer> 用户管理园区ID集合
	 */
	List<Integer> listUserPark(Integer userId);

	/**
	 * 查询当前登录的用户数
	 * 这里统计的是redis里面的 smart_oauth:access:* 这个key的数量
	 *  keys smart_oauth:access:*
	 * @return
	 */
	Integer loggedCount();

	Boolean socialLogin(String username);
}
