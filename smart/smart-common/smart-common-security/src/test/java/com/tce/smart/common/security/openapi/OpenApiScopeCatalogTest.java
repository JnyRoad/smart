package com.tce.smart.common.security.openapi;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/** 后端 capability scope 目录必须是管理端和资源服务共用的唯一权威来源。 */
public class OpenApiScopeCatalogTest {

	@Test
	public void catalogContainsCurrentCapabilitiesAndMarksLegacyServerDeprecated() {
		List<OpenApiScope> scopes = OpenApiScopeCatalog.all();

		assertEquals(OpenApiScopeCatalog.ADMITTANCE_PHOTO_READ, scopes.get(0).getValue());
		assertEquals(OpenApiScopeCatalog.ENERGY_PROJECTION_RUN, scopes.get(1).getValue());
		assertEquals(OpenApiScopeCatalog.LEGACY_SERVER, scopes.get(2).getValue());
		assertFalse(scopes.get(1).isDeprecated());
		assertTrue(scopes.get(2).isDeprecated());
		assertTrue(OpenApiScopeCatalog.contains(OpenApiScopeCatalog.ENERGY_PROJECTION_RUN));
		assertFalse(OpenApiScopeCatalog.contains("internal:energy:*"));
	}
}
