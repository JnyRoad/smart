package com.tce.smart.tool.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * @Author: Liu.jihong
 * @Date: 2020/9/27 16:12
 */
@AllArgsConstructor
@Getter
public enum AdmittanceTypeEnum {

	PERSON(1,"入场申请"),

	CAR(2,"货车预约");
	private Integer code;

	private String desc;

	public static AdmittanceTypeEnum cameraEnum(Integer code){
		return Arrays.stream(AdmittanceTypeEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().orElse(null);
	}
	public static String desc(Integer code){
		return Arrays.stream(AdmittanceTypeEnum.values()).filter(e -> e.getCode().equals(code)).findFirst().map(AdmittanceTypeEnum::getDesc).orElse(null);
	}

	public static Integer code(String desc){
		return Arrays.stream(AdmittanceTypeEnum.values()).filter(e -> e.getDesc().equals(desc)).findFirst().map(AdmittanceTypeEnum::getCode).orElse(null);
	}
}
