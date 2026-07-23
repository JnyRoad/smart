package com.tce.smart.push.controller;

import cn.hutool.core.util.StrUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.util.SecurityUtils;
import com.tce.smart.push.constant.PushConstants;
import com.tce.smart.push.dto.ApnsMessageDTO;
import com.tce.smart.push.dto.NoticeMessageDTO;
import com.tce.smart.push.dto.PushMessageDTO;
import com.tce.smart.push.service.PushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/7/2 .
 * @Modified By:
 */
@Slf4j
@RestController
@RequestMapping("/push")
public class PushController {

    @Autowired
    private PushService pushService;

    @Autowired
    private OpenApiAuthenticationAdapter openApiAuthenticationAdapter;

    /** Platform 调用推送服务的专属 client_id；受管配置缺失时必须拒绝。 */
    @Value("${security.inner.push.platform-client-id:}")
    private String platformServiceClientId;

    @PostMapping("/notice")
    @Inner
    @OpenApi("server")
    public Result notice(@RequestBody NoticeMessageDTO noticeMessageDTO,
            @RequestHeader(SecurityConstants.FROM) String from){
        assertPlatformCaller(from);
        Result result = Result.builder().code(HttpStatus.BAD_REQUEST.value()).data("").build();
        try {
            boolean success  = pushService.notice(noticeMessageDTO);
            if(success) {
				result.setCode(HttpStatus.OK.value());
			}
        }catch (IllegalArgumentException ae){
            result.setMsg(ae.getMessage());
            log.warn("{}",PushConstants.PushDesc.PUSH_ILLEGAL_ARG_ERROR,ae);
        }
        catch (Exception e) {
            result.setMsg(PushConstants.PushDesc.PUSH_NOTICE_ERROR);
            log.warn("{}",PushConstants.PushDesc.PUSH_NOTICE_ERROR,e);
        }
        return result;
    }

    @PostMapping("/transmission")
    @Inner
    @OpenApi("server")
    @ResponseBody
    public Result transmission(@RequestBody ApnsMessageDTO apnsMessageDTO,
            @RequestHeader(SecurityConstants.FROM) String from){
        assertPlatformCaller(from);
        Result result = Result.builder().code(HttpStatus.BAD_REQUEST.value()).build();
        try {
            boolean success = pushService.transmission(apnsMessageDTO);
            if(success)
                result.setCode(HttpStatus.OK.value());
        }catch (IllegalArgumentException ae){
            result.setMsg(ae.getMessage());
            log.warn("{}",PushConstants.PushDesc.PUSH_ILLEGAL_ARG_ERROR,ae);
        }
        catch (Exception e) {
            result.setMsg(PushConstants.PushDesc.PUSH_TRANSMISSION_ERROR);
            log.warn("{}",PushConstants.PushDesc.PUSH_TRANSMISSION_ERROR,e);
        }
        return result;
    }

    @PostMapping("/pushAll")
    @Inner
    @OpenApi("server")
    @ResponseBody
    public Result pushAll(@RequestBody PushMessageDTO pushMessageDTO,
            @RequestHeader(SecurityConstants.FROM) String from){
        // 当前没有已核实的业务调用方，保留路径仅用于明确拒绝历史流量，禁止任何服务令牌触发全量广播。
        throw new AccessDeniedException("全量推送接口已关闭，待注册专属调用方后重新启用");
    }

    /**
     * {@link Inner} 只校验内部来源约定；单播还必须绑定 Platform 的纯服务令牌，避免任意 server scope 横向发信。
     */
    private void assertPlatformCaller(String from) {
        Authentication authentication = SecurityUtils.getAuthentication();
        if (!SecurityConstants.FROM_IN.equals(from) || StrUtil.isBlank(platformServiceClientId)
                || authentication == null || !openApiAuthenticationAdapter.isClientOnly(authentication)
                || !platformServiceClientId.equals(openApiAuthenticationAdapter.clientId(authentication))) {
            throw new AccessDeniedException("推送内部调用未获授权");
        }
    }

}
