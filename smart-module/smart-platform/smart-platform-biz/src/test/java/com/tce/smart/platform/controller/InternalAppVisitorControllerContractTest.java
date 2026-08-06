package com.tce.smart.platform.controller;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import com.tce.smart.platform.api.dto.req.AddFellowVisitorReqDTO;
import com.tce.smart.platform.api.feign.RemoteVisitorService;
import org.junit.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;

/**
 * App 只能通过内部最小契约按当前登录工号读取或修改本人关联的访客记录。
 */
public class InternalAppVisitorControllerContractTest {
	private static final String ACTOR_BADGE_HEADER = "X-Smart-Actor-Badge";
	private static final String ACTOR_PARK_IDS_HEADER = "X-Smart-Actor-Park-Ids";
	private static final String PURPOSE_HEADER = "X-Smart-Internal-Purpose";

	@Test
	public void appVisitorRoutesAreServerOnlyAndRequireActorHeader() throws Exception {
		Class<?> controller = Class.forName("com.tce.smart.platform.controller.InternalAppVisitorController");
		assertInternalRoute(controller, "detail", GetMapping.class, "/detail/{visitorId}");
		assertInternalRoute(controller, "addFellow", PostMapping.class, "/fellow");

		Method detail = controller.getMethod("detail", Long.class, String.class, String.class, String.class, String.class);
		assertHeader(detail, 1, ACTOR_BADGE_HEADER);
		assertHeader(detail, 2, ACTOR_PARK_IDS_HEADER);
		assertHeader(detail, 3, SecurityConstants.FROM);
		assertHeader(detail, 4, PURPOSE_HEADER);

		Method fellow = controller.getMethod("addFellow", AddFellowVisitorReqDTO.class, String.class, String.class, String.class, String.class);
		assertHeader(fellow, 1, ACTOR_BADGE_HEADER);
		assertHeader(fellow, 2, ACTOR_PARK_IDS_HEADER);
		assertHeader(fellow, 3, SecurityConstants.FROM);
		assertHeader(fellow, 4, PURPOSE_HEADER);
	}

	@Test
	public void appFeignUsesNewInternalActorContractAndServiceToken() throws Exception {
		Method detail = RemoteVisitorService.class.getMethod("getAppVisitorDetailForActor", Long.class,
				String.class, String.class, String.class, String.class, String.class);
		assertEquals("/internal/app-visitor/detail/{visitorId}", detail.getAnnotation(GetMapping.class).value()[0]);
		assertHeader(detail, 1, ACTOR_BADGE_HEADER);
		assertHeader(detail, 2, ACTOR_PARK_IDS_HEADER);
		assertHeader(detail, 3, SecurityConstants.FROM);
		assertHeader(detail, 4, SecurityConstants.INTERNAL_SERVICE_AUTH);
		assertHeader(detail, 5, PURPOSE_HEADER);

		Method fellow = RemoteVisitorService.class.getMethod("addAppFellowForActor", AddFellowVisitorReqDTO.class,
				String.class, String.class, String.class, String.class, String.class);
		assertEquals("/internal/app-visitor/fellow", fellow.getAnnotation(PostMapping.class).value()[0]);
		assertHeader(fellow, 1, ACTOR_BADGE_HEADER);
		assertHeader(fellow, 2, ACTOR_PARK_IDS_HEADER);
		assertHeader(fellow, 3, SecurityConstants.FROM);
		assertHeader(fellow, 4, SecurityConstants.INTERNAL_SERVICE_AUTH);
		assertHeader(fellow, 5, PURPOSE_HEADER);
	}

	@Test
	public void legacyIdBasedVisitorDetailRoutesAreNotMapped() throws Exception {
		for (Method method : SmtVisitorController.class.getDeclaredMethods()) {
			GetMapping mapping = method.getAnnotation(GetMapping.class);
			if (mapping == null) {
				continue;
			}
			for (String route : mapping.value()) {
				assertFalse("旧平台详情路由不得恢复", "/searchVisitorDetail/{id}".equals(route));
				assertFalse("旧 App 详情路由不得恢复", "/app/searchAppVisitorDetail/{id}".equals(route));
			}
		}
	}

	private void assertInternalRoute(Class<?> controller, String name, Class<?> mappingType, String route) throws Exception {
		Method method = "detail".equals(name)
				? controller.getMethod(name, Long.class, String.class, String.class, String.class, String.class)
				: controller.getMethod(name, AddFellowVisitorReqDTO.class, String.class, String.class, String.class, String.class);
		assertNotNull(method.getAnnotation(Inner.class));
		assertEquals("server", method.getAnnotation(OpenApi.class).value());
		if (GetMapping.class.equals(mappingType)) {
			assertEquals(route, method.getAnnotation(GetMapping.class).value()[0]);
		} else {
			assertEquals(route, method.getAnnotation(PostMapping.class).value()[0]);
		}
	}

	private void assertHeader(Method method, int parameterIndex, String expected) {
		assertEquals(expected, method.getParameters()[parameterIndex].getAnnotation(RequestHeader.class).value());
	}
}
