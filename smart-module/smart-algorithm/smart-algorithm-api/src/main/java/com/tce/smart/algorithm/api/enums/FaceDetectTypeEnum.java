package com.tce.smart.algorithm.api.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @Description: 算法类型枚举
 * @ProjectName smart-platform
 * @ClassName: DeleteEnum
 * @Author jinbo
 * @Date 2019/6/21
 */
@Getter
@AllArgsConstructor
public enum FaceDetectTypeEnum {
    /**
     * 提取特征值
     */
	FACE_FEATURE(1, "人脸特征提取"),
    /**
     * 阿里OCR
     */
    FACE_CUT(2, "人脸图片裁剪");

    private Integer type;
	private String name;

    public static FaceDetectTypeEnum faceDetectType(Integer type){
        if(Objects.nonNull(type)){
            for(FaceDetectTypeEnum algorithmTypeEnum : FaceDetectTypeEnum.values()){
                if(algorithmTypeEnum.getType().equals(type)){
                    return algorithmTypeEnum;
                }
            }
        }
        return null;
    }
    public static String name(Integer type){
	FaceDetectTypeEnum faceDetectTypeEnum = faceDetectType(type);
        return Objects.nonNull(faceDetectTypeEnum) ? faceDetectTypeEnum.getName() : StringUtils.EMPTY;
    }

}
