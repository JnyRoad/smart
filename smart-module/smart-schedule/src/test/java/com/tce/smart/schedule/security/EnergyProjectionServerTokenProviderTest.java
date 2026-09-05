package com.tce.smart.schedule.security;

import com.tce.smart.schedule.config.EnergyProjectionOAuthProperties;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import java.nio.charset.StandardCharsets;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 能耗投影调度任务获取 server scope 凭证的测试。
 */
public class EnergyProjectionServerTokenProviderTest {

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void authorizationHeaderUsesClientCredentialsAndCachesUsableToken() {
		EnergyProjectionOAuthProperties properties = configuredProperties();
		RestOperations restOperations = Mockito.mock(RestOperations.class);
		Map response = new HashMap();
		response.put("access_token", "projection-token");
		response.put("expires_in", 3600);
		Mockito.when(restOperations.exchange(Mockito.eq("http://auth.example/oauth/token"), Mockito.eq(HttpMethod.POST),
				Mockito.<HttpEntity<?>>any(), Mockito.eq(Map.class)))
				.thenReturn(new ResponseEntity<Map>(response, HttpStatus.OK));

		EnergyProjectionServerTokenProvider provider = new EnergyProjectionServerTokenProvider(properties, restOperations,
				Clock.fixed(Instant.parse("2026-08-06T00:00:00Z"), ZoneOffset.UTC));

		Assert.assertEquals("Bearer projection-token", provider.authorizationHeader());
		Assert.assertEquals("Bearer projection-token", provider.authorizationHeader());

		ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		Mockito.verify(restOperations, Mockito.times(1)).exchange(Mockito.eq("http://auth.example/oauth/token"),
				Mockito.eq(HttpMethod.POST), requestCaptor.capture(), Mockito.eq(Map.class));
		HttpEntity request = requestCaptor.getValue();
		String basicValue = Base64.getEncoder()
				.encodeToString("schedule-client:schedule-secret".getBytes(StandardCharsets.UTF_8));
		Assert.assertEquals("Basic " + basicValue, request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
		Assert.assertEquals("application/x-www-form-urlencoded", request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE));
		MultiValueMap<String, String> form = (MultiValueMap<String, String>) request.getBody();
		Assert.assertEquals("client_credentials", form.getFirst("grant_type"));
		Assert.assertEquals("server", form.getFirst("scope"));
	}

	@Test
	public void authorizationHeaderRejectsMissingClientSecretWithoutCallingTokenEndpoint() {
		EnergyProjectionOAuthProperties properties = configuredProperties();
		properties.setClientSecret(" ");
		RestOperations restOperations = Mockito.mock(RestOperations.class);
		EnergyProjectionServerTokenProvider provider = new EnergyProjectionServerTokenProvider(properties, restOperations,
				Clock.systemUTC());

		try {
			provider.authorizationHeader();
			Assert.fail("缺少客户端密钥时必须安全失败");
		} catch (IllegalStateException expected) {
			Assert.assertTrue(expected.getMessage().contains("client-secret"));
		}
		Mockito.verifyZeroInteractions(restOperations);
	}

	/** 默认能耗调用直接申请 server，无需单独配置细分 scope。 */
	@Test
	public void energyProjectionAuthorizationHeaderDefaultsToServer() {
		assertProjectionScope(null, "server");
	}

	/** 旧部署明确指定的能耗细分权限仍按原值申请，避免升级时要求立即修改存量应用。 */
	@Test
	public void energyProjectionAuthorizationHeaderKeepsExplicitHistoricalScope() {
		assertProjectionScope("internal:energy:projection:run", "internal:energy:projection:run");
	}

	/** 校验实际 OAuth 请求中的授权域；Mock 授权端点只提供令牌响应，不连接外部服务。 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void assertProjectionScope(String configuredScope, String expectedScope) {
		EnergyProjectionOAuthProperties properties = configuredProperties();
		if (configuredScope != null) {
			properties.setEnergyProjectionRunScope(configuredScope);
		}
		RestOperations restOperations = Mockito.mock(RestOperations.class);
		Map response = new HashMap();
		response.put("access_token", "projection-token");
		response.put("expires_in", 3600);
		Mockito.when(restOperations.exchange(Mockito.eq("http://auth.example/oauth/token"), Mockito.eq(HttpMethod.POST),
				Mockito.<HttpEntity<?>>any(), Mockito.eq(Map.class)))
				.thenReturn(new ResponseEntity<Map>(response, HttpStatus.OK));

		EnergyProjectionServerTokenProvider provider = new EnergyProjectionServerTokenProvider(properties, restOperations,
				Clock.fixed(Instant.parse("2026-08-06T00:00:00Z"), ZoneOffset.UTC));
		Assert.assertEquals("Bearer projection-token", provider.energyProjectionAuthorizationHeader());

		ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		Mockito.verify(restOperations).exchange(Mockito.eq("http://auth.example/oauth/token"), Mockito.eq(HttpMethod.POST),
				requestCaptor.capture(), Mockito.eq(Map.class));
		MultiValueMap<String, String> form = (MultiValueMap<String, String>) requestCaptor.getValue().getBody();
		Assert.assertEquals(expectedScope, form.getFirst("scope"));
	}

	@Test
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void authorizationHeaderForCapabilityScopeCachesTokensSeparately() throws Exception {
		EnergyProjectionOAuthProperties properties = configuredProperties();
		RestOperations restOperations = Mockito.mock(RestOperations.class);
		Map firstResponse = new HashMap();
		firstResponse.put("access_token", "projection-token");
		firstResponse.put("expires_in", 3600);
		Map secondResponse = new HashMap();
		secondResponse.put("access_token", "another-capability-token");
		secondResponse.put("expires_in", 3600);
		Mockito.when(restOperations.exchange(Mockito.eq("http://auth.example/oauth/token"), Mockito.eq(HttpMethod.POST),
				Mockito.<HttpEntity<?>>any(), Mockito.eq(Map.class)))
				.thenReturn(new ResponseEntity<Map>(firstResponse, HttpStatus.OK),
						new ResponseEntity<Map>(secondResponse, HttpStatus.OK));

		EnergyProjectionServerTokenProvider provider = new EnergyProjectionServerTokenProvider(properties, restOperations,
				Clock.fixed(Instant.parse("2026-08-06T00:00:00Z"), ZoneOffset.UTC));
		Method method;
		try {
			method = EnergyProjectionServerTokenProvider.class.getMethod("authorizationHeader", String.class);
		} catch (NoSuchMethodException e) {
			Assert.fail("Provider 必须支持按 capability scope 获取令牌");
			return;
		}

		Assert.assertEquals("Bearer projection-token", method.invoke(provider, "internal:energy:projection:run"));
		Assert.assertEquals("Bearer projection-token", method.invoke(provider, "internal:energy:projection:run"));
		Assert.assertEquals("Bearer another-capability-token", method.invoke(provider, "internal:energy:meter:sync"));

		ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
		Mockito.verify(restOperations, Mockito.times(2)).exchange(Mockito.eq("http://auth.example/oauth/token"),
				Mockito.eq(HttpMethod.POST), requestCaptor.capture(), Mockito.eq(Map.class));
		MultiValueMap<String, String> firstForm = (MultiValueMap<String, String>) requestCaptor.getAllValues().get(0).getBody();
		MultiValueMap<String, String> secondForm = (MultiValueMap<String, String>) requestCaptor.getAllValues().get(1).getBody();
		Assert.assertEquals("internal:energy:projection:run", firstForm.getFirst("scope"));
		Assert.assertEquals("internal:energy:meter:sync", secondForm.getFirst("scope"));
	}

	/** 只填必需凭据，scope 使用运行配置默认值，避免夹具掩盖默认授权回归。 */
	private EnergyProjectionOAuthProperties configuredProperties() {
		EnergyProjectionOAuthProperties properties = new EnergyProjectionOAuthProperties();
		properties.setAccessTokenUri("http://auth.example/oauth/token");
		properties.setClientId("schedule-client");
		properties.setClientSecret("schedule-secret");
		properties.setRefreshBeforeExpirySeconds(60L);
		return properties;
	}
}
