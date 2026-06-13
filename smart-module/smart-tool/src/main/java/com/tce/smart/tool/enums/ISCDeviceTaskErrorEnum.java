package com.tce.smart.tool.enums;

import com.tce.smart.common.core.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

/**
 * @author
 * @date
 */
@Getter
@AllArgsConstructor
public enum ISCDeviceTaskErrorEnum {
	SUCCESS(-1, "0", "操作成功"),
	SERVICE_EXCEPTION(1, "0x15400001", "服务错误：服务异常"),
	REQUIRED_FIELD_EMPTY(22, "0x15400002", "参数错误：必填字段不能为空"),
	FIELD_VALIDATION_FAILED(23, "0x15400003", "参数错误：字段合法性校验不通过"),
	RESOURCE_NOT_FOUND(24, "0x15404001", "资源异常：资源不存在"),
	SCHEDULE_TEMPLATE_ID_NOT_FOUND(25, "0x15401001", "计划模板错误：计划模板ID不存在"),
	HOLIDAY_GROUP_ID_NOT_FOUND(26, "0x15401002", "计划模板错误：假日组ID不存在"),
	SCHEDULE_TEMPLATE_NAME_DUPLICATED(27, "0x15401003", "计划模板错误：计划模板名称重复"),
	HOLIDAY_GROUP_NAME_DUPLICATED(28, "0x15401004", "计划模板错误：假日组名称重复"),
	TASK_NOT_FOUND(29, "0x15405001", "任务操作错误：任务不存在"),
	TASK_ALREADY_DOWNLOADING(5, "0x15405002", "任务操作错误：任务已开始下载"),
	TASK_DELETE_FAILED(30, "0x15405003", "任务操作错误：删除任务失败"),
	TASK_STOP_FAILED(31, "0x15405004", "任务操作错误：停止任务失败"),
	TASK_PAUSE_FAILED(32, "0x15405005", "任务操作错误：暂停任务失败"),
	TASK_ADD_DOWNLOAD_DATA_FAILED(6, "0x15405006", "任务操作错误：添加待下载数据失败"),
	DOWNLOAD_NO_AVAILABLE_DATA(7, "0x15403007", "下载错误：无可用数据下载"),
	DOWNLOAD_PERMISSION_PACKET_SEND_FAILED(8, "0x15403008", "下载错误：权限报文下发失败"),
	DOWNLOAD_ACCESS_SERVICE_OFFLINE(9, "0x15403009", "下载错误：设备接入服务不在线或连接失败"),
	DOWNLOAD_DEVICE_OFFLINE(10, "0x1540300a", "下载错误：设备不在线或网络不通"),
	DOWNLOAD_DEVICE_BUSY(11, "0x1540300b", "下载错误：设备正在下载"),
	DOWNLOAD_DRIVER_ADDRESS_QUERY_FAILED(12, "0x1540300c", "下载错误：查询设备的驱动地址失败"),
	DOWNLOAD_CARD_PERMISSION_UNSUPPORTED(33, "0x1540300d", "下载错误：不支持卡片权限"),
	DOWNLOAD_FINGERPRINT_PERMISSION_UNSUPPORTED(34, "0x1540300e", "下载错误：不支持指纹权限"),
	DOWNLOAD_FACE_PERMISSION_UNSUPPORTED(35, "0x1540300f", "下载错误：不支持人脸权限"),
	DOWNLOAD_DEVICE_CAPACITY_FULL(13, "0x15403010", "下载错误：设备容量已满"),
	DOWNLOAD_PERSON_CARD_NOT_OPENED(14, "0x15403011", "下载错误：人员没有开卡"),
	DOWNLOAD_PERSON_NO_FINGERPRINT(36, "0x15403012", "下载错误：人员没有指纹"),
	DOWNLOAD_PERSON_NO_FACE_IMAGE(16, "0x15403013", "下载错误：人员没有人脸图片"),
	DOWNLOAD_FACE_IMAGE_ADDRESS_QUERY_FAILED(15, "0x15403014", "下载错误：获取人脸图片地址失败"),
	CALLBACK_DRIVER_RESULT_TIMEOUT(37, "0x15403501", "回调错误：驱动结果响应超时"),
	CALLBACK_DOWNLOAD_RECORD_PROCESS_FAILED(38, "0x15403502", "回调错误：下载记录处理异常"),
	CALLBACK_SCHEDULE_TEMPLATE_DOWNLOAD_FAILED(39, "0x15403503", "回调错误：计划模板下载失败"),
	CALLBACK_CLEAR_PERMISSION_FAILED(40, "0x15403504", "回调错误：清空权限失败"),
	CALLBACK_DEVICE_DOWNLOAD_TIMEOUT(19, "0x15403505", "回调错误：设备下载超时"),
	CALLBACK_PERMISSION_DATA_INVALID(17, "0x15403506", "回调错误：权限数据非法"),
	CALLBACK_CARD_NUMBER_INVALID(41, "0x15403507", "回调错误：卡号错误"),
	CALLBACK_BIOMETRIC_ALREADY_EXISTS(42, "0x15403508", "回调错误：指纹/人脸已存在"),
	CALLBACK_BIOMETRIC_QUALITY_POOR(43, "0x15403509", "回调错误：指纹/人脸质量差"),
	CALLBACK_IMAGE_SERVER_NOT_BOUND(44, "0x1540350a", "回调错误：设备未绑定图片服务器"),
	CALLBACK_FACE_IMAGE_DOWNLOAD_FAILED(45, "0x1540350b", "回调错误：下载人脸图片失败"),
	CALLBACK_FACE_MODELING_FAILED(46, "0x1540350c", "回调错误：人脸建模失败"),
	CALLBACK_FACE_EYE_DISTANCE_TOO_SMALL(20, "0x1540350d", "回调错误：人脸眼间距太小"),
	CALLBACK_CARD_PERMISSION_NOT_ISSUED(47, "0x1540350e", "回调错误：未下发卡权限"),
	CALLBACK_UNKNOWN_REASON(18, "0x1540350f", "回调错误：未知原因"),
	IMAGE_INFO_DECODE_FAILED(21, "0x15403518", "图片信息解码失败"),
	CALLBACK_FACE_IMAGE_INVALID(48, "0x15403519", "回调错误：人脸图片不符合要求"),
	UNKNOWN_DEVICE_ERROR(00, "0000", "未知异常");

	private Integer code;

	private String errorCode;

	private String desc;

	public boolean matchesErrorCode(String errorCode) {
		return this.errorCode.equals(normalizeErrorCode(errorCode));
	}

	public static Optional<ISCDeviceTaskErrorEnum> fromInternalCode(Integer code) {
		if (code != null) {
			for (ISCDeviceTaskErrorEnum error : ISCDeviceTaskErrorEnum.values()) {
				if (error.code.equals(code)) {
					return Optional.of(error);
				}
			}
		}
		return Optional.empty();
	}

	public static Optional<ISCDeviceTaskErrorEnum> fromErrorCode(String errorCode) {
		if (StringUtils.isNotEmpty(errorCode)) {
			for (ISCDeviceTaskErrorEnum error : ISCDeviceTaskErrorEnum.values()) {
				if (error.matchesErrorCode(errorCode)) {
					return Optional.of(error);
				}
			}
		}
		return Optional.empty();
	}

	public static Optional<ISCDeviceTaskErrorEnum> fromDesc(String desc) {
		if (StringUtils.isNotEmpty(desc)) {
			for (ISCDeviceTaskErrorEnum error : ISCDeviceTaskErrorEnum.values()) {
				if (error.desc.equals(desc)) {
					return Optional.of(error);
				}
			}
		}
		return Optional.empty();
	}

	public static String descriptionForErrorCode(String errorCode) {
		return fromErrorCode(errorCode)
				.map(ISCDeviceTaskErrorEnum::getDesc)
				.orElse("ISC返回未知错误");
	}

	@Deprecated
	public static ISCDeviceTaskErrorEnum desc(Integer code) {
		return fromInternalCode(code).orElse(null);
	}

	@Deprecated
	public static String errorCode(String errorCode) {
		return descriptionForErrorCode(errorCode);
	}

	@Deprecated
	public static Integer code(String desc) {
		return fromDesc(desc).map(ISCDeviceTaskErrorEnum::getCode).orElse(null);
	}

	private static String normalizeErrorCode(String errorCode) {
		return errorCode == null ? "" : errorCode.trim().toLowerCase();
	}
}
