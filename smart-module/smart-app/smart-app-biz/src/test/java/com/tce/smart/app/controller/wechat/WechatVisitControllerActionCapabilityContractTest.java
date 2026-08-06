package com.tce.smart.app.controller.wechat;

import org.junit.Test;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * 两条匿名访客校验路由必须显式接收一次性 action capability 与草稿标识。
 *
 * 仅靠 Nacos 精确放行不足以保护匿名入口；缺少任一请求头时，App 无法把请求交给
 * Platform 原子消费，因而不得进入原有业务服务。
 */
public class WechatVisitControllerActionCapabilityContractTest {

	@Test
	public void anonymousFaceAndBlacklistRoutesRequireCapabilityAndDraftHeaders() {
		assertActionHeaders("checkFace");
		assertActionHeaders("checkBlackVisitor");
	}

	private void assertActionHeaders(String methodName) {
		Method target = null;
		for (Method method : WechatVisitController.class.getMethods()) {
			if (methodName.equals(method.getName())) {
				target = method;
				break;
			}
		}
		if (target == null) {
			fail("缺少访客校验路由: " + methodName);
		}
		assertEquals("访客匿名校验必须接收 capability、draftId 和业务请求体", 3,
				target.getParameterCount());
		assertRequestHeader(target.getParameterAnnotations()[0], "X-Visitor-Action-Capability");
		assertRequestHeader(target.getParameterAnnotations()[1], "X-Visitor-Draft-Id");
	}

	private void assertRequestHeader(Annotation[] annotations, String expectedHeader) {
		for (Annotation annotation : annotations) {
			if (annotation instanceof RequestHeader) {
				assertEquals(expectedHeader, ((RequestHeader) annotation).value());
				return;
			}
		}
		fail("缺少请求头: " + expectedHeader);
	}
}
