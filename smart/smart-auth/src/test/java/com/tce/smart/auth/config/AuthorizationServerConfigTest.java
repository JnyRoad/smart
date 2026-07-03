package com.tce.smart.auth.config;

import com.tce.smart.common.core.constant.SecurityConstants;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AuthorizationServerConfig#tokenEnhancer() 单测。
 * 不启动 Spring 上下文，直接 new 配置类并 mock 依赖，纯校验 token 增强逻辑。
 */
public class AuthorizationServerConfigTest {

	/**
	 * client_credentials 授权且 additional_information 中带 allowedParkIds 时，
	 * 增强后的 token 必须携带 app_park_ids claim（供资源服务做数据范围校验）。
	 */
	@Test
	public void clientCredentialsTokenShouldCarryAppParkIdsWhenAllowedParkIdsPresent() {
		ClientDetailsService clientDetailsService = Mockito.mock(ClientDetailsService.class);
		List<Integer> allowedParkIds = Arrays.asList(1, 2, 3);
		ClientDetails clientDetails = mockClientDetails("open-app", allowedParkIds);
		Mockito.when(clientDetailsService.loadClientByClientId("open-app")).thenReturn(clientDetails);

		AuthorizationServerConfig config = new AuthorizationServerConfig(null, null, null, null);
		TokenEnhancer tokenEnhancer = config.buildTokenEnhancer(clientDetailsService);

		OAuth2AccessToken enhanced = tokenEnhancer.enhance(
				new DefaultOAuth2AccessToken("token-value"),
				mockClientCredentialsAuthentication("open-app"));

		Map<String, Object> additionalInformation = enhanced.getAdditionalInformation();
		Assert.assertEquals(allowedParkIds, additionalInformation.get("app_park_ids"));
		Assert.assertEquals(SecurityConstants.SMART_LICENSE, additionalInformation.get("license"));
	}

	/**
	 * client_credentials 授权但 additional_information 中没有 allowedParkIds 时，
	 * 不应该塞入 app_park_ids claim（避免误传 null 造成资源服务误判为"无限制"）。
	 */
	@Test
	public void clientCredentialsTokenShouldNotCarryAppParkIdsWhenAllowedParkIdsAbsent() {
		ClientDetailsService clientDetailsService = Mockito.mock(ClientDetailsService.class);
		ClientDetails clientDetails = mockClientDetails("open-app-no-park", null);
		Mockito.when(clientDetailsService.loadClientByClientId("open-app-no-park")).thenReturn(clientDetails);

		AuthorizationServerConfig config = new AuthorizationServerConfig(null, null, null, null);
		TokenEnhancer tokenEnhancer = config.buildTokenEnhancer(clientDetailsService);

		OAuth2AccessToken enhanced = tokenEnhancer.enhance(
				new DefaultOAuth2AccessToken("token-value"),
				mockClientCredentialsAuthentication("open-app-no-park"));

		Map<String, Object> additionalInformation = enhanced.getAdditionalInformation();
		Assert.assertFalse(additionalInformation.containsKey("app_park_ids"));
		Assert.assertEquals(SecurityConstants.SMART_LICENSE, additionalInformation.get("license"));
	}

	private static ClientDetails mockClientDetails(String clientId, List<Integer> allowedParkIds) {
		ClientDetails clientDetails = Mockito.mock(ClientDetails.class);
		Mockito.when(clientDetails.getClientId()).thenReturn(clientId);
		Map<String, Object> additionalInformation = new HashMap<>(2);
		if (allowedParkIds != null) {
			additionalInformation.put("allowedParkIds", allowedParkIds);
		}
		Mockito.when(clientDetails.getAdditionalInformation()).thenReturn(additionalInformation);
		return clientDetails;
	}

	private static OAuth2Authentication mockClientCredentialsAuthentication(String clientId) {
		// OAuth2Request#getGrantType() 实际读取 requestParameters 里的 grant_type 键，
		// 而不是 responseTypes 参数，这里必须放进第一个 Map 参数才能让 getGrantType() 生效
		Map<String, String> requestParameters = Collections.singletonMap(
				"grant_type", SecurityConstants.CLIENT_CREDENTIALS);
		OAuth2Request oAuth2Request = new OAuth2Request(
				requestParameters, clientId, Collections.emptyList(),
				true, Collections.emptySet(), Collections.emptySet(),
				null, Collections.emptySet(), Collections.emptyMap());
		return new OAuth2Authentication(oAuth2Request, null);
	}
}
