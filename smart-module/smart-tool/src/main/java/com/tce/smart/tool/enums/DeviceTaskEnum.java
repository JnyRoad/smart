package com.tce.smart.tool.enums;


import com.tce.smart.common.core.util.StringUtils;

import java.util.Objects;

/**
 *  任务下发
 * @author Lenovo
 *
 */
public enum DeviceTaskEnum {

	DEVICE_OK(200, "操作成功"),
	DEVICE_PARSE_JSON_FAIL(401, "解析JSON失败"),
	DEVICE_INVALID_PARAM(402, "参数错误"),
	DEVICE_DEVICE_ERROR(403, "设备错误"),
	DEVICE_OPEN_RTSP_FAIL(404, "打开RTSP码流失败"),
	DEVICE_UNKOWN_OPERATION(405, "未知操作"),
	DEVICE_DEVICE_HAS_EXIST(406, "设备已存在"),
	DEVICE_DEVICE_NOT_EXIST(407, "设备不存在"),
	DEVICE_TYPE_NOT_SUPPORT(408, "不支持该设备类型"),
	DEVICE_USER_HAS_LOCKED(409, "账户被锁定"),
	DEVICE_USER_OR_PASSWORD_ERROR(410, "用户名密码错误"),
	DEVICE_CONNECT_DEVICE_FAIL(411, "连接设备失败"),
	DEVICE_LOGIN_DEVICE_TIMEOUT(412, "登录设备超时"),
	DEVICE_DEVICE_NOT_ONLINE(413, "设备不在线"),
	DEVICE_NOT_SUPPORT_FACE_SNAP(414, "设备不支持人脸抓拍功能"),
	DEVICE_DATABASE_OPT_ERROR(415, "数据库操作失败"),
	DEVICE_NOT_SUPPORT_FUNCTION(416, "设备不支持该功能"),
	DEVICE_PARKING_HAS_EXIST(417, "停车场已存在"),
	DEVICE_PARKING_NOT_EXIST(418, "停车场不存在"),
	DEVICE_VEHICLE_HAS_EXIST(419, "车辆信息已存在"),
	DEVICE_VEHICLE_NOT_EXIST(420, "车辆信息不存在"),
	DEVICE_SUBSCRIBE_EVENT_FAIL(421, "订阅事件失败"),
	DEVICE_OPRATION_NEED_RETRY(422, "需要重试"),
	DEVICE_FACE_FORMAT_ERROR(423, "图片格式不支持"),
	DEVICE_FACE_QUALITY_LOW(424, "人脸质量太差"),
	DEVICE_FACE_IS_FULL(425, "人脸数量已满"),
	DEVICE_FACE_HAS_EXIST(426, "人脸已存在"),
	DEVICE_FACE_EYES_DISTANCE_SMALL(427, "人眼间距太小"),
	DEVICE_FACE_PIXEL_ERROR(428, "人脸像素过高或过低"),
	DEVICE_FACE_NOT_FOUND_FACE(429, "未检测到人脸"),
	DEVICE_NOT_SUPPORT_VENDOR(430, "不支持该厂家设备"),
	DEVICE_SERVER_INTER_ERROR(500, "服务器内部错误"),
	BRIGE_ERROR(400, "调取C++接口异常"),
	REPEATED_ISSUANCE(300, "重复处理异常");

    private final Integer code;

    private final String desc;

    DeviceTaskEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static DeviceTaskEnum deviceAuthority(Integer code){
		for(DeviceTaskEnum alarmType : DeviceTaskEnum.values()){
			if(alarmType.code.equals(code)){
				return alarmType;
			}
		}
        return null;
    }

    public static String desc(Integer code){
	DeviceTaskEnum alarmType = deviceAuthority(code);
        return alarmType == null ? "未知异常" : deviceAuthority(code).desc;
    }

    public static Integer code(String desc){
        if(StringUtils.isNotEmpty(desc)){
            for(DeviceTaskEnum deviceAuthority : DeviceTaskEnum.values()){
                if(deviceAuthority.desc.equals(desc)){
                    return deviceAuthority.code;
                }
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
