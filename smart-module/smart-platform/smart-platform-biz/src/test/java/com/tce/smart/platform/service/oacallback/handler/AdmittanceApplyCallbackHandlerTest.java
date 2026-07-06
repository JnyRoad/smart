package com.tce.smart.platform.service.oacallback.handler;

import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.ao.WorkFlowRecordAO;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.tool.enums.NodeStatusEnum;
import com.tce.smart.tool.enums.VisitorStatusEnum;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 入厂申请回调 handler 单测：
 * 1. 待审核判断必须用 Integer code 比较（修复原"枚举实例.equals(Integer)"恒 false 的死分支）；
 * 2. 终态落库走 claim 式幂等入口，与拉取对账（updateOaStatusTask）互斥，避免重复下发。
 */
public class AdmittanceApplyCallbackHandlerTest {

	private SmtAdmittanceApplyService applyService;
	private OaFlowRecordSupport flowRecordSupport;
	private AdmittanceApplyCallbackHandler handler;

	@Before
	public void setUp() throws Exception {
		applyService = Mockito.mock(SmtAdmittanceApplyService.class);
		flowRecordSupport = Mockito.mock(OaFlowRecordSupport.class);
		handler = new AdmittanceApplyCallbackHandler();
		setField(handler, "smtAdmittanceApplyService", applyService);
		setField(handler, "flowRecordSupport", flowRecordSupport);
	}

	@Test
	public void pendingApplyWithIntegerStatus_approvedFlow_claimsPassStatus() {
		SmtAdmittanceApply apply = pendingApply();
		Mockito.when(applyService.getByProcessId("p-1")).thenReturn(apply);
		Mockito.when(flowRecordSupport.processAndDetectReturn(Mockito.eq("p-1"), Mockito.anyList())).thenReturn(true);
		Mockito.when(applyService.claimAndApplyOaFinalStatus(apply, VisitorStatusEnum.Status_0.getCode())).thenReturn(true);

		handler.handle("p-1", ao());

		//核心断言：status 为 Integer 2 时必须进入处理分支（原枚举比较写法此处恒不进入）
		Mockito.verify(flowRecordSupport).processAndDetectReturn(Mockito.eq("p-1"), Mockito.anyList());
		Mockito.verify(applyService).claimAndApplyOaFinalStatus(apply, VisitorStatusEnum.Status_0.getCode());
		//终态只能经 claim 入口落库，handler 不得直接调 updateStatus 绕过 CAS
		Mockito.verify(applyService, Mockito.never()).updateStatus(Mockito.any(SmtAdmittanceApply.class));
	}

	@Test
	public void pendingApply_returnedFlow_claimsRejectStatus() {
		SmtAdmittanceApply apply = pendingApply();
		Mockito.when(applyService.getByProcessId("p-2")).thenReturn(apply);
		Mockito.when(flowRecordSupport.processAndDetectReturn(Mockito.eq("p-2"), Mockito.anyList())).thenReturn(false);
		Mockito.when(applyService.claimAndApplyOaFinalStatus(apply, VisitorStatusEnum.Status_1.getCode())).thenReturn(true);

		handler.handle("p-2", ao());

		Mockito.verify(applyService).claimAndApplyOaFinalStatus(apply, VisitorStatusEnum.Status_1.getCode());
	}

	@Test
	public void claimLostToReconciliationTask_skipsQuietly() {
		SmtAdmittanceApply apply = pendingApply();
		Mockito.when(applyService.getByProcessId("p-3")).thenReturn(apply);
		Mockito.when(flowRecordSupport.processAndDetectReturn(Mockito.eq("p-3"), Mockito.anyList())).thenReturn(true);
		//拉取对账已抢占终态 → 回调放弃，不得再直接写状态
		Mockito.when(applyService.claimAndApplyOaFinalStatus(Mockito.any(SmtAdmittanceApply.class), Mockito.anyInt()))
				.thenReturn(false);

		handler.handle("p-3", ao());

		Mockito.verify(applyService, Mockito.never()).updateStatus(Mockito.any(SmtAdmittanceApply.class));
	}

	@Test
	public void nonPendingApply_skipsProcessing() {
		SmtAdmittanceApply apply = pendingApply();
		apply.setStatus(VisitorStatusEnum.Status_0.getCode());
		Mockito.when(applyService.getByProcessId("p-4")).thenReturn(apply);

		handler.handle("p-4", ao());

		Mockito.verifyZeroInteractions(flowRecordSupport);
		Mockito.verify(applyService, Mockito.never())
				.claimAndApplyOaFinalStatus(Mockito.any(SmtAdmittanceApply.class), Mockito.anyInt());
	}

	@Test
	public void missingApply_skipsProcessing() {
		Mockito.when(applyService.getByProcessId("p-5")).thenReturn(null);

		handler.handle("p-5", ao());

		Mockito.verifyZeroInteractions(flowRecordSupport);
	}

	/** 状态为 Integer 2（待审核）的入厂申请 */
	private SmtAdmittanceApply pendingApply() {
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(9001L);
		apply.setStatus(VisitorStatusEnum.Status_2.getCode());
		apply.setEndTime(LocalDateTime.now().plusDays(1));
		return apply;
	}

	private WorkFlowAO ao() {
		WorkFlowRecordAO record = new WorkFlowRecordAO();
		record.setLogtype(NodeStatusEnum.APPROVE.getCode());
		WorkFlowAO ao = new WorkFlowAO();
		ao.setFlowRecord(Collections.singletonList(record));
		return ao;
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException ignored) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name + " on " + target.getClass());
	}
}
