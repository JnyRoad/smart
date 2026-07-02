package com.tce.smart.admin.service.impl;

import com.tce.smart.admin.api.entity.SysUser;
import com.tce.smart.admin.mapper.SysUserMapper;
import com.tce.smart.common.core.model.Result;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * {@link MobileServiceImpl#sendSmsCode(String)} 的安全回归测试。
 *
 * <p>覆盖两个高危缺陷：验证码明文回显（信息泄露）与限流读写 key 不一致（限流失效）。</p>
 */
@RunWith(MockitoJUnitRunner.class)
public class MobileServiceImplTest {

	private static final String MOBILE = "13800001111";

	@Mock
	@SuppressWarnings("rawtypes")
	private RedisTemplate redisTemplate;

	@Mock
	@SuppressWarnings("rawtypes")
	private ValueOperations valueOperations;

	@Mock
	private SysUserMapper userMapper;

	@InjectMocks
	private MobileServiceImpl mobileService;

	@Captor
	private ArgumentCaptor<String> keyCaptor;

	@Captor
	private ArgumentCaptor<String> valueCaptor;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() {
		// 手机号已注册，跳过“未注册”分支
		when(userMapper.selectList(any())).thenReturn(Collections.singletonList(new SysUser()));
		when(redisTemplate.opsForValue()).thenReturn(valueOperations);
	}

	/**
	 * 缺陷 1：验证码不得回显到响应 msg，否则免鉴权接口等于把码直接送给调用方。
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void sendSmsCode_doesNotEchoCodeInResponse() {
		// 无历史验证码，走正常下发分支
		when(valueOperations.get(anyString())).thenReturn(null);

		Result<Boolean> result = mobileService.sendSmsCode(MOBILE);

		// 捕获真正写入 redis 的验证码
		org.mockito.Mockito.verify(valueOperations)
			.set(anyString(), valueCaptor.capture(), anyLong(), any(TimeUnit.class));
		String writtenCode = valueCaptor.getValue();

		assertEquals(Boolean.TRUE, result.getData());
		assertNotEquals("验证码不得回显到响应体", writtenCode, result.getMsg());
	}

	/**
	 * 缺陷 2：限流读取的 key 必须与写入的 key 一致，否则“发送过频繁”检查永不命中。
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void sendSmsCode_readAndWriteUseSameKey() {
		when(valueOperations.get(keyCaptor.capture())).thenReturn(null);

		mobileService.sendSmsCode(MOBILE);

		String readKey = keyCaptor.getValue();
		org.mockito.Mockito.verify(valueOperations)
			.set(keyCaptor.capture(), anyString(), anyLong(), any(TimeUnit.class));
		String writeKey = keyCaptor.getValue();

		assertEquals("限流读写必须使用同一 key", readKey, writeKey);
	}

	/**
	 * 限流行为：redis 中已存在未过期验证码时，拒绝再次下发。
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void sendSmsCode_rejectsWhenCodeStillValid() {
		when(valueOperations.get(anyString())).thenReturn("1234");

		Result<Boolean> result = mobileService.sendSmsCode(MOBILE);

		assertEquals(Boolean.FALSE, result.getData());
		assertTrue(result.getMsg().contains("频繁"));
	}
}
