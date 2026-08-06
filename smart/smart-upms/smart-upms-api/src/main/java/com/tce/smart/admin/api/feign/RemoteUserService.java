package com.tce.smart.admin.api.feign;

import com.tce.smart.admin.api.dto.UserDTO;
import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * @date 2018/6/22
 */
@FeignClient(value = ServiceNameConstants.UMPS_SERVICE)
public interface RemoteUserService {
	/**
	 * 通过用户名查询用户、角色信息
	 *
	 * @param username 用户名
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/api/user/info/{username}")
    Result<UserInfo> info(@PathVariable("username") String username
			, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 通过手机号码查询用户、角色信息
	 *
	 * @param mobile 手机号码
	 * @param from     调用标志
	 * @return Result
	 */
	@GetMapping("/api/user/query/{mobile}")
	Result<UserInfo> queryByMobile(@PathVariable("mobile") String mobile
			, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping("/api/user/verify/{mobile}")
	Result<Boolean> verifyMobile(@PathVariable("mobile") String mobile, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 通过社交账号或手机号查询用户、角色信息
	 *
	 * @param inStr appid@code
	 * @param from  调用标志
	 * @return
	 */
	@GetMapping("/social/info/{inStr}")
    Result<UserInfo> social(@PathVariable("inStr") String inStr
			, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 查询上级部门的用户信息
	 *
	 * @param username 用户名
	 * @return Result
	 */
	@GetMapping("/api/user/ancestor/{username}")
    Result<List<SysUser>> ancestorUsers(@PathVariable("username") String username, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping(value = {"/api/user/simple"})
	Result<Boolean> simpleLogin(@RequestParam("username") String userName, @RequestParam("password") String password, @RequestHeader(SecurityConstants.FROM) String from);

	@GetMapping(value = {"/api/user/social/simple"})
	Result<Boolean> socialLogin(@RequestParam("username") String userName, @RequestHeader(SecurityConstants.FROM) String from);

    /**
     * 修改个人信息
     *
     * @param userDto userDto
     * @return success/false
     */
    @PostMapping("/api/user/edit")
    Result<Boolean> updateUserInfo(@RequestBody UserDTO userDto, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 修改用户
	 *
	 * @param userDto userDto
	 * @return success/false
	 */
	@PostMapping("/api/user/update")
	Result<Boolean> updateUser(@RequestBody UserDTO userDto, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * 新增用户
	 *
	 * @param userDto userDto
	 * @return success/false
	 */
	@PostMapping("/api/user/save")
	Result<Boolean> saveUser(@RequestBody UserDTO userDto, @RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * platform平台删除用户信息
	 *
	 * @param username 用户名
	 * @param from 内部调用标识
	 * @return Result
	 */
	@PostMapping("/api/user/delete/{username}")
	Result<Boolean> delUserForPlatform(@PathVariable("username") String username,@RequestHeader(SecurityConstants.FROM) String from);

	/**
	 * @param userId 用户ID
	 * @param from 内部调用标识
	 * @return 用户关联园区列表
	 */
	@GetMapping("/api/user/park/list/{userId}")
	Result<List<Integer>> listUserPark(@PathVariable("userId") Integer userId,@RequestHeader(SecurityConstants.FROM) String from);
}
