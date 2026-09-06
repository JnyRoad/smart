package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.dto.authoperation.AuthOperationBatchPageQuery;
import com.tce.smart.platform.dto.authoperation.AuthOperationBatchDetailView;
import com.tce.smart.platform.dto.authoperation.AuthOperationBatchView;
import com.tce.smart.platform.dto.authoperation.AuthOperationTargetPageQuery;
import com.tce.smart.platform.dto.authoperation.AuthOperationTargetView;
import com.tce.smart.platform.service.impl.AuthOperationManagementService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 权限操作管理控制器的冻结路由与安全上下文传递测试。
 */
public class AuthOperationControllerTest {

	private AuthOperationManagementService service;
	private AuthOperationController controller;

	@Before
	public void setUp() {
		service = mock(AuthOperationManagementService.class);
		controller = new AuthOperationController(service);
		SmartUser user = new SmartUser(1, 1, "operator", Arrays.asList(1, 2), "password",
				true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "password", Collections.emptyList()));
	}

	@After
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void exposesFrozenReadOnlyRoutes() throws Exception {
		RequestMapping root = AuthOperationController.class.getAnnotation(RequestMapping.class);
		assertThat(root.value()).containsExactly("/device/authority/operation");
		assertGetRoute("getBatchPage", AuthOperationBatchPageQuery.class, "/batch/page");
		assertGetRoute("getBatch", Long.class, "/batch/{batchId}");
		assertGetRoute("getTargetPage", AuthOperationTargetPageQuery.class, "/target/page");
	}

	@Test
	public void forwardsOnlyAuthenticatedParkScopeToBatchPage() {
		AuthOperationBatchPageQuery query = new AuthOperationBatchPageQuery();
		Page<AuthOperationBatchView> page = new Page<>(1, 20);
		when(service.getBatchPage(eq(query), eq(Arrays.asList(1, 2)))).thenReturn(page);

		Result result = controller.getBatchPage(query);

		assertThat(result.getData()).isSameAs(page);
		verify(service).getBatchPage(query, Arrays.asList(1, 2));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void preservesFailureReasonInBatchListAndDetailResults() {
		AuthOperationBatchView listView = AuthOperationBatchView.builder()
				.batchId("12").status("VERIFYING").failureReason("缺少设备依据，转人工核验").build();
		Page<AuthOperationBatchView> page = new Page<>(1, 20);
		page.setRecords(Collections.singletonList(listView));
		AuthOperationBatchDetailView detailView = AuthOperationBatchDetailView.builder()
				.batchId("12").status("VERIFYING").failureReason("缺少设备依据，转人工核验").build();
		AuthOperationBatchPageQuery query = new AuthOperationBatchPageQuery();
		when(service.getBatchPage(query, Arrays.asList(1, 2))).thenReturn(page);
		when(service.getBatch(12L, Arrays.asList(1, 2))).thenReturn(detailView);

		Result listResult = controller.getBatchPage(query);
		Result detailResult = controller.getBatch(12L);

		assertThat(((Page<AuthOperationBatchView>) listResult.getData()).getRecords().get(0).getFailureReason())
				.isEqualTo("缺少设备依据，转人工核验");
		assertThat(((AuthOperationBatchDetailView) detailResult.getData()).getFailureReason())
				.isEqualTo("缺少设备依据，转人工核验");
	}

	@Test
	public void forwardsOnlyAuthenticatedParkScopeToTargetPage() {
		AuthOperationTargetPageQuery query = new AuthOperationTargetPageQuery();
		query.setBatchId(12L);
		Page<AuthOperationTargetView> page = new Page<>(1, 20);
		when(service.getTargetPage(eq(query), eq(Arrays.asList(1, 2)))).thenReturn(page);

		Result result = controller.getTargetPage(query);

		assertThat(result.getData()).isSameAs(page);
		verify(service).getTargetPage(query, Arrays.asList(1, 2));
	}

	private void assertGetRoute(String methodName, Class<?> argumentType, String route) throws Exception {
		Method method = AuthOperationController.class.getMethod(methodName, argumentType);
		GetMapping mapping = method.getAnnotation(GetMapping.class);
		assertThat(mapping).isNotNull();
		assertThat(mapping.value()).containsExactly(route);
	}
}
