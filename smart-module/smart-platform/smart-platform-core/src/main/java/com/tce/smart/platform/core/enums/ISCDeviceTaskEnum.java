package com.tce.smart.platform.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sunfujian
 * @date 2021/8/26 15:29
 */
@Getter
@AllArgsConstructor
public enum ISCDeviceTaskEnum {
	DEVICE_OK(200, "操作成功"),
	AUTH_CONFIG_OK(201, "权限配置成功"),
	AUTH_CONFIG_DOWN_OK(202, "任务已提交"),
	DEVICE_DEVICE_NOT_ONLINE(413, "设备不在线"),
	DEVICE_RESP_ERROR(501, "ISC平台响应参数错误"),
	ADD_PERSON_ERROR(502, "添加人员至ISC平台异常"),
	ADD_FACE_ERROR(503, "添加人脸图片至ISC平台异常"),
	UPDATE_FACE_ERROR(504, "更新人脸图片至ISC平台异常"),
	AUTH_CONFIG_ERROR(505, "权限配置添加异常"),
	AUTH_CONFIG_DOWN_FAIL(506, "下载权限失败"),
	FACE_IMG_NOT_EXIST(507, "人脸图片不存在"),
	FACE_IMG_SIZE_ERROR(508, "人脸图片大小不符合要求"),
	PERSON_NOT_EXIST(509, "人员信息不存在"),
	PERSON_HAVE_RESIGNED(510, "人员已离职"),
	DEVICE_ERROR(500, "未知异常");

	private Integer code;

	private String desc;
}
