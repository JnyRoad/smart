package com.tce.smart.platform.service.oacallback.handler;

import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.ao.WorkFlowRecordAO;
import com.tce.smart.platform.core.entity.SmtVisitor;
import com.tce.smart.platform.service.SmtVisitorService;
import com.tce.smart.platform.service.oacallback.OaFlowRecordSupport;
import com.tce.smart.tool.enums.NodeStatusEnum;
import com.tce.smart.tool.enums.VisitorStatusEnum;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;

/**
 * 合肥访客预约回调 handler 单测：
 * 1. 待审核判断必须用 Integer code 比较（修复原"枚举实例.equals(Integer)"恒 false 的死分支）；
 * 2. 终态落库走 claim 式幂等入口，与拉取对账（updateOaStatusTask）互斥，避免重复下发/重复短信。
 */
public class HfVisitorCallbackHandlerTest {

	private SmtVisitorService visitorService;
	private OaFlowRecordSupport flowRecordSupport;
	private HfVisitorCallbackHandler handler;

	@Before
	public void setUp() throws Exception {
		visitorService = Mockito.mock(SmtVisitorService.class);
		flowRecordSupport = Mockito.mock(OaFlowRecordSupport.class);
		handler = new HfVisitorCallbackHandler();
		setField(handler, "smtVisitorService", visitorService);
		setField(handler, "flowRecordSupport", flowRecordSupport);
	}

	@Test
	public void pendingVisitorWithIntegerStatus_approvedFlow_claimsPassStatus() {
		SmtVisitor visitor = pendingVisitor();
		Mockito.when(visitorService.getOne(Mockito.any())).thenReturn(visitor);
		Mockito.when(flowRecordSupport.processAndDetectReturn(Mockito.eq("p-1"), Mockito.anyList())).thenReturn(true);
		Mockito.when(visitorService.claimAndApplyHfOaFinalStatus(visitor, VisitorStatusEnum.Status_0.getCode())).thenReturn(true);

		handler.handle("p-1", ao());

		//核心断言：status 为 Integer 2 时必须进入处理分支（原枚举比较写法此处恒不进入）
		Mockito.verify(flowRecordSupport).processAndDetectReturn(Mockito.eq("p-1"), Mockito.anyList());
		Mockito.verify(visitorService).claimAndApplyHfOaFinalStatus(visitor, VisitorStatusEnum.Status_0.getCode());
		//终态只能经 claim 入口落库，handler 不得直接调 updateHfStatus 绕过 CAS
		Mockito.verify(visitorService, Mockito.never()).updateHfStatus(Mockito.any(SmtVisitor.class));
	}

	@Test
	public void pendingVisitor_returnedFlow_claimsRejectStatus() {
		SmtVisitor visitor = pendingVisitor();
		Mockito.when(visitorService.getOne(Mockito.any())).thenReturn(visitor);
		Mockito.when(flowRecordSupport.processAndDetectReturn(Mockito.eq("p-2"), Mockito.anyList())).thenReturn(false);
		Mockito.when(visitorService.claimAndApplyHfOaFinalStatus(visitor, VisitorStatusEnum.Status_1.getCode())).thenReturn(true);

		handler.handle("p-2", ao());

		Mockito.verify(visitorService).claimAndApplyHfOaFinalStatus(visitor, VisitorStatusEnum.Status_1.getCode());
	}

	@Test
	public void claimLostToReconciliationTask_skipsQuietly() {
		SmtVisitor visitor = pendingVisitor();
		Mockito.when(visitorService.getOne(Mockito.any())).thenReturn(visitor);
		Mockito.when(flowRecordSupport.processAndDetectReturn(Mockito.eq("p-3"), Mockito.anyList())).thenReturn(true);
		//拉取对账已抢占终态 → 回调放弃，不得再直接写状态
		Mockito.when(visitorService.claimAndApplyHfOaFinalStatus(Mockito.any(SmtVisitor.class), Mockito.anyInt()))
				.thenReturn(false);

		handler.handle("p-3", ao());

		Mockito.verify(visitorService, Mockito.never()).updateHfStatus(Mockito.any(SmtVisitor.class));
	}

	@Test
	public void nonPendingVisitor_skipsProcessing() {
		SmtVisitor visitor = pendingVisitor();
		visitor.setStatus(VisitorStatusEnum.Status_0.getCode());
		Mockito.when(visitorService.getOne(Mockito.any())).thenReturn(visitor);

		handler.handle("p-4", ao());

		Mockito.verifyZeroInteractions(flowRecordSupport);
		Mockito.verify(visitorService, Mockito.never())
				.claimAndApplyHfOaFinalStatus(Mockito.any(SmtVisitor.class), Mockito.anyInt());
	}

	@Test
	public void missingVisitor_skipsProcessing() {
		Mockito.when(visitorService.getOne(Mockito.any())).thenReturn(null);

		handler.handle("p-5", ao());

		Mockito.verifyZeroInteractions(flowRecordSupport);
	}

	/** 状态为 Integer 2（待审核）的访客预约 */
	private SmtVisitor pendingVisitor() {
		SmtVisitor visitor = new SmtVisitor();
		visitor.setId(9101L);
		visitor.setStatus(VisitorStatusEnum.Status_2.getCode());
		visitor.setEndTime(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000L));
		return visitor;
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
