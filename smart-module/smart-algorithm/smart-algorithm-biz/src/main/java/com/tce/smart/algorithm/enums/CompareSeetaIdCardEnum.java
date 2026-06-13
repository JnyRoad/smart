package com.tce.smart.algorithm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author wxjason
 */
@Getter
@AllArgsConstructor
public enum CompareSeetaIdCardEnum {
    /**
     * 身份证照片
     */
    ID_CARD(1, "身份证照片"),
    NOT_ID_CARD(0, "非身份证照片");

    private Integer code;
    private String desc;

    public static CompareSeetaIdCardEnum result(Integer code) {
        if (Objects.nonNull(code)) {
            for (CompareSeetaIdCardEnum t : CompareSeetaIdCardEnum.values()) {
                if (Objects.nonNull(t.code) && t.code.equals(code)) {
                    return t;
                }
            }
        }
        return NOT_ID_CARD;
    }

    public static String desc (Integer code) {
        CompareSeetaIdCardEnum faceApiResult = result(code);
        return Objects.nonNull(faceApiResult) ? faceApiResult.getDesc() : "";
    }
}
