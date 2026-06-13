package com.tce.smart.app.service.yht;

import cn.hutool.json.JSONObject;

/**
 * @author sunfujian
 * @since 2021/10/12 9:46
 */
public interface YhtAuthService {

	/**
	 * 获取access_token
	 * @return
	 */
	String getAccessToken();

	/**
	 * 根据access_token和code获取userId
	 * @param accessToken
	 * @param code
	 * @return
	 */
	String getUserId(String accessToken, String code);

	/**
	 * 根据access_token和userId获取用户信息
	 * @param accessToken
	 * @param userId
	 * @return
	 */
	JSONObject getUserInfo(String accessToken, String userId);

	/**
	 * 根据userId从DHR系统获取工号
	 * @param userId
	 * @return
	 */
	String getBadgeByUserId(String userId);

	/**
	 * 根据授权code获取用户工号
	 * @param code
	 * @return
	 */
	String getUserBadge(String code);
}
