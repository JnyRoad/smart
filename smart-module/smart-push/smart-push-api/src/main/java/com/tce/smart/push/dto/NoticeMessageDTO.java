package com.tce.smart.push.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知消息数据
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/7/2 .
 * @Modified By:
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class NoticeMessageDTO extends PushMessageDTO {
    private static final long serialVersionUID = 8897979086463947398L;
    /**
     * cid
     */
    private String clientId;
}
