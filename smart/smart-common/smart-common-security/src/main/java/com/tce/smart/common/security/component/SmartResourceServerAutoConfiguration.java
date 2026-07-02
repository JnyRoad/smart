package com.tce.smart.common.security.component;

import com.tce.smart.common.security.openapi.OpenApiAuthenticationAdapter;
import com.tce.smart.common.security.openapi.OpenApiInterceptor;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;

@ComponentScan("com.tce.smart.common.security")
public class SmartResourceServerAutoConfiguration {

	@Value("${rest.config.connectTimeout:10000}")
	private Integer connectTimeout;

	@Value("${rest.config.readTimeout:10000}")
	private Integer readTimeout;

	@Value("${rest.config.connectionRequestTimeout:10000}")
	private Integer connectionRequestTimeout;

	@Value("${rest.config.maxTotal:1000}")
	private Integer maxTotal;

	@Value("${rest.config.maxPerRoute:1000}")
	private Integer maxPerRoute;

	@Value("${rest.config.validateAfterInactivity:30000}")
	private Integer validateAfterInactivity;

	@Bean
	@Primary
	@LoadBalanced
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			public void handleError(ClientHttpResponse response) throws IOException {
				if (response.getRawStatusCode() != HttpStatus.BAD_REQUEST.value()) {
					super.handleError(response);
				}
			}
		});
		return restTemplate;
	}

	/**
	 * 客户端请求链接策略
	 *
	 * @return
	 */
	@Bean
	public ClientHttpRequestFactory clientHttpRequestFactory() {
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		clientHttpRequestFactory.setHttpClient(httpClientBuilder().build());
		// 连接超时时间/毫秒
		clientHttpRequestFactory.setConnectTimeout(connectTimeout);
		// 读写超时时间/毫秒
		clientHttpRequestFactory.setReadTimeout(readTimeout);
		// 请求超时时间/毫秒
		clientHttpRequestFactory.setConnectionRequestTimeout(connectionRequestTimeout);
		return clientHttpRequestFactory;
	}

	/**
	 * 设置HTTP连接管理器,连接池相关配置管理
	 *
	 * @return 客户端链接管理器
	 */
	@Bean
	public HttpClientBuilder httpClientBuilder() {
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		httpClientBuilder.setConnectionManager(poolingConnectionManager());
		return httpClientBuilder;
	}

	/**
	 * 链接线程池管理,可以keep-alive不断开链接请求,这样速度会更快 MaxTotal 连接池最大连接数 DefaultMaxPerRoute
	 * 每个主机的并发 ValidateAfterInactivity
	 * 可用空闲连接过期时间,重用空闲连接时会先检查是否空闲时间超过这个时间，如果超过，释放socket重新建立
	 *
	 * @return
	 */
	@Bean
	public HttpClientConnectionManager poolingConnectionManager() {
		PoolingHttpClientConnectionManager poolingConnectionManager = new PoolingHttpClientConnectionManager();
		poolingConnectionManager.setMaxTotal(maxTotal);
		poolingConnectionManager.setDefaultMaxPerRoute(maxPerRoute);
		poolingConnectionManager.setValidateAfterInactivity(validateAfterInactivity);
		return poolingConnectionManager;
	}

	@Bean
	public OpenApiAuthenticationAdapter openApiAuthenticationAdapter() {
		return new OpenApiAuthenticationAdapter();
	}

	/**
	 * 全局注册开放 API 鉴权拦截器：拦截所有请求路径，业务服务无需任何额外配置即可生效。
	 */
	@Bean
	public WebMvcConfigurer openApiWebMvcConfigurer(OpenApiAuthenticationAdapter openApiAuthenticationAdapter) {
		return new WebMvcConfigurer() {
			@Override
			public void addInterceptors(InterceptorRegistry registry) {
				registry.addInterceptor(new OpenApiInterceptor(openApiAuthenticationAdapter)).addPathPatterns("/**");
			}
		};
	}
}
