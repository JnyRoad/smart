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
public enum FaceTypeEnum {
    /**
     * 证件照
     */
    CERT("CERT", "证件照"),
    /**
     * 活体照
     */
    LIVE("LIVE", "活体照"),

    UNKNOWN("","未知");

    private String type;
    private String desc;

    public static FaceTypeEnum faceType(String type){
        if(Objects.nonNull(type)){
            for(FaceTypeEnum algorithmTypeEnum : FaceTypeEnum.values()){
                if(algorithmTypeEnum.getType().equals(type)){
                    return algorithmTypeEnum;
                }
            }
        }
        return FaceTypeEnum.UNKNOWN;
    }
    public static String desc(String type){
        return faceType(type).getDesc();
    }
    public static String tag(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(FaceTypeEnum algorithmTypeEnum : FaceTypeEnum.values()){
                if(algorithmTypeEnum.getDesc().equals(desc)){
                    return algorithmTypeEnum.getType();
                }
            }
        }
        return "";
    }

}
