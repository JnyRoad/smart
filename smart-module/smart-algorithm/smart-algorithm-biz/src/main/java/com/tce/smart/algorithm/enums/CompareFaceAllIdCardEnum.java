package com.tce.smart.algorithm.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @author wxjason
 */
@Getter
@AllArgsConstructor
public enum CompareFaceAllIdCardEnum {
    /**
     * 身份证照片
     */
    ID_CARD(1, "身份证照片"),
    NOT_ID_CARD(0, "非身份证照片");

    private Integer code;
    private String desc;

    public static CompareFaceAllIdCardEnum result(Integer code) {
        if (Objects.nonNull(code)) {
            for (CompareFaceAllIdCardEnum t : CompareFaceAllIdCardEnum.values()) {
                if (Objects.nonNull(t.code) && t.code.equals(code)) {
                    return t;
                }
            }
        }
        return NOT_ID_CARD;
    }

    public static String desc (Integer code) {
        CompareFaceAllIdCardEnum faceApiResult = result(code);
        return Objects.nonNull(faceApiResult) ? faceApiResult.getDesc() : "";
    }
}
