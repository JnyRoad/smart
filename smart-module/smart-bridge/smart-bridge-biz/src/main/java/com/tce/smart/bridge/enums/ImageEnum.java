package com.tce.smart.bridge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Description: TODO
 * @ProjectName smart-dispatcher
 * @ClassName: ImageEnum
 * @Author jinbo
 * @Date 2019/11/6
 */
@Getter
@AllArgsConstructor
public enum ImageEnum {
	CAMERA(0, "摄像头抓拍"),
	ACCESS(1, "门禁抓拍"),
	GATE(2, "道闸抓拍"),
	VISITOR(3, "访客机抓拍"),
	BORDER(4, "防越界机抓拍"),
	UNKNOWN(-1, "未知");

	private Integer type;
	private String desc;
}
