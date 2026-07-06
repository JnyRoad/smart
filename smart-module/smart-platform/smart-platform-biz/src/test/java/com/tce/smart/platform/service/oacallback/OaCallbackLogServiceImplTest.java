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
}
