package com.tce.smart.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.tce.smart.admin.api.dto.UserDTO;
import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.dto.PasswordUpdateReqDTO;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.service.SysUserService;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.enums.ExceptionType;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.util.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
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
@RequestMapping("/user")
@Api(value = "user", description = "用户管理模块")
public class UserController extends BaseController {
    private final SysUserService userService;
    /**
     * 获取当前用户全部信息
     *
     * @return 用户信息
     */
    @GetMapping(value = {"/info"})
    public Result info() {
        String username = SecurityUtils.getUser().getUsername();
        SysUser user = userService.getOne(Wrappers.<SysUser>query()
                .lambda()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDelFlag, CommonConstants.STATUS_NORMAL));
        if (user == null) {
            throw new TCEException("获取当前用户信息失败");
        }
        UserInfo userInfo = userService.findUserInfo(user);
        return success(userInfo);
    }

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

	// 仅供内部服务调用（认证服务社交登录）：合法入口是 RemoteUserService -> /api/user/social/simple，
	// 本 /user/social/simple 为历史遗留副本，补 @Inner 收敛为内部专用，防止无 token 请求直接触发建号/登录
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
        if (!SecurityUtils.getUser().getId().equals(user.getUserId())) {
            throw new TCEException(ExceptionType.ROLE_HAVE_NOT);
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
        if (!SecurityUtils.getUser().getId().equals(user.getUserId())) {
            throw new TCEException(ExceptionType.ROLE_HAVE_NOT);
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
     * 冻结用户
     *
     * @return
     */
    @SysLog("冻结用户")
    @GetMapping("/freeze/{id}")
    @PreAuthorize("@pms.hasPermission('sys_user_freeze')")
    @ApiOperation(value = "冻结用户", notes = "根据ID冻结用户")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "int", paramType = "path")
    public Result freeze(@PathVariable Integer userId) {
        boolean result = userService.freeze(userId);
        return success(result);
    }

    /**
     * 重置用户密码
     *
     * @return
     */
    @SysLog("重置密码")
    @GetMapping("/reset/{id}")
    @PreAuthorize("@pms.hasPermission('sys_user_reset')")
    @ApiOperation(value = "重置密码", notes = "根据用户ID重置密码")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "int", paramType = "path")
    public Result reset(@PathVariable Integer userId) {
        boolean result = userService.reset(userId);
        return success(result);
    }

    /**
     * 通过ID查询用户信息
     *
     * @param id ID
     * @return 用户信息
     */
    @GetMapping("/{id}")
    public Result user(@PathVariable Integer id) {
        return success(userService.selectUserVoById(id));
    }

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return
     */
    @GetMapping("/details/{username}")
    public Result user(@PathVariable String username) {
        SysUser condition = new SysUser();
        condition.setUsername(username);
        return success(userService.getOne(new QueryWrapper<>(condition)));
    }

    /**
     * 删除用户信息
     *
     * @param id ID
     * @return Result
     */
    @SysLog("删除用户信息")
    @PostMapping("/{id}")
    @PreAuthorize("@pms.hasPermission('sys_user_del')")
    @ApiOperation(value = "删除用户", notes = "根据ID删除用户")
    @ApiImplicitParam(name = "id", value = "用户ID", required = true, dataType = "int", paramType = "path")
    public Result userDel(@PathVariable Integer id) {
        SysUser sysUser = userService.getById(id);
        return success(userService.deleteUserById(sysUser));
    }

    /**
     * 添加用户
     *
     * @param userDto 用户信息
     * @return success/false
     */
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
    @SysLog("更新用户信息")
    @PostMapping("/update")
    @PreAuthorize("@pms.hasPermission('sys_user_edit')")
    public Result updateUser(@Valid @RequestBody UserDTO userDto) {
        return success(userService.updateUser(userDto));
    }

    @SysLog("无权限验证更新用户信息")
    @PostMapping("/inner/user/edit")
    public Result updateUserPwd(@Valid @RequestBody UserDTO userDto) {
        if (!SecurityUtils.getUser().getId().equals(userDto.getUserId())) {
            throw new TCEException(ExceptionType.ROLE_HAVE_NOT);
        }
        return success(userService.updateUser(userDto));
    }

    /**
     * 分页查询用户
     *
     * @param page    参数集
     * @param userDTO 查询参数列表
     * @return 用户集合
     */
    @GetMapping("/page")
    public Result getUserPage(Page page, UserDTO userDTO) {
        log.info("查询用户数据:{}",userDTO.getUsername());
        IPage pageData = null;
        try {
            pageData = userService.getUsersWithRolePage(page, userDTO);
        } catch (Exception e) {
            log.warn("查询用户数据出错",e);
        }
        return success(pageData);
    }

    /**
     * 修改个人信息
     *
     * @param userDto userDto
     * @return success/false
     */
    @SysLog("修改个人信息")
    @PostMapping("/edit")
    public Result updateUserInfo(@Valid @RequestBody UserDTO userDto) {
        if (!SecurityUtils.getUser().getId().equals(userDto.getUserId())) {
            throw new TCEException(ExceptionType.ROLE_HAVE_NOT);
        }
        return userService.updateUserInfo(userDto);
    }

    /**
     * @param username 用户名称
     * @return 上级部门用户列表
     */
    @GetMapping("/ancestor/{username}")
    public Result listAncestorUsers(@PathVariable String username) {
        return success(userService.listAncestorUsers(username));
    }

    /**
     * 修改用户密码
     *
     * @param username 用户名
     * @param password 用户新密码
     * @param updateAuthCode 授权码
     *
     * @return  Result<Boolean> true-成功
     */
    @PutMapping("/password/update")
    public Result<Boolean> updatePwd(@RequestBody PasswordUpdateReqDTO request) {
        if (request == null) {
            throw new TCEException("修改密码请求不能为空");
        }
        return new Result<Boolean>(userService.updatePwd(request.getUsername(), request.getPassword(), request.getUpdateAuthCode()));
    }

	/**
	 * platform平台删除用户信息
	 *
	 * @param username 用户名
	 * @return Result
	 */
	// 仅供内部服务调用（platform 平台删除用户）：合法入口是 RemoteUserService -> /api/user/delete/{username}，
	// 本 /user/delete/{username} 为历史遗留副本，补 @Inner 收敛为内部专用，防止无 token 请求直接删除任意账号
	@Inner
	@HystrixCommand(fallbackMethod = "delUserForPlatformFallback")
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

	public Result<Boolean> delUserForPlatformFallback(@PathVariable String username) {
		log.warn("请求异常，执行回退方式");
		SysUser user = userService.getOne(Wrappers.<SysUser>query()
				.lambda().eq(SysUser::getUsername, username));
		if (Objects.nonNull(user)) {
			return userService.deleteUserById(user);
		} else {
			return fail(String.format("用户信息为空 %s", username));
		}
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
	 * @return 当前登录用户数量
	 */
	@GetMapping("/logged/count")
	public Result<Integer> loggedCount() {
		return success(userService.loggedCount());
	}
}
