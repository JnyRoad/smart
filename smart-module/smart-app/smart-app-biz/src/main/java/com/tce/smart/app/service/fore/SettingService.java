package com.tce.smart.app.service.fore;

import com.tce.smart.app.vo.fore.CheckVersionVo;

/**
 * App设置服务接口
 *
 * @author mkwu
 * @date 2019-07-03
 */
public interface SettingService {

	/**
	 * 检查App版本
	 *
	 *
	 * @param appId appId
	 * @param appVersion App当前版本号
	 * @return 最新版本信息
	 */
	CheckVersionVo checkVersion(String appId, String appVersion);

	/**
	 * 验证原有电话号码
	 * @param mobile
	 * @param smsCode
	 * @return
	 */
	boolean verifyOldMobile(String mobile,String smsCode);

	/***
	 * 发送手机验证码
	 * @param mobile
	 * @return
	 */
	boolean sendMobileMsg(String mobile);

	/***
	 * 保存新手机号码
	 * @param mobile
	 * @param smsCode
	 * @return
	 */
	boolean updateNewPhone(String mobile,String smsCode);

}
