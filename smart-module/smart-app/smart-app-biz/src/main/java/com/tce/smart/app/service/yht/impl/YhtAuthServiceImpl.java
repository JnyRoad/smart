package com.tce.smart.app.service.yht.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.app.service.yht.YhtAuthService;
import com.tce.smart.app.utils.SignHelper;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.data.api.feign.dhrview.RemoteYutoDhrYsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 友互通授权服务
 * @author sunfujian
 * @since 2021/10/12 9:47
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class YhtAuthServiceImpl implements YhtAuthService {

	@Value("${spring.yht.appKey}")
	private String yhtAppKey;

	@Value("${spring.yht.appSecret}")
	private String yhtAppSecret;

	@Value("${spring.yht.accessTokenUrl}")
	private String accessTokenUrl;

	@Value("${spring.yht.userIdUrl}")
	private String userIdUrl;

	@Value("${spring.yht.userInfoUrl}")
	private String userInfoUrl;

	private final CacheManager cacheManager;

	private final RemoteYutoDhrYsService remoteYutoDhrYsService;

	@Override
	public String getAccessToken() {
		Cache yhtDetails = cacheManager.getCache("yht_details");
		Long tokenExpire = Objects.requireNonNull(yhtDetails).get("token_expire_time", Long.class);
		long afterOneMinute = LocalDateTime.now().plusMinutes(1L).toEpochSecond(ZoneOffset.of("+8"));
		if (tokenExpire != null && afterOneMinute < tokenExpire) {
			return yhtDetails.get("access_token", String.class);
		}
		Map<String, String> params = new HashMap<>();
		String timestamp = String.valueOf(System.currentTimeMillis());
		// 除签名外的其他参数
		params.put("appKey", yhtAppKey);
		params.put("timestamp", timestamp);
		try {
			// 计算签名
			String signature = SignHelper.sign(params, yhtAppSecret);
			params.put("signature", signature);
		} catch (Exception e) {
			log.error("签名异常：", e);
			throw new SmartException("签名异常");
		}

		String url = accessTokenUrl.replaceAll("APPKEY", yhtAppKey)
				.replaceAll("TIMESTAMP", timestamp)
				.replaceAll("SIGNATURE", params.get("signature"));
		log.info("请求友互通access_token");
		String result = HttpUtil.get(url);

		JSONObject resultObj = JSONUtil.parseObj(result);
		String code = resultObj.getStr("code");
		if (!"00000".equals(code)) {
			log.info("获取友互通access_token失败,code:{}, msg:{}", code, resultObj.getStr("message"));
			throw new SmartException("获取友互通access_token失败");
		}
		JSONObject dataObj = resultObj.getJSONObject("data");
		// 获取到的凭证
		String accessToken = dataObj.getStr("access_token");
		// 凭证有效时间，单位：秒
		Integer expiresIn = dataObj.getInt("expire");
		yhtDetails.put("access_token", accessToken);
		yhtDetails.put("token_expire_time", LocalDateTime.now().plusSeconds(expiresIn).toEpochSecond(ZoneOffset.of("+8")));
		return accessToken;
	}

	@Override
	public String getUserId(String accessToken, String code) {
		String url = userIdUrl.replaceAll("ACCESS_TOKEN", accessToken).replaceAll("CODE", code);
/*		log.info("友互通获取userId请求URL:{}", url);*/
		String result = HttpUtil.get(url);
		log.info("友互通获取userId响应报文:{}", result);

		JSONObject resultObj = JSONUtil.parseObj(result);
		String respCode = resultObj.getStr("code");
		if (!"00000".equals(respCode)) {
			log.info("获取友互通userId失败,code:{}, msg:{}", respCode, resultObj.getStr("message"));
			throw new SmartException("获取友互通userId失败");
		}
		JSONObject dataObj = resultObj.getJSONObject("data");
		return dataObj.getStr("yhtUserId");
	}

	@Override
	public JSONObject getUserInfo(String accessToken, String userId) {
		String url = userInfoUrl.replaceAll("ACCESS_TOKEN", accessToken);
		Map<String, Object> params = new HashMap<>(1);
		params.put("userIds", new String[]{userId});

		String result = HttpUtil.post(url, params);
		log.info("友互通获取用户信息响应报文:{}", result);

		JSONObject resultObj = JSONUtil.parseObj(result);
		String respCode = resultObj.getStr("code");
		if (!"200".equals(respCode)) {
			log.info("获取友互通用户信息失败,code:{}, msg:{}", respCode, resultObj.getStr("message"));
			throw new SmartException("获取友互通用户信息失败");
		}
		JSONArray dataArr = resultObj.getJSONArray("data");
		if (dataArr == null || dataArr.size() == 0) {
			return null;
		}
		return dataArr.getJSONObject(0);
	}

	@Override
	public String getBadgeByUserId(String userId) {
		Result<String> result = remoteYutoDhrYsService.getBadgeByUserId(userId, SecurityConstants.FROM_IN,
				SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
		log.info("根据userId查询工号响应：{}", result);
		if (!result.isSuccess()) {
			throw new SmartException("获取工号失败");
		}
		if (StrUtil.isBlank(result.getData())) {
			throw new SmartException("暂未绑定用友APP，请先绑定");
		}
		return result.getData();
	}

	@Override
	public String getUserBadge(String code) {
		return getBadgeByUserId(getUserId(getAccessToken(), code));
	}
}
