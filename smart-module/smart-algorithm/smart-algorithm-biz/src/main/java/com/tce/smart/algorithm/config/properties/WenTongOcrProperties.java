package com.tce.smart.algorithm.config.properties;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: WenTongOcrProperties
 * @Package com.tce.smart.yunxun.algorithm.config.properties
 * @Description:
 * @Author wuxinjian
 * @Date 2019-10-10 14:22
 * @Version V1.0
 */
@Data
public class WenTongOcrProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文通接口地址
     */
    private String url;

    /**
     * 文通配置白名单
     */
    private String username;
}
