package com.tce.smart.gateway.filter;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.http.HttpUtil;
import com.tce.smart.common.core.constant.SecurityConstants;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 密码解密工具类
 */
@Slf4j
@Component
public class PasswordDecoderFilter extends AbstractGatewayFilterFactory {
	private static final String KEY_ALGORITHM = "AES";
	@Value("${security.encode.key:}")
	private String encodeKey;

	@SneakyThrows
	private static String decryptAES(String data, String pass) {
		AES aes = new AES(Mode.CBC,Padding.NoPadding,
				new SecretKeySpec(pass.getBytes(), KEY_ALGORITHM),
				new IvParameterSpec(pass.getBytes()));
		byte[] result = aes.decrypt(Base64.decode(data.getBytes(StandardCharsets.UTF_8)));
		return new String(result, StandardCharsets.UTF_8);
	}

	@Override
	public GatewayFilter apply(Object config) {
		return (exchange, chain) -> {
			ServerHttpRequest request = exchange.getRequest();

			// 不是登录请求，直接向下执行
			if (!StrUtil.containsAnyIgnoreCase(request.getURI().getPath(), SecurityConstants.OAUTH_TOKEN_URL)
					&& !StrUtil.containsAnyIgnoreCase(request.getURI().getPath(), SecurityConstants.UPDATE_PWD_URL)) {

				return chain.filter(exchange);
			}

			URI uri = exchange.getRequest().getURI();
			String queryParam = uri.getRawQuery();
			Map<String, String> paramMap = HttpUtil.decodeParamMap(queryParam, CharsetUtil.UTF_8);

			String password = paramMap.get(SecurityConstants.PASSWORD);
			if (StrUtil.isNotBlank(password)) {
				if (StrUtil.isBlank(encodeKey)) {
					log.error("密码解密密钥未配置");
					return Mono.error(new IllegalStateException("security.encode.key is not configured"));
				}
				try {
					password = decryptAES(password, encodeKey);
				} catch (Exception e) {
					log.error("密码解密失败", e);
					return Mono.error(e);
				}
				paramMap.put(SecurityConstants.PASSWORD, password.trim());
			}

			URI newUri = UriComponentsBuilder.fromUri(uri)
				.replaceQuery(HttpUtil.toParams(paramMap))
				.build(true)
				.toUri();

			ServerHttpRequest newRequest = exchange.getRequest().mutate().uri(newUri).build();
			return chain.filter(exchange.mutate().request(newRequest).build());
		};
	}
}
