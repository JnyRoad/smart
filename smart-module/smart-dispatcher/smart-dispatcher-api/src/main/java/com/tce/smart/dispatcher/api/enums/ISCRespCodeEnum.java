package com.tce.smart.dispatcher.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sunfujian
 * @date 2021/8/27 14:40
 */
@Getter
@AllArgsConstructor
public enum ISCRespCodeEnum {
	CODE_0X14C00002(348127234, "该用户不存在"),
	CODE_0X00072202(467458, "资源异常：资源不存在"),
	CODE_0X14C02303(348136195, "资源不存在");

	private Integer code;
	private String desc;
}
