package com.tce.smart.admin.controller;

import com.tce.smart.admin.service.SysOauthClientDetailsService;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.openapi.OpenApiScope;
import com.tce.smart.common.security.openapi.OpenApiScopeCatalog;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** 客户端管理页应从后端读取 capability scope 目录，而不是维护独立前端白名单。 */
public class OauthClientDetailsControllerTest {

	@Test
	public void scopesReturnsAuthoritativeCatalogWithoutClientSecrets() {
		SysOauthClientDetailsService service = mock(SysOauthClientDetailsService.class);
		OauthClientDetailsController controller = new OauthClientDetailsController(service);

		Result<List<OpenApiScope>> result = controller.scopes();

		assertThat(result.getData()).extracting(OpenApiScope::getValue)
				.contains(OpenApiScopeCatalog.ADMITTANCE_PHOTO_READ, OpenApiScopeCatalog.ENERGY_PROJECTION_RUN,
						OpenApiScopeCatalog.LEGACY_SERVER);
		assertThat(result.getData()).filteredOn(OpenApiScope::isDeprecated)
				.extracting(OpenApiScope::getValue).containsExactly(OpenApiScopeCatalog.LEGACY_SERVER);
	}
}
