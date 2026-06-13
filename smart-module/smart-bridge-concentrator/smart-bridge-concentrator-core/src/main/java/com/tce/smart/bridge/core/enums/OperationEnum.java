package com.tce.smart.bridge.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: TODO
 * @ProjectName smart-dispatcher
 * @ClassName: OperationEnum
 * @Author jinbo
 * @Date 2019/11/6
 */
@Getter
@AllArgsConstructor
public enum OperationEnum {
	GET(0, "get"),
	POST(1, "post"),
	KAFKA(2, "kafka"),
	UNKNOWN(-1, "未知");

	private Integer code;
	private String desc;
}
