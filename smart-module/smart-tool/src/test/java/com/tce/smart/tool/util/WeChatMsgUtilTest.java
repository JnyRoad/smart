package com.tce.smart.tool.util;

import cn.hutool.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * WeChatMsgUtil 参数化单测：只测消息体/请求参数构建（纯函数），不发真实 HTTP。
 * 背景：模板名原先写死「访客出入园提醒」，本次把模板名与「显示名/路由工号」解耦，
 * 旧 sendMsg 签名必须保持行为逐字节不变（全系统 17 个调用点依赖）。
 */
public class WeChatMsgUtilTest {

	/** displayName 应落到 thing18，body 应落到 thing14，time4 非空（发送时刻） */
	@Test
	public void buildMessageData_displayNameAndBodyLandOnCorrectFields() {
		JSONObject data = WeChatMsgUtil.buildMessageData("张三", "保密权限下发完成 成功3/共5");

		assertEquals("张三", data.getJSONObject("thing18").getStr("value"));
		assertEquals("保密权限下发完成 成功3/共5", data.getJSONObject("thing14").getStr("value"));
		assertFalse(data.getJSONObject("time4").getStr("value").isEmpty());
	}

	/** displayName 为空时沿用「系统通知」兜底（旧行为） */
	@Test
	public void buildMessageData_emptyDisplayNameFallsBackToSystemNotice() {
		JSONObject data = WeChatMsgUtil.buildMessageData(null, "正文");

		assertEquals("系统通知", data.getJSONObject("thing18").getStr("value"));
	}

	/** templateName 参数应落到请求的 templateName 字段；loginName/openId 有值才写入（旧行为） */
	@Test
	public void buildRequestParameter_templateNameIsParameterized() {
		JSONObject data = WeChatMsgUtil.buildMessageData("张三", "正文");
		JSONObject param = WeChatMsgUtil.buildRequestParameter(
				"某新模板", "8056297", null, "http://example.com", data);

		assertEquals("某新模板", param.getStr("templateName"));
		assertEquals("8056297", param.getStr("loginName"));
		assertFalse("openId 为空不应写入", param.containsKey("openId"));
		assertEquals("http://example.com", param.getStr("url"));
	}

	/** 默认模板名常量必须保持「访客出入园提醒」——17 个旧调用点靠它维持现状 */
	@Test
	public void defaultTemplateNameUnchanged() {
		assertEquals("访客出入园提醒", WeChatMsgUtil.DEFAULT_TEMPLATE_NAME);
	}
}
