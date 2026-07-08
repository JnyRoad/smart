package com.tce.smart.tool.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Oracle IN 子句分批查询工具的单元测试。
 * 核心保障点：
 * 1. 空入参绝不触发查询（规避 CollUtil.split 对空集合返回 [[]] 导致空 IN 生成非法 SQL 的坑）；
 * 2. 单批参数个数不超过 Oracle 上限 1000（超限即 ORA-01795）；
 * 3. 各批结果按批次顺序合并，语义等价于一次完整 IN 查询（不去重、不改序）。
 */
public class OracleInBatchUtilsTest {

	/** 空列表：直接返回空结果，且查询函数一次都不能被调用 */
	@Test
	public void emptyParamsReturnsEmptyWithoutQuery() {
		AtomicInteger queryCount = new AtomicInteger();
		List<String> result = OracleInBatchUtils.listInBatches(Collections.<Integer>emptyList(), batch -> {
			queryCount.incrementAndGet();
			return Collections.singletonList("不应该出现");
		});
		Assert.assertTrue(result.isEmpty());
		Assert.assertEquals(0, queryCount.get());
	}

	/** null 列表：同空列表处理，直接返回空结果，不触发查询 */
	@Test
	public void nullParamsReturnsEmptyWithoutQuery() {
		AtomicInteger queryCount = new AtomicInteger();
		List<String> result = OracleInBatchUtils.listInBatches(null, batch -> {
			queryCount.incrementAndGet();
			return Collections.singletonList("不应该出现");
		});
		Assert.assertTrue(result.isEmpty());
		Assert.assertEquals(0, queryCount.get());
	}

	/** 少量参数（远小于 1000）：只查一次，参数与结果原样透传 */
	@Test
	public void smallListQueriesOnceAndPassesThrough() {
		List<List<Integer>> capturedBatches = new ArrayList<>();
		List<Integer> params = IntStream.rangeClosed(1, 3).boxed().collect(Collectors.toList());

		List<String> result = OracleInBatchUtils.listInBatches(params, batch -> {
			capturedBatches.add(new ArrayList<>(batch));
			return batch.stream().map(id -> "staff-" + id).collect(Collectors.toList());
		});

		Assert.assertEquals(1, capturedBatches.size());
		Assert.assertEquals(params, capturedBatches.get(0));
		Assert.assertEquals(IntStream.rangeClosed(1, 3).mapToObj(id -> "staff-" + id).collect(Collectors.toList()), result);
	}

	/** 恰好 1000 个参数：仍是一批，不多拆 */
	@Test
	public void exactlyOneThousandStaysSingleBatch() {
		List<List<Integer>> capturedBatches = new ArrayList<>();
		List<Integer> params = IntStream.rangeClosed(1, 1000).boxed().collect(Collectors.toList());

		OracleInBatchUtils.listInBatches(params, batch -> {
			capturedBatches.add(new ArrayList<>(batch));
			return Collections.<String>emptyList();
		});

		Assert.assertEquals(1, capturedBatches.size());
		Assert.assertEquals(1000, capturedBatches.get(0).size());
	}

	/** 1001 个参数：拆成 1000 + 1 两批，批内保持原顺序，结果按批次顺序合并 */
	@Test
	public void oneThousandAndOneSplitsIntoTwoOrderedBatches() {
		List<List<Integer>> capturedBatches = new ArrayList<>();
		List<Integer> params = IntStream.rangeClosed(1, 1001).boxed().collect(Collectors.toList());

		List<String> result = OracleInBatchUtils.listInBatches(params, batch -> {
			capturedBatches.add(new ArrayList<>(batch));
			return batch.stream().map(id -> "row-" + id).collect(Collectors.toList());
		});

		Assert.assertEquals(2, capturedBatches.size());
		Assert.assertEquals(1000, capturedBatches.get(0).size());
		Assert.assertEquals(1, capturedBatches.get(1).size());
		// 批内顺序 = 原列表顺序
		Assert.assertEquals(Integer.valueOf(1), capturedBatches.get(0).get(0));
		Assert.assertEquals(Integer.valueOf(1000), capturedBatches.get(0).get(999));
		Assert.assertEquals(Integer.valueOf(1001), capturedBatches.get(1).get(0));
		// 合并结果 = 各批结果按批次顺序拼接，总量不丢不重
		Assert.assertEquals(1001, result.size());
		Assert.assertEquals("row-1", result.get(0));
		Assert.assertEquals("row-1001", result.get(1000));
	}

	/** 结果合并不做去重：与单条 IN 查询中数据库返回重复行的语义保持一致，去重责任在调用方 */
	@Test
	public void mergeKeepsDuplicatesAcrossBatches() {
		// 1001 个参数拆两批，两批都返回同一行，合并结果应保留两条
		List<Integer> params = IntStream.rangeClosed(1, 1001).boxed().collect(Collectors.toList());

		List<String> result = OracleInBatchUtils.listInBatches(params, batch -> Collections.singletonList("同一行"));

		Assert.assertEquals(2, result.size());
	}

	/** 某一批查询抛异常：立即上抛，不吞错、不返回半截结果 */
	@Test
	public void queryFailureFailsFast() {
		List<Integer> params = IntStream.rangeClosed(1, 1001).boxed().collect(Collectors.toList());
		AtomicInteger queryCount = new AtomicInteger();

		try {
			OracleInBatchUtils.listInBatches(params, batch -> {
				if (queryCount.incrementAndGet() == 2) {
					throw new IllegalStateException("第二批查询失败");
				}
				return Collections.<String>emptyList();
			});
			Assert.fail("应当把批次查询的异常原样上抛");
		} catch (IllegalStateException expected) {
			Assert.assertEquals("第二批查询失败", expected.getMessage());
		}
		Assert.assertEquals(2, queryCount.get());
	}
}
