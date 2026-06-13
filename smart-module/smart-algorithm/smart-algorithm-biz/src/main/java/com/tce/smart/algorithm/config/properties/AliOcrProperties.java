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
public class AliOcrProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 阿里OCR识别应用键值
     */
    private String appKey;

    /**
     * 阿里OCR识别应用密钥
     */
    private String appSecret;
}
