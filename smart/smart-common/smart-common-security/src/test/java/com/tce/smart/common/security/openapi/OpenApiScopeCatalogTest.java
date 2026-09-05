package com.tce.smart.common.security.openapi;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** 后端 capability scope 目录必须是管理端和资源服务共用的唯一权威来源。 */
public class OpenApiScopeCatalogTest {

	/** 校验目录仅将 server 列为正常授权，保留两项废弃细分权限并拒绝通配值，不访问外部资源。 */
	@Test
	public void catalogContainsActiveServerAndMarksFineGrainedScopesDeprecated() {
		List<OpenApiScope> scopes = OpenApiScopeCatalog.all();

		assertEquals("server", scopes.get(0).getValue());
		assertEquals(OpenApiScopeCatalog.ADMITTANCE_PHOTO_READ, scopes.get(1).getValue());
		assertEquals(OpenApiScopeCatalog.ENERGY_PROJECTION_RUN, scopes.get(2).getValue());
		assertFalse(scopes.get(0).isDeprecated());
		assertTrue(scopes.get(1).isDeprecated());
		assertTrue(scopes.get(2).isDeprecated());
		assertTrue(OpenApiScopeCatalog.contains("server"));
		assertFalse(OpenApiScopeCatalog.isDeprecated("server"));
		assertTrue(OpenApiScopeCatalog.contains(OpenApiScopeCatalog.ENERGY_PROJECTION_RUN));
		assertFalse(OpenApiScopeCatalog.contains("internal:energy:*"));
	}
}
