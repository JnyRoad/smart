package com.tce.smart.platform.service.oacallback;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.OaCallbackLog;
import com.tce.smart.platform.support.RedisMutexLock;
import com.tce.smart.platform.service.oacallback.impl.OaCallbackReplayServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** 重放服务单测（spec §3.3 N2 + 三审缺口 3） */
public class OaCallbackReplayServiceTest {

	private RedisMutexLock lock;
	private OaCallbackLogService logService;
	private OaWorkflowCallbackHandler h1, h2;
	private OaCallbackReplayServiceImpl service;

	/**
	 * 手动预热 MyBatis-Plus lambda 缓存：纯单测无 Spring 容器扫描 mapper，
	 * LambdaUpdateWrapper 需要 TableInfoHelper 缓存过的实体元数据才能解析字段（与 SmtVisitorServiceImplTest 同做法）。
	 */
	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), OaCallbackLog.class);
	}

	@Before
	public void setUp() {
		lock = mock(RedisMutexLock.class);
		logService = mock(OaCallbackLogService.class);
		h1 = mock(OaWorkflowCallbackHandler.class);
		when(h1.name()).thenReturn("h1");
		h2 = mock(OaWorkflowCallbackHandler.class);
		when(h2.name()).thenReturn("h2");
		service = new OaCallbackReplayServiceImpl(Arrays.asList(h1, h2), lock, logService);
		when(lock.acquire(anyString(), anyLong())).thenReturn("token");
	}

	/**
	 * 从 LambdaUpdateWrapper#getSqlSet() 里为指定列解析出其绑定的占位符名（如 MPGENVAL3），
	 * 再从 paramNameValuePairs 里按该占位符名精确取值。
	 * 三审缺口修复：避免用 boundValues.contains(xxx) 做整体断言——多个 set 列的绑定值
	 * 恰好都是同一个值（如 1）时，contains() 断言会互相掩护，改错某一列实现也测不出来。
	 */
	private String boundParamFor(String sqlSet, String column) {
		Pattern pattern = Pattern.compile(Pattern.quote(column) + "=#\\{ew\\.paramNameValuePairs\\.(MPGENVAL\\d+)\\}");
		Matcher matcher = pattern.matcher(sqlSet);
		assertTrue("sqlSet 中未找到列 " + column + " 对应的绑定占位符：" + sqlSet, matcher.find());
		return matcher.group(1);
	}

	private OaCallbackLog partialLog() {
		OaCallbackLog log = new OaCallbackLog();
		log.setId(100L);
		log.setRequestId("28753680");
		WorkFlowAO ao = new WorkFlowAO();
		ao.setRequestid("28753680");
		log.setPayload(JSONUtil.toJsonStr(ao));
		log.setStatus(OaCallbackLog.STATUS_PARTIAL_FAIL);
		log.setResolved(OaCallbackLog.RESOLVED_NO);
		log.setSucceededHandlers("h1");
		log.setFailedHandlers("h2");
		log.setRetryCount(0);
		return log;
	}

	@Test
	@SuppressWarnings("unchecked")
	public void replay_onlyFailedHandlerRuns_successUpdatesOriginal() {
		when(logService.getById(100L)).thenReturn(partialLog());
		// CAS 回写成功
		when(logService.update(any(), any())).thenReturn(true);
		boolean ok = service.replay(100L).isSuccess();
		assertTrue(ok);
		verify(h1, never()).handle(anyString(), any());
		verify(h2).handle(eq("28753680"), any());

		// 三审缺口修复：仅验证 handler 调用不足以防"条件颠倒/漏 set"类回归，
		// 必须捕获实际回写的 Wrapper 内容并断言 set 的列名与绑定值。
		ArgumentCaptor<Wrapper<OaCallbackLog>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
		verify(logService).update(isNull(), wrapperCaptor.capture());
		LambdaUpdateWrapper<OaCallbackLog> wrapper = (LambdaUpdateWrapper<OaCallbackLog>) wrapperCaptor.getValue();
		String sqlSet = wrapper.getSqlSet();
		Map<String, Object> params = wrapper.getParamNameValuePairs();
		// 三审缺口修复：按列名精确定位各自的绑定占位符再断言，
		// 避免多列断言共用同一集合 contains() 造成互相掩护（评审者已用变异测试实证）。
		// retry_count 由原 retryCount=0 自增为 1
		assertEquals("retry_count 绑定值应为自增后的 1", Integer.valueOf(1),
				params.get(boundParamFor(sqlSet, "retry_count")));
		// succeeded_handlers 合并为 h1,h2（h1 原本已成功，h2 本次重放成功）
		assertEquals("succeeded_handlers 绑定值应为合并后的 h1,h2", "h1,h2",
				params.get(boundParamFor(sqlSet, "succeeded_handlers")));
		// 全部成功：status 置为 1（STATUS_SUCCESS），resolved 置为 1（RESOLVED_YES）
		assertEquals("status 绑定值应为 STATUS_SUCCESS", Integer.valueOf(OaCallbackLog.STATUS_SUCCESS),
				params.get(boundParamFor(sqlSet, "status")));
		assertEquals("resolved 绑定值应为 RESOLVED_YES", Integer.valueOf(OaCallbackLog.RESOLVED_YES),
				params.get(boundParamFor(sqlSet, "resolved")));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void replay_stillFailing_keepsUnresolvedAndUpdatesFailure() {
		when(logService.getById(100L)).thenReturn(partialLog());
		// h2 重放依旧失败，模拟"重放后仍有失败"分支
		doThrow(new RuntimeException("still boom")).when(h2).handle(anyString(), any());
		when(logService.update(any(), any())).thenReturn(true);

		boolean ok = service.replay(100L).isSuccess();
		assertFalse(ok);
		verify(h1, never()).handle(anyString(), any());
		verify(h2).handle(eq("28753680"), any());

		ArgumentCaptor<Wrapper<OaCallbackLog>> wrapperCaptor = ArgumentCaptor.forClass(Wrapper.class);
		verify(logService).update(isNull(), wrapperCaptor.capture());
		LambdaUpdateWrapper<OaCallbackLog> wrapper = (LambdaUpdateWrapper<OaCallbackLog>) wrapperCaptor.getValue();
		String sqlSet = wrapper.getSqlSet();
		Collection<Object> boundValues = wrapper.getParamNameValuePairs().values();
		// 仍未解决：status 保持 2（STATUS_PARTIAL_FAIL），resolved 保持 0（RESOLVED_NO）
		assertTrue("sqlSet 应包含 status", sqlSet.contains("status"));
		assertTrue("绑定值应包含 STATUS_PARTIAL_FAIL=2", boundValues.contains(OaCallbackLog.STATUS_PARTIAL_FAIL));
		assertTrue("sqlSet 应包含 resolved", sqlSet.contains("resolved"));
		assertTrue("绑定值应包含 RESOLVED_NO=0", boundValues.contains(OaCallbackLog.RESOLVED_NO));
		// failed_handlers 应保留仍失败的 h2
		assertTrue("sqlSet 应包含 failed_handlers", sqlSet.contains("failed_handlers"));
		assertTrue("绑定值应包含仍失败的 h2", boundValues.contains("h2"));
		// last_error 非空（记录本次失败摘要）
		assertTrue("sqlSet 应包含 last_error", sqlSet.contains("last_error"));
		boolean hasNonBlankLastError = boundValues.stream()
				.anyMatch(v -> v instanceof String && ((String) v).contains("h2"));
		assertTrue("绑定值应包含含 handler 名的非空 lastError", hasNonBlankLastError);
		// retry_count 由原 retryCount=0 自增为 1（仍算一次重放尝试）
		assertTrue("绑定值应包含自增后的 retryCount=1", boundValues.contains(1));
	}

	@Test
	public void replay_lockHeld_rejected() {
		when(logService.getById(100L)).thenReturn(partialLog());
		when(lock.acquire(anyString(), anyLong())).thenReturn(null);
		assertFalse(service.replay(100L).isSuccess());
		verify(h2, never()).handle(anyString(), any());
	}

	@Test
	public void replay_alreadyResolved_rejected() {
		OaCallbackLog resolved = partialLog();
		resolved.setResolved(OaCallbackLog.RESOLVED_YES);
		when(logService.getById(100L)).thenReturn(resolved);
		assertFalse(service.replay(100L).isSuccess());
		verify(lock, never()).acquire(anyString(), anyLong());
	}

	@Test
	public void replay_notFound_rejected() {
		when(logService.getById(100L)).thenReturn(null);
		assertFalse(service.replay(100L).isSuccess());
	}
}
