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
			public OaCallbackLogMapper getBaseMapper() {
				return mapper;
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
		// 返回值必须来自 mapper.delete 的真实受影响行数，而非旁证的 count
		when(mapper.delete(any())).thenReturn(5);
		int deleted = service.cleanExpiredLogs();
		assertEquals(5, deleted);
		verify(mapper).delete(any());
	}

	@Test
	public void cleanExpiredLogs_noExpired_returnsZero() {
		when(mapper.delete(any())).thenReturn(0);
		int deleted = service.cleanExpiredLogs();
		assertEquals(0, deleted);
	}

	@Test
	public void cleanExpiredLogs_withUnresolvedPartial_stillDeletes() {
		// 90 天未重放的 partial 一并删除（留存承诺优先，WARN 日志兜底可见性）
		OaCallbackLog partial = new OaCallbackLog();
		partial.setRequestId("28753680");
		stubUnresolvedExpired.add(partial);
		when(mapper.delete(any())).thenReturn(3);
		int deleted = service.cleanExpiredLogs();
		assertEquals(3, deleted);
	}
}
