package com.tce.smart.platform.api.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 车辆卡片删除信息
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/18 .
 * @Modified By:
 */
@Data
@NoArgsConstructor
public class FaceSearchDTO implements Serializable {

    private static final long serialVersionUID = -8472474903351635282L;
    /**
     * 园区id
     */
    private Integer parkId;

    /**
     * 园区名
     */
    private String parkName;

    /**
     * 人员id
     */
    private Long personId;

    /**
     * 人员类型(员工、访客)
     */
    private Integer type;

    /**
     * 性别(1:男;2:女)
     */
    private Integer sex;

    /**
     * 人员姓名
     */
    private String name;

    /**
     * 人员身份证号
     */
    private String identityCard;

    /**
     * 人脸base64图片数据
     */
    private String faceBase64;

}
