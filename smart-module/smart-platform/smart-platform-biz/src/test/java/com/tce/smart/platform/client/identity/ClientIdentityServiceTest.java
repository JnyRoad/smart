package com.tce.smart.platform.client.identity;

import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.client.release.ReleaseAccessProperties;
import com.tce.smart.platform.client.supplier.SupplierAccessProperties;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 同一现场岗位由两项 App 业务复用时，目录应合并为一个岗位而不是因 String/Integer 表示差异失败。 */
public class ClientIdentityServiceTest {
	@After public void clearContext() { SecurityContextHolder.clearContext(); }

	@Test
	public void mergesEquivalentReleaseAndSupplierPostAcrossWireRepresentations() {
		ClientPersonnelDirectory personnel = mock(ClientPersonnelDirectory.class);
		when(personnel.require("SEC-1")).thenReturn(new ClientPerson("SEC-1", "合成安检员", "合成单位", "outsourced"));
		ReleaseAccessProperties releases = new ReleaseAccessProperties();
		releases.setEnabled(true); releases.setPosts(Collections.singletonList(releasePost("gate-a")));
		SupplierAccessProperties suppliers = new SupplierAccessProperties();
		suppliers.setEnabled(true); suppliers.setPosts(Collections.singletonList(supplierPost("gate-a")));
		List<SimpleGrantedAuthority> authorities = Arrays.asList(
				new SimpleGrantedAuthority("item-pass:execute"), new SimpleGrantedAuthority("item-pass:post:gate-a"),
				new SimpleGrantedAuthority("supplier:execute"), new SimpleGrantedAuthority("supplier:post:gate-a"));
		SmartUser user = new SmartUser(1, 1, "SEC-1", Collections.singletonList(1), "unused", true, true, true, true, authorities);
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, authorities));

		Map<String, Object> response = new ClientIdentityService(personnel, releases, suppliers).response();
		List<?> posts = (List<?>) response.get("posts");
		Assert.assertEquals(1, posts.size());
		Map<?, ?> post = (Map<?, ?>) posts.get(0);
		Assert.assertEquals("gate-a", post.get("id"));
		Assert.assertEquals("1", post.get("parkId"));
	}

	private static ReleaseAccessProperties.Post releasePost(String id) {
		ReleaseAccessProperties.Post post = new ReleaseAccessProperties.Post();
		post.setId(id); post.setName("合成安检岗"); post.setParkId(1); post.setParkName("合成园区"); return post;
	}

	private static SupplierAccessProperties.Post supplierPost(String id) {
		SupplierAccessProperties.Post post = new SupplierAccessProperties.Post();
		post.setId(id); post.setName("合成安检岗"); post.setParkId(1); post.setParkName("合成园区");
		post.setAreaId("area-a"); post.setAreaName("合成区域"); post.setAdmittanceAreaTypeCode("1"); return post;
	}
}
