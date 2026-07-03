package com.tce.smart.auth.endpoint;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.constant.PaginationConstants;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.exception.NotStrongPasswordException;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.ConvertingCursor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.security.core.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 删除token端点
 */
@RestController
@AllArgsConstructor
@RequestMapping("/token")
public class SmartTokenEndpoint {
	private static final String SMART_OAUTH_ACCESS = SecurityConstants.SMART_PREFIX + SecurityConstants.OAUTH_PREFIX + "auth_to_access:";
	private final TokenStore tokenStore;
	private final RedisTemplate redisTemplate;
	private final CacheManager cacheManager;

	/**
	 * 认证页面
	 *
	 * @return ModelAndView
	 */
	@GetMapping("/login")
	public ModelAndView require() {
		return new ModelAndView("ftl/login");
	}

	/**
	 * 退出token
	 *
	 * @param authHeader Authorization
	 */
	@PostMapping("/logout")
	public Result logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
		if (StrUtil.isBlank(authHeader)) {
			return new Result<>(Boolean.TRUE);
			//token 为空直接成功
//			return Result.builder()
//					.code(CommonConstants.FAIL)
//					.data(Boolean.FALSE)
//					.msg("退出失败，token 为空").build();
		}

		String tokenValue = authHeader.replace(OAuth2AccessToken.BEARER_TYPE, StrUtil.EMPTY).trim();
		OAuth2AccessToken accessToken = tokenStore.readAccessToken(tokenValue);
		if (accessToken == null || StrUtil.isBlank(accessToken.getValue())) {
			return new Result<>(Boolean.TRUE);
		}

		OAuth2Authentication auth2Authentication = tokenStore.readAuthentication(accessToken);
		Objects.requireNonNull(cacheManager.getCache("user_details")).evict(auth2Authentication.getName());
		tokenStore.removeAccessToken(accessToken);
		return new Result<>(Boolean.TRUE);
	}

	/**
	 * 令牌管理调用
	 *
	 * @param token token
	 * @return
	 */
	@Inner
	@PostMapping("/{token}")
	public Result<Boolean> delToken(@PathVariable("token") String token) {
		OAuth2AccessToken oAuth2AccessToken = tokenStore.readAccessToken(token);
		tokenStore.removeAccessToken(oAuth2AccessToken);
		return new Result<>();
	}

	/**
	 * 按 clientId 批量吊销该客户端签发的所有 token（access token + refresh token）。
	 * 用于 upms 重置 client secret 或删除 client 后，让旧 token 立即失效（返回 401/403）。
	 *
	 * <p>快速失败：tokenStore 非 RedisTokenStore（不支持 findTokensByClientId）时直接抛出，
	 * 不允许静默跳过吊销、制造“已吊销”的假象。</p>
	 *
	 * @param clientId 客户端ID
	 * @return 吊销的 token 数量
	 */
	@Inner
	@DeleteMapping("/client/{clientId}")
	public Result<Integer> revokeTokensByClientId(@PathVariable("clientId") String clientId) {
		if (!(tokenStore instanceof RedisTokenStore)) {
			// TokenStore 实现被替换为非 Redis 方案时，findTokensByClientId 能力不存在，
			// 必须显式报错让调用方（upms）感知吊销失败，而不是悄悄返回 0 造成“看起来成功”的假象。
			throw new IllegalStateException("当前 TokenStore 不支持按 clientId 批量吊销：" + tokenStore.getClass().getName());
		}
		RedisTokenStore redisTokenStore = (RedisTokenStore) tokenStore;
		Collection<OAuth2AccessToken> accessTokens = redisTokenStore.findTokensByClientId(clientId);
		int revokedCount = 0;
		for (OAuth2AccessToken accessToken : accessTokens) {
			OAuth2RefreshToken refreshToken = accessToken.getRefreshToken();
			if (refreshToken != null) {
				redisTokenStore.removeRefreshToken(refreshToken);
			}
			redisTokenStore.removeAccessToken(accessToken);
			revokedCount++;
		}
		return new Result<>(revokedCount);
	}

	/**
	 * 查询token
	 *
	 * @param params 分页参数
	 * @return
	 */
	@Inner
	@PostMapping("/page")
	public Result<Page> tokenList(@RequestBody Map<String, Object> params) {
		//根据分页参数获取对应数据
		String key = String.format("%s*", SMART_OAUTH_ACCESS);
		List<String> pages = findKeysForPage(key, MapUtil.getInt(params, PaginationConstants.CURRENT)
				, MapUtil.getInt(params, PaginationConstants.SIZE));

		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
		Page result = new Page(MapUtil.getInt(params, PaginationConstants.CURRENT), MapUtil.getInt(params, PaginationConstants.SIZE));
		result.setRecords(redisTemplate.opsForValue().multiGet(pages));
		result.setTotal(Long.valueOf(redisTemplate.keys(key).size()));
		return new Result<>(result);
	}


	private List<String> findKeysForPage(String patternKey, int pageNum, int pageSize) {
		ScanOptions options = ScanOptions.scanOptions().match(patternKey).build();
		RedisSerializer<String> redisSerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
		Cursor cursor = (Cursor) redisTemplate.executeWithStickyConnection(redisConnection -> new ConvertingCursor<>(redisConnection.scan(options), redisSerializer::deserialize));
		List<String> result = new ArrayList<>();
		int tmpIndex = 0;
		int startIndex = (pageNum - 1) * pageSize;
		int end = pageNum * pageSize;

		assert cursor != null;
		while (cursor.hasNext()) {
			if (tmpIndex >= startIndex && tmpIndex < end) {
				result.add(cursor.next().toString());
				tmpIndex++;
				continue;
			}
			if (tmpIndex >= end) {
				break;
			}
			tmpIndex++;
			cursor.next();
		}
		return result;
	}
}
