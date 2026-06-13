package com.tce.smart.platform.core.service.impl;

import cn.hutool.core.io.IoUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tce.smart.platform.api.dto.req.isc.IscCardTaskPageReqDTO;
import com.tce.smart.platform.core.entity.SmtIscCardTask;
import com.tce.smart.platform.core.mapper.SmtIscCardTaskMapper;
import com.tce.smart.platform.core.service.SmtIscStaffCardService;
import com.tce.smart.platform.core.vo.IscCardTaskPageVO;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.DeviceTaskStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.dao.DuplicateKeyException;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtIscCardTaskServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscCardTask.class);
	}

	@Test
	public void createAddStaffCardTaskCreatesPendingParkScopedTask() {
		SmtIscCardTaskMapper mapper = Mockito.mock(SmtIscCardTaskMapper.class);
		SmtIscCardTaskServiceImpl service = service(mapper);
		Mockito.when(mapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(mapper.insert(Mockito.any(SmtIscCardTask.class))).thenReturn(1);

		boolean created = service.createAddStaffCardTask(1001L, "JA26086", 5000021, "AB123456");

		Assert.assertTrue(created);
		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(mapper).selectCount(queryCaptor.capture());
		String sqlSegment = queryCaptor.getValue().getSqlSegment().toUpperCase(Locale.ROOT);
		Assert.assertTrue(sqlSegment.contains("ACTIVE_KEY"));
		Assert.assertTrue(sqlSegment.contains("STATUS"));
		Assert.assertFalse(sqlSegment.contains("DEVICE_CODE"));

		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		Mockito.verify(mapper).insert(taskCaptor.capture());
		SmtIscCardTask task = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.DOWN.getCode(), task.getAction());
		Assert.assertEquals(DeviceTaskStatusEnum.INIT.getCode(), task.getStatus());
		Assert.assertEquals(Long.valueOf(1001L), task.getSourceId());
		Assert.assertEquals("STAFF", task.getSourceType());
		Assert.assertEquals("JA26086", task.getBadge());
		Assert.assertEquals(Integer.valueOf(5000021), task.getParkId());
		Assert.assertEquals("AB123456", task.getCardNo());
		Assert.assertEquals("STAFF|1001|JA26086|5000021|AB123456|1", task.getActiveKey());
		Assert.assertEquals(Integer.valueOf(50), task.getPriority());
	}

	@Test
	public void createDeleteStaffCardTaskCarriesLatestResolvedPersonId() {
		SmtIscCardTaskMapper mapper = Mockito.mock(SmtIscCardTaskMapper.class);
		SmtIscCardTaskServiceImpl service = service(mapper);
		Mockito.when(mapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(mapper.getLatestPersonId("JA26086", 5000021, "12345678")).thenReturn("isc-person-1");
		Mockito.when(mapper.insert(Mockito.any(SmtIscCardTask.class))).thenReturn(1);

		boolean created = service.createDeleteStaffCardTask(1001L, "JA26086", 5000021, "12345678");

		Assert.assertTrue(created);
		ArgumentCaptor<SmtIscCardTask> taskCaptor = ArgumentCaptor.forClass(SmtIscCardTask.class);
		Mockito.verify(mapper).insert(taskCaptor.capture());
		SmtIscCardTask task = taskCaptor.getValue();
		Assert.assertEquals(DeviceTaskActionEnum.DEL.getCode(), task.getAction());
		Assert.assertEquals("isc-person-1", task.getPersonId());
		Assert.assertEquals("12345678", task.getCardNo());
		Assert.assertEquals("STAFF|1001|JA26086|5000021|12345678|2", task.getActiveKey());
		Assert.assertEquals(Integer.valueOf(100), task.getPriority());
	}

	@Test
	public void createAddStaffCardTaskDoesNotCancelOtherPendingAddsForSameStaffAndPark() {
		SmtIscCardTaskMapper mapper = Mockito.mock(SmtIscCardTaskMapper.class);
		SmtIscCardTaskServiceImpl service = service(mapper);
		Mockito.when(mapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(mapper.insert(Mockito.any(SmtIscCardTask.class))).thenReturn(1);

		Assert.assertTrue(service.createAddStaffCardTask(1001L, "JA26086", 5000021, "87654321"));

		Mockito.verify(mapper, Mockito.never()).update(Mockito.any(SmtIscCardTask.class), Mockito.any());
		Mockito.verify(mapper).insert(Mockito.argThat(task -> DeviceTaskActionEnum.DOWN.getCode().equals(task.getAction())
					&& "87654321".equals(task.getCardNo())
					&& "STAFF|1001|JA26086|5000021|87654321|1".equals(task.getActiveKey())));
	}

	@Test
	public void updateDoingTaskUsesExpectedActiveKeyLeaseTokenAndDoingStatus() {
		SmtIscCardTaskMapper mapper = Mockito.mock(SmtIscCardTaskMapper.class);
		SmtIscCardTaskServiceImpl service = service(mapper);
		Mockito.when(mapper.update(Mockito.any(SmtIscCardTask.class), Mockito.any())).thenReturn(1);
		SmtIscCardTask task = new SmtIscCardTask();
		task.setId(10L);
		task.setStatus(DeviceTaskStatusEnum.SUCCESS.getCode());
		task.setActiveKey(null);

		Assert.assertTrue(service.updateDoingTask(task, "STAFF|1001|JA26086|5000021|12345|1", "lease-token-1"));

		ArgumentCaptor<AbstractWrapper> updateQueryCaptor = ArgumentCaptor.forClass(AbstractWrapper.class);
		Mockito.verify(mapper).update(Mockito.eq(task), updateQueryCaptor.capture());
		String sqlSegment = updateQueryCaptor.getValue().getSqlSegment().toUpperCase(Locale.ROOT);
		Assert.assertTrue(sqlSegment.contains("STATUS"));
		Assert.assertTrue(sqlSegment.contains("ACTIVE_KEY"));
		Assert.assertTrue(sqlSegment.contains("LEASE_TOKEN"));
		Assert.assertTrue(updateQueryCaptor.getValue().getParamNameValuePairs().values()
				.contains(DeviceTaskStatusEnum.DOING.getCode()));
		Assert.assertTrue(updateQueryCaptor.getValue().getParamNameValuePairs().values()
				.contains("STAFF|1001|JA26086|5000021|12345|1"));
		Assert.assertTrue(updateQueryCaptor.getValue().getParamNameValuePairs().values()
				.contains("lease-token-1"));
	}

	@Test
	public void isCurrentStaffCardAddTaskReturnsFalseWhenCardMasterNoLongerHasActiveCard() {
		SmtIscCardTaskServiceImpl service = service(Mockito.mock(SmtIscCardTaskMapper.class));
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		Mockito.when(staffCardService.isActiveStaffCard(1001L, "JA26086", 5000021, "12345")).thenReturn(false);
		setField(service, "smtIscStaffCardService", staffCardService);

		SmtIscCardTask task = new SmtIscCardTask();
		task.setAction(DeviceTaskActionEnum.DOWN.getCode());
		task.setSourceType("STAFF");
		task.setSourceId(1001L);
		task.setBadge("JA26086");
		task.setParkId(5000021);
		task.setCardNo("12345");

		Assert.assertFalse(service.isCurrentStaffCardAddTask(task));
	}

	@Test
	public void isCurrentStaffCardAddTaskReturnsTrueWhenCardMasterHasActiveCard() {
		SmtIscCardTaskServiceImpl service = service(Mockito.mock(SmtIscCardTaskMapper.class));
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		Mockito.when(staffCardService.isActiveStaffCard(1001L, "JA26086", 5000021, "12345")).thenReturn(true);
		setField(service, "smtIscStaffCardService", staffCardService);

		SmtIscCardTask task = new SmtIscCardTask();
		task.setAction(DeviceTaskActionEnum.DOWN.getCode());
		task.setSourceType("STAFF");
		task.setSourceId(1001L);
		task.setBadge("JA26086");
		task.setParkId(5000021);
		task.setCardNo("12345");

		Assert.assertTrue(service.isCurrentStaffCardAddTask(task));
	}

	@Test
	public void createStaffCardTasksSkipVirtualCards() {
		SmtIscCardTaskMapper mapper = Mockito.mock(SmtIscCardTaskMapper.class);
		SmtIscCardTaskServiceImpl service = service(mapper);

		Assert.assertTrue(service.createAddStaffCardTask(1001L, "JA26086", 5000021, "9990000001"));
		Assert.assertTrue(service.createDeleteStaffCardTask(1001L, "JA26086", 5000021, "9990000001"));

		Mockito.verify(mapper, Mockito.never()).getLatestPersonId(Mockito.anyString(), Mockito.anyInt(), Mockito.anyString());
		Mockito.verify(mapper, Mockito.never()).insert(Mockito.any(SmtIscCardTask.class));
	}

	@Test
	public void createAddStaffCardTaskTreatsConcurrentDuplicateAsExistingTask() {
		SmtIscCardTaskMapper mapper = Mockito.mock(SmtIscCardTaskMapper.class);
		SmtIscCardTaskServiceImpl service = service(mapper);
		Mockito.when(mapper.selectCount(Mockito.any())).thenReturn(0);
		Mockito.when(mapper.insert(Mockito.any(SmtIscCardTask.class)))
				.thenThrow(new DuplicateKeyException("UK_SMT_ISC_CARD_TASK_ACTIVE"));

			boolean created = service.createAddStaffCardTask(1001L, "JA26086", 5000021, "12345678");

			Assert.assertTrue(created);
		}

	@Test
	public void createStaffCardTaskRejectsInvalidHikvisionCardNo() {
		SmtIscCardTaskMapper mapper = Mockito.mock(SmtIscCardTaskMapper.class);
		SmtIscCardTaskServiceImpl service = service(mapper);

		try {
			service.createAddStaffCardTask(1001L, "JA26086", 5000021, "111111");
			Assert.fail("expected TCEException");
		} catch (com.tce.smart.common.core.exception.TCEException e) {
			Assert.assertTrue(e.getMessage().contains("8-20位数字或大写字母"));
		}

		Mockito.verify(mapper, Mockito.never()).insert(Mockito.any(SmtIscCardTask.class));
	}

	@Test
	public void markDoingUsesMapperLeaseUpdate() {
		SmtIscCardTaskMapper mapper = Mockito.mock(SmtIscCardTaskMapper.class);
		SmtIscCardTaskServiceImpl service = service(mapper);
			Mockito.when(mapper.releaseExpiredRunningTask(Mockito.eq("STAFF|1001"), Mockito.eq(100L), Mockito.any()))
					.thenReturn(1);
			Mockito.when(mapper.markDoing(Mockito.eq(10L), Mockito.eq(100L), Mockito.eq(700L), Mockito.anyString(),
							Mockito.eq("STAFF|1001"), Mockito.any(), Mockito.anyInt()))
					.thenReturn(1);
			SmtIscCardTask task = new SmtIscCardTask();
			task.setId(10L);
		task.setSourceType("STAFF");
		task.setSourceId(1001L);
		task.setParkId(5000021);

			Assert.assertTrue(service.markDoing(task, 100L, 700L, 10));
			Mockito.verify(mapper).markDoing(Mockito.eq(10L), Mockito.eq(100L), Mockito.eq(700L),
					Mockito.eq(task.getLeaseToken()), Mockito.eq("STAFF|1001"), Mockito.any(), Mockito.eq(10));
			Assert.assertEquals(DeviceTaskStatusEnum.DOING.getCode(), task.getStatus());
			Assert.assertEquals(Long.valueOf(700L), task.getOverTime());
			Assert.assertEquals("STAFF|1001", task.getRunningKey());
			Assert.assertNotNull(task.getLeaseToken());
			InOrder inOrder = Mockito.inOrder(mapper);
			inOrder.verify(mapper).releaseExpiredRunningTask(Mockito.eq("STAFF|1001"), Mockito.eq(100L), Mockito.any());
			inOrder.verify(mapper).markDoing(Mockito.eq(10L), Mockito.eq(100L), Mockito.eq(700L),
					Mockito.eq(task.getLeaseToken()), Mockito.eq("STAFF|1001"), Mockito.any(), Mockito.eq(10));
		}

	@Test
	public void markDoingSqlBlocksOtherActiveTasksForSameStaffAcrossParks() throws Exception {
		InputStream stream = getClass().getClassLoader().getResourceAsStream("mapper/SmtIscCardTaskMapper.xml");
		Assert.assertNotNull(stream);
		String mapperXml = IoUtil.read(stream, StandardCharsets.UTF_8).toUpperCase(Locale.ROOT);

		Assert.assertTrue(mapperXml.contains("NOT EXISTS"));
		Assert.assertTrue(mapperXml.contains("SOURCE_TYPE"));
		Assert.assertTrue(mapperXml.contains("SOURCE_ID"));
		Assert.assertTrue(mapperXml.contains("STATUS = 3"));
		Assert.assertTrue(mapperXml.contains("LEASE_TOKEN"));
		Assert.assertTrue(mapperXml.contains("RUNNING_KEY"));
		Assert.assertTrue(mapperXml.contains("BLOCK_TASK.PRIORITY"));
		Assert.assertFalse(mapperXml.contains("BLOCK_TASK.PARK_ID"));
		Assert.assertFalse(mapperXml.contains("CURRENT_TASK.PARK_ID"));
	}

	@Test
	public void getPageDelegatesParkScopedQueryToMapper() {
		SmtIscCardTaskMapper mapper = Mockito.mock(SmtIscCardTaskMapper.class);
		SmtIscCardTaskServiceImpl service = service(mapper);
		Page<IscCardTaskPageVO> page = new Page<>(1, 20);
		IscCardTaskPageReqDTO query = new IscCardTaskPageReqDTO();
		query.setParkId(5000021);
		query.setParkIds(Arrays.asList(5000021, 5000022));
		query.setBadge("JA26086");
		query.setName("张三");
		query.setCardNo("123456");
		Mockito.when(mapper.getPage(Mockito.eq(page), Mockito.eq(query))).thenReturn(page);

		Assert.assertSame(page, service.getPage(page, query));

		Mockito.verify(mapper).getPage(page, query);
	}

	@Test
	public void getPageReturnsEmptyWhenRequestedParkIsOutsideUserParks() {
		SmtIscCardTaskMapper mapper = Mockito.mock(SmtIscCardTaskMapper.class);
		SmtIscCardTaskServiceImpl service = service(mapper);
		Page<IscCardTaskPageVO> page = new Page<>(1, 20);
		IscCardTaskPageReqDTO query = new IscCardTaskPageReqDTO();
		query.setParkId(5000023);
		query.setParkIds(Collections.singletonList(5000021));

		Assert.assertTrue(service.getPage(page, query).getRecords().isEmpty());

		Mockito.verify(mapper, Mockito.never()).getPage(Mockito.any(), Mockito.any());
	}

	@Test
	public void getPageReturnsEmptyWhenPageOrUserParksAreMissing() {
		SmtIscCardTaskMapper mapper = Mockito.mock(SmtIscCardTaskMapper.class);
		SmtIscCardTaskServiceImpl service = service(mapper);
		IscCardTaskPageReqDTO query = new IscCardTaskPageReqDTO();

		Assert.assertTrue(service.getPage(null, query).getRecords().isEmpty());

		Mockito.verify(mapper, Mockito.never()).getPage(Mockito.any(), Mockito.any());
	}

	@Test
	public void getPageSqlSupportsParkBadgeNameAndCardFilters() throws Exception {
		InputStream stream = getClass().getClassLoader().getResourceAsStream("mapper/SmtIscCardTaskMapper.xml");
		Assert.assertNotNull(stream);
		String mapperXml = IoUtil.read(stream, StandardCharsets.UTF_8).toUpperCase(Locale.ROOT);

		Assert.assertTrue(mapperXml.contains("ID=\"GETPAGE\""));
		Assert.assertTrue(mapperXml.contains("SMT_ISC_CARD_TASK"));
		Assert.assertTrue(mapperXml.contains("SMT_STAFF"));
		Assert.assertTrue(mapperXml.contains("SMT_PARK"));
		Assert.assertTrue(mapperXml.contains("T.PARK_ID = #{QUERY.PARKID}".toUpperCase(Locale.ROOT)));
		Assert.assertTrue(mapperXml.contains("T.BADGE LIKE"));
		Assert.assertTrue(mapperXml.contains("STAFF.NAME LIKE"));
		Assert.assertTrue(mapperXml.contains("T.CARD_NO LIKE"));
		Assert.assertTrue(mapperXml.contains("QUERY.PARKIDS"));
	}

	@Test
	public void pendingTasksAndMarkDoingSkipTasksAfterMaxRetryTimes() {
		InputStream stream = getClass().getClassLoader().getResourceAsStream("mapper/SmtIscCardTaskMapper.xml");
		Assert.assertNotNull(stream);
		String mapperXml = IoUtil.read(stream, StandardCharsets.UTF_8).toUpperCase(Locale.ROOT);
		int pendingStart = mapperXml.indexOf("ID=\"GETPENDINGTASKS\"");
		Assert.assertTrue(pendingStart >= 0);
		int pendingEnd = mapperXml.indexOf("</SELECT>", pendingStart);
		Assert.assertTrue(mapperXml.substring(pendingStart, pendingEnd)
				.contains("TIMES IS NULL OR TIMES <![CDATA[<]]> #{MAXRETRYTIMES}"));
		int doingStart = mapperXml.indexOf("ID=\"MARKDOING\"");
		Assert.assertTrue(doingStart >= 0);
		int doingEnd = mapperXml.indexOf("</UPDATE>", doingStart);
		Assert.assertTrue(mapperXml.substring(doingStart, doingEnd)
				.contains("CURRENT_TASK.TIMES IS NULL OR CURRENT_TASK.TIMES <![CDATA[<]]> #{MAXRETRYTIMES}"));
	}

	@Test
	public void stopExceededRetryCardTasksMarksFailAndSyncsStaffCardState() {
		SmtIscCardTaskMapper mapper = Mockito.mock(SmtIscCardTaskMapper.class);
		SmtIscCardTaskServiceImpl service = service(mapper);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		setField(service, "smtIscStaffCardService", staffCardService);
		SmtIscCardTask addTask = new SmtIscCardTask();
		addTask.setId(10L);
		addTask.setAction(DeviceTaskActionEnum.DOWN.getCode());
		addTask.setTimes(10);
		SmtIscCardTask deleteTask = new SmtIscCardTask();
		deleteTask.setId(11L);
		deleteTask.setAction(DeviceTaskActionEnum.DEL.getCode());
		deleteTask.setTimes(10);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Arrays.asList(addTask, deleteTask));
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);

		Assert.assertTrue(service.stopExceededRetryCardTasks(10, "请人工介入处理"));

		ArgumentCaptor<LambdaUpdateWrapper> wrapperCaptor = ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
		Mockito.verify(mapper, Mockito.times(2)).update(Mockito.isNull(), wrapperCaptor.capture());
		LambdaUpdateWrapper wrapper = wrapperCaptor.getAllValues().get(0);
		String sqlSegment = wrapper.getSqlSegment().toUpperCase();
		Assert.assertTrue(sqlSegment.contains("TIMES"));
		Assert.assertTrue(wrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> String.valueOf(DeviceTaskStatusEnum.FAIL.getCode()).equals(String.valueOf(value))));
		Assert.assertTrue(wrapper.getParamNameValuePairs().values().stream()
				.anyMatch(value -> "10".equals(String.valueOf(value))));
		// 新增类任务需要联动收敛卡片同步状态；删除类不需要
		Mockito.verify(staffCardService).markAddTaskFailed(addTask, false);
		Mockito.verify(staffCardService, Mockito.never()).markAddTaskFailed(Mockito.eq(deleteTask), Mockito.anyBoolean());
	}

	@Test
	public void stopExceededRetryCardTasksRejectsInvalidThreshold() {
		SmtIscCardTaskServiceImpl service = service(Mockito.mock(SmtIscCardTaskMapper.class));
		Assert.assertFalse(service.stopExceededRetryCardTasks(0, "x"));
	}

	private SmtIscCardTaskServiceImpl service(SmtIscCardTaskMapper mapper) {
		SmtIscCardTaskServiceImpl service = new SmtIscCardTaskServiceImpl();
		setField(service, "baseMapper", mapper);
		return service;
	}

	private void setField(Object target, String name, Object value) {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException e) {
				type = type.getSuperclass();
			} catch (IllegalAccessException e) {
				throw new IllegalStateException(e);
			}
		}
		throw new IllegalStateException("field not found: " + name);
	}
}
