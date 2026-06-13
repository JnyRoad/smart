package com.tce.smart.platform.core.service.impl;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.platform.api.dto.req.isc.EditIscStaffCardReqDTO;
import com.tce.smart.platform.core.entity.SmtIscParkConfig;
import com.tce.smart.platform.core.entity.SmtIscStaffCard;
import com.tce.smart.platform.core.entity.SmtIscCardTask;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.enums.DeviceSyncEnum;
import com.tce.smart.platform.core.mapper.SmtIscStaffCardMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.service.SmtIscCardTaskService;
import com.tce.smart.platform.core.service.SmtIscParkConfigService;
import com.tce.smart.platform.core.enums.ISCDeviceTaskEnum;
import com.tce.smart.tool.enums.DeleteStatusEnum;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtIscStaffCardServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtIscStaffCard.class);
	}

	@Test
	public void activeKeyUpdateStrategyWritesNullOnSoftDelete() {
		TableFieldInfo activeKeyInfo = activeKeyFieldInfo();

		Assert.assertEquals(FieldStrategy.IGNORED, activeKeyInfo.getUpdateStrategy());
		String sqlSet = activeKeyInfo.getSqlSet(false, null).toUpperCase(Locale.ROOT);
		Assert.assertTrue(sqlSet.contains("ACTIVE_KEY"));
		Assert.assertFalse(sqlSet.contains("<IF"));
	}

	@Test
	public void saveStaffCardCreatesActiveCardAndAddTaskForBoundDispatcherPark() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtIscParkConfigService parkConfigService = Mockito.mock(SmtIscParkConfigService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtStaff staff = staff();
		SmtIscParkConfig config = parkConfig(5000021, 6000001, DeviceSyncEnum.YES.getCode());
		Mockito.when(staffMapper.selectById(1001L)).thenReturn(staff);
		Mockito.when(parkConfigService.getConfigByPark(5000021)).thenReturn(config);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(mapper.insert(Mockito.any(SmtIscStaffCard.class))).thenReturn(1);
		Mockito.when(taskService.createAddStaffCardTask(1001L, "JA26086", 6000001, "AB123456")).thenReturn(true);
		setField(service, "smtStaffMapper", staffMapper);
		setField(service, "smtIscParkConfigService", parkConfigService);
		setField(service, "smtIscCardTaskService", taskService);

		Assert.assertTrue(service.saveStaffCard(editReq(null, 1001L, 5000021, "AB123456")));

		ArgumentCaptor<SmtIscStaffCard> cardCaptor = ArgumentCaptor.forClass(SmtIscStaffCard.class);
		Mockito.verify(mapper).insert(cardCaptor.capture());
		SmtIscStaffCard card = cardCaptor.getValue();
		Assert.assertEquals(Long.valueOf(1001L), card.getStaffId());
		Assert.assertEquals("JA26086", card.getBadge());
		Assert.assertEquals(Integer.valueOf(5000021), card.getParkId());
		Assert.assertEquals(Integer.valueOf(6000001), card.getDispatcherParkId());
		Assert.assertEquals("AB123456", card.getCardNo());
		Assert.assertEquals("6000001:AB123456", card.getActiveKey());
		Assert.assertEquals(Integer.valueOf(0), card.getSyncStatus());
		Assert.assertEquals(DeleteStatusEnum.NOT_DELETE.getCode(), card.getDelFlag());
		Mockito.verify(taskService).createAddStaffCardTask(1001L, "JA26086", 6000001, "AB123456");
	}

	@Test
	public void saveStaffCardRejectsSameStaffDuplicateWithClearMessage() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtIscParkConfigService parkConfigService = Mockito.mock(SmtIscParkConfigService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		Mockito.when(staffMapper.selectById(1001L)).thenReturn(staff());
		Mockito.when(parkConfigService.getConfigByPark(5000021))
				.thenReturn(parkConfig(5000021, 6000001, DeviceSyncEnum.YES.getCode()));
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(
				activeCard(2001L, 2002L, "JA26086", 5000021, 6000001, "12345678")));
		setField(service, "smtStaffMapper", staffMapper);
		setField(service, "smtIscParkConfigService", parkConfigService);
		setField(service, "smtIscCardTaskService", taskService);

		try {
			service.saveStaffCard(editReq(null, 1001L, 5000021, "12345678"));
			Assert.fail("expected TCEException");
		} catch (TCEException e) {
			Assert.assertTrue(e.getMessage().contains("该员工已存在相同ISC卡号"));
		}

		Mockito.verify(mapper, Mockito.never()).insert(Mockito.any(SmtIscStaffCard.class));
		Mockito.verify(taskService, Mockito.never()).createAddStaffCardTask(Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyInt(), Mockito.anyString());
	}

	@Test
	public void saveStaffCardTreatsDifferentBadgeAsOtherStaffEvenWhenStaffIdMatches() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtIscParkConfigService parkConfigService = Mockito.mock(SmtIscParkConfigService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		Mockito.when(staffMapper.selectById(1001L)).thenReturn(staff());
		Mockito.when(parkConfigService.getConfigByPark(5000021))
				.thenReturn(parkConfig(5000021, 6000001, DeviceSyncEnum.YES.getCode()));
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(
				activeCard(2001L, 1001L, "OLD26086", 5000021, 6000001, "12345678")));
		setField(service, "smtStaffMapper", staffMapper);
		setField(service, "smtIscParkConfigService", parkConfigService);
		setField(service, "smtIscCardTaskService", taskService);

		try {
			service.saveStaffCard(editReq(null, 1001L, 5000021, "12345678"));
			Assert.fail("expected TCEException");
		} catch (TCEException e) {
			Assert.assertTrue(e.getMessage().contains("该卡号已被其他员工占用"));
			Assert.assertTrue(e.getMessage().contains("OLD26086"));
		}

		Mockito.verify(mapper, Mockito.never()).insert(Mockito.any(SmtIscStaffCard.class));
		Mockito.verify(taskService, Mockito.never()).createAddStaffCardTask(Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyInt(), Mockito.anyString());
	}

	@Test
	public void saveStaffCardRejectsOtherStaffDuplicateWithOwnerBadge() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtIscParkConfigService parkConfigService = Mockito.mock(SmtIscParkConfigService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		Mockito.when(staffMapper.selectById(1001L)).thenReturn(staff());
		Mockito.when(parkConfigService.getConfigByPark(5000021))
				.thenReturn(parkConfig(5000021, 6000001, DeviceSyncEnum.YES.getCode()));
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(
				activeCard(2002L, 2002L, "JA30001", 5000021, 6000001, "12345678")));
		setField(service, "smtStaffMapper", staffMapper);
		setField(service, "smtIscParkConfigService", parkConfigService);
		setField(service, "smtIscCardTaskService", taskService);

		try {
			service.saveStaffCard(editReq(null, 1001L, 5000021, "12345678"));
			Assert.fail("expected TCEException");
		} catch (TCEException e) {
			Assert.assertTrue(e.getMessage().contains("该卡号已被其他员工占用"));
			Assert.assertTrue(e.getMessage().contains("JA30001"));
		}

		Mockito.verify(mapper, Mockito.never()).insert(Mockito.any(SmtIscStaffCard.class));
		Mockito.verify(taskService, Mockito.never()).createAddStaffCardTask(Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyInt(), Mockito.anyString());
	}

	@Test
	public void saveStaffCardSoftDeletesOldCardAndCreatesReplacementDeleteAndAddTasks() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtIscParkConfigService parkConfigService = Mockito.mock(SmtIscParkConfigService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCard oldCard = activeCard(2001L, 1001L, "JA26086", 5000021, 6000001, "87654321");
		Mockito.when(mapper.selectById(2001L)).thenReturn(oldCard);
		Mockito.when(staffMapper.selectById(1001L)).thenReturn(staff());
		Mockito.when(parkConfigService.getConfigByPark(5000021))
				.thenReturn(parkConfig(5000021, 6000001, DeviceSyncEnum.YES.getCode()));
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(mapper.updateById(Mockito.any(SmtIscStaffCard.class))).thenReturn(1);
		Mockito.when(mapper.insert(Mockito.any(SmtIscStaffCard.class))).thenReturn(1);
		Mockito.when(taskService.createAddStaffCardTask(1001L, "JA26086", 6000001, "12345678")).thenReturn(true);
		Mockito.when(taskService.createDeleteStaffCardTask(1001L, "JA26086", 6000001, "87654321")).thenReturn(true);
		setField(service, "smtStaffMapper", staffMapper);
		setField(service, "smtIscParkConfigService", parkConfigService);
		setField(service, "smtIscCardTaskService", taskService);

		Assert.assertTrue(service.saveStaffCard(editReq(2001L, 1001L, 5000021, "12345678")));

		ArgumentCaptor<SmtIscStaffCard> updateCaptor = ArgumentCaptor.forClass(SmtIscStaffCard.class);
		Mockito.verify(mapper).updateById(updateCaptor.capture());
		Assert.assertEquals(Long.valueOf(2001L), updateCaptor.getValue().getId());
		Assert.assertEquals(DeleteStatusEnum.IS_DELETE.getCode(), updateCaptor.getValue().getDelFlag());
		Assert.assertNull(updateCaptor.getValue().getActiveKey());
		Mockito.verify(mapper).insert(Mockito.argThat(card -> "12345678".equals(card.getCardNo())
					&& Integer.valueOf(6000001).equals(card.getDispatcherParkId())
					&& "6000001:12345678".equals(card.getActiveKey())
					&& DeleteStatusEnum.NOT_DELETE.getCode().equals(card.getDelFlag())));
		InOrder taskOrder = Mockito.inOrder(taskService);
		taskOrder.verify(taskService).createDeleteStaffCardTask(1001L, "JA26086", 6000001, "87654321");
		taskOrder.verify(taskService).createAddStaffCardTask(1001L, "JA26086", 6000001, "12345678");
	}

	@Test
	public void saveStaffCardRejectsVirtualCardsWithoutPersistingOrSyncing() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		setField(service, "smtIscCardTaskService", taskService);

		try {
			service.saveStaffCard(editReq(null, 1001L, 5000021, "9990000001"));
			Assert.fail("expected TCEException");
		} catch (TCEException e) {
			Assert.assertTrue(e.getMessage().contains("ISC虚拟卡号"));
		}

		Mockito.verify(mapper, Mockito.never()).insert(Mockito.any(SmtIscStaffCard.class));
		Mockito.verify(taskService, Mockito.never()).createAddStaffCardTask(Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyInt(), Mockito.anyString());
	}

	@Test
	public void saveStaffCardRejectsInvalidHikvisionCardNoWithoutPersistingOrSyncing() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtIscParkConfigService parkConfigService = Mockito.mock(SmtIscParkConfigService.class);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		Mockito.when(staffMapper.selectById(1001L)).thenReturn(staff());
		Mockito.when(parkConfigService.getConfigByPark(5000021))
				.thenReturn(parkConfig(5000021, 6000001, DeviceSyncEnum.YES.getCode()));
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(mapper.insert(Mockito.any(SmtIscStaffCard.class))).thenReturn(1);
		Mockito.when(taskService.createAddStaffCardTask(Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyInt(), Mockito.anyString())).thenReturn(true);
		setField(service, "smtStaffMapper", staffMapper);
		setField(service, "smtIscParkConfigService", parkConfigService);
		setField(service, "smtIscCardTaskService", taskService);

		for (String invalidCardNo : Arrays.asList("111111", "ab123456", "AB12-456")) {
			try {
				service.saveStaffCard(editReq(null, 1001L, 5000021, invalidCardNo));
				Assert.fail("expected TCEException");
			} catch (TCEException e) {
				Assert.assertTrue(e.getMessage().contains("8-20位数字或大写字母"));
			}
		}

		Mockito.verify(mapper, Mockito.never()).insert(Mockito.any(SmtIscStaffCard.class));
		Mockito.verify(taskService, Mockito.never()).createAddStaffCardTask(Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyInt(), Mockito.anyString());
	}

	@Test
	public void importStaffCardFromIscCreatesActiveCardWithoutCreatingCardTask() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtStaff staff = staff();
		SmtIscParkConfig config = parkConfig(5000021, 6000001, DeviceSyncEnum.YES.getCode());
		Mockito.when(mapper.selectOne(Mockito.any())).thenReturn(null);
		Mockito.when(mapper.insert(Mockito.any(SmtIscStaffCard.class))).thenReturn(1);
		setField(service, "smtIscCardTaskService", taskService);

		SmtIscStaffCard imported = service.importStaffCardFromIsc(staff, config, " 12345678 ", "首次从ISC导入");

		Assert.assertEquals("12345678", imported.getCardNo());
		Assert.assertEquals(Long.valueOf(1001L), imported.getStaffId());
		Assert.assertEquals("JA26086", imported.getBadge());
		Assert.assertEquals(Integer.valueOf(5000021), imported.getParkId());
		Assert.assertEquals(Integer.valueOf(6000001), imported.getDispatcherParkId());
		Assert.assertEquals("6000001:12345678", imported.getActiveKey());
		Assert.assertEquals(Integer.valueOf(1), imported.getSyncStatus());
		Assert.assertEquals("首次从ISC导入", imported.getRemark());
		Mockito.verify(mapper).insert(Mockito.argThat(card -> "12345678".equals(card.getCardNo())
					&& "6000001:12345678".equals(card.getActiveKey())
					&& Integer.valueOf(1).equals(card.getSyncStatus())
					&& DeleteStatusEnum.NOT_DELETE.getCode().equals(card.getDelFlag())));
		Mockito.verify(taskService, Mockito.never()).createAddStaffCardTask(Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyInt(), Mockito.anyString());
		Mockito.verify(taskService, Mockito.never()).createDeleteStaffCardTask(Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyInt(), Mockito.anyString());
	}

	@Test
	public void importStaffCardFromIscAllowsResignedStaffForInitialSync() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		SmtStaff resignedStaff = staff();
		resignedStaff.setStatus(StaffStatusEnum.STAFF_STATUS_QUIT.getCode());
		Mockito.when(mapper.selectOne(Mockito.any())).thenReturn(null);
		Mockito.when(mapper.insert(Mockito.any(SmtIscStaffCard.class))).thenReturn(1);

		SmtIscStaffCard imported = service.importStaffCardFromIsc(resignedStaff,
				parkConfig(5000021, 6000001, DeviceSyncEnum.YES.getCode()), "12345678", "首次从ISC导入");

		Assert.assertEquals(Long.valueOf(1001L), imported.getStaffId());
		Assert.assertEquals("12345678", imported.getCardNo());
		Mockito.verify(mapper).insert(Mockito.any(SmtIscStaffCard.class));
	}

	@Test
	public void importStaffCardFromIscReturnsExistingCardWhenSameStaffAlreadyHasSameCard() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCard existing = activeCard(2001L, 2002L, "JA26086", 5000021, 6000001, "12345678");
		Mockito.when(mapper.selectOne(Mockito.any())).thenReturn(existing);
		setField(service, "smtIscCardTaskService", taskService);

		SmtIscStaffCard imported = service.importStaffCardFromIsc(staff(),
				parkConfig(5000021, 6000001, DeviceSyncEnum.YES.getCode()), "12345678", "首次从ISC导入");

		Assert.assertSame(existing, imported);
		Mockito.verify(mapper, Mockito.never()).insert(Mockito.any(SmtIscStaffCard.class));
		Mockito.verify(taskService, Mockito.never()).createAddStaffCardTask(Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyInt(), Mockito.anyString());
	}

	@Test
	public void importStaffCardFromIscRejectsCardOwnedByAnotherStaff() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCard otherStaffCard = activeCard(2001L, 2002L, "JA30001", 5000021, 6000001, "12345678");
		Mockito.when(mapper.selectOne(Mockito.any())).thenReturn(otherStaffCard);
		setField(service, "smtIscCardTaskService", taskService);

		try {
				service.importStaffCardFromIsc(staff(), parkConfig(5000021, 6000001, DeviceSyncEnum.YES.getCode()),
						"12345678", "首次从ISC导入");
			Assert.fail("expected TCEException");
		} catch (TCEException e) {
			Assert.assertTrue(e.getMessage().contains("该卡号已被其他员工占用"));
			Assert.assertTrue(e.getMessage().contains("JA30001"));
		}

		Mockito.verify(mapper, Mockito.never()).insert(Mockito.any(SmtIscStaffCard.class));
		Mockito.verify(taskService, Mockito.never()).createAddStaffCardTask(Mockito.anyLong(), Mockito.anyString(),
				Mockito.anyInt(), Mockito.anyString());
	}

	@Test
	public void removeStaffCardSoftDeletesActiveCardAndCreatesDeleteTask() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		SmtIscStaffCard card = activeCard(2001L, 1001L, "JA26086", 5000021, 6000001, "87654321");
		Mockito.when(mapper.selectById(2001L)).thenReturn(card);
		Mockito.when(mapper.updateById(Mockito.any(SmtIscStaffCard.class))).thenReturn(1);
		Mockito.when(taskService.createDeleteStaffCardTask(1001L, "JA26086", 6000001, "87654321")).thenReturn(true);
		setField(service, "smtIscCardTaskService", taskService);

		Assert.assertTrue(service.removeStaffCard(2001L));

		ArgumentCaptor<SmtIscStaffCard> updateCaptor = ArgumentCaptor.forClass(SmtIscStaffCard.class);
		Mockito.verify(mapper).updateById(updateCaptor.capture());
		Assert.assertEquals(Long.valueOf(2001L), updateCaptor.getValue().getId());
		Assert.assertEquals(DeleteStatusEnum.IS_DELETE.getCode(), updateCaptor.getValue().getDelFlag());
		Assert.assertNull(updateCaptor.getValue().getActiveKey());
		Mockito.verify(taskService).createDeleteStaffCardTask(1001L, "JA26086", 6000001, "87654321");
	}

	@Test
	public void removeStaffCardsByStaffIdSoftDeletesActiveCardsAndCreatesDeleteTasks() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		SmtIscCardTaskService taskService = Mockito.mock(SmtIscCardTaskService.class);
		Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Arrays.asList(
					activeCard(2001L, 1001L, "JA26086", 5000021, 6000001, "87654321"),
					activeCard(2002L, 1001L, "JA26086", 5000021, 6000001, "12345678")));
		Mockito.when(mapper.updateById(Mockito.any(SmtIscStaffCard.class))).thenReturn(1);
		Mockito.when(taskService.createDeleteStaffCardTask(Mockito.eq(1001L), Mockito.eq("JA26086"),
				Mockito.eq(6000001), Mockito.anyString())).thenReturn(true);
		setField(service, "smtIscCardTaskService", taskService);

		Assert.assertTrue(service.removeStaffCardsByStaffId(1001L));

		Mockito.verify(mapper, Mockito.times(2)).updateById(Mockito.any(SmtIscStaffCard.class));
		Mockito.verify(taskService).createDeleteStaffCardTask(1001L, "JA26086", 6000001, "87654321");
		Mockito.verify(taskService).createDeleteStaffCardTask(1001L, "JA26086", 6000001, "12345678");
	}

	@Test
	public void isActiveStaffCardUsesIndependentCardTable() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		Mockito.when(mapper.selectCount(Mockito.any())).thenReturn(1);

			Assert.assertTrue(service.isActiveStaffCard(1001L, "JA26086", 6000001, "87654321"));

		ArgumentCaptor<LambdaQueryWrapper> queryCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
		Mockito.verify(mapper).selectCount(queryCaptor.capture());
		String sqlSegment = queryCaptor.getValue().getSqlSegment().toUpperCase(Locale.ROOT);
		Assert.assertTrue(sqlSegment.contains("STAFF_ID"));
		Assert.assertTrue(sqlSegment.contains("BADGE"));
		Assert.assertTrue(sqlSegment.contains("DISPATCHER_PARK_ID"));
		Assert.assertTrue(sqlSegment.contains("CARD_NO"));
		Assert.assertTrue(sqlSegment.contains("DEL_FLAG"));
	}

	@Test
	public void listStaffCardsOnlyReturnsActiveCardsForOneStaff() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
			Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(
					activeCard(2001L, 1001L, "JA26086", 5000021, 6000001, "87654321")));

		Assert.assertEquals(1, service.listStaffCards(1001L).size());

		ArgumentCaptor<AbstractWrapper> queryCaptor = ArgumentCaptor.forClass(AbstractWrapper.class);
		Mockito.verify(mapper).selectList(queryCaptor.capture());
		String sqlSegment = queryCaptor.getValue().getSqlSegment().toUpperCase(Locale.ROOT);
		Assert.assertTrue(sqlSegment.contains("STAFF_ID"));
		Assert.assertTrue(sqlSegment.contains("DEL_FLAG"));
		Assert.assertTrue(sqlSegment.contains("CREATE_TIME"));
	}

	@Test
	public void getFirstActiveCardNoByBadgeOrdersWithoutAppendingRawRowNumPredicate() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
			Mockito.when(mapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(
					activeCard(2001L, 1001L, "JA26086", 5000021, 6000001, "12345678")));

			Assert.assertEquals("12345678", service.getFirstActiveCardNoByBadge(" JA26086 "));

		ArgumentCaptor<AbstractWrapper> queryCaptor = ArgumentCaptor.forClass(AbstractWrapper.class);
		Mockito.verify(mapper).selectList(queryCaptor.capture());
		String sqlSegment = queryCaptor.getValue().getSqlSegment().toUpperCase(Locale.ROOT);
		Assert.assertTrue(sqlSegment.contains("BADGE"));
		Assert.assertTrue(sqlSegment.contains("DEL_FLAG"));
		Assert.assertTrue(sqlSegment.contains("SYNC_STATUS"));
		Assert.assertTrue(sqlSegment.contains("UPDATE_TIME"));
		Assert.assertFalse(sqlSegment.contains("ROWNUM"));
	}

	@Test
	public void markAddTaskSuccessUpdatesMatchingActiveCardSyncState() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);

		service.markAddTaskSuccess(addTask(3001L, "JA26086", "12345678"));

		ArgumentCaptor<AbstractWrapper> wrapperCaptor = ArgumentCaptor.forClass(AbstractWrapper.class);
		Mockito.verify(mapper).update(Mockito.isNull(), wrapperCaptor.capture());
		String sqlSet = wrapperCaptor.getValue().getSqlSet().toUpperCase(Locale.ROOT);
		String sqlSegment = wrapperCaptor.getValue().getSqlSegment().toUpperCase(Locale.ROOT);
		Assert.assertTrue(sqlSet.contains("SYNC_STATUS"));
		Assert.assertTrue(sqlSet.contains("LAST_TASK_ID"));
		Assert.assertTrue(sqlSet.contains("LAST_SYNC_CODE"));
		Assert.assertTrue(sqlSet.contains("LAST_SYNC_REMARK"));
		Assert.assertTrue(sqlSet.contains("LAST_SYNC_TIME"));
		Assert.assertFalse(sqlSet.contains("ACTIVE_KEY"));
		Assert.assertFalse(sqlSet.contains("DEL_FLAG"));
		Assert.assertTrue(sqlSegment.contains("STAFF_ID"));
		Assert.assertTrue(sqlSegment.contains("DISPATCHER_PARK_ID"));
		Assert.assertTrue(sqlSegment.contains("CARD_NO"));
		Assert.assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue(3001L));
		Assert.assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue(ISCDeviceTaskEnum.DEVICE_OK.getCode()));
		Assert.assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue("操作成功"));
	}

	@Test
	public void markAddTaskFailedSoftDeletesInvalidLocalCardWhenRequested() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		SmtIscCardTask task = addTask(3002L, "JA26086", "111111");
		task.setCode(ISCDeviceTaskEnum.AUTH_CONFIG_DOWN_FAIL.getCode());
		task.setRemark("ISC卡号必须为8-20位数字或大写字母");

		service.markAddTaskFailed(task, true);

		ArgumentCaptor<AbstractWrapper> wrapperCaptor = ArgumentCaptor.forClass(AbstractWrapper.class);
		Mockito.verify(mapper).update(Mockito.isNull(), wrapperCaptor.capture());
		String sqlSet = wrapperCaptor.getValue().getSqlSet().toUpperCase(Locale.ROOT);
		Assert.assertTrue(sqlSet.contains("SYNC_STATUS"));
		Assert.assertTrue(sqlSet.contains("LAST_TASK_ID"));
		Assert.assertTrue(sqlSet.contains("LAST_SYNC_CODE"));
		Assert.assertTrue(sqlSet.contains("LAST_SYNC_REMARK"));
		Assert.assertTrue(sqlSet.contains("LAST_SYNC_TIME"));
		Assert.assertTrue(sqlSet.contains("DEL_FLAG"));
		Assert.assertTrue(sqlSet.contains("ACTIVE_KEY"));
		Assert.assertTrue(sqlSet.contains("DELETE_TIME"));
		Assert.assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue(3002L));
		Assert.assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue("ISC卡号必须为8-20位数字或大写字母"));
	}

	@Test
	public void markAddTaskFailedKeepsConflictCardVisibleAsFailed() {
		SmtIscStaffCardMapper mapper = Mockito.mock(SmtIscStaffCardMapper.class);
		SmtIscStaffCardServiceImpl service = service(mapper);
		Mockito.when(mapper.update(Mockito.isNull(), Mockito.any())).thenReturn(1);
		SmtIscCardTask task = addTask(3003L, "JA26086", "12345678");
		task.setCode(0x04a12700);
		task.setRemark("ISC接口请求异常: 卡号已存在 (code=0x04a12700)");

		service.markAddTaskFailed(task, false);

		ArgumentCaptor<AbstractWrapper> wrapperCaptor = ArgumentCaptor.forClass(AbstractWrapper.class);
		Mockito.verify(mapper).update(Mockito.isNull(), wrapperCaptor.capture());
		String sqlSet = wrapperCaptor.getValue().getSqlSet().toUpperCase(Locale.ROOT);
		Assert.assertTrue(sqlSet.contains("SYNC_STATUS"));
		Assert.assertTrue(sqlSet.contains("LAST_TASK_ID"));
		Assert.assertTrue(sqlSet.contains("LAST_SYNC_CODE"));
		Assert.assertTrue(sqlSet.contains("LAST_SYNC_REMARK"));
		Assert.assertTrue(sqlSet.contains("LAST_SYNC_TIME"));
		Assert.assertFalse(sqlSet.contains("ACTIVE_KEY"));
		Assert.assertFalse(sqlSet.contains("DEL_FLAG"));
		Assert.assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue(3003L));
		Assert.assertTrue(wrapperCaptor.getValue().getParamNameValuePairs().containsValue(0x04a12700));
		Assert.assertTrue(wrapperCaptor.getValue().getParamNameValuePairs()
				.containsValue("ISC接口请求异常: 卡号已存在 (code=0x04a12700)"));
	}

	private SmtIscStaffCardServiceImpl service(SmtIscStaffCardMapper mapper) {
		SmtIscStaffCardServiceImpl service = new SmtIscStaffCardServiceImpl();
		setField(service, "baseMapper", mapper);
		return service;
	}

	private EditIscStaffCardReqDTO editReq(Long id, Long staffId, Integer parkId, String cardNo) {
		EditIscStaffCardReqDTO reqDTO = new EditIscStaffCardReqDTO();
		reqDTO.setId(id);
		reqDTO.setStaffId(staffId);
		reqDTO.setParkId(parkId);
		reqDTO.setCardNo(cardNo);
		return reqDTO;
	}

	private SmtStaff staff() {
		SmtStaff staff = new SmtStaff();
		staff.setId(1001L);
		staff.setBadge("JA26086");
		staff.setName("张三");
		staff.setStatus(StaffStatusEnum.STAFF_STATUS_IN.getCode());
		return staff;
	}

	private SmtIscParkConfig parkConfig(Integer parkId, Integer dispatcherParkId, Integer enabled) {
		SmtIscParkConfig config = new SmtIscParkConfig();
		config.setParkId(parkId);
		config.setParkName("许昌");
		config.setDispatcherParkId(dispatcherParkId);
		config.setDispatcherParkName("许昌ISC");
		config.setCardSyncEnabled(enabled);
		config.setDelFlag(DeleteStatusEnum.NOT_DELETE.getCode());
		return config;
	}

	private SmtIscStaffCard activeCard(Long id, Long staffId, String badge, Integer parkId,
									   Integer dispatcherParkId, String cardNo) {
		SmtIscStaffCard card = new SmtIscStaffCard();
		card.setId(id);
		card.setStaffId(staffId);
		card.setBadge(badge);
		card.setParkId(parkId);
		card.setParkName("许昌");
		card.setDispatcherParkId(dispatcherParkId);
		card.setDispatcherParkName("许昌ISC");
		card.setCardNo(cardNo);
		card.setDelFlag(DeleteStatusEnum.NOT_DELETE.getCode());
		return card;
	}

	private SmtIscCardTask addTask(Long taskId, String badge, String cardNo) {
		SmtIscCardTask task = new SmtIscCardTask();
		task.setId(taskId);
		task.setAction(DeviceTaskActionEnum.DOWN.getCode());
		task.setSourceType("STAFF");
		task.setSourceId(1001L);
		task.setBadge(badge);
		task.setParkId(6000001);
		task.setCardNo(cardNo);
		task.setCode(ISCDeviceTaskEnum.DEVICE_OK.getCode());
		task.setRemark("操作成功");
		return task;
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

	private TableFieldInfo activeKeyFieldInfo() {
		return TableInfoHelper.getTableInfo(SmtIscStaffCard.class).getFieldList().stream()
				.filter(field -> "activeKey".equals(field.getProperty()))
				.findFirst()
				.orElseThrow(() -> new AssertionError("activeKey field not found"));
	}
}
