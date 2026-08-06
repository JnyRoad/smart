package com.tce.smart.common.security.feign;

import cn.hutool.core.collection.CollUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.security.oauth2.client.AccessTokenContextRelay;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Enumeration;

/**
 * 扩展OAuth2FeignRequestInterceptor
 */
@Slf4j
public class SmartFeignClientInterceptor extends OAuth2FeignRequestInterceptor {
	private final OAuth2ClientContext oAuth2ClientContext;
	private final AccessTokenContextRelay accessTokenContextRelay;
	private final RequestInterceptor internalServiceTokenInterceptor;

	/**
	 * Default constructor which uses the provided OAuth2ClientContext and Bearer tokens
	 * within Authorization header
	 *
	 * @param oAuth2ClientContext     provided context
	 * @param resource                type of resource to be accessed
	 * @param accessTokenContextRelay
	 */
	public SmartFeignClientInterceptor(OAuth2ClientContext oAuth2ClientContext
		, OAuth2ProtectedResourceDetails resource, AccessTokenContextRelay accessTokenContextRelay) {
		this(oAuth2ClientContext, resource, accessTokenContextRelay, template -> {
			throw new IllegalStateException("内部服务令牌拦截器未配置，拒绝调用下游服务");
		});
	}

	/**
	 * @param internalServiceTokenInterceptor 显式内部调用专用的 client_credentials 拦截器
	 */
	public SmartFeignClientInterceptor(OAuth2ClientContext oAuth2ClientContext,
			OAuth2ProtectedResourceDetails resource, AccessTokenContextRelay accessTokenContextRelay,
			RequestInterceptor internalServiceTokenInterceptor) {
		super(oAuth2ClientContext, resource);
		this.oAuth2ClientContext = oAuth2ClientContext;
		this.accessTokenContextRelay = accessTokenContextRelay;
		this.internalServiceTokenInterceptor = internalServiceTokenInterceptor;
	}


	/**
	 * Create a template with the header of provided name and extracted extract
	 * 1. 如果使用 非web 请求，header 区别
	 * 2. 根据authentication 还原请求token
	 *
	 * @param template
	 */
	@Override
	public void apply(RequestTemplate template) {

		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		if(null == attributes){
			// 只有显式标记的敏感内部调用才在无入站请求时申请服务令牌，
			// 其余历史 Feign 调用保持原有行为，避免未迁移端点被客户端令牌拒绝。
			if (requiresServiceToken(template)) {
				applyServiceToken(template);
			}
			return;
		}

		// 服务令牌端点绝不能复制入站 Authorization；先清除模板和 OAuth 上下文中的用户令牌。
		if (requiresServiceToken(template)) {
			applyServiceToken(template);
			return;
		}

		HttpServletRequest request = attributes.getRequest();
		Enumeration<String> headerNames = request.getHeaderNames();
		if (headerNames != null) {
			while (headerNames.hasMoreElements()) {
				String name = headerNames.nextElement();
				String values = request.getHeader(name);
				template.header(name, values);
			}

			Collection<String> fromHeader = template.headers().get(SecurityConstants.FROM);
			if (CollUtil.isNotEmpty(fromHeader) && fromHeader.contains(SecurityConstants.FROM_IN)) {
				return;
			}

			accessTokenContextRelay.copyToken();
			if (oAuth2ClientContext != null && oAuth2ClientContext.getAccessToken() != null) {
				super.apply(template);
			}
		}
	}

	/**
	 * 服务令牌使用独立 OAuth 上下文；不读取或修改入站用户 OAuth 上下文。
	 */
	private void applyServiceToken(RequestTemplate template) {
		template.header("Authorization");
		internalServiceTokenInterceptor.apply(template);
	}

	/**
	 * 服务令牌只由 Feign 契约显式声明，避免把所有历史 {@code from=Y} 调用一次性切换为客户端凭据。
	 */
	private boolean requiresServiceToken(RequestTemplate template) {
		Collection<String> values = template.headers().get(SecurityConstants.INTERNAL_SERVICE_AUTH);
		return CollUtil.isNotEmpty(values) && values.contains(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED);
	}
}
