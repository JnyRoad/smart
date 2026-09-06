package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tce.smart.common.security.component.PermissionService;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ActionResult;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ReviewRow;
import com.tce.smart.platform.core.service.impl.AuthOperationGovernanceService;
import com.tce.smart.platform.dto.authgovernance.AuthOperationActionResultView;
import com.tce.smart.platform.dto.authgovernance.AuthOperationReviewPageQuery;
import com.tce.smart.platform.dto.authgovernance.AuthOperationRetryItem;
import com.tce.smart.platform.dto.authgovernance.AuthOperationRetryRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Web 编排必须从 SecurityContext 建 actor，并保持 64 位 ID 的字符串精度。 */
@RunWith(MockitoJUnitRunner.class)
public class AuthOperationManagementActionTest {

	@Mock
	private AuthOperationGovernanceService governance;
	private AuthOperationManagementActionService service;

	@Before
	public void setUp() {
		service = new AuthOperationManagementActionService(governance, new PermissionService(), new ObjectMapper());
	}

	@After
	public void clearSecurity() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void retryParsesLargeIdsExactlyAndNeverAcceptsActorFromRequest() {
		login(71, Collections.singletonList(17), "platform_auth_operation_retry");
		AuthOperationRetryRequest request = new AuthOperationRetryRequest();
		request.setIdempotencyKey("large-id");
		request.setReasonText("管理员请求重新领取完全未发送尝试");
		AuthOperationRetryItem item = new AuthOperationRetryItem();
		item.setTargetId("9007199254740993");
		item.setExpectedOperationVersion("9007199254740995");
		item.setExpectedAttemptId("9007199254740997");
		item.setExpectedAttemptNo(1);
		item.setExpectedState("EXECUTING");
		request.setTargets(Collections.singletonList(item));
		when(governance.retryKnownUnsent(any(), any())).thenReturn(ActionResult.builder()
				.actionId(9007199254740999L).targetId(9007199254740993L).outcome("REQUEUED").build());

		String actionId = service.retry(request).get(0).getActionId();

		assertThat(actionId).isEqualTo("9007199254740999");
		ArgumentCaptor<com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.RetryCommand> command =
				ArgumentCaptor.forClass(com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.RetryCommand.class);
		org.mockito.Mockito.verify(governance).retryKnownUnsent(any(), command.capture());
		assertThat(command.getValue().getTargetId()).isEqualTo(9007199254740993L);
		assertThat(command.getValue().getExpectedOperationVersion()).isEqualTo(9007199254740995L);
		assertThat(command.getValue().getIdempotencyKey()).isEqualTo("large-id");
	}

	@Test
	public void ordinaryReviewRejectsEmptyParkScopeWhileExplicitGlobalPermissionStillWorks() {
		login(72, Collections.emptyList(), "platform_auth_operation_global_review_view");
		AuthOperationReviewPageQuery query = new AuthOperationReviewPageQuery();
		query.setParkId(17);
		assertThatThrownBy(() -> service.getParkReviews(query))
				.isInstanceOf(org.springframework.security.access.AccessDeniedException.class);

		Page<ReviewRow> page = new Page<>(1, 20);
		when(governance.getGlobalReviews(any(), org.mockito.ArgumentMatchers.eq(1), org.mockito.ArgumentMatchers.eq(20)))
				.thenReturn(page);
		assertThat(service.getGlobalReviews(new AuthOperationReviewPageQuery()).getTotal()).isZero();
	}

	@Test
	public void parkPermissionCannotOpenGlobalReview() {
		login(73, Collections.singletonList(17), "platform_auth_operation_review_view");
		assertThatThrownBy(() -> service.getGlobalReviews(new AuthOperationReviewPageQuery()))
				.isInstanceOf(org.springframework.security.access.AccessDeniedException.class);
	}

	@Test
	public void retryRejectsMoreThanOneHundredTargetsBeforeCoreCalls() {
		login(74, Collections.singletonList(17), "platform_auth_operation_retry");
		AuthOperationRetryRequest request = new AuthOperationRetryRequest();
		request.setIdempotencyKey("too-many");
		request.setReasonText("批量范围越界");
		AuthOperationRetryItem item = new AuthOperationRetryItem();
		item.setTargetId("1"); item.setExpectedOperationVersion("1"); item.setExpectedAttemptId("1");
		item.setExpectedAttemptNo(1); item.setExpectedState("EXECUTING");
		request.setTargets(Collections.nCopies(101, item));

		assertThatThrownBy(() -> service.retry(request)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("100");
	}

	@Test
	public void duplicateKeyRaceIsReplayedThroughASecondCoreTransactionCall() {
		login(75, Collections.singletonList(17), "platform_auth_operation_retry");
		AuthOperationRetryRequest request = new AuthOperationRetryRequest();
		request.setIdempotencyKey("concurrent-one");
		request.setReasonText("并发请求复用相同规范载荷");
		AuthOperationRetryItem item = new AuthOperationRetryItem();
		item.setTargetId("9007199254740993");
		item.setExpectedOperationVersion("7");
		item.setExpectedAttemptId("9007199254740995");
		item.setExpectedAttemptNo(2);
		item.setExpectedState("EXECUTING");
		request.setTargets(Collections.singletonList(item));
		when(governance.retryKnownUnsent(any(), any()))
				.thenThrow(new DuplicateKeyException("唯一键竞争"))
				.thenReturn(ActionResult.builder().actionId(9007199254740997L)
						.targetId(9007199254740993L).outcome("ALREADY_APPLIED").replay(true).build());

		AuthOperationActionResultView result = service.retry(request).get(0);

		assertThat(result.getOutcome()).isEqualTo("ALREADY_APPLIED");
		assertThat(result.isReplay()).isTrue();
		verify(governance, times(2)).retryKnownUnsent(any(), any());
	}

	private void login(int id, java.util.List<Integer> parks, String... permissions) {
		java.util.List<SimpleGrantedAuthority> authorities = new java.util.ArrayList<>();
		for (String permission : permissions) authorities.add(new SimpleGrantedAuthority(permission));
		SmartUser user = new SmartUser(id, 1, "user-" + id, parks, "pwd", true, true, true, true, authorities);
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "pwd", authorities));
	}
}
