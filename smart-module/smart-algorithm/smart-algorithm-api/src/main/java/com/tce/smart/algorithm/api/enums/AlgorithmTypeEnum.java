package com.tce.smart.algorithm.api.enums;

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
public enum AlgorithmTypeEnum {
    /**
     * 文通OCR
     */
    OCR_WENTONG("ocr_wentong", "文通OCR识别"),
    /**
     * 阿里OCR
     */
    OCR_ALI("ocr_ali", "阿里OCR识别"),
	/**
	 * 飞搜人脸检测
	 */
	FACEDETECT_FACEALL("facedetect_faceall", "飞搜人脸检测"),
	/**
	 * 中科视拓人脸检测
	 */
	FACEDETECT_SEETA("facedetect_seeta", "中科视拓人脸检测"),
    /**
     * 百度静默活体
     */
	LIVENESS_STATIC_BAIDU_OFFLINE("liveness_static_baidu_offline", "百度离线静默活体"),
	/**
	 * 中科视拓静默活体检测
	 */
	LIVENESS_STATIC_SEETA("liveness_static_seeta", "中科视拓静默活体"),
	/**
	 * 中科视拓动态活体
	 */
	LIVENESS_DYNAMIC_SEETA("liveness_dynamic_seeta", "中科视拓动态活体"),
	/**
     * 百度人像比对
     */
    COMPARE_BAIDU_OFFLINE("compare_baidu_offline", "百度离线人像比对"),
	/**
	 * 飞搜人像比对
	 */
	COMPARE_FACEALL("compare_faceall", "飞搜人像比对"),
	/**
	 * 中科视拓比对
	 */
	COMPARE_SEETA("compare_seeta", "中科视拓比对"),

	/**
	 * 人脸特征值
	 */
	FACE_FEATURES_EXTRACT("face_features_extract", "中科视拓比对"),

    UNKNOWN("","未知");

    private String type;
	private String name;

    public static AlgorithmTypeEnum algorithmType(String type){
        if(Objects.nonNull(type)){
            for(AlgorithmTypeEnum algorithmTypeEnum : AlgorithmTypeEnum.values()){
                if(algorithmTypeEnum.getType().equals(type)){
                    return algorithmTypeEnum;
                }
            }
        }
        return AlgorithmTypeEnum.UNKNOWN;
    }
    public static String name(String type){
        return algorithmType(type).getName();
    }

}
