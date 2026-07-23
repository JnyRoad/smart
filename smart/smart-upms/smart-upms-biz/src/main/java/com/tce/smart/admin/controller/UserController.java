package com.tce.smart.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.dto.AdminUserProfileRespDTO;
import com.tce.smart.admin.api.dto.PasswordUpdateReqDTO;
import com.tce.smart.admin.api.dto.SelfUserInfoRespDTO;
import com.tce.smart.admin.api.dto.SelfUserProfileRespDTO;
import com.tce.smart.admin.api.dto.UserDTO;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.api.vo.UserVO;
import com.tce.smart.admin.service.SysUserService;
import com.tce.smart.common.core.constant.CommonConstants;
import com.tce.smart.common.core.constant.enums.ExceptionType;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.log.annotation.SysLog;
import com.tce.smart.common.security.util.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.stream.Collectors;

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
    public Result<SelfUserInfoRespDTO> info() {
        String username = SecurityUtils.getUser().getUsername();
        SysUser user = userService.getOne(Wrappers.<SysUser>query()
                .lambda()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDelFlag, CommonConstants.STATUS_NORMAL));
        if (user == null) {
            throw new TCEException("获取当前用户信息失败");
        }
        return success(toSelfUserInfo(userService.findUserInfo(user)));
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
    @PreAuthorize("@pms.hasPermission('sys_user_edit')")
    public Result user(@PathVariable Integer id) {
        return success(toAdminProfile(userService.selectUserVoById(id)));
    }

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return
     */
    @GetMapping("/details/{username}")
    @PreAuthorize("@pms.hasPermission('sys_user_edit')")
    public Result user(@PathVariable String username) {
        SysUser condition = new SysUser();
        condition.setUsername(username);
        return success(toAdminProfile(userService.getOne(new QueryWrapper<>(condition))));
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

    /**
     * 分页查询用户
     *
     * @param page    参数集
     * @param userDTO 查询参数列表
     * @return 用户集合
     */
    @GetMapping("/page")
    @PreAuthorize("@pms.hasPermission('sys_user_edit')")
    public Result getUserPage(Page page, UserDTO userDTO) {
        log.info("查询用户数据:{}",userDTO.getUsername());
        IPage<UserVO> pageData = null;
        try {
            pageData = (IPage<UserVO>) userService.getUsersWithRolePage(page, userDTO);
        } catch (Exception e) {
            log.warn("查询用户数据出错",e);
        }
        if (pageData == null) {
            return success(null);
        }
        Page<AdminUserProfileRespDTO> responsePage = new Page<>(pageData.getCurrent(), pageData.getSize(), pageData.getTotal());
        responsePage.setRecords(pageData.getRecords().stream().map(this::toAdminProfile).collect(Collectors.toList()));
        return success(responsePage);
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
    @PreAuthorize("@pms.hasPermission('sys_user_edit')")
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
	 * @return 当前登录用户数量
	 */
	@GetMapping("/logged/count")
	public Result<Integer> loggedCount() {
		return success(userService.loggedCount());
	}

    /** 将完整用户实体收敛为当前会话所需投影，避免密码和第三方标识进入浏览器。 */
    private SelfUserInfoRespDTO toSelfUserInfo(UserInfo userInfo) {
        SysUser user = userInfo.getSysUser();
        SelfUserProfileRespDTO profile = new SelfUserProfileRespDTO();
        profile.setUserId(user.getUserId());
        profile.setUsername(user.getUsername());
        profile.setFullName(user.getFullName());
        profile.setPhone(user.getPhone());
        profile.setAvatar(user.getAvatar());
        profile.setDeptId(user.getDeptId());

        SelfUserInfoRespDTO response = new SelfUserInfoRespDTO();
        response.setProfile(profile);
        response.setPermissions(userInfo.getPermissions());
        response.setRoles(userInfo.getRoles());
        response.setSalaryTypeName(userInfo.getSalaryTypeName());
        return response;
    }

    /** 管理端用户视图同样不得返回 UserVO 中的密码、salt 或第三方标识。 */
    private AdminUserProfileRespDTO toAdminProfile(UserVO source) {
        if (source == null) {
            return null;
        }
        AdminUserProfileRespDTO response = new AdminUserProfileRespDTO();
        response.setUserId(source.getUserId());
        response.setUsername(source.getUsername());
        response.setFullName(source.getFullName());
        response.setPhone(source.getPhone());
        response.setAvatar(source.getAvatar());
        response.setDeptId(source.getDeptId());
        response.setDeptName(source.getDeptName());
        response.setLockFlag(source.getLockFlag());
        response.setRoleList(source.getRoleList());
        response.setParkList(source.getParkList());
        return response;
    }

    private AdminUserProfileRespDTO toAdminProfile(SysUser source) {
        if (source == null) {
            return null;
        }
        AdminUserProfileRespDTO response = new AdminUserProfileRespDTO();
        response.setUserId(source.getUserId());
        response.setUsername(source.getUsername());
        response.setFullName(source.getFullName());
        response.setPhone(source.getPhone());
        response.setAvatar(source.getAvatar());
        response.setDeptId(source.getDeptId());
        response.setLockFlag(source.getLockFlag());
        return response;
    }
}
