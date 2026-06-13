package com.tce.smart.platform.api.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 人脸
 * @Author: yangxu.
 * @Description: TODO()
 * @Date:Created in 2019/4/30 .
 * @Modified By:
 */
@Data
@NoArgsConstructor
public class FaceSnapDTO implements Serializable {
    private static final long serialVersionUID = 2806293154348990213L;

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
     * 人脸图片ID
     */
    private String faceId;

    /**
     * 人脸base64图片数据
     */
    private String faceBase64;

    @Builder
    public FaceSnapDTO(Integer parkId, String parkName, Long personId, Integer type, Integer sex, String name, String identityCard, String faceId, String faceBase64) {
        this.parkId = parkId;
        this.parkName = parkName;
        this.personId = personId;
        this.type = type;
        this.sex = sex;
        this.name = name;
        this.identityCard = identityCard;
        this.faceId = faceId;
        this.faceBase64 = faceBase64;
    }
}
