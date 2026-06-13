package com.tce.smart.bridge.core.enums;

import cn.hutool.core.util.StrUtil;
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
public enum ExceptionEnum {

	UNKNOWN(-1, "未知状态码"),
	CHECK_SUCCESS(200, "验证成功"),
	SAVE_SUCCESS(200, "保存成功"),
	OPERATE_SUCCESS(200, "操作成功"),
	CHECK_FAILD(500, "验证失败"),
	SAVE_FAILD(500, "保存失败"),
	OPERATE_ERROR(500, "操作失败"),
	SERVER_ERROR(500, "服务异常"),
	/**
	 * 登录
	 */
	PHONE_UNREGISTERED(500101, "手机号未注册"),
	PHONE_FREQUENTLY_ERROR(500102, "验证码发送过频繁"),

	LOGOUT_ERROR_TOKEN_EMPTY(500103, "退出失败，Token为空"),
	LOGOUT_ERROR_TOKEN_INVALID(500104, "退出失败，token 无效"),
	CAPTCHA_ILLEGAL(500105, "验证码不合法"),
	CAPTCHA_EMPTY(500106, "验证码不能为空"),
	CAPTCHA_INVALID(500107, "验证码无效"),
	CAPTCHA_ERROR(500108, "验证码错误"),
	PHONE_SEND_ERROR(500109, "验证码发送失败"),
	/**
	 * Excel
	 */
	EXCEL_UPLOAD_FAIL_TITLE_ERROR(20001, "上传失败，模板不正确"),
	EXCEL_CONTENT_EMPTY(20002, "Excel中内容为空"),

	PERSON_CANNOT_DELETE(600001,"用户存在警报人员配置，不能删除!"),
	CONFIG_CANNOT_OPERATE(600002,"系统默认配置，不能修改！"),
	PERSON_TYPE_CANNOT_UPDATE(600003,"不能修改人员类型！"),
	DEVICE_REMOTE_DELETE_FAIL(600004,"设备端处理失败，删除失败！"),
	DEVICE_REMOTE_ADD_FAIL(600005,"设备端处理失败，新增失败！"),
	DEVICE_REMOTE_UPDATE_FAIL(600006,"设备端处理失败，修改失败！"),
	EXIST_SAME_IP_DEVICE(600007,"已存在相同IP的设备!"),
	EXIST_SAME_NAME_DEVICE(600008,"已存在相同名称的设备!"),
	EXIST_SAME_NAME_AREA(600009,"已存在相同名称的区域!"),
	EXIST_DEVICE_UNDER_AREA(600010,"该区域或子区域下绑定了设备,不能删除!");


	private Integer code;
	private String desc;

	public static ExceptionEnum exception(Integer code) {
		if (Objects.nonNull(code)) {
			for (ExceptionEnum exception : ExceptionEnum.values()) {
				if (exception.getCode().equals(code)) {
					return exception;
				}
			}
		}
		return ExceptionEnum.UNKNOWN;
	}

	public static String desc(Integer code) {
		return exception(code).getDesc();
	}

	public static Integer code(String desc) {
		if (StrUtil.isNotEmpty(desc)) {
			for (ExceptionEnum exception : ExceptionEnum.values()) {
				if (exception.getDesc().equals(desc)) {
					return exception.getCode();
				}
			}
		}
		return null;
	}

}
