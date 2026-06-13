package com.tce.smart.platform.api.dto.resp.remoteLock;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author sunfujian
 * @since 2021/9/16 15:46
 */
@Data
public class DeviceTypeInfoDTO implements Serializable {

    /**
     * 是否适用密码 1.适用 0.不适用
     */
    private Integer applyPwd;

    /**
     * 是否适用刷卡 1.适用 0.不适用
     */
    private Integer applyCard;

    /**
     * 是否适用指纹 1.适用 0.不适用
     */
    private Integer applyFinger;
}
