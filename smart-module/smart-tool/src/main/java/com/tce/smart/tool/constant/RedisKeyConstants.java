package com.tce.smart.tool.constant;

/**
 * redis信息key枚举
 *
 * @author Lenovo
 *
 */
public interface RedisKeyConstants {

	/**
	 * app模块信息
	 */
	String SMAT_APP = "smart_app:";

	/**
	 * app模块-手机app信息
	 */
	String SMAT_APP_APP = "smart_app:app:";

	/**
	 * app模块-手机wechat信息
	 */
	String SMAT_APP_WECHAT = "smart_app:wechat:";

	/**
	 * app模块-手机smscode信息
	 */
	String SMAT_APP_WECHAT_SMSCODE = "smart_app:wechat:auth:sms:code:";

	/**
	 * app模块-手机设备信息
	 */
	String SMAT_APP_DEVICE = "smart_app:device:";

	/**
	 * 智能锁修改密码限制key
	 */
	String SMAT_LOCK_UPDATE_PASSWORD_LIMIT = "smart_app:lock:update:password:";
}
