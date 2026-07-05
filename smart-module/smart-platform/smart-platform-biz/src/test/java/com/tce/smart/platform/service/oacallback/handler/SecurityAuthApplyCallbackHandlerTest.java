package com.tce.smart.platform.service.oacallback.handler;

import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthApply;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthApplyService;
import com.tce.smart.tool.enums.ApproveListStateEnum;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * Task 22 单测：验证 handler 已改接 CAS claim 流程（spec §3.2.1）。
 * 覆盖：命中+通过+claim 成功触发下发；命中+通过+claim 失败（对账已处理）不下发；
 * 命中+回退 claim REFUSE 且不下发；未命中不产生任何交互。
 */
public class SecurityAuthApplyCallbackHandlerTest {

	private SmtSecurityAuthApplyService smtSecurityAuthApplyService;
	private OaFlowRecordSupport flowRecordSupport;
	private SecurityAuthApplyCallbackHandler handler;

	@Before
	public void setUp() {
		smtSecurityAuthApplyService = mock(SmtSecurityAuthApplyService.class);
		flowRecordSupport = mock(OaFlowRecordSupport.class);
		handler = new SecurityAuthApplyCallbackHandler();
		setField(handler, "smtSecurityAuthApplyService", smtSecurityAuthApplyService);
		setField(handler, "flowRecordSupport", flowRecordSupport);
	}

	private void setField(Object target, String name, Object value) {
		try {
			java.lang.reflect.Field field = target.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(target, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 用例1：命中 + 无回退（审批通过）+ claim 成功 → claimOaFinalStatus(id, AGREE) 且 triggerDownDevice 被调。
	 */
	@Test
	public void handle_hitNoReturnClaimSucceeds_triggersDownDevice() {
		SmtSecurityAuthApply authApply = new SmtSecurityAuthApply();
		authApply.setId(1001L);
		when(smtSecurityAuthApplyService.getByProcessId("P1")).thenReturn(authApply);
		when(flowRecordSupport.processAndDetectReturn(eq("P1"), anyList())).thenReturn(true);
		when(smtSecurityAuthApplyService.claimOaFinalStatus(1001L, ApproveListStateEnum.AGREE.getCode())).thenReturn(true);

		WorkFlowAO ao = new WorkFlowAO();
		ao.setFlowRecord(Collections.emptyList());
		handler.handle("P1", ao);

		verify(smtSecurityAuthApplyService).claimOaFinalStatus(1001L, ApproveListStateEnum.AGREE.getCode());
		verify(smtSecurityAuthApplyService).triggerDownDevice(authApply);
		verify(smtSecurityAuthApplyService, never()).updateStatus(any(SmtSecurityAuthApply.class));
	}

	/**
	 * 用例2：命中 + 无回退 + claim 失败（对账任务已抢先处理终态）→ triggerDownDevice 不被调。
	 */
	@Test
	public void handle_hitNoReturnClaimFails_doesNotTriggerDownDevice() {
		SmtSecurityAuthApply authApply = new SmtSecurityAuthApply();
		authApply.setId(1002L);
		when(smtSecurityAuthApplyService.getByProcessId("P2")).thenReturn(authApply);
		when(flowRecordSupport.processAndDetectReturn(eq("P2"), anyList())).thenReturn(true);
		when(smtSecurityAuthApplyService.claimOaFinalStatus(1002L, ApproveListStateEnum.AGREE.getCode())).thenReturn(false);

		WorkFlowAO ao = new WorkFlowAO();
		ao.setFlowRecord(Collections.emptyList());
		handler.handle("P2", ao);

		verify(smtSecurityAuthApplyService).claimOaFinalStatus(1002L, ApproveListStateEnum.AGREE.getCode());
		verify(smtSecurityAuthApplyService, never()).triggerDownDevice(any(SmtSecurityAuthApply.class));
	}

	/**
	 * 用例3：命中 + 回退（审批被退回）→ claimOaFinalStatus(id, REFUSE)，即便 claim 成功也不触发下发。
	 */
	@Test
	public void handle_hitWithReturn_claimsRefuseAndNeverTriggersDownDevice() {
		SmtSecurityAuthApply authApply = new SmtSecurityAuthApply();
		authApply.setId(1003L);
		when(smtSecurityAuthApplyService.getByProcessId("P3")).thenReturn(authApply);
		when(flowRecordSupport.processAndDetectReturn(eq("P3"), anyList())).thenReturn(false);
		when(smtSecurityAuthApplyService.claimOaFinalStatus(1003L, ApproveListStateEnum.REFUSE.getCode())).thenReturn(true);

		WorkFlowAO ao = new WorkFlowAO();
		ao.setFlowRecord(Collections.emptyList());
		handler.handle("P3", ao);

		verify(smtSecurityAuthApplyService).claimOaFinalStatus(1003L, ApproveListStateEnum.REFUSE.getCode());
		verify(smtSecurityAuthApplyService, never()).triggerDownDevice(any(SmtSecurityAuthApply.class));
	}

	/**
	 * 用例4：未命中（getByProcessId 返回 null）→ 不做任何后续交互，flowRecordSupport 也不应被调用。
	 */
	@Test
	public void handle_noMatch_doesNothing() {
		when(smtSecurityAuthApplyService.getByProcessId("P4")).thenReturn(null);

		WorkFlowAO ao = new WorkFlowAO();
		ao.setFlowRecord(Collections.emptyList());
		handler.handle("P4", ao);

		// 项目里 Mockito 版本较旧（Spring Boot 2.1 默认管理的 2.x），没有 verifyNoInteractions，用 verifyZeroInteractions 代替
		verifyZeroInteractions(flowRecordSupport);
		verify(smtSecurityAuthApplyService, never()).claimOaFinalStatus(any(Long.class), any(Integer.class));
		verify(smtSecurityAuthApplyService, never()).triggerDownDevice(any(SmtSecurityAuthApply.class));
		verify(smtSecurityAuthApplyService, never()).updateStatus(any(SmtSecurityAuthApply.class));
	}
}
