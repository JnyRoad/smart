package com.tce.smart.admin.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.tce.smart.admin.api.dto.UserDTO;
import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.service.SysUserService;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import io.swagger.annotations.Api;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api/user")
@Api(value = "user", description = "用户管理模块-内部接口")
public class UserApiController extends BaseController {
    private final SysUserService userService;

    /**
     * 账户名+密码登录
     *
     * @return 用户信息
     */
    @Inner
    @GetMapping(value = {"/simple"})
    public Result<Boolean> simpleLogin(@RequestParam("username") String username, @RequestParam("password") String password) {
        return new Result<Boolean>(userService.simpleLogin(username, password));
    }

	@Inner
	@GetMapping(value = {"/social/simple"})
	public Result<Boolean> socialLogin(@RequestParam("username") String username) {
		return new Result<>(userService.socialLogin(username));
	}

    /**
     * 获取指定用户全部信息
     *
     * @return 用户信息
     */
	@Inner
    @GetMapping("/info/{username}")
    public Result info(@PathVariable String username) {
        SysUser user = userService.getOne(Wrappers.<SysUser>query()
                .lambda().eq(SysUser::getUsername, username));
        if (user == null) {
			throw new TCEException(String.format("用户信息为空 %s", username));
        }
        UserInfo userInfo = userService.findUserInfo(user);
        return success(userInfo);
    }

	@Inner
	@GetMapping("/query/{mobile}")
	public Result queryByMobile(@PathVariable String mobile) {
		SysUser user = userService.getOne(Wrappers.<SysUser>query()
				.lambda().eq(SysUser::getPhone, mobile));
		if (user == null) {
			throw new TCEException(String.format("用户信息为空 %s", mobile));
		}
		UserInfo userInfo = userService.findUserInfo(user);
		return success(userInfo);
	}

	@Inner
	@GetMapping("/verify/{mobile}")
	public Result verifyMobile(@PathVariable String mobile) {
		return success(userService.verifyMobile(mobile));
	}

	/**
	 * 修改个人信息
	 *
	 * @param userDto userDto
	 * @return success/false
	 */
	@Inner
	@SysLog("修改个人信息")
	@PostMapping("/edit")
	public Result updateUserInfo(@Valid @RequestBody UserDTO userDto) {
		return userService.updateUserInfo(userDto);
	}


    /**
     * 添加用户
     *
     * @param userDto 用户信息
     * @return success/false
     */
	@Inner
    @SysLog("添加用户")
    @PostMapping("/save")
    @PreAuthorize("@pms.hasPermission('sys_user_add')")
    public Result saveUser(@RequestBody UserDTO userDto) {
        return success(userService.saveUser(userDto));
    }

    /**
     * 更新用户信息
     *
     * @param userDto 用户信息
     * @return Result
     */
	@Inner
    @SysLog("更新用户信息")
    @PostMapping("/update")
    @PreAuthorize("@pms.hasPermission('sys_user_edit')")
    public Result updateUser(@Valid @RequestBody UserDTO userDto) {
        return success(userService.updateUser(userDto));
    }


    /**
     * @param userId 用户ID
     * @return 用户关联园区列表
     */
    @GetMapping("/park/list/{userId}")
    @Inner
    public Result<List<Integer>> listUserPark(@PathVariable Integer userId) {
        return success(userService.listUserPark(userId));
    }

	/**
	 * @param username 用户名称
	 * @return 上级部门用户列表
	 */
	@Inner
	@GetMapping("/ancestor/{username}")
	public Result listAncestorUsers(@PathVariable String username) {
		return success(userService.listAncestorUsers(username));
	}

	/**
	 * platform平台删除用户信息
	 *
	 * @param username 用户名
	 * @return Result
	 */
	@Inner
	@PostMapping("/delete/{username}")
	public Result<Boolean> delUserForPlatform(@PathVariable String username) {
		SysUser user = userService.getOne(Wrappers.<SysUser>query()
				.lambda().eq(SysUser::getUsername, username));
		if (Objects.nonNull(user)) {
			return userService.deleteUserById(user);
		} else {
			return fail(String.format("用户信息为空 %s", username));
		}
	}
}
