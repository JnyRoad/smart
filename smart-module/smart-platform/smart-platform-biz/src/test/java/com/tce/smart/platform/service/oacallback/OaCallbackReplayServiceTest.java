package com.tce.smart.platform.service.oacallback;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.ao.WorkFlowAO;
import com.tce.smart.platform.core.entity.OaCallbackLog;
import com.tce.smart.platform.support.RedisMutexLock;
import com.tce.smart.platform.service.oacallback.impl.OaCallbackReplayServiceImpl;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;

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
	public void replay_onlyFailedHandlerRuns_successUpdatesOriginal() {
		when(logService.getById(100L)).thenReturn(partialLog());
		// CAS 回写成功
		when(logService.update(any(), any())).thenReturn(true);
		boolean ok = service.replay(100L).isSuccess();
		assertTrue(ok);
		verify(h1, never()).handle(anyString(), any());
		verify(h2).handle(eq("28753680"), any());
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
