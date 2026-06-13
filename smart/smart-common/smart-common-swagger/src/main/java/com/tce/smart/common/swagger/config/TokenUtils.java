package com.tce.smart.common.swagger.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class TokenUtils {

	private static final String CLIENT_ID = "smart";

	private static final String CLIENT_SECRET_PROPERTY = "swagger.oauth.client-secret";

	private static final String CLIENT_SECRET_ENV = "SWAGGER_OAUTH_CLIENT_SECRET";

	private static final String TOKEN_ENDPOINT = "/oauth/token";

	private static final String APPLICATION_NAME = "smart-auth";

	/**
	 * 从注册中心获取auth的地址，并以客户端的模式，申请token
	 *
	 * @param loadBalancerClient
	 * @return
	 */
	public static String getSwaggerToken(LoadBalancerClient loadBalancerClient) {
		return getSwaggerToken(loadBalancerClient, getClientSecret());
	}

	public static String getSwaggerToken(LoadBalancerClient loadBalancerClient, String clientSecret) {
		try {
			if (isBlank(clientSecret)) {
				log.error("获取临时的token失败，Swagger OAuth client secret 未配置");
				return null;
			}
			//组装参数
			MultiValueMap<String, Object> parameters = new LinkedMultiValueMap<>();
			parameters.add("client_id", CLIENT_ID);
			parameters.add("grant_type", "client_credentials");
			parameters.add("scope", "server");
			parameters.add("client_secret", clientSecret);
			//通过rest的方式从注册中心获取到对应auth的服务的地址
			ServiceInstance instance = loadBalancerClient.choose(APPLICATION_NAME);
			String url = "http://" + instance.getHost() + ":" + instance.getPort() + TOKEN_ENDPOINT;
			HttpHeaders headers = new HttpHeaders();
			RestTemplate template = new RestTemplate();
			HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(parameters, headers);
			//发起请求申请token
			ResponseEntity<OAuth2AccessToken> result = template.postForEntity(url, httpEntity, OAuth2AccessToken.class);
			if (result.getStatusCode().is2xxSuccessful()) {
				//拼接token
				return result.getBody().getTokenType() + " " + result.getBody().getValue();
			}
		} catch (Exception e) {
			log.error("获取临时的token失败", e);
		}
		return null;
	}

	private static String getClientSecret() {
		String value = System.getProperty(CLIENT_SECRET_PROPERTY);
		if (!isBlank(value)) {
			return value;
		}
		value = System.getenv(CLIENT_SECRET_ENV);
		return isBlank(value) ? null : value;
	}

	private static boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}
