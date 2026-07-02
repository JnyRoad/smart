package com.tce.smart.common.security.openapi;

import org.junit.Test;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * OpenApiAuthenticationAdapter#appParkIds 防御性解析回归测试。
 * <p>
 * 背景（Task 2 遗留问题）：{@code AuthorizationServerConfig#buildTokenEnhancer} 把 client 的
 * {@code allowedParkIds} 原样透传进 token claim，未做类型校验；资源服务侧反序列化后可能拿到
 * 脏数据（非 List、List 内含非 Integer 元素、claim 缺失/为 null）。约定：一律不抛异常，
 * 按「WARN + 空列表」降级，空列表语义等价于拒绝一切园区数据（最安全方向）。
 */
public class OpenApiAuthenticationAdapterTest {

	private static final String APP_PARK_IDS_CLAIM = "app_park_ids";

	private final OpenApiAuthenticationAdapter adapter = new OpenApiAuthenticationAdapter();

	/** 构造携带指定 extensions 的 client_credentials OAuth2Authentication。 */
	private OAuth2Authentication authenticationWithExtensions(Map<String, Serializable> extensions) {
		OAuth2Request oAuth2Request = new OAuth2Request(
				Collections.emptyMap(), "open-app", Collections.emptyList(),
				true, Collections.emptySet(), Collections.emptySet(),
				null, Collections.emptySet(), extensions);
		return new OAuth2Authentication(oAuth2Request, null);
	}

	@Test
	public void extensionValueIsNonList_returnsEmptyList() {
		// extensions 中该键为非 List（如 String）→ 空列表，且不抛异常
		Map<String, Serializable> extensions = new HashMap<>();
		extensions.put(APP_PARK_IDS_CLAIM, "not-a-list");

		List<Integer> result = adapter.appParkIds(authenticationWithExtensions(extensions));

		assertTrue(result.isEmpty());
	}

	@Test
	public void listContainsNonNumericElement_returnsEmptyList() {
		// List 内含非数字元素（如 String）→ 按现实现语义：整体判定为脏数据，返回空列表，不抛异常
		Map<String, Serializable> extensions = new HashMap<>();
		ArrayList<Object> dirtyList = new ArrayList<>(Arrays.asList(1, "bad-element", 3));
		extensions.put(APP_PARK_IDS_CLAIM, dirtyList);

		List<Integer> result = adapter.appParkIds(authenticationWithExtensions(extensions));

		assertTrue(result.isEmpty());
	}

	@Test
	public void claimKeyMissing_returnsEmptyList() {
		// extensions 中根本不存在该键 → 空列表
		List<Integer> result = adapter.appParkIds(authenticationWithExtensions(Collections.emptyMap()));

		assertTrue(result.isEmpty());
	}

	@Test
	public void claimValueIsNull_returnsEmptyList() {
		// extensions 中该键存在但值为 null → 空列表
		Map<String, Serializable> extensions = new HashMap<>();
		extensions.put(APP_PARK_IDS_CLAIM, null);

		List<Integer> result = adapter.appParkIds(authenticationWithExtensions(extensions));

		assertTrue(result.isEmpty());
	}

	@Test
	public void wellFormedIntegerList_returnsAsIs() {
		// 合法数据（全部为 Integer 的 List）→ 原样返回，覆盖正常路径不受防御性解析影响
		Map<String, Serializable> extensions = new HashMap<>();
		extensions.put(APP_PARK_IDS_CLAIM, new ArrayList<>(Arrays.asList(1, 2, 3)));

		List<Integer> result = adapter.appParkIds(authenticationWithExtensions(extensions));

		assertEquals(Arrays.asList(1, 2, 3), result);
	}
}
