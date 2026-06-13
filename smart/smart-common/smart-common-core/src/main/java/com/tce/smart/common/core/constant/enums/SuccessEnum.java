package com.tce.smart.common.core.constant.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

/**
 * @Description: TODO
 * @ProjectName smart-platform
 * @ClassName: SuccessEnum
 * @Author jinbo
 * @Date 2019/6/21
 */
@Getter
@AllArgsConstructor
public enum SuccessEnum {
    FAIL(0, "失败"),

    SUCCESS(1, "成功"),

    UNKNOWN(-1, "未知");

    private Integer code;
    private String desc;

    public static SuccessEnum success(Boolean status) {
        if (Objects.nonNull(status)) {
            return status ? SUCCESS : FAIL;
        }
        return SuccessEnum.UNKNOWN;
    }

    public static SuccessEnum success(Integer code) {
        if (Objects.nonNull(code)) {
            for (SuccessEnum success : SuccessEnum.values()) {
                if (success.getCode().equals(code)) {
                    return success;
                }
            }
        }
        return SuccessEnum.UNKNOWN;
    }

    public static String desc(Integer code) {
        return success(code).getDesc();
    }

    public static Integer code(String desc) {
        if (StringUtils.isNotEmpty(desc)) {
            for (SuccessEnum success : SuccessEnum.values()) {
                if (success.getDesc().equals(desc)) {
                    return success.getCode();
                }
            }
        }
        return null;
    }
}
