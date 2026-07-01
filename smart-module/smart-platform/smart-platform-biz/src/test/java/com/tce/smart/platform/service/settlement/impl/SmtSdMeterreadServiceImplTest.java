package com.tce.smart.platform.service.settlement.impl;

import com.tce.smart.platform.core.dto.StaffInRoomNumDTO;
import com.tce.smart.platform.core.mapper.SmtSdMeterreadMapper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 覆盖 getInRoomNumBatch：宿舍水电导出接口原来对每个员工循环调用一次 getInRoomNum，
 * 楼栋人数一多（本例对应生产上 1280 行的宿舍导出）就会把接口拖到 45-50 秒，
 * 前端 30 秒超时必现失败。这里验证批量查询会把多次单条查询合并为按 1000 一批的批量查询。
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtSdMeterreadServiceImplTest {

	@Test
	public void getInRoomNumBatchReturnsEmptyMapWithoutQueryingWhenBadgesEmpty() throws Exception {
		SmtSdMeterreadMapper mapper = Mockito.mock(SmtSdMeterreadMapper.class);
		SmtSdMeterreadServiceImpl service = serviceWithMapper(mapper);

		Map<String, Integer> result = service.getInRoomNumBatch(Collections.emptyList(), new Date());

		Assert.assertTrue(result.isEmpty());
		Mockito.verify(mapper, Mockito.never()).getInRoomNumBatch(Mockito.anyList(), Mockito.any(), Mockito.any());
	}

	@Test
	public void getInRoomNumBatchMapsMapperRowsByBadgeAndDefaultsMissingBadgesToAbsent() throws Exception {
		SmtSdMeterreadMapper mapper = Mockito.mock(SmtSdMeterreadMapper.class);
		SmtSdMeterreadServiceImpl service = serviceWithMapper(mapper);
		Mockito.when(mapper.getInRoomNumBatch(Mockito.anyList(), Mockito.any(), Mockito.any()))
				.thenReturn(Arrays.asList(dto("badge-a", 2), dto("badge-b", 0)));

		Map<String, Integer> result = service.getInRoomNumBatch(Arrays.asList("badge-a", "badge-b", "badge-c"), new Date());

		Assert.assertEquals(Integer.valueOf(2), result.get("badge-a"));
		Assert.assertEquals(Integer.valueOf(0), result.get("badge-b"));
		Assert.assertFalse("未匹配到记录的工号不应该出现在结果里，调用方需要用 getOrDefault 兜底",
				result.containsKey("badge-c"));
	}

	@Test
	public void getInRoomNumBatchIssuesExactlyOneMapperCallForASingleDormitoryWorthOfBadges() throws Exception {
		SmtSdMeterreadMapper mapper = Mockito.mock(SmtSdMeterreadMapper.class);
		SmtSdMeterreadServiceImpl service = serviceWithMapper(mapper);
		Mockito.when(mapper.getInRoomNumBatch(Mockito.anyList(), Mockito.any(), Mockito.any()))
				.thenReturn(Collections.emptyList());

		// 生产上一次宿舍导出实测是 1280 行分组后的 badge 数，这里取一个未超过 1000 分批阈值的代表值，
		// 如果这里还是逐条查询就会退化回原来的 N+1
		List<String> badges = new ArrayList<>();
		for (int i = 0; i < 800; i++) {
			badges.add("badge-" + i);
		}
		service.getInRoomNumBatch(badges, new Date());

		Mockito.verify(mapper, Mockito.times(1)).getInRoomNumBatch(Mockito.anyList(), Mockito.any(), Mockito.any());
	}

	@Test
	public void getInRoomNumBatchSplitsIntoChunksOfAtMost1000ForOracleInClauseLimit() throws Exception {
		SmtSdMeterreadMapper mapper = Mockito.mock(SmtSdMeterreadMapper.class);
		SmtSdMeterreadServiceImpl service = serviceWithMapper(mapper);
		Mockito.when(mapper.getInRoomNumBatch(Mockito.anyList(), Mockito.any(), Mockito.any()))
				.thenReturn(Collections.emptyList());

		List<String> badges = new ArrayList<>();
		for (int i = 0; i < 1500; i++) {
			badges.add("badge-" + i);
		}
		service.getInRoomNumBatch(badges, new Date());

		ArgumentCaptor<List> badgeCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(mapper, Mockito.times(2)).getInRoomNumBatch(badgeCaptor.capture(), Mockito.any(), Mockito.any());
		List<List> chunks = badgeCaptor.getAllValues();
		Assert.assertEquals(1000, chunks.get(0).size());
		Assert.assertEquals(500, chunks.get(1).size());
	}

	private StaffInRoomNumDTO dto(String badge, int inRoomNum) {
		StaffInRoomNumDTO dto = new StaffInRoomNumDTO();
		dto.setBadge(badge);
		dto.setInRoomNum(inRoomNum);
		return dto;
	}

	private SmtSdMeterreadServiceImpl serviceWithMapper(SmtSdMeterreadMapper mapper) throws Exception {
		SmtSdMeterreadServiceImpl service = new SmtSdMeterreadServiceImpl();
		setField(service, "baseMapper", mapper);
		return service;
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException ignored) {
				type = type.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name + " on " + target.getClass());
	}
}
