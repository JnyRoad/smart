package com.tce.smart.auth.config;

import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.security.component.SmartWebResponseExceptionTranslator;
import com.tce.smart.common.security.service.SmartClientDetailsService;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.common.security.service.SmartUserDetailsService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务器配置
 */
@Configuration
@AllArgsConstructor
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
	private final DataSource dataSource;
	private final SmartUserDetailsService smartUserDetailsService;
	private final AuthenticationManager authenticationManagerBean;
	private final RedisConnectionFactory redisConnectionFactory;

	/**
	 * 配置客户端详情服务
	 * @param clients
	 */
	@Override
	@SneakyThrows
	public void configure(ClientDetailsServiceConfigurer clients) {
		clients.withClientDetails(smartClientDetailsService());
	}

	/**
	 * client详情服务，供 configure(ClientDetailsServiceConfigurer) 和 tokenEnhancer() 复用。
	 * 提为 Bean（而非局部 new）后，loadClientByClientId 上的 @Cacheable 才能走 Spring 代理生效。
	 * @return SmartClientDetailsService
	 */
	@Bean
	public SmartClientDetailsService smartClientDetailsService() {
		SmartClientDetailsService clientDetailsService = new SmartClientDetailsService(dataSource);
		clientDetailsService.setSelectClientDetailsSql(SecurityConstants.DEFAULT_SELECT_STATEMENT);
		clientDetailsService.setFindClientDetailsSql(SecurityConstants.DEFAULT_FIND_STATEMENT);
		return clientDetailsService;
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
		oauthServer
				.allowFormAuthenticationForClients()
				.checkTokenAccess("isAuthenticated()");
	}

	/**
	 * 配置授权以及令牌的访问端点和令牌服务
	 * @param endpoints
	 */
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
		endpoints
				.allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST)
                //保存token
				.tokenStore(tokenStore())
                //生成自定义令牌
				.tokenEnhancer(tokenEnhancer())
				.userDetailsService(smartUserDetailsService)
				.authenticationManager(authenticationManagerBean)
                //该字段设置refresh token是否重复使用,true:reuse;false:no reuse。默认为true
				.reuseRefreshTokens(false)
                //自定义授权异常处理
				.exceptionTranslator(new SmartWebResponseExceptionTranslator());
	}


	@Bean
	public TokenStore tokenStore() {
		RedisTokenStore tokenStore = new RedisTokenStore(redisConnectionFactory);
		tokenStore.setPrefix(SecurityConstants.SMART_PREFIX + SecurityConstants.OAUTH_PREFIX);
		tokenStore.setAuthenticationKeyGenerator(new DefaultAuthenticationKeyGenerator() {
			@Override
			public String extractKey(OAuth2Authentication authentication) {
				return super.extractKey(authentication);
			}
		});
		return tokenStore;
	}

	/**
	 * token增强：用户密码模式写入用户信息 claim；client_credentials 模式（开放 API 应用）
	 * 写入该应用绑定的园区范围 claim，供资源服务做数据范围校验，不信任请求参数。
	 *
	 * @return TokenEnhancer
	 */
	@Bean
	public TokenEnhancer tokenEnhancer() {
		// 通过 smartClientDetailsService() 自调用拿到的是 CGLIB 代理返回的同一个单例 Bean，
		// 因此 SmartClientDetailsService#loadClientByClientId 上的 @Cacheable 依然生效
		return buildTokenEnhancer(smartClientDetailsService());
	}

	/**
	 * 抽出为包内可见方法，便于不启动 Spring 上下文、直接 new 配置类 + mock ClientDetailsService 做单测。
	 *
	 * @param clientDetailsService client 详情查询服务
	 * @return TokenEnhancer
	 */
	TokenEnhancer buildTokenEnhancer(ClientDetailsService clientDetailsService) {
		return (accessToken, authentication) -> {
			if (SecurityConstants.CLIENT_CREDENTIALS
					.equals(authentication.getOAuth2Request().getGrantType())) {
				// 开放应用 token：把应用绑定的园区范围写入 token claim，
				// 资源服务据此做数据范围校验，不信任请求参数
				Map<String, Object> info = new HashMap<>(4);
				ClientDetails client = clientDetailsService.loadClientByClientId(
						authentication.getOAuth2Request().getClientId());
				Object parkIds = client.getAdditionalInformation().get("allowedParkIds");
				if (parkIds != null) {
					info.put("app_park_ids", parkIds);
				}
				info.put("license", SecurityConstants.SMART_LICENSE);
				((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);
				return accessToken;
			}

			final Map<String, Object> additionalInfo = new HashMap<>(8);
			SmartUser smartUser = (SmartUser) authentication.getUserAuthentication().getPrincipal();
			additionalInfo.put("user_id", smartUser.getId());
			additionalInfo.put("username", smartUser.getUsername());
			additionalInfo.put("dept_id", smartUser.getDeptId());
			additionalInfo.put("license", SecurityConstants.SMART_LICENSE);
			additionalInfo.put("parkList",smartUser.getParkIdList());
			additionalInfo.put("isStrongPwd",smartUser.getIsStrongPwd());
			additionalInfo.put("salaryTypeName",smartUser.getSalaryTypeName());
			((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
			return accessToken;
		};
	}
}
