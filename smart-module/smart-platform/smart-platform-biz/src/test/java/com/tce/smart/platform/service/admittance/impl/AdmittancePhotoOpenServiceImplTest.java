package com.tce.smart.platform.service.admittance.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceApply;
import com.tce.smart.platform.core.entity.admittance.SmtAdmittanceFellow;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceApplyService;
import com.tce.smart.platform.service.admittance.SmtAdmittanceFellowService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 照片开放接口 service 层单测：
 * 覆盖园区范围为空的短路、pending 过滤条件、photoId 空值过滤与图片读取兜底。
 * 与既有测试一致采用纯 Mockito 风格，断言查询谓词与行为，不起 Spring 容器。
 */
public class AdmittancePhotoOpenServiceImplTest {

	private SmtAdmittanceApplyService applyService;
	private SmtAdmittanceFellowService fellowService;
	private SmtImageService imageService;
	private AdmittancePhotoOpenServiceImpl service;

	@Before
	public void setUp() {
		// 纯单测需手动初始化 MyBatis-Plus 实体元数据缓存，否则 LambdaQueryWrapper 取列名时报 lambda cache 缺失（沿用既有测试写法）
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtAdmittanceApply.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtAdmittanceFellow.class);
		applyService = mock(SmtAdmittanceApplyService.class);
		fellowService = mock(SmtAdmittanceFellowService.class);
		imageService = mock(SmtImageService.class);
		service = new AdmittancePhotoOpenServiceImpl(applyService, fellowService, imageService);
	}

	/** 园区范围为空（应用未绑定园区或 claim 脏数据被防御成空）：直接返回空列表，禁止查库 */
	@Test
	public void listPendingPhotoIds_emptyParkIds_returnsEmptyWithoutQuery() {
		List<String> result = service.listPendingPhotoIds(Collections.emptyList());
		assertTrue(result.isEmpty());
		verify(applyService, never()).list(any());
		verify(fellowService, never()).list(any());
	}

	/** pending 查询谓词：审批通过、未过期、非车辆、园区 IN 绑定范围 */
	@Test
	public void listPendingPhotoIds_appliesSpecFilters() {
		when(applyService.list(any())).thenReturn(Collections.emptyList());

		service.listPendingPhotoIds(Arrays.asList(1, 2));

		@SuppressWarnings("rawtypes")
		ArgumentCaptor<LambdaQueryWrapper> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		verify(applyService).list(captor.capture());
		String sql = captor.getValue().getSqlSegment();
		assertTrue("应包含状态过滤", sql.contains("status"));
		assertTrue("应包含过期时间过滤", sql.contains("end_time"));
		assertTrue("应包含申请类型过滤", sql.contains("apply_type"));
		assertTrue("应包含园区 IN 过滤", sql.contains("park_id") && sql.contains("IN"));
		// 无符合条件申请时不应再查随行人员
		verify(fellowService, never()).list(any());
	}

	/**
	 * Oracle 方言回归（生产踩坑实录）：Oracle 中空串即 NULL，查询条件带 {@code <> ''} 对所有行
	 * 恒不成立，会把整个 pending 清单过滤成空。随行人员查询只允许 IS NOT NULL，
	 * 禁止向查询参数绑定空串；空串/空白的过滤由内存层完成（方言无关）。
	 */
	@Test
	public void listPendingPhotoIds_fellowQueryMustNotBindEmptyString() {
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(100L);
		when(applyService.list(any())).thenReturn(Collections.singletonList(apply));
		when(fellowService.list(any())).thenReturn(Collections.emptyList());

		service.listPendingPhotoIds(Collections.singletonList(1));

		@SuppressWarnings("rawtypes")
		ArgumentCaptor<LambdaQueryWrapper> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		verify(fellowService).list(captor.capture());
		String sql = captor.getValue().getSqlSegment();
		assertTrue("应保留照片ID非空过滤", sql.contains("IS NOT NULL"));
		for (Object bound : captor.getValue().getParamNameValuePairs().values()) {
			assertFalse("查询参数绑定了空串：Oracle 下 <> '' 恒不成立，pending 会永远为空", "".equals(bound));
		}
	}

	/**
	 * Oracle IN 上限回归：有效申请单超过 1000 个时，随行人员查询必须按每批 ≤1000 拆分，
	 * 否则单条 SQL 的 IN 列表超过 Oracle 表达式上限（ORA-01795），/pending 接口整体报错，
	 * 拉取链路（含新照片下载）全部瘫痪。
	 */
	@Test
	public void listPendingPhotoIds_splitsFellowQueryToAvoidOracleInLimit() {
		List<SmtAdmittanceApply> applies = new ArrayList<>();
		for (long i = 1; i <= 2500; i++) {
			SmtAdmittanceApply apply = new SmtAdmittanceApply();
			apply.setId(i);
			applies.add(apply);
		}
		when(applyService.list(any())).thenReturn(applies);

		// 每批查询返回一个带独立 photoId 的随行人员，用于验证分批结果被合并返回
		AtomicInteger batchNo = new AtomicInteger();
		when(fellowService.list(any())).thenAnswer(invocation -> {
			SmtAdmittanceFellow fellow = new SmtAdmittanceFellow();
			fellow.setFellowPhotoId("photo-batch-" + batchNo.incrementAndGet());
			return Collections.singletonList(fellow);
		});

		List<String> result = service.listPendingPhotoIds(Collections.singletonList(1));

		// 2500 个申请单应拆成 1000/1000/500 三批
		@SuppressWarnings("rawtypes")
		ArgumentCaptor<LambdaQueryWrapper> captor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		verify(fellowService, times(3)).list(captor.capture());
		int totalBoundApplyIds = 0;
		for (LambdaQueryWrapper<?> wrapper : captor.getAllValues()) {
			// IN 的参数绑定发生在 SQL 片段生成时（懒绑定），先触发生成再统计绑定数
			wrapper.getSqlSegment();
			int boundCount = wrapper.getParamNameValuePairs().size();
			assertTrue("单批 IN 绑定参数数超过 Oracle 1000 上限：" + boundCount, boundCount <= 1000);
			totalBoundApplyIds += boundCount;
		}
		assertEquals("分批绑定的申请单 ID 总数应等于有效申请单数", 2500, totalBoundApplyIds);
		assertEquals("各批查询结果应合并返回", Arrays.asList("photo-batch-1", "photo-batch-2", "photo-batch-3"), result);
	}

	/** photoId 为空/空串的随行人员被过滤，重复 photoId 去重 */
	@Test
	public void listPendingPhotoIds_filtersBlankAndDuplicatePhotoIds() {
		SmtAdmittanceApply apply = new SmtAdmittanceApply();
		apply.setId(100L);
		when(applyService.list(any())).thenReturn(Collections.singletonList(apply));

		SmtAdmittanceFellow ok = new SmtAdmittanceFellow();
		ok.setVisitorId(100L);
		ok.setFellowPhotoId("eed9a5c2-2b38-4ff5-96d2-e56c237337e1");
		SmtAdmittanceFellow dup = new SmtAdmittanceFellow();
		dup.setVisitorId(100L);
		dup.setFellowPhotoId("eed9a5c2-2b38-4ff5-96d2-e56c237337e1");
		SmtAdmittanceFellow blank = new SmtAdmittanceFellow();
		blank.setVisitorId(100L);
		blank.setFellowPhotoId("");
		SmtAdmittanceFellow nul = new SmtAdmittanceFellow();
		nul.setVisitorId(100L);
		when(fellowService.list(any())).thenReturn(Arrays.asList(ok, dup, blank, nul));

		List<String> result = service.listPendingPhotoIds(Collections.singletonList(1));
		assertEquals(1, result.size());
		assertEquals("eed9a5c2-2b38-4ff5-96d2-e56c237337e1", result.get(0));
	}

	/** 图片不存在返回 null（由控制器映射 404），不抛异常 */
	@Test
	public void loadPhoto_missingImage_returnsNull() {
		when(imageService.getImageBinaryByCode(anyString())).thenReturn(null);
		assertNull(service.loadPhoto("eed9a5c2-2b38-4ff5-96d2-e56c237337e1"));
	}

	/** 图片存在返回二进制 */
	@Test
	public void loadPhoto_existingImage_returnsBytes() {
		byte[] bytes = new byte[] {1, 2, 3};
		when(imageService.getImageBinaryByCode("eed9a5c2-2b38-4ff5-96d2-e56c237337e1")).thenReturn(bytes);
		assertEquals(bytes, service.loadPhoto("eed9a5c2-2b38-4ff5-96d2-e56c237337e1"));
	}
}
