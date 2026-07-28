package com.tce.smart.push.feign;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.constant.ServiceNameConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.push.dto.ApnsMessageDTO;
import com.tce.smart.push.dto.NoticeMessageDTO;
import com.tce.smart.push.dto.PushMessageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * 推送接口
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/7/2.
 * @Modified By:
 */
@FeignClient(value = ServiceNameConstants.PUSH_SERVICE)
public interface RemotePushService {

    /**
     * 单条消息通知,供androic推送调用
     * @param noticeMessageDTO
     * @param from
     * @return
     */
    @PostMapping("/push/notice")
    Result notice(@RequestBody NoticeMessageDTO noticeMessageDTO, @RequestHeader(SecurityConstants.FROM) String from,
                  @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

    /**
     * 单条消息透传,供ios推送调用
     * @param apnsMessageDTO
     * @param from
     * @return
     */
    @PostMapping("/push/transmission")
    Result transmission(@RequestBody ApnsMessageDTO apnsMessageDTO, @RequestHeader(SecurityConstants.FROM) String from,
                        @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);

    /**
     * 向所有用户推送消息
     * @param pushMessageDTO
     * @param from
     * @return
     */
    @PostMapping("/push/pushAll")
    Result pushAll(@RequestBody PushMessageDTO pushMessageDTO, @RequestHeader(SecurityConstants.FROM) String from,
                   @RequestHeader(SecurityConstants.INTERNAL_SERVICE_AUTH) String serviceAuth);
}
