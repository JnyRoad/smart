package com.tce.smart.push.service;

import com.tce.smart.push.dto.ApnsMessageDTO;
import com.tce.smart.push.dto.NoticeMessageDTO;
import com.tce.smart.push.dto.PushMessageDTO;

/**
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/7/2 .
 * @Modified By:
 */
public interface PushService {
    /**
     * 推送-通知
     * @param noticeMessageDTO
     * @return
     */
    boolean notice(NoticeMessageDTO noticeMessageDTO);

    /**
     * 推送-透传
     * @param apnsMessageDTO
     * @return
     */
    boolean transmission(ApnsMessageDTO apnsMessageDTO);

    /**
     * 向所有用户推送消息
     * @param pushMessageDTO
     * @return
     */
    boolean pushAll(PushMessageDTO pushMessageDTO);
}
