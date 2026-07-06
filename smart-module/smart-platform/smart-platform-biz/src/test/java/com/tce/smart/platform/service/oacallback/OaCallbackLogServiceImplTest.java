package com.tce.smart.platform.service.oacallback;

import com.tce.smart.platform.core.entity.OaCallbackLog;
import com.tce.smart.platform.core.mapper.OaCallbackLogMapper;
import com.tce.smart.platform.service.oacallback.impl.OaCallbackLogServiceImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** OaCallbackLogService 单测 */
public class OaCallbackLogServiceImplTest {

	private OaCallbackLogServiceImpl service;
	private OaCallbackLogMapper mapper;
	/** cleanExpiredLogs 桩数据：list() 返回的未解决 partial */
	private java.util.List<OaCallbackLog> stubUnresolvedExpired = new java.util.ArrayList<>();
	/** cleanExpiredLogs 桩数据：count() 返回的过期总行数 */
	private int stubExpiredCount = 0;
	/** 记录 remove 是否被调用 */
	private boolean removeCalled = false;

	@Before
	public void setUp() {
		mapper = mock(OaCallbackLogMapper.class);
		service = new OaCallbackLogServiceImpl() {
			// ServiceImpl.save 依赖 Spring 注入的 baseMapper，这里覆写为直接走 mock mapper
			@Override
			public boolean save(OaCallbackLog entity) {
				entity.setId(9L);
				return mapper.insert(entity) > 0;
			}

			// cleanExpiredLogs 依赖的 MP 查询方法全部覆写为桩，隔离数据库
			@Override
			public java.util.List<OaCallbackLog> list(com.baomidou.mybatisplus.core.conditions.Wrapper<OaCallbackLog> wrapper) {
				return stubUnresolvedExpired;
			}

			@Override
			public int count(com.baomidou.mybatisplus.core.conditions.Wrapper<OaCallbackLog> wrapper) {
				return stubExpiredCount;
			}

			@Override
			public boolean remove(com.baomidou.mybatisplus.core.conditions.Wrapper<OaCallbackLog> wrapper) {
				removeCalled = true;
				return true;
			}
		};
	}

	@Test
	public void saveReceived_success_returnsId() {
		when(mapper.insert(any())).thenReturn(1);
		Long id = service.saveReceived("28753680", "{\"requestid\":\"28753680\"}");
		assertNotNull(id);
	}

	@Test
	public void saveReceived_insertThrows_returnsNullNotThrow() {
		// 落库失败仅记日志不阻断分发（spec §3.3）
		when(mapper.insert(any())).thenThrow(new RuntimeException("db down"));
		assertNull(service.saveReceived("28753680", "{}"));
	}

	@Test
	public void cleanExpiredLogs_deletesAndReturnsCount() {
		stubExpiredCount = 5;
		int deleted = service.cleanExpiredLogs();
		assertEquals(5, deleted);
		assertTrue("有过期数据必须执行删除", removeCalled);
	}

	@Test
	public void cleanExpiredLogs_noExpired_skipsDelete() {
		stubExpiredCount = 0;
		int deleted = service.cleanExpiredLogs();
		assertEquals(0, deleted);
		assertFalse("无过期数据不应执行删除", removeCalled);
	}

	@Test
	public void cleanExpiredLogs_withUnresolvedPartial_stillDeletes() {
		// 90 天未重放的 partial 一并删除（留存承诺优先，WARN 日志兜底可见性）
		stubExpiredCount = 3;
		OaCallbackLog partial = new OaCallbackLog();
		partial.setRequestId("28753680");
		stubUnresolvedExpired.add(partial);
		int deleted = service.cleanExpiredLogs();
		assertEquals(3, deleted);
		assertTrue(removeCalled);
	}
}
