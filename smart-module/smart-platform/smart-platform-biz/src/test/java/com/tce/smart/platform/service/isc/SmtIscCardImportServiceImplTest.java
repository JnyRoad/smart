package com.tce.smart.platform.service.isc;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.tce.smart.common.core.constant.SecurityConstants;
import com.tce.smart.common.core.exception.TCEException;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.dispatcher.api.dto.req.DispatcherDTO;
import com.tce.smart.dispatcher.api.enums.EventEnum;
import com.tce.smart.dispatcher.api.feign.RemoteDispatcherService;
import com.tce.smart.platform.core.entity.SmtIscCardImportBatch;
import com.tce.smart.platform.core.entity.SmtIscCardImportDetail;
import com.tce.smart.platform.core.entity.SmtIscParkConfig;
import com.tce.smart.platform.core.entity.SmtIscStaffCard;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.enums.IscCardImportModeEnum;
import com.tce.smart.platform.core.enums.IscCardImportResultEnum;
import com.tce.smart.platform.core.enums.IscCardImportStaffScopeEnum;
import com.tce.smart.platform.core.mapper.SmtIscCardImportBatchMapper;
import com.tce.smart.platform.core.mapper.SmtIscCardImportDetailMapper;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.service.SmtIscParkConfigService;
import com.tce.smart.platform.core.service.SmtIscStaffCardService;
import com.tce.smart.platform.service.isc.impl.SmtIscCardImportServiceImpl;
import com.tce.smart.tool.enums.DeleteStatusEnum;
import com.tce.smart.tool.enums.StaffStatusEnum;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SmtIscCardImportServiceImplTest {

	private static final String RESULT_REMOVED = "REMOVED";

	@Test
	public void iscCardEventsUseDocumentedHikvisionCardApiPaths() {
		Assert.assertEquals("/api/cis/v1/card/bindings", EventEnum.ISC_CARD_ADD.getKey());
		Assert.assertEquals("/api/cis/v1/card/deletion", EventEnum.ISC_CARD_DELETE.getKey());
		Assert.assertEquals("/api/irds/v1/card/advance/cardList", EventEnum.ISC_CARD_LIST_GET.getKey());
	}

	@Test
	public void dryRunRecordsReadyImportWithoutPersistingStaffCard() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtIscCardImportServiceImpl service = service(batchMapper, detailMapper, staffCardService);
		Mockito.when(batchMapper.getById(1L)).thenReturn(batch(1L, IscCardImportModeEnum.DRY_RUN.getCode()));
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class))).thenReturn(Collections.emptyList());

		service.executeBatch(1L);

		ArgumentCaptor<SmtIscCardImportDetail> detailCaptor = ArgumentCaptor.forClass(SmtIscCardImportDetail.class);
		Mockito.verify(detailMapper).insert(detailCaptor.capture());
		Assert.assertEquals(IscCardImportResultEnum.READY_IMPORT.getCode(), detailCaptor.getValue().getResultCode());
		Assert.assertEquals("12345678", detailCaptor.getValue().getIscCardNo());
		Mockito.verify(staffCardService, Mockito.never()).importStaffCardFromIsc(Mockito.any(), Mockito.any(),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}

	@Test
	public void importBatchImportsReadyCardsFromIscIntoLocalCardTable() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtIscCardImportServiceImpl service = service(batchMapper, detailMapper, staffCardService);
		Mockito.when(batchMapper.getById(2L)).thenReturn(batch(2L, IscCardImportModeEnum.IMPORT.getCode()));
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class))).thenReturn(Collections.emptyList());
		Mockito.when(staffCardService.importStaffCardFromIsc(Mockito.any(), Mockito.any(), Mockito.eq("12345678"),
				Mockito.anyString(), Mockito.eq("admin"))).thenReturn(activeCard(2001L, 1001L, "JA26086", "12345678"));

		service.executeBatch(2L);

		Mockito.verify(staffCardService).importStaffCardFromIsc(Mockito.argThat(staff -> "JA26086".equals(staff.getBadge())),
				Mockito.argThat(config -> Integer.valueOf(6000001).equals(config.getDispatcherParkId())),
				Mockito.eq("12345678"), Mockito.contains("ISC"), Mockito.eq("admin"));
		ArgumentCaptor<SmtIscCardImportDetail> detailCaptor = ArgumentCaptor.forClass(SmtIscCardImportDetail.class);
		Mockito.verify(detailMapper).insert(detailCaptor.capture());
		Assert.assertEquals(IscCardImportResultEnum.IMPORTED.getCode(), detailCaptor.getValue().getResultCode());
	}

	@Test
	public void importBatchAcceptsHikvisionUppercaseLetterCardNo() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtIscCardImportServiceImpl service = service(batchMapper, detailMapper, staffCardService,
				"{\"list\":[{\"jobNo\":\"JA26086\",\"personId\":\"isc-person-1\"}]}",
				"{\"list\":[{\"personId\":\"isc-person-1\",\"cardNo\":\"AB123456\"}],\"total\":1}");
		Mockito.when(batchMapper.getById(26L)).thenReturn(batch(26L, IscCardImportModeEnum.IMPORT.getCode()));
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class))).thenReturn(Collections.emptyList());
		Mockito.when(staffCardService.importStaffCardFromIsc(Mockito.any(), Mockito.any(), Mockito.eq("AB123456"),
				Mockito.anyString(), Mockito.eq("admin"))).thenReturn(activeCard(2601L, 1001L, "JA26086", "AB123456"));

		service.executeBatch(26L);

		Mockito.verify(staffCardService).importStaffCardFromIsc(Mockito.any(), Mockito.any(),
				Mockito.eq("AB123456"), Mockito.contains("ISC"), Mockito.eq("admin"));
		ArgumentCaptor<SmtIscCardImportDetail> detailCaptor = ArgumentCaptor.forClass(SmtIscCardImportDetail.class);
		Mockito.verify(detailMapper).insert(detailCaptor.capture());
		Assert.assertEquals(IscCardImportResultEnum.IMPORTED.getCode(), detailCaptor.getValue().getResultCode());
		Assert.assertEquals("AB123456", detailCaptor.getValue().getIscCardNo());
	}

	@Test
	public void importBatchRemovesCardAfterImportingResignedStaffCardFromIsc() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtStaff resignedStaff = staff();
		resignedStaff.setStatus(StaffStatusEnum.STAFF_STATUS_QUIT.getCode());
		SmtIscCardImportServiceImpl service = service(batchMapper, detailMapper, staffCardService, resignedStaff);
		Mockito.when(batchMapper.getById(21L)).thenReturn(batch(21L, IscCardImportModeEnum.IMPORT.getCode()));
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class))).thenReturn(Collections.emptyList());
		Mockito.when(staffCardService.importStaffCardFromIsc(Mockito.any(), Mockito.any(), Mockito.eq("12345678"),
				Mockito.anyString(), Mockito.eq("admin"))).thenReturn(activeCard(2001L, 1001L, "JA26086", "12345678"));
		Mockito.when(staffCardService.removeStaffCard(2001L)).thenReturn(Boolean.TRUE);

		service.executeBatch(21L);

		Mockito.verify(staffCardService).removeStaffCard(2001L);
		ArgumentCaptor<SmtIscCardImportDetail> detailCaptor = ArgumentCaptor.forClass(SmtIscCardImportDetail.class);
		Mockito.verify(detailMapper).insert(detailCaptor.capture());
		Assert.assertEquals(IscCardImportResultEnum.IMPORTED.getCode(), detailCaptor.getValue().getResultCode());
		Assert.assertTrue(detailCaptor.getValue().getReason().contains("退卡清理"));
	}

	@Test
	public void importBatchRemovesExistingSameCardForResignedStaff() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtStaff resignedStaff = staff();
		resignedStaff.setStatus(StaffStatusEnum.STAFF_STATUS_QUIT.getCode());
		SmtIscCardImportServiceImpl service = service(batchMapper, detailMapper, staffCardService, resignedStaff);
		Mockito.when(batchMapper.getById(23L)).thenReturn(batch(23L, IscCardImportModeEnum.IMPORT.getCode()));
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class)))
				.thenReturn(Collections.singletonList(activeCard(2301L, 1001L, "JA26086", "12345678")));
		Mockito.when(staffCardService.removeStaffCard(2301L)).thenReturn(Boolean.TRUE);

		service.executeBatch(23L);

		Mockito.verify(staffCardService).removeStaffCard(2301L);
		Mockito.verify(staffCardService, Mockito.never()).importStaffCardFromIsc(Mockito.any(), Mockito.any(),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
		ArgumentCaptor<SmtIscCardImportDetail> detailCaptor = ArgumentCaptor.forClass(SmtIscCardImportDetail.class);
		Mockito.verify(detailMapper).insert(detailCaptor.capture());
		Assert.assertEquals(RESULT_REMOVED, detailCaptor.getValue().getResultCode());
		Assert.assertTrue(detailCaptor.getValue().getReason().contains("退卡清理"));
	}

	@Test
	public void importBatchRemovesLocalOnlyCardForResignedStaff() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtStaff resignedStaff = staff();
		resignedStaff.setStatus(StaffStatusEnum.STAFF_STATUS_QUIT.getCode());
		SmtIscCardImportServiceImpl service = service(batchMapper, detailMapper, staffCardService, resignedStaff,
				"{\"list\":[{\"jobNo\":\"JA26086\",\"personId\":\"isc-person-1\"}]}",
				"{\"list\":[],\"total\":0}");
		Mockito.when(batchMapper.getById(24L)).thenReturn(batch(24L, IscCardImportModeEnum.IMPORT.getCode()));
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class)))
				.thenReturn(Collections.singletonList(activeCard(2401L, 1001L, "JA26086", "12345678")));
		Mockito.when(staffCardService.removeStaffCard(2401L)).thenReturn(Boolean.TRUE);

		service.executeBatch(24L);

		Mockito.verify(staffCardService).removeStaffCard(2401L);
		ArgumentCaptor<SmtIscCardImportDetail> detailCaptor = ArgumentCaptor.forClass(SmtIscCardImportDetail.class);
		Mockito.verify(detailMapper, Mockito.times(2)).insert(detailCaptor.capture());
		Assert.assertTrue(detailCaptor.getAllValues().stream()
				.anyMatch(detail -> RESULT_REMOVED.equals(detail.getResultCode())
						&& detail.getReason().contains("退卡清理")));
	}

	@Test
	public void importBatchCleansResignedOwnerCardBeforeImportingCurrentIscOwner() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtStaff activeStaff = staff();
		SmtStaff resignedStaff = staff();
		resignedStaff.setId(1002L);
		resignedStaff.setBadge("JA26087");
		resignedStaff.setName("李四");
		resignedStaff.setStatus(StaffStatusEnum.STAFF_STATUS_QUIT.getCode());
		SmtIscCardImportServiceImpl service = service(batchMapper, detailMapper, staffCardService,
				activeStaff, resignedStaff);
		Mockito.when(batchMapper.getById(25L)).thenReturn(batch(25L, IscCardImportModeEnum.IMPORT.getCode()));
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class)))
				.thenReturn(Collections.singletonList(activeCard(2501L, 1002L, "JA26087", "12345678")));
		Mockito.when(staffCardService.removeStaffCard(2501L)).thenReturn(Boolean.TRUE);
		Mockito.when(staffCardService.importStaffCardFromIsc(Mockito.eq(activeStaff), Mockito.any(),
				Mockito.eq("12345678"), Mockito.anyString(), Mockito.eq("admin")))
				.thenReturn(activeCard(2502L, 1001L, "JA26086", "12345678"));

		service.executeBatch(25L);

		Mockito.verify(staffCardService).removeStaffCard(2501L);
		Mockito.verify(staffCardService).importStaffCardFromIsc(Mockito.eq(activeStaff), Mockito.any(),
				Mockito.eq("12345678"), Mockito.anyString(), Mockito.eq("admin"));
		ArgumentCaptor<SmtIscCardImportDetail> detailCaptor = ArgumentCaptor.forClass(SmtIscCardImportDetail.class);
		Mockito.verify(detailMapper, Mockito.times(3)).insert(detailCaptor.capture());
		Assert.assertTrue(detailCaptor.getAllValues().stream()
				.anyMatch(detail -> RESULT_REMOVED.equals(detail.getResultCode())
						&& detail.getReason().contains("退卡清理")));
		Assert.assertTrue(detailCaptor.getAllValues().stream()
				.anyMatch(detail -> IscCardImportResultEnum.IMPORTED.getCode().equals(detail.getResultCode())
						&& "JA26086".equals(detail.getBadge())));
		Assert.assertFalse(detailCaptor.getAllValues().stream()
				.anyMatch(detail -> IscCardImportResultEnum.CONFLICT.getCode().equals(detail.getResultCode())));
	}

	@Test
	public void conflictCardOwnedByAnotherStaffIsRecordedButNotImported() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtIscCardImportServiceImpl service = service(batchMapper, detailMapper, staffCardService);
		Mockito.when(batchMapper.getById(3L)).thenReturn(batch(3L, IscCardImportModeEnum.IMPORT.getCode()));
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class)))
				.thenReturn(Collections.singletonList(activeCard(3001L, 2002L, "JA30001", "12345678")));

		service.executeBatch(3L);

		ArgumentCaptor<SmtIscCardImportDetail> detailCaptor = ArgumentCaptor.forClass(SmtIscCardImportDetail.class);
		Mockito.verify(detailMapper).insert(detailCaptor.capture());
		Assert.assertEquals(IscCardImportResultEnum.CONFLICT.getCode(), detailCaptor.getValue().getResultCode());
		Mockito.verify(staffCardService, Mockito.never()).importStaffCardFromIsc(Mockito.any(), Mockito.any(),
				Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
	}

	@Test
	public void activeIscPersonIsPreferredWhenDeletedPersonIsReturnedFirst() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtIscCardImportServiceImpl service = service(batchMapper, detailMapper, staffCardService,
				"{\"list\":[{\"jobNo\":\"JA26086\",\"personId\":\"deleted-person\",\"status\":-1},"
						+ "{\"jobNo\":\"JA26086\",\"personId\":\"active-person\",\"status\":1}]}",
				"{\"list\":[{\"personId\":\"active-person\",\"cardNo\":\"12345678\"}],\"total\":1}");
		Mockito.when(batchMapper.getById(4L)).thenReturn(batch(4L, IscCardImportModeEnum.DRY_RUN.getCode()));
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class))).thenReturn(Collections.emptyList());

		service.executeBatch(4L);

		ArgumentCaptor<SmtIscCardImportDetail> detailCaptor = ArgumentCaptor.forClass(SmtIscCardImportDetail.class);
		Mockito.verify(detailMapper).insert(detailCaptor.capture());
		Assert.assertEquals(IscCardImportResultEnum.READY_IMPORT.getCode(), detailCaptor.getValue().getResultCode());
		Assert.assertEquals("active-person", detailCaptor.getValue().getPersonId());
	}

	@Test
	public void personDetailCardFieldsAreNotUsedWhenDocumentedCardQueryReturnsEmpty() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtIscCardImportServiceImpl service = service(batchMapper, detailMapper, staffCardService,
				"{\"list\":[{\"jobNo\":\"JA26086\",\"personId\":\"isc-person-1\",\"cardNo\":\"12345678\","
						+ "\"cardList\":[{\"cardNo\":\"12345\"}]}]}",
				"{\"list\":[],\"total\":0}");
		Mockito.when(batchMapper.getById(5L)).thenReturn(batch(5L, IscCardImportModeEnum.DRY_RUN.getCode()));
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class))).thenReturn(Collections.emptyList());

		service.executeBatch(5L);

		ArgumentCaptor<SmtIscCardImportDetail> detailCaptor = ArgumentCaptor.forClass(SmtIscCardImportDetail.class);
		Mockito.verify(detailMapper).insert(detailCaptor.capture());
		Assert.assertEquals(IscCardImportResultEnum.ISC_EMPTY.getCode(), detailCaptor.getValue().getResultCode());
		Assert.assertNull(detailCaptor.getValue().getIscCardNo());
	}

	@Test
	public void undocumentedCardListFieldsAreIgnored() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtIscCardImportServiceImpl service = service(batchMapper, detailMapper, staffCardService,
				"{\"list\":[{\"jobNo\":\"JA26086\",\"personId\":\"isc-person-1\"}]}",
				"{\"records\":[{\"personId\":\"isc-person-1\",\"cardNumber\":\"12345678\"}],\"totalCount\":1}");
		Mockito.when(batchMapper.getById(6L)).thenReturn(batch(6L, IscCardImportModeEnum.DRY_RUN.getCode()));
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class))).thenReturn(Collections.emptyList());

		service.executeBatch(6L);

		ArgumentCaptor<SmtIscCardImportDetail> detailCaptor = ArgumentCaptor.forClass(SmtIscCardImportDetail.class);
		Mockito.verify(detailMapper).insert(detailCaptor.capture());
		Assert.assertEquals(IscCardImportResultEnum.ISC_EMPTY.getCode(), detailCaptor.getValue().getResultCode());
		Assert.assertNull(detailCaptor.getValue().getIscCardNo());
	}

	@Test
	public void longIscFailureMessageIsTruncatedBeforeUpdatingBatch() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtIscCardImportServiceImpl service = serviceWithPersonQueryFailure(batchMapper, detailMapper, staffCardService,
				String.join("", Collections.nCopies(1100, "A")));
		Mockito.when(batchMapper.getById(7L)).thenReturn(batch(7L, IscCardImportModeEnum.DRY_RUN.getCode()));
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class))).thenReturn(Collections.emptyList());

		service.executeBatch(7L);

		ArgumentCaptor<SmtIscCardImportBatch> batchCaptor = ArgumentCaptor.forClass(SmtIscCardImportBatch.class);
		Mockito.verify(batchMapper, Mockito.times(2)).updateById(batchCaptor.capture());
		SmtIscCardImportBatch failedBatch = batchCaptor.getAllValues().get(1);
		Assert.assertEquals("FAIL", failedBatch.getStatus());
		Assert.assertEquals(1000, failedBatch.getRemark().length());
	}

	@Test
	public void asyncBatchUsesStoredParkConfigWithoutCurrentUserContext() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtIscParkConfigService parkConfigService = Mockito.mock(SmtIscParkConfigService.class);
		SmtIscCardImportServiceImpl service = service(batchMapper, detailMapper, staffCardService, parkConfigService);
		Mockito.when(batchMapper.getById(8L)).thenReturn(batch(8L, IscCardImportModeEnum.DRY_RUN.getCode()));
		Mockito.when(parkConfigService.getConfigByPark(5000021))
				.thenThrow(new TCEException("未获取到当前登录用户园区权限"));
		Mockito.when(parkConfigService.getOne(Mockito.any(Wrapper.class), Mockito.eq(false))).thenReturn(parkConfig());
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class))).thenReturn(Collections.emptyList());

		service.executeBatch(8L);

		ArgumentCaptor<SmtIscCardImportDetail> detailCaptor = ArgumentCaptor.forClass(SmtIscCardImportDetail.class);
		Mockito.verify(detailMapper).insert(detailCaptor.capture());
		Assert.assertEquals(IscCardImportResultEnum.READY_IMPORT.getCode(), detailCaptor.getValue().getResultCode());
	}

	@Test
	public void batchPassesStoredStaffScopeToStaffMapper() throws Exception {
		SmtIscCardImportBatchMapper batchMapper = Mockito.mock(SmtIscCardImportBatchMapper.class);
		SmtIscCardImportDetailMapper detailMapper = Mockito.mock(SmtIscCardImportDetailMapper.class);
		SmtIscStaffCardService staffCardService = Mockito.mock(SmtIscStaffCardService.class);
		SmtIscCardImportServiceImpl service = service(batchMapper, detailMapper, staffCardService);
		SmtIscCardImportBatch batch = batch(22L, IscCardImportModeEnum.DRY_RUN.getCode());
		batch.setParamsJson("{\"staffScope\":\"ACTIVE\"}");
		Mockito.when(batchMapper.getById(22L)).thenReturn(batch);
		Mockito.when(staffCardService.list(Mockito.any(Wrapper.class))).thenReturn(Collections.emptyList());

		service.executeBatch(22L);

		SmtStaffMapper staffMapper = (SmtStaffMapper) getField(service, "smtStaffMapper");
		Mockito.verify(staffMapper).listIscCardImportStaff(5000021, null,
				IscCardImportStaffScopeEnum.ACTIVE.getCode());
	}

	private SmtIscCardImportServiceImpl service(SmtIscCardImportBatchMapper batchMapper,
													SmtIscCardImportDetailMapper detailMapper,
													SmtIscStaffCardService staffCardService) throws Exception {
		return service(batchMapper, detailMapper, staffCardService, staff());
	}

	private SmtIscCardImportServiceImpl service(SmtIscCardImportBatchMapper batchMapper,
													SmtIscCardImportDetailMapper detailMapper,
													SmtIscStaffCardService staffCardService,
													SmtStaff staff) throws Exception {
		return service(batchMapper, detailMapper, staffCardService,
				staff,
				"{\"list\":[{\"jobNo\":\"JA26086\",\"personId\":\"isc-person-1\"}]}",
				"{\"list\":[{\"personId\":\"isc-person-1\",\"cardNo\":\"12345678\"}],\"total\":1}");
	}

	private SmtIscCardImportServiceImpl service(SmtIscCardImportBatchMapper batchMapper,
													SmtIscCardImportDetailMapper detailMapper,
													SmtIscStaffCardService staffCardService,
													String personResponse,
													String cardResponse) throws Exception {
		return service(batchMapper, detailMapper, staffCardService, staff(), personResponse, cardResponse);
	}

	private SmtIscCardImportServiceImpl service(SmtIscCardImportBatchMapper batchMapper,
													SmtIscCardImportDetailMapper detailMapper,
													SmtIscStaffCardService staffCardService,
													SmtStaff staff,
													String personResponse,
													String cardResponse) throws Exception {
		SmtIscParkConfigService parkConfigService = Mockito.mock(SmtIscParkConfigService.class);
		Mockito.when(parkConfigService.getConfigByPark(5000021)).thenReturn(parkConfig());
		Mockito.when(parkConfigService.getOne(Mockito.any(Wrapper.class), Mockito.eq(false))).thenReturn(parkConfig());
		return service(batchMapper, detailMapper, staffCardService, parkConfigService, staff, personResponse,
				cardResponse);
	}

	private SmtIscCardImportServiceImpl service(SmtIscCardImportBatchMapper batchMapper,
													SmtIscCardImportDetailMapper detailMapper,
													SmtIscStaffCardService staffCardService,
													SmtIscParkConfigService parkConfigService) throws Exception {
		return service(batchMapper, detailMapper, staffCardService, parkConfigService,
				staff(),
				"{\"list\":[{\"jobNo\":\"JA26086\",\"personId\":\"isc-person-1\"}]}",
				"{\"list\":[{\"personId\":\"isc-person-1\",\"cardNo\":\"12345678\"}],\"total\":1}");
	}

	private SmtIscCardImportServiceImpl service(SmtIscCardImportBatchMapper batchMapper,
													SmtIscCardImportDetailMapper detailMapper,
													SmtIscStaffCardService staffCardService,
													SmtIscParkConfigService parkConfigService,
													SmtStaff staff,
													String personResponse,
													String cardResponse) throws Exception {
		SmtIscCardImportServiceImpl service = new SmtIscCardImportServiceImpl();
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		Mockito.when(staffMapper.listIscCardImportStaff(Mockito.eq(5000021), Mockito.isNull(), Mockito.anyString()))
				.thenReturn(Collections.singletonList(staff));
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenAnswer(invocation -> {
					DispatcherDTO<Map> dto = invocation.getArgument(0);
					Assert.assertEquals(Integer.valueOf(6000001), dto.getParkId());
					if (EventEnum.ISC_PERSON_GET.getCode().equals(dto.getEventType())) {
						return Result.success(personResponse);
					}
					Assert.assertEquals(EventEnum.ISC_CARD_LIST_GET.getCode(), dto.getEventType());
					Assert.assertTrue(dto.getData().containsKey("personIds"));
					Assert.assertFalse(dto.getData().containsKey("personId"));
					return Result.success(cardResponse);
				});
		setField(service, "baseMapper", batchMapper);
		setField(service, "smtIscCardImportDetailMapper", detailMapper);
		setField(service, "smtStaffMapper", staffMapper);
		setField(service, "smtIscParkConfigService", parkConfigService);
		setField(service, "smtIscStaffCardService", staffCardService);
		setField(service, "remoteDispatcherService", dispatcherService);
		return service;
	}

	private SmtIscCardImportServiceImpl service(SmtIscCardImportBatchMapper batchMapper,
													SmtIscCardImportDetailMapper detailMapper,
													SmtIscStaffCardService staffCardService,
													SmtStaff activeStaff,
													SmtStaff resignedStaff) throws Exception {
		SmtIscCardImportServiceImpl service = new SmtIscCardImportServiceImpl();
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtIscParkConfigService parkConfigService = Mockito.mock(SmtIscParkConfigService.class);
		Mockito.when(staffMapper.listIscCardImportStaff(Mockito.eq(5000021), Mockito.isNull(), Mockito.anyString()))
				.thenReturn(Arrays.asList(activeStaff, resignedStaff));
		Mockito.when(parkConfigService.getConfigByPark(5000021)).thenReturn(parkConfig());
		Mockito.when(parkConfigService.getOne(Mockito.any(Wrapper.class), Mockito.eq(false))).thenReturn(parkConfig());
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenAnswer(invocation -> {
					DispatcherDTO<Map> dto = invocation.getArgument(0);
					Assert.assertEquals(Integer.valueOf(6000001), dto.getParkId());
					if (EventEnum.ISC_PERSON_GET.getCode().equals(dto.getEventType())) {
						return Result.success("{\"list\":[{\"jobNo\":\"JA26086\",\"personId\":\"isc-person-active\"},"
								+ "{\"jobNo\":\"JA26087\",\"personId\":\"isc-person-resigned\"}]}");
					}
					Assert.assertEquals(EventEnum.ISC_CARD_LIST_GET.getCode(), dto.getEventType());
					Assert.assertTrue(dto.getData().containsKey("personIds"));
					String personId = String.valueOf(dto.getData().get("personIds"));
					if ("isc-person-active".equals(personId)) {
						return Result.success("{\"list\":[{\"personId\":\"isc-person-active\","
								+ "\"cardNo\":\"12345678\"}],\"total\":1}");
					}
					return Result.success("{\"list\":[],\"total\":0}");
				});
		setField(service, "baseMapper", batchMapper);
		setField(service, "smtIscCardImportDetailMapper", detailMapper);
		setField(service, "smtStaffMapper", staffMapper);
		setField(service, "smtIscParkConfigService", parkConfigService);
		setField(service, "smtIscStaffCardService", staffCardService);
		setField(service, "remoteDispatcherService", dispatcherService);
		return service;
	}

	private SmtIscCardImportServiceImpl serviceWithPersonQueryFailure(SmtIscCardImportBatchMapper batchMapper,
																	  SmtIscCardImportDetailMapper detailMapper,
																	  SmtIscStaffCardService staffCardService,
																	  String message) throws Exception {
		SmtIscCardImportServiceImpl service = new SmtIscCardImportServiceImpl();
		RemoteDispatcherService dispatcherService = Mockito.mock(RemoteDispatcherService.class);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtIscParkConfigService parkConfigService = Mockito.mock(SmtIscParkConfigService.class);
		Mockito.when(staffMapper.listIscCardImportStaff(Mockito.eq(5000021), Mockito.isNull(), Mockito.anyString()))
				.thenReturn(Collections.singletonList(staff()));
		Mockito.when(parkConfigService.getConfigByPark(5000021)).thenReturn(parkConfig());
		Mockito.when(parkConfigService.getOne(Mockito.any(Wrapper.class), Mockito.eq(false))).thenReturn(parkConfig());
		Mockito.when(dispatcherService.dispatch(Mockito.any(), Mockito.eq(SecurityConstants.FROM_IN), Mockito.eq(SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)))
				.thenReturn(Result.fail(message));
		setField(service, "baseMapper", batchMapper);
		setField(service, "smtIscCardImportDetailMapper", detailMapper);
		setField(service, "smtStaffMapper", staffMapper);
		setField(service, "smtIscParkConfigService", parkConfigService);
		setField(service, "smtIscStaffCardService", staffCardService);
		setField(service, "remoteDispatcherService", dispatcherService);
		return service;
	}

	private SmtIscCardImportBatch batch(Long id, String mode) {
		SmtIscCardImportBatch batch = new SmtIscCardImportBatch();
		batch.setId(id);
		batch.setMode(mode);
		batch.setParkId(5000021);
		batch.setDispatcherParkId(6000001);
		batch.setOptUser("admin");
		return batch;
	}

	private SmtStaff staff() {
		SmtStaff staff = new SmtStaff();
		staff.setId(1001L);
		staff.setBadge("JA26086");
		staff.setName("张三");
		staff.setStatus(1);
		return staff;
	}

	private SmtIscParkConfig parkConfig() {
		SmtIscParkConfig config = new SmtIscParkConfig();
		config.setParkId(5000021);
		config.setParkName("许昌园区");
		config.setDispatcherParkId(6000001);
		config.setDispatcherParkName("许昌ISC");
		config.setCardSyncEnabled(1);
		config.setDelFlag(DeleteStatusEnum.NOT_DELETE.getCode());
		return config;
	}

	private SmtIscStaffCard activeCard(Long id, Long staffId, String badge, String cardNo) {
		SmtIscStaffCard card = new SmtIscStaffCard();
		card.setId(id);
		card.setStaffId(staffId);
		card.setBadge(badge);
		card.setDispatcherParkId(6000001);
		card.setCardNo(cardNo);
		card.setDelFlag(DeleteStatusEnum.NOT_DELETE.getCode());
		return card;
	}

	private void setField(Object target, String name, Object value) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				field.set(target, value);
				return;
			} catch (NoSuchFieldException e) {
				type = type.getSuperclass();
			}
		}
		throw new IllegalStateException("field not found: " + name);
	}

	private Object getField(Object target, String name) throws Exception {
		Class<?> type = target.getClass();
		while (type != null) {
			try {
				Field field = type.getDeclaredField(name);
				field.setAccessible(true);
				return field.get(target);
			} catch (NoSuchFieldException e) {
				type = type.getSuperclass();
			}
		}
		throw new IllegalStateException("field not found: " + name);
	}
}
