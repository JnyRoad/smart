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
	/**
	 * 默认公众号模板名：历史上全系统所有业务推送都借用「访客出入园提醒」模板壳。
	 * 旧 sendMsg 签名继续用它以保持 18 个存量调用点行为不变；
	 * 新业务请走 sendTemplateMsg 显式传模板名（可用模板清单需运维确认）。
	 */
	public static final String DEFAULT_TEMPLATE_NAME = "访客出入园提醒";
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
	 * 发送微信消息（旧签名，行为不变：displayName 取 loginName、走默认模板）。
	 * 注意 loginName 双重职责的历史包袱：既是中转服务查 openId 的路由键，
	 * 又直接显示在模板 thing18 字段——需要姓名展示的场景请改用 sendTemplateMsg。
	 *
	 * @param loginName 登录名（工号，同时作为 thing18 展示值）
	 * @param remark 消息备注（正文，thing 字段超 20 字会被微信截断）
	 * @param openId 微信OpenId
	 * @param url 跳转链接
	 * @return 发送结果
	 */
	public static Boolean sendMsg(String loginName, String remark, String openId, String url) {
		return sendTemplateMsg(DEFAULT_TEMPLATE_NAME, loginName, remark, loginName, openId, url);
	}

	/**
	 * 发送微信模板消息（模板名参数化，显示名与路由工号解耦）。
	 *
	 * @param templateName 公众号模板名（中转服务按名称匹配模板）
	 * @param displayName 展示名（落 thing18 字段，空则显示「系统通知」）
	 * @param body 正文（落 thing14 字段，超 20 字会被微信截断，调用方自行控制长度）
	 * @param loginName 路由工号（中转服务据此查 openId，可空）
	 * @param openId 微信OpenId（与 loginName 二选一，可空）
	 * @param url 跳转链接
	 * @return 发送结果（失败/异常一律返回 false，不抛出——18 个存量调用点依赖此契约）
	 */
	public static Boolean sendTemplateMsg(String templateName, String displayName,
			String body, String loginName, String openId, String url) {
		try {
			log.info("【微信推送开始】templateName: {}, loginName: {}", templateName, loginName);

			// 参数校验
			validateParameters(body);

			// 构建消息数据
			JSONObject dataObj = buildMessageData(displayName, body);

			// 构建请求参数
			JSONObject parameter = buildRequestParameter(templateName, loginName, openId, url, dataObj);

			// 发送HTTP请求
			String result = sendHttpRequest(parameter);

			// 解析响应结果
			return parseResponse(result);

		} catch (Exception e) {
			log.error("微信消息发送异常: templateName={}, loginName={}, error={}", templateName, loginName, e.getMessage(), e);
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
	 * 构建消息数据（包内可见以便单测直接验证字段落位）
	 */
	static JSONObject buildMessageData(String displayName, String body) {
		// 构建展示名字段（历史字段名 thing18 语义是「访客姓名」，借用为通用展示名）
		JSONObject userNameObj = JSONUtil.createObj();
		if (StrUtil.isNotEmpty(displayName)) {
			userNameObj.put("value", displayName);
		} else {
			userNameObj.put("value", DEFAULT_SYSTEM_NOTICE);
		}

		// 构建时间字段
		JSONObject timeObj = JSONUtil.createObj();
		timeObj.put("value", DateUtils.convert(LocalDateTime.now()));

		// 构建正文字段
		JSONObject remarkObj = JSONUtil.createObj();
		remarkObj.put("value", body);

		// 组装数据对象
		JSONObject dataObj = JSONUtil.createObj();
		dataObj.put(FIELD_THING18, userNameObj);
		dataObj.put(FIELD_TIME4, timeObj);
		dataObj.put(FIELD_THING14, remarkObj);

		return dataObj;
	}

	/**
	 * 构建请求参数（包内可见以便单测直接验证模板名参数化）
	 */
	static JSONObject buildRequestParameter(String templateName, String loginName,
			String openId, String url, JSONObject dataObj) {
		JSONObject parameter = JSONUtil.createObj();

		if (StrUtil.isNotEmpty(loginName)) {
			parameter.put("loginName", loginName);
		}
		if (StrUtil.isNotEmpty(openId)) {
			parameter.put("openId", openId);
		}

		parameter.put("url", url);
		parameter.put("templateName", templateName);
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
