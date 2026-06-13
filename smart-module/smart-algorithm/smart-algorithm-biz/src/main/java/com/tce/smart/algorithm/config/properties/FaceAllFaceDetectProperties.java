package com.tce.smart.algorithm.config.properties;

import lombok.Data;

import java.io.Serializable;

/**
 * @ClassName: SeeTaStaticLiveProperties
 * @Package com.tce.smart.yunxun.algorithm.config.properties
 * @Description:
 * @Author wuxinjian
 * @Date 2019-10-10 14:22
 * @Version V1.0
 */
@Data
public class FaceAllFaceDetectProperties implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 飞搜人脸检测接口地址
     */
    private String url;

}
