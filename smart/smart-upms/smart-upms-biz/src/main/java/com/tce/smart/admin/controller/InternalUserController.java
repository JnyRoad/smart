package com.tce.smart.admin.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tce.smart.admin.api.dto.InternalUserLoginRespDTO;
import com.tce.smart.admin.api.dto.InternalUserPhoneSyncReqDTO;
import com.tce.smart.admin.api.dto.InternalUserSummaryRespDTO;
import com.tce.smart.admin.api.dto.InternalParkAdminProvisionReqDTO;
import com.tce.smart.admin.api.dto.InternalParkAdminUpdateReqDTO;
import com.tce.smart.admin.api.dto.RoleDTO;
import com.tce.smart.admin.api.dto.UserDTO;
import com.tce.smart.admin.api.dto.UserInfo;
import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.service.SysSocialDetailsService;
import com.tce.smart.admin.service.SysUserService;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.core.wrapper.BaseController;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
/**
 * 内部服务专用的用户资料接口。
 *
 * <p>旧 {@code /api/user/**} 路由禁止继续承载员工和用户资料；每个新端点均要求 server scope、受管 client_id
 * 和用途头，避免任何普通用户 token 或泛用服务令牌读取登录资料。</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/user")
public class InternalUserController extends BaseController {

    private static final String AUTHENTICATION_PURPOSE = "user-authentication";
    private static final String PLATFORM_MANAGEMENT_PURPOSE = "platform-user-management";
    private static final String APP_PHONE_SYNC_PURPOSE = "app-user-phone-sync";
    private static final String PLATFORM_PHONE_SYNC_PURPOSE = "platform-user-phone-sync";
    private static final String PLATFORM_OFFBOARD_PURPOSE = "platform-user-offboard";

    private final SysUserService userService;
    private final SysSocialDetailsService socialDetailsService;
    private final OpenApiAuthenticationAdapter openApiAuthenticationAdapter;

    /** 认证服务 client_id 由受管 Nacos/环境配置提供，缺失时 fail-closed。 */
    @Value("${security.inner.user.auth-client-id:}")
    private String authServiceClientId;

    /** Platform 服务 client_id 预留给后续管理类内部命令，缺失时 fail-closed。 */
    @Value("${security.inner.user.platform-client-id:}")
    private String platformServiceClientId;

    /** App 服务 client_id 预留给后续本人手机号同步命令，缺失时 fail-closed。 */
    @Value("${security.inner.user.app-client-id:}")
    private String appServiceClientId;

    /**
     * 认证链按账号读取必要的登录投影；不返回 {@code UserInfo} 或 {@code SysUser} 实体。
     */
    @Inner
    @OpenApi("server")
    @GetMapping("/login/username/{username}")
    public Result<InternalUserLoginRespDTO> getLoginUserByUsername(@PathVariable String username,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose) {
        assertManagedCaller(from, purpose, AUTHENTICATION_PURPOSE, authServiceClientId);
        SysUser user = userService.getOne(Wrappers.<SysUser>query().lambda().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new TCEException("登录用户不存在");
        }
        return success(toLoginProjection(user));
    }

    /** 认证链按手机号查找登录投影，结果不包含被查询手机号。 */
    @Inner
    @OpenApi("server")
    @GetMapping("/login/mobile/{mobile}")
    public Result<InternalUserLoginRespDTO> getLoginUserByMobile(@PathVariable String mobile,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose) {
        assertManagedCaller(from, purpose, AUTHENTICATION_PURPOSE, authServiceClientId);
        SysUser user = userService.getOne(Wrappers.<SysUser>query().lambda().eq(SysUser::getPhone, mobile));
        if (user == null) {
            throw new TCEException("登录用户不存在");
        }
        return success(toLoginProjection(user));
    }

    /** 社交认证仅向认证服务返回最小登录投影。 */
    @Inner
    @OpenApi("server")
    @GetMapping("/login/social/{inStr}")
    public Result<InternalUserLoginRespDTO> getLoginUserBySocial(@PathVariable String inStr,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose) {
        assertManagedCaller(from, purpose, AUTHENTICATION_PURPOSE, authServiceClientId);
        UserInfo userInfo = socialDetailsService.getUserInfo(inStr);
        if (userInfo == null || userInfo.getSysUser() == null) {
            throw new TCEException("登录用户不存在");
        }
        return success(toLoginProjection(userInfo));
    }

    /** 手机验证码认证只返回是否可登录，不泄露用户资料。 */
    @Inner
    @OpenApi("server")
    @GetMapping("/login/mobile-eligible/{mobile}")
    public Result<Boolean> verifyMobileForLogin(@PathVariable String mobile,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose) {
        assertManagedCaller(from, purpose, AUTHENTICATION_PURPOSE, authServiceClientId);
        return success(userService.verifyMobile(mobile));
    }

    /** 外部账号密码校验只能由认证服务的服务令牌发起。 */
    @Inner
    @OpenApi("server")
    @GetMapping("/login/password")
    public Result<Boolean> verifyPasswordLogin(@RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose) {
        assertManagedCaller(from, purpose, AUTHENTICATION_PURPOSE, authServiceClientId);
        return success(userService.simpleLogin(username, password));
    }

    /** 工号登录初始化只允许认证服务调用。 */
    @Inner
    @OpenApi("server")
    @GetMapping("/login/badge/{badge}")
    public Result<Boolean> provisionBadgeLogin(@PathVariable String badge,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose) {
        assertManagedCaller(from, purpose, AUTHENTICATION_PURPOSE, authServiceClientId);
        return success(userService.socialLogin(badge));
    }

    /** Platform 管理流程仅取得用户 ID 与角色名称，不返回完整账号实体。 */
    @Inner
    @OpenApi("server")
    @GetMapping("/summary/{username}")
    public Result<InternalUserSummaryRespDTO> getSummary(@PathVariable String username,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose) {
        assertManagedCaller(from, purpose, PLATFORM_MANAGEMENT_PURPOSE, platformServiceClientId);
        SysUser user = userService.getOne(Wrappers.<SysUser>query().lambda().eq(SysUser::getUsername, username));
        if (user == null) {
            return success((InternalUserSummaryRespDTO) null);
        }
        UserInfo userInfo = userService.findUserInfo(user);
        InternalUserSummaryRespDTO response = new InternalUserSummaryRespDTO();
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        List<RoleDTO> roleList = userInfo == null ? null : userInfo.getRoleList();
        response.setRoleNames(roleList == null ? Collections.emptyList()
                : roleList.stream().map(RoleDTO::getRoleName).collect(Collectors.toList()));
        return success(response);
    }

    /** Platform 创建园区企业管理员，命令字段固定为账号、密码、园区与已选择的角色。 */
    @Inner
    @OpenApi("server")
    @PostMapping("/platform-admin")
    public Result<Boolean> provisionPlatformAdmin(@Valid @RequestBody InternalParkAdminProvisionReqDTO request,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose) {
        assertManagedCaller(from, purpose, PLATFORM_MANAGEMENT_PURPOSE, platformServiceClientId);
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(request.getUsername());
        userDTO.setPassword(request.getPassword());
        userDTO.setRole(Collections.singletonList(request.getRoleId()));
        userDTO.setPark(Collections.singletonList(request.getParkId()));
        return success(userService.saveUser(userDTO));
    }

    /** Platform 只能更新园区企业管理员的账号、密码与园区归属。 */
    @Inner
    @OpenApi("server")
    @PostMapping("/platform-admin/update")
    public Result<Boolean> updatePlatformAdmin(@Valid @RequestBody InternalParkAdminUpdateReqDTO request,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose) {
        assertManagedCaller(from, purpose, PLATFORM_MANAGEMENT_PURPOSE, platformServiceClientId);
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(request.getUserId());
        userDTO.setUsername(request.getUsername());
        userDTO.setPassword(request.getPassword());
        userDTO.setPark(Collections.singletonList(request.getParkId()));
        return success(userService.updateUser(userDTO));
    }

    /** App 本人手机号同步使用最小请求并由受管 App 服务客户端发起。 */
    @Inner
    @OpenApi("server")
    @PostMapping("/phone/app")
    public Result<Boolean> syncAppPhone(@Valid @RequestBody InternalUserPhoneSyncReqDTO request,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose) {
        assertManagedCaller(from, purpose, APP_PHONE_SYNC_PURPOSE, appServiceClientId);
        return syncPhone(request);
    }

    /** Platform 的员工资料同步使用独立用途，避免 App client 横向更新其他员工账号。 */
    @Inner
    @OpenApi("server")
    @PostMapping("/phone/platform")
    public Result<Boolean> syncPlatformPhone(@Valid @RequestBody InternalUserPhoneSyncReqDTO request,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose) {
        assertManagedCaller(from, purpose, PLATFORM_PHONE_SYNC_PURPOSE, platformServiceClientId);
        return syncPhone(request);
    }

    /** Platform 离职或组织管理员删除只接受账号名和独立下线用途。 */
    @Inner
    @OpenApi("server")
    @PostMapping("/platform/delete/{username}")
    public Result<Boolean> deletePlatformUser(@PathVariable String username,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose) {
        assertManagedCaller(from, purpose, PLATFORM_OFFBOARD_PURPOSE, platformServiceClientId);
        SysUser user = userService.getOne(Wrappers.<SysUser>query().lambda().eq(SysUser::getUsername, username));
        if (user == null) {
            return success(Boolean.FALSE);
        }
        Result<Boolean> deleteResult = userService.deleteUserById(user);
        return success(deleteResult != null && Boolean.TRUE.equals(deleteResult.getData()));
    }

    private InternalUserLoginRespDTO toLoginProjection(SysUser user) {
        return toLoginProjection(userService.findUserInfo(user));
    }

    private InternalUserLoginRespDTO toLoginProjection(UserInfo userInfo) {
        SysUser user = userInfo.getSysUser();
        InternalUserLoginRespDTO response = new InternalUserLoginRespDTO();
        response.setUserId(user.getUserId());
        response.setDeptId(user.getDeptId());
        response.setUsername(user.getUsername());
        response.setPasswordHash(user.getPassword());
        response.setLockFlag(user.getLockFlag());
        response.setRoleIds(userInfo.getRoles());
        response.setPermissions(userInfo.getPermissions());
        response.setSalaryTypeName(userInfo.getSalaryTypeName());
        response.setParkIds(userService.listUserPark(user.getUserId()));
        return response;
    }

    private Result<Boolean> syncPhone(InternalUserPhoneSyncReqDTO request) {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(request.getUsername());
        userDTO.setPhone(request.getPhone());
        Result<Boolean> result = userService.updateUserInfo(userDTO);
        return success(result != null && result.isSuccess() && Boolean.TRUE.equals(result.getData()));
    }

    /**
     * {@link Inner} 只验证来源约定，真正的调用方认证还必须校验纯 client_credentials、client_id 与用途。
     */
    private void assertManagedCaller(String from, String purpose, String expectedPurpose, String expectedClientId) {
        Authentication authentication = SecurityUtils.getAuthentication();
        if (!SecurityConstants.FROM_IN.equals(from) || StrUtil.isBlank(expectedClientId)
                || !expectedPurpose.equals(purpose) || authentication == null
                || !openApiAuthenticationAdapter.isClientOnly(authentication)
                || !expectedClientId.equals(openApiAuthenticationAdapter.clientId(authentication))) {
            throw new AccessDeniedException("内部用户资料调用未获授权");
        }
    }
}
