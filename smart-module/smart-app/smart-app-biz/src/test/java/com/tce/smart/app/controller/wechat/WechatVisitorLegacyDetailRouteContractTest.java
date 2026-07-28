package com.tce.smart.app.controller.wechat;

import org.junit.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;

/** 已下线微信公众号不得恢复按访客 ID 查询详情的公开路由。 */
public class WechatVisitorLegacyDetailRouteContractTest {
	@Test
	public void legacyDetailRoutesAreNotMapped() {
		for (Method method : WechatVisitController.class.getDeclaredMethods()) {
			GetMapping mapping = method.getAnnotation(GetMapping.class);
			if (mapping == null) {
				continue;
			}
			for (String route : mapping.value()) {
				assertFalse("旧公众号详情路由不得恢复", "/record/detail".equals(route));
				assertFalse("旧公众号详情路由不得恢复", "/record/detailById/{id}".equals(route));
			}
		}
	}

	@Test
	public void legacyPhotoLookupRouteIsNotMapped() {
		for (Method method : WechatVisitController.class.getDeclaredMethods()) {
			PostMapping mapping = method.getAnnotation(PostMapping.class);
			if (mapping == null) {
				continue;
			}
			for (String route : mapping.value()) {
				assertFalse("旧公众号不得按任意图片 ID 返回照片 URL", "/getFace".equals(route));
			}
		}
	}
}
