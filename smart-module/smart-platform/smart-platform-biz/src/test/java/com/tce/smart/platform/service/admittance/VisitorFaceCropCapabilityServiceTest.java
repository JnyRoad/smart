package com.tce.smart.platform.service.admittance.impl;

import com.tce.smart.common.core.exception.SmartException;
import com.tce.smart.platform.api.dto.admittance.VisitorActionCapabilityAction;
import com.tce.smart.platform.service.admittance.VisitorFaceDraftCredential;
import com.tce.smart.platform.service.admittance.VisitorFaceCropCapabilityService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * 微信访客人脸裁剪能力必须由短时草稿会话派生，且裁剪能力只能消费一次。
 */
public class VisitorFaceCropCapabilityServiceTest {

	@Test
	public void issueDraftStoresOnlyHashedWechatIdentityAndExpires() {
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(values);
		VisitorFaceCropCapabilityServiceImpl service = new VisitorFaceCropCapabilityServiceImpl(redisTemplate,
				new SequenceTokenSupplier("draft-token", "draft-id"));

		VisitorFaceDraftCredential credential = service.issueDraft("wx-openid-123");

		assertEquals("draft-token", credential.getDraftToken());
		assertEquals("draft-id", credential.getDraftId());
		ArgumentCaptor<String> record = ArgumentCaptor.forClass(String.class);
		Mockito.verify(values).set(Mockito.contains("draft-token"), record.capture(), Mockito.anyLong(),
				Mockito.eq(TimeUnit.SECONDS));
		assertFalse("Redis 不得保存原始微信 openId", record.getValue().contains("wx-openid-123"));
	}

	@Test
	public void resolveUnionIdRequiresTheMatchingDraftAndNeverReturnsItToTheBrowserCredential() {
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(values);
		Mockito.when(values.get(Mockito.contains("draft-token"))).thenAnswer(invocation -> {
			String key = invocation.getArgument(0);
			return key.contains(":union:") ? "server-union-id" : "owner-hash|draft-id";
		});
		VisitorFaceCropCapabilityServiceImpl service = new VisitorFaceCropCapabilityServiceImpl(redisTemplate,
				new SequenceTokenSupplier("unused"));

		assertEquals("server-union-id", service.resolveUnionId("draft-token", "draft-id"));
		try {
			service.resolveUnionId("draft-token", "other-draft");
			fail("草稿不匹配时不得解析服务端 unionId");
		} catch (SmartException expected) {
			assertNotNull(expected.getMessage());
		}
	}

	@Test
	public void cropCapabilityRequiresMatchingDraftSessionBeforeItIsMinted() {
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(values);
		Mockito.when(values.get(Mockito.contains("draft-token"))).thenReturn("owner-hash|other-draft");
		VisitorFaceCropCapabilityServiceImpl service = new VisitorFaceCropCapabilityServiceImpl(redisTemplate,
				new SequenceTokenSupplier("crop-token"));

		try {
			service.issueCropCapability("draft-token", "draft-id");
			fail("草稿不匹配时不得签发裁剪能力");
		} catch (SmartException expected) {
			Mockito.verify(values, Mockito.never()).set(Mockito.contains("crop:"), Mockito.anyString(),
					Mockito.anyLong(), Mockito.eq(TimeUnit.SECONDS));
		}
	}

	@Test
	public void receptionistSelectionRequiresMatchingDraftAndIsConsumedOnce() {
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(values);
		Mockito.when(values.get(Mockito.contains("draft-token"))).thenReturn("owner-hash|draft-id");
		Mockito.when(redisTemplate.execute(Mockito.any(), Mockito.anyList(), Mockito.anyString()))
				.thenReturn("8031249\u001f张三\u001f13800000000");
		VisitorFaceCropCapabilityServiceImpl service = new VisitorFaceCropCapabilityServiceImpl(redisTemplate,
				new SequenceTokenSupplier("unused"));

		VisitorFaceCropCapabilityService.VisitorReceptionistSelection selection = service
				.consumeReceptionistSelection("draft-token", "draft-id");

		assertEquals("8031249", selection.getReceptionistBadge());
		assertEquals("张三", selection.getReceptionistName());
		assertEquals("13800000000", selection.getReceptionistPhone());
		Mockito.verify(redisTemplate).execute(Mockito.any(), Mockito.anyList(), Mockito.eq(""));
	}

	@Test
	public void receptionistSelectionPrecheckReadIsBoundToDraftAndDoesNotConsumeTheFinalSelection() {
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		@SuppressWarnings("unchecked")
		ValueOperations<String, String> values = Mockito.mock(ValueOperations.class);
		Mockito.when(redisTemplate.opsForValue()).thenReturn(values);
		Mockito.when(values.get(Mockito.contains("draft-token"))).thenReturn("owner-hash|draft-id");
		Mockito.when(values.get(Mockito.contains(":receptionist:draft-id"))).thenReturn("8031249\u001f张三\u001f13800000000");
		VisitorFaceCropCapabilityServiceImpl service = new VisitorFaceCropCapabilityServiceImpl(redisTemplate,
				new SequenceTokenSupplier("unused"));

		VisitorFaceCropCapabilityService.VisitorReceptionistSelection selection = service
				.getReceptionistSelection("draft-token", "draft-id");

		assertEquals("8031249", selection.getReceptionistBadge());
		Mockito.verify(redisTemplate, Mockito.never()).execute(Mockito.any(), Mockito.anyList(), Mockito.anyString());
	}

	@Test
	public void cropCapabilityIsConsumedAtomicallyAndCannotBeReused() {
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		Mockito.when(redisTemplate.execute(Mockito.any(), Mockito.anyList(), Mockito.anyString()))
				.thenReturn(1L)
				.thenReturn(0L);
		VisitorFaceCropCapabilityServiceImpl service = new VisitorFaceCropCapabilityServiceImpl(redisTemplate,
				new SequenceTokenSupplier(Arrays.asList("unused")));

		service.consumeCropCapability("crop-token", "draft-id");
		try {
			service.consumeCropCapability("crop-token", "draft-id");
			fail("已消费的裁剪能力不得重放");
		} catch (SmartException expected) {
			assertNotNull(expected.getMessage());
		}
	}

	@Test
	public void blacklistCapabilityRequiresAndAtomicallyComparesIdentityPayloadHash() {
		StringRedisTemplate redisTemplate = Mockito.mock(StringRedisTemplate.class);
		Mockito.when(redisTemplate.execute(Mockito.any(), Mockito.anyList(), Mockito.anyString())).thenReturn(1L);
		VisitorFaceCropCapabilityServiceImpl service = new VisitorFaceCropCapabilityServiceImpl(redisTemplate,
				new SequenceTokenSupplier(Arrays.asList("unused")));
		String hash = "5b012e396a3e0bc4c43b600a31d30d68b79a1f34f19aadf667e141a3a7c2440c";

		service.consumeActionCapability("black-ticket", "draft-id", VisitorActionCapabilityAction.BLACKLIST_CHECK, hash);
		Mockito.verify(redisTemplate).execute(Mockito.any(), Mockito.anyList(),
				Mockito.eq("draft-id|BLACKLIST_CHECK|" + hash));
		try {
			service.consumeActionCapability("black-ticket", "draft-id", VisitorActionCapabilityAction.BLACKLIST_CHECK, null);
			fail("黑名单 capability 缺少身份摘要时不得消费");
		} catch (SmartException expected) {
			assertNotNull(expected.getMessage());
		}
	}

	private static final class SequenceTokenSupplier implements java.util.function.Supplier<String> {
		private final java.util.Iterator<String> values;

		private SequenceTokenSupplier(String... values) {
			this(Arrays.asList(values));
		}

		private SequenceTokenSupplier(java.util.List<String> values) {
			this.values = values.iterator();
		}

		@Override
		public String get() {
			return values.next();
		}
	}
}
