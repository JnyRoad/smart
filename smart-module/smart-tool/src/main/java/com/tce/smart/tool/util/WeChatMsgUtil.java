package com.tce.smart.tool.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tce.smart.common.core.util.DateUtils;
import com.tce.smart.tool.constant.SymbolConstants;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;

/**
 * 微信消息接口工具类
 * @author: Li.JiaJun
 * @since: 2021/8/5 9:11
 */
@Slf4j
public class WeChatMsgUtil {

	/**
	 * 微信消息推送相关常量
	 */
	private static final String WECHAT_API_URL = "https://xchr.szyuto.com:8888/commonData/getData.html";
	private static final String API_KEY = "insertTemplateMsg";
	private static final String TEMPLATE_NAME = "访客出入园提醒";
	private static final String DEFAULT_SYSTEM_NOTICE = "系统通知";
	private static final String ENCODING_UTF8 = "utf-8";

	/**
	 * 微信模板消息字段常量
	 */
	private static final String FIELD_THING18 = "thing18";
	private static final String FIELD_TIME4 = "time4";
	private static final String FIELD_THING14 = "thing14";

	/**
	 * 响应状态码
	 */
	private static final String SUCCESS_CODE = "1";

	/**
	 * 发送微信消息
	 *
	 * @param loginName 登录名
	 * @param remark 消息备注
	 * @param openId 微信OpenId
	 * @param url 跳转链接
	 * @return 发送结果
	 */
	public static Boolean sendMsg(String loginName, String remark, String openId, String url) {
		try {
			log.info("【微信推送开始】loginName: {}", loginName);

			// 参数校验
			validateParameters(remark);

			// 构建消息数据
			JSONObject dataObj = buildMessageData(loginName, remark);

			// 构建请求参数
			JSONObject parameter = buildRequestParameter(loginName, openId, url, dataObj);

			// 发送HTTP请求
			String result = sendHttpRequest(parameter);

			// 解析响应结果
			return parseResponse(result);

		} catch (Exception e) {
			log.error("微信消息发送异常: loginName={}, error={}", loginName, e.getMessage(), e);
			return Boolean.FALSE;
		}
	}

	/**
	 * 参数校验
	 */
	private static void validateParameters(String remark) {
		if (StrUtil.isEmpty(remark)) {
			throw new IllegalArgumentException("消息内容不能为空");
		}
	}

	/**
	 * 构建消息数据
	 */
	private static JSONObject buildMessageData(String loginName, String remark) {
		// 构建用户名字段
		JSONObject userNameObj = JSONUtil.createObj();
		if (StrUtil.isNotEmpty(loginName)) {
			userNameObj.put("value", loginName);
		} else {
			userNameObj.put("value", DEFAULT_SYSTEM_NOTICE);
		}

		// 构建时间字段
		JSONObject timeObj = JSONUtil.createObj();
		timeObj.put("value", DateUtils.convert(LocalDateTime.now()));

		// 构建备注字段
		JSONObject remarkObj = JSONUtil.createObj();
		remarkObj.put("value", remark);

		// 组装数据对象
		JSONObject dataObj = JSONUtil.createObj();
		dataObj.put(FIELD_THING18, userNameObj);
		dataObj.put(FIELD_TIME4, timeObj);
		dataObj.put(FIELD_THING14, remarkObj);

		return dataObj;
	}

	/**
	 * 构建请求参数
	 */
	private static JSONObject buildRequestParameter(String loginName, String openId, String url, JSONObject dataObj) {
		JSONObject parameter = JSONUtil.createObj();

		if (StrUtil.isNotEmpty(loginName)) {
			parameter.put("loginName", loginName);
		}
		if (StrUtil.isNotEmpty(openId)) {
			parameter.put("openId", openId);
		}

		parameter.put("url", url);
		parameter.put("templateName", TEMPLATE_NAME);
		parameter.put("data", dataObj);

		return parameter;
	}

	/**
	 * 发送HTTP请求
	 */
	private static String sendHttpRequest(JSONObject parameter) {
		String encodeParam = encodeParameter(parameter);

		log.info("微信推送请求开始");

		String requestUrl = WECHAT_API_URL + "?key=" + API_KEY + "&parameter=" + encodeParam;

		String result = HttpRequest.post(requestUrl)
				.timeout(10000) // 设置10秒超时
				.execute()
				.body();

		log.info("微信推送响应完成");
		return result;
	}

	/**
	 * 参数编码
	 */
	private static String encodeParameter(JSONObject parameter) {
		try {
			return URLEncoder.encode(JSONUtil.toJsonStr(parameter), ENCODING_UTF8);
		} catch (UnsupportedEncodingException e) {
			log.error("参数编码异常: {}", e.getMessage(), e);
			// 编码失败时返回原始JSON字符串
			return JSONUtil.toJsonStr(parameter);
		}
	}

	/**
	 * 解析响应结果
	 */
	private static Boolean parseResponse(String result) {
		if (StrUtil.isEmpty(result)) {
			log.warn("微信推送响应结果为空");
			return Boolean.FALSE;
		}

		try {
			JSONObject res = JSONUtil.parseObj(result);
			String code = res.getStr("code");

			if (SUCCESS_CODE.equals(code)) {
				log.info("微信消息发送成功");
				return Boolean.TRUE;
			} else {
				log.warn("微信消息发送失败, 响应码: {}, 响应消息: {}", code, res.getStr("message"));
				return Boolean.FALSE;
			}
		} catch (Exception e) {
			log.error("解析微信推送响应异常: {}", e.getMessage(), e);
			return Boolean.FALSE;
		}
	}
}
