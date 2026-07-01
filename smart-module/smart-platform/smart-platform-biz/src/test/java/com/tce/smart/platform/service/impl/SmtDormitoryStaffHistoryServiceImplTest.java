package com.tce.smart.platform.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.SmtDormitoryStaffHistory;
import com.tce.smart.platform.core.mapper.SmtDormitoryStaffHistoryMapper;
import com.tce.smart.platform.service.dormitoryconfig.SmtDormitoryPersonService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 覆盖 getByBadgeBatch：原 getByBadge 只能按单个工号查询，宿舍水电导出接口对每个姓名为空的员工
 * 都要循环调用一次，这里验证批量查询会合并成一次（按 1000 分批）查询，且保留原来
 * “同一工号取查到的第一条记录”的语义。
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtDormitoryStaffHistoryServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtDormitoryStaffHistory.class);
	}

	@Test
	public void getByBadgeBatchReturnsEmptyMapWithoutQueryingWhenBadgesEmpty() throws Exception {
		SmtDormitoryStaffHistoryMapper baseMapper = Mockito.mock(SmtDormitoryStaffHistoryMapper.class);
		SmtDormitoryStaffHistoryServiceImpl service = service(baseMapper);

		Map<String, String> result = service.getByBadgeBatch(Collections.emptyList());

		Assert.assertTrue(result.isEmpty());
		Mockito.verify(baseMapper, Mockito.never()).selectList(Mockito.any());
	}

	@Test
	public void getByBadgeBatchPicksFirstMatchingHistoryRecordPerBadge() throws Exception {
		SmtDormitoryStaffHistoryMapper baseMapper = Mockito.mock(SmtDormitoryStaffHistoryMapper.class);
		SmtDormitoryStaffHistoryServiceImpl service = service(baseMapper);
		Mockito.when(baseMapper.selectList(Mockito.any())).thenReturn(Arrays.asList(
				history("badge-a", "张三"),
				history("badge-a", "张三-旧记录"),
				history("badge-b", "李四")
		));

		Map<String, String> result = service.getByBadgeBatch(Arrays.asList("badge-a", "badge-b", "badge-c"));

		Assert.assertEquals("张三", result.get("badge-a"));
		Assert.assertEquals("李四", result.get("badge-b"));
		Assert.assertFalse("没有历史记录的工号不应该出现在结果里，调用方需要用 getOrDefault 兜底",
				result.containsKey("badge-c"));
	}

	@Test
	public void getByBadgeBatchIssuesExactlyOneQueryForASingleDormitoryWorthOfBadges() throws Exception {
		SmtDormitoryStaffHistoryMapper baseMapper = Mockito.mock(SmtDormitoryStaffHistoryMapper.class);
		SmtDormitoryStaffHistoryServiceImpl service = service(baseMapper);
		Mockito.when(baseMapper.selectList(Mockito.any())).thenReturn(Collections.emptyList());

		List<String> badges = new ArrayList<>();
		for (int i = 0; i < 34; i++) {
			badges.add("badge-" + i);
		}
		service.getByBadgeBatch(badges);

		Mockito.verify(baseMapper, Mockito.times(1)).selectList(Mockito.any());
	}

	private SmtDormitoryStaffHistory history(String badge, String name) {
		SmtDormitoryStaffHistory history = new SmtDormitoryStaffHistory();
		history.setStaffBadge(badge);
		history.setStaffName(name);
		return history;
	}

	private SmtDormitoryStaffHistoryServiceImpl service(SmtDormitoryStaffHistoryMapper baseMapper) throws Exception {
		SmtDormitoryStaffHistoryServiceImpl service = new SmtDormitoryStaffHistoryServiceImpl(
				Mockito.mock(SmtDormitoryStaffHistoryMapper.class), Mockito.mock(SmtDormitoryPersonService.class));
		setField(service, "baseMapper", baseMapper);
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
