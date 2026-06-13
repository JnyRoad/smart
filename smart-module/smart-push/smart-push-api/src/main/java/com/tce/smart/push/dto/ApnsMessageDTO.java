package com.tce.smart.push.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * apns透传数据
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/7/2 .
 * @Modified By:
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApnsMessageDTO extends PushMessageDTO{
    private static final long serialVersionUID = -1635996488501435372L;
    /**
     * apns token
     */
    private String deviceToken;
}
