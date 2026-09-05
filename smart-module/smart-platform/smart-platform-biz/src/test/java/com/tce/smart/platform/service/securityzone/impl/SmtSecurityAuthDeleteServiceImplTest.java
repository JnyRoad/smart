package com.tce.smart.platform.service.securityzone.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.core.entity.SmtDeviceAuthority;
import com.tce.smart.platform.core.entity.SmtDeviceAuthorityRelation;
import com.tce.smart.platform.core.entity.SmtSnapPerson;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtStaffDeviceAuth;
import com.tce.smart.platform.core.dto.securityzone.SecurityAuthDeleteTaskRef;
import com.tce.smart.platform.core.mapper.SmtSecurityAuthDeleteMapper;
import com.tce.smart.platform.api.dto.req.securityzone.SecurityAuthDeleteReqDTO;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDelete;
import com.tce.smart.platform.core.entity.securityzone.SmtSecurityAuthDeleteLog;
import com.tce.smart.platform.service.SmtDeviceAuthorityRelationService;
import com.tce.smart.platform.service.SmtDeviceAuthorityService;
import com.tce.smart.platform.service.securityzone.SmtSecurityAuthDeleteLogService;
import com.tce.smart.platform.service.securityzone.SmtSecurityWhiteService;
import com.tce.smart.platform.service.SmtSnapPersonService;
import com.tce.smart.platform.service.SmtStaffDeviceAuthService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.constant.DeviceTaskConstants;
import org.junit.Test;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.TransactionDefinition;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * 保密区权限自动删除的天数阈值回归测试。
 */
public class SmtSecurityAuthDeleteServiceImplTest {

	/**
	 * 验证白名单开关关闭时，未达到配置删除天数的权限不能被误判为应删除。
	 *
	 * @throws Exception 反射调用私有的纯计算方法失败时抛出。
	 */
	@Test
	public void freeDay_usesDeleteDayInsteadOfWhiteListFlag() throws Exception {
		SmtSecurityAuthDelete config = new SmtSecurityAuthDelete();
		config.setDeleteDay(30);
		config.setIsWhiteList(0);

		boolean shouldDelete = invokeFreeDay(config,
				DateUtil.parse("2026-09-01 00:00:00"),
				DateUtil.parse("2026-09-11 00:00:00"));

		assertFalse("未满 deleteDay 天的权限不应因白名单开关为 0 而删除", shouldDelete);
	}

	/**
	 * 验证间隔恰好等于删除天数时，权限仍处于允许保留的边界内。
	 *
	 * @throws Exception 反射调用私有的纯计算方法失败时抛出。
	 */
	@Test
	public void freeDay_atConfiguredDayBoundary_doesNotDelete() throws Exception {
		SmtSecurityAuthDelete config = new SmtSecurityAuthDelete();
		config.setDeleteDay(1);
		config.setIsWhiteList(0);
		Date boundaryTime = DateUtil.parse("2026-09-01 00:00:00");

		boolean shouldDelete = invokeFreeDay(config, boundaryTime, boundaryTime);

		assertFalse("间隔等于 deleteDay 时不应删除", shouldDelete);
	}

	/**
	 * 验证超过管理员配置的删除天数后，权限会被判定为应删除。
	 *
	 * @throws Exception 反射调用私有的纯计算方法失败时抛出。
	 */
	@Test
	public void freeDay_afterConfiguredDayBoundary_deletes() throws Exception {
		SmtSecurityAuthDelete config = new SmtSecurityAuthDelete();
		config.setDeleteDay(3);
		config.setIsWhiteList(0);

		boolean shouldDelete = invokeFreeDay(config,
				DateUtil.parse("2026-09-01 00:00:00"),
				DateUtil.parse("2026-09-11 00:00:00"));

		assertTrue("超过 deleteDay 天且无过滤项时应删除", shouldDelete);
	}

	/**
	 * 验证单条权限失败后，自动任务仍会继续处理后续权限，而不是让首条异常中断整批扫描。
	 * 当前旧实现会直接向外抛出首条删除异常，因此该测试在实现前应失败。
	 */
	@Test
	public void deleteAuthTask_singleRelationFailure_continuesWithLaterRelations() throws Exception {
		SmtSecurityAuthDeleteMapper mapper = Mockito.mock(SmtSecurityAuthDeleteMapper.class);
		SmtSecurityAuthDelete config = dueConfig();
		SmtStaffDeviceAuth first = authRelation(100L, 7, 701);
		SmtStaffDeviceAuth second = authRelation(101L, 8, 702);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(config));

		SmtSecurityAuthDeleteServiceImpl service = newDeleteService(mapper, config,
				Arrays.asList(first, second), Arrays.asList(Boolean.FALSE, Boolean.TRUE));
		assertDoesNotThrow(service::deleteAuthTask);

		SmtStaffDeviceAuthService staffAuthService = (SmtStaffDeviceAuthService) readField(service, "smtStaffDeviceAuthService");
		Mockito.verify(staffAuthService, Mockito.times(2)).removeById(Mockito.any());
		List<AuditRecord> records = verifyAuditRecords(service, 2);
		assertAuditRecord(records.get(0), "FAILED", 100L, "badge-100", "staff-100", "dept-100", 7, "保密区权限");
		assertNull(records.get(0).log.getLastSnapTime());
		assertTrue(records.get(0).taskRefs.isEmpty());
		assertAuditRecord(records.get(1), "PROCESSING", 101L, "badge-101", "staff-101", "dept-101", 8, "保密区权限");
		assertNull(records.get(1).log.getLastSnapTime());
		assertEquals("NORMAL", ((SecurityAuthDeleteTaskRef) records.get(1).taskRefs.get(0)).getTaskSource());
		assertEquals("9001", ((SecurityAuthDeleteTaskRef) records.get(1).taskRefs.get(0)).getTaskId());
		assertEquals("device-1", ((SecurityAuthDeleteTaskRef) records.get(1).taskRefs.get(0)).getDeviceCode());
		assertEquals(DeviceTaskConstants.DEL, ((SecurityAuthDeleteTaskRef) records.get(1).taskRefs.get(0)).getAction());
	}

	/** 校验单条业务事务回滚后才开启失败审计事务，且两者均使用 REQUIRES_NEW。 */
	@Test
	public void deleteAuthTask_rollsBackBusinessBeforeFailureAuditCommit() throws Exception {
		SmtSecurityAuthDeleteMapper mapper = Mockito.mock(SmtSecurityAuthDeleteMapper.class);
		SmtSecurityAuthDelete config = dueConfig();
		SmtStaffDeviceAuth relation = authRelation(100L, 7, 701);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(config));
		SmtSecurityAuthDeleteServiceImpl service = newDeleteService(mapper, config,
				Collections.singletonList(relation), Collections.singletonList(Boolean.FALSE));

		service.deleteAuthTask();

		PlatformTransactionManager manager = (PlatformTransactionManager) readField(service, "transactionManager");
		ArgumentCaptor<TransactionDefinition> definitionCaptor = ArgumentCaptor.forClass(TransactionDefinition.class);
		Mockito.verify(manager, Mockito.times(2)).getTransaction(definitionCaptor.capture());
		for (TransactionDefinition definition : definitionCaptor.getAllValues()) {
			org.junit.Assert.assertEquals(TransactionDefinition.PROPAGATION_REQUIRES_NEW,
					definition.getPropagationBehavior());
		}
		InOrder order = Mockito.inOrder(manager);
		order.verify(manager).getTransaction(Mockito.any(TransactionDefinition.class));
		order.verify(manager).rollback(Mockito.any(TransactionStatus.class));
		order.verify(manager).getTransaction(Mockito.any(TransactionDefinition.class));
		order.verify(manager).commit(Mockito.any(TransactionStatus.class));
		AuditRecord failureRecord = verifySingleAudit(service);
		assertAuditRecord(failureRecord, "FAILED", 100L, "badge-100", "staff-100", "dept-100", 7, "保密区权限");
		assertTrue(failureRecord.taskRefs.isEmpty());
	}

	/** 审计主记录写入失败时，即使失败快照成功，也必须把原失败向任务调用方抛出。 */
	@Test
	public void deleteAuthTask_auditFailureIsNotSwallowed() throws Exception {
		SmtSecurityAuthDeleteMapper mapper = Mockito.mock(SmtSecurityAuthDeleteMapper.class);
		SmtSecurityAuthDelete config = dueConfig();
		SmtStaffDeviceAuth relation = authRelation(100L, 7, 701);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(config));
		SmtSecurityAuthDeleteServiceImpl service = newDeleteService(mapper, config,
				Collections.singletonList(relation), Collections.singletonList(Boolean.TRUE));
		SmtSecurityAuthDeleteLogService auditService = auditService(service);
		Mockito.doThrow(new IllegalStateException("审计保存失败")).doNothing().when(auditService)
				.record(Mockito.any(SmtSecurityAuthDeleteLog.class), Mockito.anyList());

		try {
			service.deleteAuthTask();
			org.junit.Assert.fail("审计保存失败不得被吞掉");
		} catch (RuntimeException expected) {
			List<AuditRecord> records = verifyAuditRecords(service, 2);
			assertEquals("PROCESSING", records.get(0).log.getResult());
			assertEquals("FAILED", records.get(1).log.getResult());
		}
	}

	/** 验证新配置拒绝 0/1 之外的演练值，避免非法值被当成正式任务写入。 */
	@Test
	public void editConfig_invalidDryRun_isRejected() throws Exception {
		SecurityAuthDeleteReqDTO request = new SecurityAuthDeleteReqDTO();
		request.setDryRun(2);

		SmtSecurityAuthDeleteServiceImpl service = new SmtSecurityAuthDeleteServiceImpl();
		try {
			service.editConfig(request);
			org.junit.Assert.fail("非法 dryRun 应在保存前被拒绝");
		} catch (RuntimeException expected) {
			// 兼容 SmartException 或参数校验异常，具体异常类型不属于接口契约。
		}
	}

	/** 验证新建园区配置时演练值默认为正式模式 0。 */
	@Test
	public void getConfig_newPark_defaultsDryRunToZero() throws Exception {
		SmtSecurityAuthDeleteMapper mapper = Mockito.mock(SmtSecurityAuthDeleteMapper.class);
		Mockito.when(mapper.selectOne(Mockito.any())).thenReturn(null);
		Mockito.when(mapper.insert(Mockito.any())).thenReturn(1);
		SmtSecurityAuthDeleteServiceImpl service = new SmtSecurityAuthDeleteServiceImpl();
		setField(service, "baseMapper", mapper);

		SmtSecurityAuthDelete config = service.getConfig(10);
		org.junit.Assert.assertEquals(Integer.valueOf(0), config.getDryRun());
	}

	/** 演练命中时只写判定记录，不删除权限关联，也不创建设备任务。 */
	@Test
	public void deleteAuthTask_dryRun_recordsWithoutDeletingOrCreatingTask() throws Exception {
		SmtSecurityAuthDeleteMapper mapper = Mockito.mock(SmtSecurityAuthDeleteMapper.class);
		SmtSecurityAuthDelete config = dueConfig();
		config.setDryRun(1);
		SmtStaffDeviceAuth relation = authRelation(100L, 7, 701);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(config));
		SmtSecurityAuthDeleteServiceImpl service = newDeleteService(mapper, config,
				Collections.singletonList(relation), Collections.singletonList(Boolean.FALSE));

		service.deleteAuthTask();

		SmtStaffDeviceAuthService staffAuthService = (SmtStaffDeviceAuthService) readField(service, "smtStaffDeviceAuthService");
		Mockito.verify(staffAuthService, Mockito.never()).removeById(Mockito.any());
		SmtStaffService staffService = (SmtStaffService) readField(service, "smtStaffService");
		Mockito.verify(staffService, Mockito.never()).savePersonCardTasksWithResult(Mockito.anyInt(), Mockito.anyLong(),
				Mockito.anyLong(), Mockito.any(SmtStaff.class), Mockito.anyList());
		AuditRecord record = verifySingleAudit(service);
		assertAuditRecord(record, "DRY_RUN", 100L, "badge-100", "staff-100", "dept-100", 7, "保密区权限");
		assertTrue(record.log.getTriggerReason().contains("授权创建时间"));
		assertTrue(record.log.getTriggerReason().contains("超过1天"));
		assertTrue(record.taskRefs.isEmpty());
	}

	/** 历史权限缺少全部时间依据时明确跳过，并继续处理后续有依据的权限。 */
	@Test
	public void deleteAuthTask_missingAnchor_skipsWithoutRollbackAndContinues() throws Exception {
		SmtSecurityAuthDeleteMapper mapper = Mockito.mock(SmtSecurityAuthDeleteMapper.class);
		SmtSecurityAuthDelete config = dueConfig();
		SmtStaffDeviceAuth missingTime = authRelation(100L, 7, 701);
		missingTime.setCreateTime(null);
		SmtStaffDeviceAuth validTime = authRelation(101L, 8, 702);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(config));
		SmtSecurityAuthDeleteServiceImpl service = newDeleteService(mapper, config,
				Arrays.asList(missingTime, validTime), Collections.singletonList(Boolean.TRUE));

		service.deleteAuthTask();

		List<AuditRecord> records = verifyAuditRecords(service, 2);
		assertAuditRecord(records.get(0), "SKIPPED_MISSING_TIME", 100L, "badge-100", "staff-100", "dept-100", 7, "保密区权限");
		assertEquals("缺少进出记录和授权创建时间", records.get(0).log.getTriggerReason());
		assertNull(records.get(0).log.getLastSnapTime());
		assertTrue(records.get(0).taskRefs.isEmpty());
		assertEquals("PROCESSING", records.get(1).log.getResult());
		SmtStaffDeviceAuthService authService = (SmtStaffDeviceAuthService) readField(service, "smtStaffDeviceAuthService");
		Mockito.verify(authService, Mockito.never()).removeById(missingTime.getId());
		Mockito.verify(authService).removeById(validTime.getId());
		SmtStaffService staffService = (SmtStaffService) readField(service, "smtStaffService");
		Mockito.verify(staffService, Mockito.times(1)).savePersonCardTasksWithResult(Mockito.anyInt(), Mockito.anyLong(),
				Mockito.anyLong(), Mockito.any(SmtStaff.class), Mockito.anyList());
		PlatformTransactionManager manager = (PlatformTransactionManager) readField(service, "transactionManager");
		Mockito.verify(manager, Mockito.never()).rollback(Mockito.any(TransactionStatus.class));
	}

	/** 授权创建时间缺失但有真实进出记录时，仍按进出时间正常计算演练命中。 */
	@Test
	public void deleteAuthTask_missingCreateTime_usesActualSnapTime() throws Exception {
		SmtSecurityAuthDeleteMapper mapper = Mockito.mock(SmtSecurityAuthDeleteMapper.class);
		SmtSecurityAuthDelete config = dueConfig();
		config.setDryRun(1);
		SmtStaffDeviceAuth relation = authRelation(100L, 7, 701);
		relation.setCreateTime(null);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(config));
		SmtSecurityAuthDeleteServiceImpl service = newDeleteService(mapper, config,
				Collections.singletonList(relation), Collections.singletonList(Boolean.FALSE));
		SmtSnapPerson snap = new SmtSnapPerson();
		snap.setSnapTime(DateUtil.offsetDay(new Date(), -10));
		SmtSnapPersonService snapService = (SmtSnapPersonService) readField(service, "smtSnapPersonService");
		Mockito.when(snapService.list(Mockito.any())).thenReturn(Collections.singletonList(snap));

		service.deleteAuthTask();

		AuditRecord record = verifySingleAudit(service);
		assertEquals("DRY_RUN", record.log.getResult());
		assertNotNull(record.log.getLastSnapTime());
		assertTrue(record.log.getTriggerReason().contains("最后进出时间"));
		assertTrue(record.taskRefs.isEmpty());
	}

	/** 白名单判定在查询抓拍记录之前完成，命中时不读取最后进出时间。 */
	@Test
	public void deleteAuthTask_whitelistSkipsBeforeSnapQuery() throws Exception {
		SmtSecurityAuthDeleteMapper mapper = Mockito.mock(SmtSecurityAuthDeleteMapper.class);
		SmtSecurityAuthDelete config = dueConfig();
		config.setIsWhiteList(1);
		SmtStaffDeviceAuth relation = authRelation(100L, 7, 701);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(config));
		SmtSecurityAuthDeleteServiceImpl service = newDeleteService(mapper, config,
				Collections.singletonList(relation), Collections.singletonList(Boolean.FALSE));
		SmtSecurityWhiteService whiteService = (SmtSecurityWhiteService) readField(service, "smtSecurityWhiteService");
		Mockito.when(whiteService.isExist(config.getId(), relation.getStaffId())).thenReturn(Boolean.TRUE);

		service.deleteAuthTask();

		SmtSnapPersonService snapPersonService = (SmtSnapPersonService) readField(service, "smtSnapPersonService");
		Mockito.verify(snapPersonService, Mockito.never()).list(Mockito.any());
		AuditRecord record = verifySingleAudit(service);
		assertAuditRecord(record, "SKIPPED_WHITELIST", 100L, "badge-100", "staff-100", "dept-100", 7, "保密区权限");
		assertNull(record.log.getLastSnapTime());
		assertTrue(record.taskRefs.isEmpty());
	}

	/** 未到删除期限时保留权限关联且不创建任务。 */
	@Test
	public void deleteAuthTask_notDue_keepsRelationAndSkipsTask() throws Exception {
		SmtSecurityAuthDeleteMapper mapper = Mockito.mock(SmtSecurityAuthDeleteMapper.class);
		SmtSecurityAuthDelete config = dueConfig();
		config.setDeleteDay(30);
		SmtStaffDeviceAuth relation = authRelation(100L, 7, 701);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(config));
		SmtSecurityAuthDeleteServiceImpl service = newDeleteService(mapper, config,
				Collections.singletonList(relation), Collections.singletonList(Boolean.FALSE));

		service.deleteAuthTask();

		SmtStaffDeviceAuthService staffAuthService = (SmtStaffDeviceAuthService) readField(service, "smtStaffDeviceAuthService");
		Mockito.verify(staffAuthService, Mockito.never()).removeById(Mockito.any());
		AuditRecord record = verifySingleAudit(service);
		assertAuditRecord(record, "SKIPPED_NOT_DUE", 100L, "badge-100", "staff-100", "dept-100", 7, "保密区权限");
		assertTrue(record.log.getTriggerReason().contains("授权创建时间"));
		assertTrue(record.log.getTriggerReason().contains("未达到30天"));
		assertTrue(record.taskRefs.isEmpty());
	}

	/** 无设备关联时保存跳过记录，不读取抓拍记录或删除员工权限。 */
	@Test
	public void deleteAuthTask_noDevice_recordsSkipWithoutRemoval() throws Exception {
		SmtSecurityAuthDeleteMapper mapper = Mockito.mock(SmtSecurityAuthDeleteMapper.class);
		SmtSecurityAuthDelete config = dueConfig();
		SmtStaffDeviceAuth relation = authRelation(100L, 7, 701);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(config));
		SmtSecurityAuthDeleteServiceImpl service = newDeleteService(mapper, config,
				Collections.singletonList(relation), Collections.singletonList(Boolean.FALSE));
		SmtDeviceAuthorityRelationService relationService =
				(SmtDeviceAuthorityRelationService) readField(service, "smtDeviceAuthorityRelationService");
		Mockito.when(relationService.list(Mockito.any())).thenReturn(Collections.emptyList());

		service.deleteAuthTask();

		SmtStaffDeviceAuthService staffAuthService = (SmtStaffDeviceAuthService) readField(service, "smtStaffDeviceAuthService");
		Mockito.verify(staffAuthService, Mockito.never()).removeById(Mockito.any());
		SmtSnapPersonService snapPersonService = (SmtSnapPersonService) readField(service, "smtSnapPersonService");
		Mockito.verify(snapPersonService, Mockito.never()).list(Mockito.any());
		SmtStaffService staffService = (SmtStaffService) readField(service, "smtStaffService");
		Mockito.verify(staffService, Mockito.never()).savePersonCardTasksWithResult(Mockito.anyInt(), Mockito.anyLong(),
				Mockito.anyLong(), Mockito.any(SmtStaff.class), Mockito.anyList());
		AuditRecord record = verifySingleAudit(service);
		assertAuditRecord(record, "SKIPPED_NO_DEVICE", 100L, "badge-100", "staff-100", "dept-100", 7, "保密区权限");
		assertNull(record.log.getLastSnapTime());
		assertTrue(record.taskRefs.isEmpty());
	}

	/** 人员缺失时保存缺失快照，不读取抓拍记录或生成设备任务。 */
	@Test
	public void deleteAuthTask_staffMissing_recordsSkipWithoutSnapLookup() throws Exception {
		SmtSecurityAuthDeleteMapper mapper = Mockito.mock(SmtSecurityAuthDeleteMapper.class);
		SmtSecurityAuthDelete config = dueConfig();
		SmtStaffDeviceAuth relation = authRelation(100L, 7, 701);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(config));
		SmtSecurityAuthDeleteServiceImpl service = newDeleteService(mapper, config,
				Collections.singletonList(relation), Collections.singletonList(Boolean.FALSE));
		SmtStaffService staffService = (SmtStaffService) readField(service, "smtStaffService");
		Mockito.doReturn(null).when(staffService).getById(Mockito.any());

		service.deleteAuthTask();

		SmtSnapPersonService snapPersonService = (SmtSnapPersonService) readField(service, "smtSnapPersonService");
		Mockito.verify(snapPersonService, Mockito.never()).list(Mockito.any());
		Mockito.verify(staffService, Mockito.never()).savePersonCardTasksWithResult(Mockito.anyInt(), Mockito.anyLong(),
				Mockito.anyLong(), Mockito.any(SmtStaff.class), Mockito.anyList());
		AuditRecord record = verifySingleAudit(service);
		assertAuditRecord(record, "SKIPPED_STAFF_MISSING", 100L, null, null, null, 7, "保密区权限");
		assertNull(record.log.getLastSnapTime());
		assertTrue(record.taskRefs.isEmpty());
	}

	/** 旧客户端未传 dryRun 时，编辑已有配置必须保留数据库中的演练值。 */
	@Test
	public void editConfig_withoutDryRun_preservesExistingValue() throws Exception {
		// 保留真实 saveOrUpdate 行为，补齐它所依赖的实体表元数据。
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtSecurityAuthDelete.class);
		SmtSecurityAuthDeleteMapper mapper = Mockito.mock(SmtSecurityAuthDeleteMapper.class);
		SmtSecurityAuthDelete existing = dueConfig();
		existing.setId(11L);
		existing.setDryRun(1);
		Mockito.when(mapper.selectById(Mockito.any())).thenReturn(existing);
		Mockito.when(mapper.updateById(Mockito.any())).thenReturn(1);
		SmtSecurityWhiteService whiteService = Mockito.mock(SmtSecurityWhiteService.class);
		Mockito.when(whiteService.editList(Mockito.anyList(), Mockito.eq(11L))).thenReturn(Boolean.TRUE);
		SmtSecurityAuthDeleteServiceImpl service = new SmtSecurityAuthDeleteServiceImpl();
		setField(service, "baseMapper", mapper);
		setField(service, "smtSecurityWhiteService", whiteService);
		SecurityAuthDeleteReqDTO request = new SecurityAuthDeleteReqDTO();
		request.setId(11L);
		request.setParkId(10);
		request.setWhiteList(Collections.emptyList());

		service.editConfig(request);

		ArgumentCaptor<SmtSecurityAuthDelete> captor = ArgumentCaptor.forClass(SmtSecurityAuthDelete.class);
		Mockito.verify(mapper).updateById(captor.capture());
		org.junit.Assert.assertEquals(Integer.valueOf(1), captor.getValue().getDryRun());
	}

	/**
	 * 调用自动删除任务的纯天数判定逻辑，避免依赖数据库、远程服务或设备任务。
	 *
	 * @param config 自动删除配置，包含删除天数与过滤开关。
	 * @param startTime 最后一次进出或权限创建时间。
	 * @param endTime 本次任务的判定时间。
	 * @return 是否达到删除条件。
	 * @throws Exception 反射调用失败时抛出。
	 */
	private boolean invokeFreeDay(SmtSecurityAuthDelete config, Date startTime, Date endTime) throws Exception {
		Method freeDay = SmtSecurityAuthDeleteServiceImpl.class.getDeclaredMethod("freeDay",
				SmtSecurityAuthDelete.class, String.class, Date.class, Date.class);
		freeDay.setAccessible(true);
		return (Boolean) freeDay.invoke(new SmtSecurityAuthDeleteServiceImpl(), config, "test-badge", startTime, endTime);
	}

	/** 构造已达到阈值且关闭所有可选过滤项的自动删除配置。 */
	private SmtSecurityAuthDelete dueConfig() {
		SmtSecurityAuthDelete config = new SmtSecurityAuthDelete();
		config.setId(1L);
		config.setParkId(10);
		config.setDeleteDay(1);
		config.setIsWhiteList(0);
		config.setIsHoliday(0);
		config.setIsBusiness(0);
		config.setIsLeave(0);
		config.setIsCompensatory(0);
		return config;
	}

	/** 构造一条已存在较长时间的员工权限关系，使测试进入正式删除判定分支。 */
	private SmtStaffDeviceAuth authRelation(Long staffId, Integer authId, Integer relationId) {
		SmtStaffDeviceAuth relation = new SmtStaffDeviceAuth();
		relation.setId(relationId);
		relation.setStaffId(staffId);
		relation.setAuthId(authId);
		relation.setCreateTime(DateUtil.offsetDay(new Date(), -10));
		return relation;
	}

	/** 组装自动删权服务的 Mockito 依赖，测试只保留判定链路本身。 */
	private SmtSecurityAuthDeleteServiceImpl newDeleteService(SmtSecurityAuthDeleteMapper mapper,
			SmtSecurityAuthDelete config, List<SmtStaffDeviceAuth> relations,
			List<Boolean> removeResults) throws Exception {
		SmtSecurityAuthDeleteServiceImpl service = new SmtSecurityAuthDeleteServiceImpl();
		setField(service, "baseMapper", mapper);
		SmtSecurityWhiteService whiteService = Mockito.mock(SmtSecurityWhiteService.class);
		SmtStaffDeviceAuthService staffAuthService = Mockito.mock(SmtStaffDeviceAuthService.class);
		SmtDeviceAuthorityRelationService deviceRelationService = Mockito.mock(SmtDeviceAuthorityRelationService.class);
		SmtSnapPersonService snapPersonService = Mockito.mock(SmtSnapPersonService.class);
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmtDeviceAuthorityService authorityService = Mockito.mock(SmtDeviceAuthorityService.class);

		Mockito.when(staffAuthService.querySecurityAuth(10)).thenReturn(relations);
		final int[] removeIndex = {0};
		Mockito.when(staffAuthService.removeById(Mockito.any())).thenAnswer(invocation -> {
			int index = Math.min(removeIndex[0]++, removeResults.size() - 1);
			return removeResults.get(index);
		});
		SmtDeviceAuthorityRelation deviceRelation = new SmtDeviceAuthorityRelation();
		deviceRelation.setAuthorityId(7);
		deviceRelation.setDeviceId("device-1");
		Mockito.when(deviceRelationService.list(Mockito.any())).thenReturn(Collections.singletonList(deviceRelation));
		Mockito.when(snapPersonService.list(Mockito.any())).thenReturn(Collections.<SmtSnapPerson>emptyList());
		Map<Long, SmtStaff> staffMap = new HashMap<>();
		for (SmtStaffDeviceAuth relation : relations) {
			SmtStaff staff = new SmtStaff();
			staff.setId(relation.getStaffId());
			staff.setBadge("badge-" + relation.getStaffId());
			staff.setName("staff-" + relation.getStaffId());
			staff.setDepName("dept-" + relation.getStaffId());
			staffMap.put(relation.getStaffId(), staff);
		}
		Mockito.when(staffService.getById(Mockito.any())).thenAnswer(invocation ->
				staffMap.get(((Number) invocation.getArgument(0)).longValue()));
		Mockito.when(staffService.savePersonCardTasksWithResult(Mockito.anyInt(), Mockito.anyLong(), Mockito.anyLong(),
				Mockito.any(SmtStaff.class), Mockito.anyList())).thenReturn(Collections.singletonList(
				new SecurityAuthDeleteTaskRef("NORMAL", "9001", "device-1", DeviceTaskConstants.DEL)));
		SmtDeviceAuthority authority = new SmtDeviceAuthority();
		authority.setAuthorityName("保密区权限");
		Mockito.when(authorityService.getById(Mockito.any())).thenReturn(authority);
		SmtSecurityAuthDeleteLogService auditService = Mockito.mock(SmtSecurityAuthDeleteLogService.class);

		setField(service, "smtSecurityWhiteService", whiteService);
		setField(service, "smtStaffDeviceAuthService", staffAuthService);
		setField(service, "smtDeviceAuthorityRelationService", deviceRelationService);
		setField(service, "smtSnapPersonService", snapPersonService);
		setField(service, "smtStaffService", staffService);
		setField(service, "smtDeviceAuthorityService", authorityService);
		setField(service, "smtSecurityAuthDeleteLogService", auditService);
		installTransactionSupport(service);
		return service;
	}

	/** 为采用编程式事务的实现安装同步执行回调的事务依赖。 */
	private void installTransactionSupport(Object target) throws Exception {
		PlatformTransactionManager manager = Mockito.mock(PlatformTransactionManager.class);
		TransactionStatus status = Mockito.mock(TransactionStatus.class);
		Mockito.when(manager.getTransaction(Mockito.any())).thenReturn(status);
		setField(target, "transactionManager", manager);
	}

	/** 获取当前测试使用的强类型审计 Mock，所有分支都从真实 record 参数断言快照。 */
	private SmtSecurityAuthDeleteLogService auditService(SmtSecurityAuthDeleteServiceImpl service) throws Exception {
		return (SmtSecurityAuthDeleteLogService) readField(service, "smtSecurityAuthDeleteLogService");
	}

	/** 捕获并按写入顺序返回审计主记录和任务引用，避免只验证删除副作用。 */
	private List<AuditRecord> verifyAuditRecords(SmtSecurityAuthDeleteServiceImpl service, int count) throws Exception {
		ArgumentCaptor<SmtSecurityAuthDeleteLog> logCaptor = ArgumentCaptor.forClass(SmtSecurityAuthDeleteLog.class);
		ArgumentCaptor<List> taskRefCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(auditService(service), Mockito.times(count)).record(logCaptor.capture(), taskRefCaptor.capture());
		List<AuditRecord> records = new ArrayList<>();
		for (int i = 0; i < logCaptor.getAllValues().size(); i++) {
			records.add(new AuditRecord(logCaptor.getAllValues().get(i), taskRefCaptor.getAllValues().get(i)));
		}
		return records;
	}

	/** 捕获单条审计记录。 */
	private AuditRecord verifySingleAudit(SmtSecurityAuthDeleteServiceImpl service) throws Exception {
		return verifyAuditRecords(service, 1).get(0);
	}

	/** 校验人员、部门和权限名称均来自判定时快照。 */
	private void assertAuditRecord(AuditRecord record, String result, Long staffId, String badge, String name,
			String department, Integer authId, String authName) {
		assertEquals(result, record.log.getResult());
		assertEquals(Integer.valueOf(10), record.log.getParkId());
		assertNotNull(record.log.getExecTime());
		assertEquals(staffId, record.log.getStaffId());
		assertEquals(badge, record.log.getStaffBadge());
		assertEquals(name, record.log.getStaffName());
		assertEquals(department, record.log.getDepartment());
		assertEquals(authId, record.log.getAuthId());
		assertEquals(authName, record.log.getAuthName());
	}

	/** 审计主记录及其全部设备任务引用的测试投影。 */
	private static final class AuditRecord {
		private final SmtSecurityAuthDeleteLog log;
		private final List<?> taskRefs;

		private AuditRecord(SmtSecurityAuthDeleteLog log, List<?> taskRefs) {
			this.log = log;
			this.taskRefs = taskRefs;
		}
	}

	/** 断言批处理入口不会因为单条业务异常而中断后续扫描。 */
	private void assertDoesNotThrow(ThrowingOperation operation) {
		try {
			operation.run();
		} catch (Throwable ex) {
			org.junit.Assert.fail("不应因单条权限失败中断任务：" + ex.getMessage());
		}
	}

	/** 反射设置字段，兼容实现字段位于父类的情况。 */
	private void setField(Object target, String name, Object value) throws Exception {
		Field field = findField(target.getClass(), name);
		field.setAccessible(true);
		field.set(target, value);
	}

	/** 读取字段，兼容实现字段位于父类的情况。 */
	private Object readField(Object target, String name) throws Exception {
		Field field = findField(target.getClass(), name);
		field.setAccessible(true);
		return field.get(target);
	}

	/** 在类层次中查找指定字段，供测试初始化和行为断言复用。 */
	private Field findField(Class<?> type, String name) throws NoSuchFieldException {
		Class<?> current = type;
		while (current != null) {
			try {
				return current.getDeclaredField(name);
			} catch (NoSuchFieldException ignored) {
				current = current.getSuperclass();
			}
		}
		throw new NoSuchFieldException(name);
	}

	/** 允许测试断言同步执行的业务操作并检查其是否向外抛错。 */
	@FunctionalInterface
	private interface ThrowingOperation {
		void run() throws Throwable;
	}
}
