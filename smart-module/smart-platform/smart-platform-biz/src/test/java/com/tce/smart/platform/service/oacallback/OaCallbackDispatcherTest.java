package com.tce.smart.platform.service.oacallback;

import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.OaCallbackLog;
import com.tce.smart.platform.support.RedisMutexLock;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DuplicateKeyException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 分发器单测：隔离、跳过集合、partial 关闭、锁失败、唯一索引冲突 */
public class OaCallbackDispatcherTest {

	private RedisMutexLock lock;
	private OaCallbackLogService logService;
	private List<OaWorkflowCallbackHandler> handlers;
	private OaWorkflowCallbackHandler h1, h2;
	private OaCallbackDispatcher dispatcher;

	@Before
	public void setUp() {
		lock = mock(RedisMutexLock.class);
		logService = mock(OaCallbackLogService.class);
		h1 = mock(OaWorkflowCallbackHandler.class);
		when(h1.name()).thenReturn("h1");
		h2 = mock(OaWorkflowCallbackHandler.class);
		when(h2.name()).thenReturn("h2");
		handlers = Arrays.asList(h1, h2);
		when(lock.acquire(anyString(), anyLong())).thenReturn("token");
		when(logService.saveReceived(anyString(), anyString())).thenReturn(100L);
		when(logService.findLatestUnresolved(anyString())).thenReturn(null);
		dispatcher = new OaCallbackDispatcher(handlers, lock, logService);
	}

	private WorkFlowAO ao() {
		WorkFlowAO ao = new WorkFlowAO();
		ao.setRequestid("28753680");
		return ao;
	}

	@Test
	public void allSuccess_returns200Semantics_andLogStatus1() {
		DispatchResult r = dispatcher.dispatch(ao());
		assertTrue(r.isAllSuccess());
		ArgumentCaptor<OaCallbackLog> c = ArgumentCaptor.forClass(OaCallbackLog.class);
		verify(logService).updateById(c.capture());
		assertEquals(Integer.valueOf(OaCallbackLog.STATUS_SUCCESS), c.getValue().getStatus());
		assertEquals("h1,h2", c.getValue().getSucceededHandlers());
	}

	@Test
	public void oneHandlerThrows_othersStillRun_resultPartialFail() {
		// 隔离：h1 抛异常，h2 照常执行（spec §3.2.2）
		doThrow(new RuntimeException("boom")).when(h1).handle(anyString(), any());
		DispatchResult r = dispatcher.dispatch(ao());
		assertFalse(r.isAllSuccess());
		verify(h2).handle(eq("28753680"), any());
		ArgumentCaptor<OaCallbackLog> c = ArgumentCaptor.forClass(OaCallbackLog.class);
		verify(logService).updateById(c.capture());
		assertEquals(Integer.valueOf(OaCallbackLog.STATUS_PARTIAL_FAIL), c.getValue().getStatus());
		assertEquals("h1", c.getValue().getFailedHandlers());
	}

	@Test
	public void oldPartialExists_succeededHandlerSkipped_andOldResolved() {
		// 存在未解决 partial（h1 已成功）→ 只跑 h2，处理完关闭旧记录（spec §3.2.2 N1）
		OaCallbackLog old = new OaCallbackLog();
		old.setId(50L);
		old.setSucceededHandlers("h1");
		old.setStatus(OaCallbackLog.STATUS_PARTIAL_FAIL);
		old.setResolved(OaCallbackLog.RESOLVED_NO);
		when(logService.findLatestUnresolved("28753680")).thenReturn(old);
		dispatcher.dispatch(ao());
		verify(h1, never()).handle(anyString(), any());
		verify(h2).handle(anyString(), any());
		// 旧 partial 被无条件关闭
		ArgumentCaptor<OaCallbackLog> c = ArgumentCaptor.forClass(OaCallbackLog.class);
		verify(logService, times(2)).updateById(c.capture());
		OaCallbackLog closedOld = c.getAllValues().stream()
				.filter(l -> Long.valueOf(50L).equals(l.getId())).findFirst().orElse(null);
		assertNotNull(closedOld);
		assertEquals(Integer.valueOf(OaCallbackLog.RESOLVED_YES), closedOld.getResolved());
		// 新记录 succeeded 为合并值（跳过的 h1 + 本次成功的 h2）
		OaCallbackLog current = c.getAllValues().stream()
				.filter(l -> Long.valueOf(100L).equals(l.getId())).findFirst().orElse(null);
		assertEquals("h1,h2", current.getSucceededHandlers());
	}

	@Test
	public void lockExhausted_returnsFailure_noHandlerRuns() {
		// 锁重试耗尽 → 不执行任何 handler，结果失败（交给 OA 重试，spec §3.2.2）
		when(lock.acquire(anyString(), anyLong())).thenReturn(null);
		DispatchResult r = dispatcher.dispatch(ao());
		assertFalse(r.isAllSuccess());
		verify(h1, never()).handle(anyString(), any());
		verify(lock, times(3)).acquire(anyString(), anyLong());
	}

	@Test
	public void duplicateKeyOnPartialWrite_fallbackResolvedSnapshot() {
		// 唯一索引冲突（TTL 过期极端窗口）→ 落为 resolved=1 失败快照 + ERROR（spec §3.2.2）
		doThrow(new RuntimeException("boom")).when(h1).handle(anyString(), any());
		when(logService.updateById(argThat(l -> l != null
				&& Integer.valueOf(OaCallbackLog.STATUS_PARTIAL_FAIL).equals(l.getStatus())
				&& Integer.valueOf(OaCallbackLog.RESOLVED_NO).equals(l.getResolved()))))
				.thenThrow(new DuplicateKeyException("ux_oa_cb_unresolved"));
		DispatchResult r = dispatcher.dispatch(ao());
		assertFalse(r.isAllSuccess());
		// 二次回写为 resolved=1 快照
		verify(logService, atLeast(2)).updateById(any(OaCallbackLog.class));
	}

	@Test
	public void lockAlwaysReleased_evenWhenHandlerThrows() {
		doThrow(new RuntimeException("boom")).when(h1).handle(anyString(), any());
		dispatcher.dispatch(ao());
		verify(lock).release(eq("oa:callback:lock:28753680"), eq("token"));
	}

	@Test
	public void logSaveFails_processingContinues() {
		// 落库失败不阻断处理（spec §3.3）
		when(logService.saveReceived(anyString(), anyString())).thenReturn(null);
		DispatchResult r = dispatcher.dispatch(ao());
		assertTrue(r.isAllSuccess());
		verify(h1).handle(anyString(), any());
	}

	@Test
	public void ttlGreaterThanDerivedUpperBound() {
		// 硬校验：TTL 必须大于 handler 数 × 单 handler 最坏耗时（spec 终审 High）
		assertTrue(OaCallbackDispatcher.LOCK_TTL_SECONDS >
				OaCallbackDispatcher.HANDLER_COUNT * OaCallbackDispatcher.MAX_HANDLER_SECONDS);
	}

	@Test
	public void lockExhausted_writesLastErrorToLog() {
		// 锁获取失败后，writeLockFailure 将错误信息写入日志记录（spec §3.2.2）
		when(lock.acquire(anyString(), anyLong())).thenReturn(null);
		DispatchResult r = dispatcher.dispatch(ao());
		assertFalse(r.isAllSuccess());
		ArgumentCaptor<OaCallbackLog> c = ArgumentCaptor.forClass(OaCallbackLog.class);
		verify(logService).updateById(c.capture());
		OaCallbackLog capturedLog = c.getValue();
		assertEquals(Long.valueOf(100L), capturedLog.getId());
		assertEquals("acquire request_id lock timeout", capturedLog.getLastError());
	}
}
