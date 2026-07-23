package com.tce.smart.admin.api.feign;

import com.tce.smart.admin.api.dto.InternalParkAdminProvisionReqDTO;
import com.tce.smart.admin.api.dto.InternalParkAdminUpdateReqDTO;
import com.tce.smart.admin.api.dto.InternalUserLoginRespDTO;
import com.tce.smart.admin.api.dto.InternalUserPhoneSyncReqDTO;
import com.tce.smart.admin.api.dto.InternalUserSummaryRespDTO;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 受服务令牌保护的 UPMS 用户内部契约。
 *
 * <p>每个默认方法都显式声明 {@code from=Y}、服务令牌标记和固定用途，避免调用方重新退化为仅传来源头。</p>
 */
@FeignClient(value = ServiceNameConstants.UMPS_SERVICE)
public interface RemoteUserInternalService {

    String PURPOSE_AUTHENTICATION = "user-authentication";
    String PURPOSE_PLATFORM_MANAGEMENT = "platform-user-management";
    String PURPOSE_APP_PHONE_SYNC = "app-user-phone-sync";
    String PURPOSE_PLATFORM_PHONE_SYNC = "platform-user-phone-sync";
    String PURPOSE_PLATFORM_OFFBOARD = "platform-user-offboard";

    @GetMapping("/internal/user/login/username/{username}")
    Result<InternalUserLoginRespDTO> loginUserByUsername(@PathVariable("username") String username,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose);

    default Result<InternalUserLoginRespDTO> loginUserByUsername(String username) {
        return loginUserByUsername(username, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
                PURPOSE_AUTHENTICATION);
    }

    @GetMapping("/internal/user/login/mobile/{mobile}")
    Result<InternalUserLoginRespDTO> loginUserByMobile(@PathVariable("mobile") String mobile,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose);

    default Result<InternalUserLoginRespDTO> loginUserByMobile(String mobile) {
        return loginUserByMobile(mobile, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
                PURPOSE_AUTHENTICATION);
    }

    @GetMapping("/internal/user/login/social/{inStr}")
    Result<InternalUserLoginRespDTO> loginUserBySocial(@PathVariable("inStr") String inStr,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose);

    default Result<InternalUserLoginRespDTO> loginUserBySocial(String inStr) {
        return loginUserBySocial(inStr, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
                PURPOSE_AUTHENTICATION);
    }

    @GetMapping("/internal/user/login/mobile-eligible/{mobile}")
    Result<Boolean> verifyMobileForLogin(@PathVariable("mobile") String mobile,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose);

    default Result<Boolean> verifyMobileForLogin(String mobile) {
        return verifyMobileForLogin(mobile, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
                PURPOSE_AUTHENTICATION);
    }

    @GetMapping("/internal/user/login/password")
    Result<Boolean> verifyPasswordLogin(@RequestParam("username") String username, @RequestParam("password") String password,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose);

    default Result<Boolean> verifyPasswordLogin(String username, String password) {
        return verifyPasswordLogin(username, password, SecurityConstants.FROM_IN,
                SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED, PURPOSE_AUTHENTICATION);
    }

    @GetMapping("/internal/user/login/badge/{badge}")
    Result<Boolean> provisionBadgeLogin(@PathVariable("badge") String badge,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose);

    default Result<Boolean> provisionBadgeLogin(String badge) {
        return provisionBadgeLogin(badge, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
                PURPOSE_AUTHENTICATION);
    }

    @GetMapping("/internal/user/summary/{username}")
    Result<InternalUserSummaryRespDTO> summary(@PathVariable("username") String username,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose);

    default Result<InternalUserSummaryRespDTO> summary(String username) {
        return summary(username, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
                PURPOSE_PLATFORM_MANAGEMENT);
    }

    @PostMapping("/internal/user/platform-admin")
    Result<Boolean> provisionPlatformAdmin(@RequestBody InternalParkAdminProvisionReqDTO request,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose);

    default Result<Boolean> provisionPlatformAdmin(InternalParkAdminProvisionReqDTO request) {
        return provisionPlatformAdmin(request, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
                PURPOSE_PLATFORM_MANAGEMENT);
    }

    @PostMapping("/internal/user/platform-admin/update")
    Result<Boolean> updatePlatformAdmin(@RequestBody InternalParkAdminUpdateReqDTO request,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose);

    default Result<Boolean> updatePlatformAdmin(InternalParkAdminUpdateReqDTO request) {
        return updatePlatformAdmin(request, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
                PURPOSE_PLATFORM_MANAGEMENT);
    }

    @PostMapping("/internal/user/phone/app")
    Result<Boolean> syncAppPhone(@RequestBody InternalUserPhoneSyncReqDTO request,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose);

    default Result<Boolean> syncAppPhone(InternalUserPhoneSyncReqDTO request) {
        return syncAppPhone(request, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
                PURPOSE_APP_PHONE_SYNC);
    }

    @PostMapping("/internal/user/phone/platform")
    Result<Boolean> syncPlatformPhone(@RequestBody InternalUserPhoneSyncReqDTO request,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose);

    default Result<Boolean> syncPlatformPhone(InternalUserPhoneSyncReqDTO request) {
        return syncPlatformPhone(request, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
                PURPOSE_PLATFORM_PHONE_SYNC);
    }

    @PostMapping("/internal/user/platform/delete/{username}")
    Result<Boolean> deletePlatformUser(@PathVariable("username") String username,
            @RequestHeader(SecurityConstants.FROM) String from,
            @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth,
            @RequestHeader("X-Smart-Internal-Purpose") String purpose);

    default Result<Boolean> deletePlatformUser(String username) {
        return deletePlatformUser(username, SecurityConstants.FROM_IN, SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED,
                PURPOSE_PLATFORM_OFFBOARD);
    }
}
