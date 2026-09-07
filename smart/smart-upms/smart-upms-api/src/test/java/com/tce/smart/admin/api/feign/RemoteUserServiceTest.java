package com.tce.smart.admin.api.feign;

import com.tce.smart.admin.api.dto.UserCredentialDTO;
import com.tce.smart.common.core.constant.SecurityConstants;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RemoteUserServiceTest {

	@Test
	public void explicitAuthenticationUsesJsonPostBody() {
		Method method = Arrays.stream(RemoteUserService.class.getMethods())
				.filter(candidate -> candidate.getName().equals("authenticate"))
				.filter(candidate -> Arrays.equals(candidate.getParameterTypes(),
						new Class<?>[]{UserCredentialDTO.class, String.class}))
				.findFirst()
				.orElse(null);
		assertNotNull(method);

		PostMapping mapping = method.getAnnotation(PostMapping.class);

		assertNotNull(mapping);
		assertArrayEquals(new String[]{"/api/user/simple"}, mapping.value());
		assertArrayEquals(new String[]{MediaType.APPLICATION_JSON_VALUE}, mapping.consumes());
		assertTrue(hasAnnotation(method.getParameterAnnotations()[0], RequestBody.class));
		RequestHeader requestHeader = findAnnotation(method.getParameterAnnotations()[1], RequestHeader.class);
		assertNotNull(requestHeader);
		assertEquals(SecurityConstants.FROM, requestHeader.value());
	}

	@Test
	public void appSessionAuthenticationUsesDedicatedJsonPostBody() {
		Method method = Arrays.stream(RemoteUserService.class.getMethods())
				.filter(candidate -> candidate.getName().equals("authenticateAppSession"))
				.filter(candidate -> Arrays.equals(candidate.getParameterTypes(),
						new Class<?>[]{UserCredentialDTO.class, String.class}))
				.findFirst()
				.orElse(null);
		assertNotNull(method);

		PostMapping mapping = method.getAnnotation(PostMapping.class);
		assertNotNull(mapping);
		assertArrayEquals(new String[]{"/api/user/session"}, mapping.value());
		assertArrayEquals(new String[]{MediaType.APPLICATION_JSON_VALUE}, mapping.consumes());
		assertTrue(hasAnnotation(method.getParameterAnnotations()[0], RequestBody.class));
		RequestHeader requestHeader = findAnnotation(method.getParameterAnnotations()[1], RequestHeader.class);
		assertNotNull(requestHeader);
		assertEquals(SecurityConstants.FROM, requestHeader.value());
	}

	private boolean hasAnnotation(Annotation[] annotations, Class<? extends Annotation> type) {
		return Arrays.stream(annotations).anyMatch(type::isInstance);
	}

	private <T extends Annotation> T findAnnotation(Annotation[] annotations, Class<T> type) {
		return Arrays.stream(annotations)
				.filter(type::isInstance)
				.map(type::cast)
				.findFirst()
				.orElse(null);
	}
}
