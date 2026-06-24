package com.tce.smart.schedule.service.platform.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.api.dto.SmtStaffDTO;
import com.tce.smart.platform.api.feign.RemoteStaffService;
import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtOrganizeRelationMapper;
import com.tce.smart.platform.core.mapper.SmtParkBuMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.tool.enums.DeviceTaskActionEnum;
import com.tce.smart.tool.enums.SmtImageEnum;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SmtStaffTaskServiceImplTest {

	@BeforeClass
	public static void initMybatisPlusLambdaCache() {
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtStaff.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtParkBu.class);
		TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SmtOrganizeRelation.class);
	}

	@Test
	public void syncXCStaffPhotoUsesRecentFiveDayWindowInsteadOfEpochFallback() throws Exception {
		LocalDateTime fixedNow = LocalDateTime.of(2026, 6, 1, 16, 30, 0);
		TestableSmtStaffTaskServiceImpl service = new TestableSmtStaffTaskServiceImpl(fixedNow,
				Mockito.mock(SmtImageService.class), Mockito.mock(RemoteStaffService.class));
		setField(service, "syncStaffPhotoUrl", "http://photo-service");
		setField(service, "syncStaffPhotoLookbackDays", 5);

		service.syncXCStaffPhoto();

		Assert.assertEquals(1, service.requestedUrls.size());
		String url = service.requestedUrls.get(0);
		long expectedStartTime = fixedNow.minusDays(5).toInstant(ZoneOffset.of("+8")).toEpochMilli();
		long expectedEndTime = fixedNow.toInstant(ZoneOffset.of("+8")).toEpochMilli();
		Assert.assertTrue(url, url.contains("startTime=" + expectedStartTime));
		Assert.assertTrue(url, url.contains("endTime=" + expectedEndTime));
		Assert.assertTrue(url, url.contains("pageNo=1"));
		Assert.assertTrue(url, url.contains("pageSize=50"));
		Assert.assertFalse(url, url.contains("startTime=-"));
	}

	@Test
	public void syncXCStaffPhotoFallsBackToFiveDayWindowWhenConfiguredDaysInvalid() throws Exception {
		LocalDateTime fixedNow = LocalDateTime.of(2026, 6, 1, 16, 30, 0);
		TestableSmtStaffTaskServiceImpl service = new TestableSmtStaffTaskServiceImpl(fixedNow,
				Mockito.mock(SmtImageService.class), Mockito.mock(RemoteStaffService.class));
		setField(service, "syncStaffPhotoUrl", "http://photo-service");
		setField(service, "syncStaffPhotoLookbackDays", 0);

		service.syncXCStaffPhoto();

		String url = service.requestedUrls.get(0);
		long expectedStartTime = fixedNow.minusDays(5).toInstant(ZoneOffset.of("+8")).toEpochMilli();
		Assert.assertTrue(url, url.contains("startTime=" + expectedStartTime));
	}

	@Test
	public void staffPhotoCompensationWindowStartsThreeDaysBeforeCurrentTime() {
		LocalDateTime now = LocalDateTime.of(2026, 6, 24, 17, 46, 57);

		LocalDateTime startTime = SmtStaffTaskServiceImpl.getStaffPhotoCompensationStartTime(now);

		Assert.assertEquals(LocalDateTime.of(2026, 6, 21, 17, 46, 57), startTime);
	}

	@Test
	public void staffPhotoCompensationWindowIncludesCurrentDayStaff() {
		LocalDateTime now = LocalDateTime.of(2026, 6, 24, 17, 46, 57);
		LocalDateTime todayStaffCreateTime = LocalDateTime.of(2026, 6, 24, 0, 0, 0);
		LocalDateTime startTime = SmtStaffTaskServiceImpl.getStaffPhotoCompensationStartTime(now);

		Assert.assertFalse(todayStaffCreateTime.isBefore(startTime));
		Assert.assertFalse(todayStaffCreateTime.isAfter(now));
	}

	@Test
	public void staffPhotoCompensationWindowExcludesStaffBeforeThreeDays() {
		LocalDateTime now = LocalDateTime.of(2026, 6, 24, 17, 46, 57);
		LocalDateTime oldStaffCreateTime = LocalDateTime.of(2026, 6, 20, 23, 59, 59);
		LocalDateTime startTime = SmtStaffTaskServiceImpl.getStaffPhotoCompensationStartTime(now);

		Assert.assertTrue(oldStaffCreateTime.isBefore(startTime));
	}

	@Test
	public void syncStaffNoPhotoQueriesXcStaffCreatedWithinThreeDays() throws Exception {
		LocalDateTime fixedNow = LocalDateTime.of(2026, 6, 24, 17, 46, 57);
		SmtParkBuMapper parkBuMapper = Mockito.mock(SmtParkBuMapper.class);
		SmtOrganizeRelationMapper relationMapper = Mockito.mock(SmtOrganizeRelationMapper.class);
		Mockito.when(parkBuMapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(parkBu("BU1")));
		Mockito.when(relationMapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(relation(123L)));
		TestableSmtStaffTaskServiceImpl service = new TestableSmtStaffTaskServiceImpl(fixedNow,
				Mockito.mock(SmtImageService.class), Mockito.mock(RemoteStaffService.class), parkBuMapper, relationMapper);
		setField(service, "xcParkId", 5000021);

		service.syncStaffNoPhoto(1);

		Assert.assertEquals(1, service.staffListSqlSegments.size());
		assertNoPhotoStaffWindowSql(service.staffListSqlSegments.get(0), service.staffListParamValues.get(0),
				fixedNow.minusDays(3), fixedNow);
		Assert.assertTrue(service.staffListSqlSegments.get(0).toLowerCase().contains("comp_id in"));
	}

	@Test
	public void syncStaffNoPhotoQueriesNonXcStaffCreatedWithinThreeDays() throws Exception {
		LocalDateTime fixedNow = LocalDateTime.of(2026, 6, 24, 17, 46, 57);
		SmtParkBuMapper parkBuMapper = Mockito.mock(SmtParkBuMapper.class);
		SmtOrganizeRelationMapper relationMapper = Mockito.mock(SmtOrganizeRelationMapper.class);
		Mockito.when(parkBuMapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(parkBu("BU1")));
		Mockito.when(relationMapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(relation(123L)));
		TestableSmtStaffTaskServiceImpl service = new TestableSmtStaffTaskServiceImpl(fixedNow,
				Mockito.mock(SmtImageService.class), Mockito.mock(RemoteStaffService.class), parkBuMapper, relationMapper);
		setField(service, "xcParkId", 5000021);

		service.syncStaffNoPhoto(2);

		Assert.assertEquals(1, service.staffListSqlSegments.size());
		assertNoPhotoStaffWindowSql(service.staffListSqlSegments.get(0), service.staffListParamValues.get(0),
				fixedNow.minusDays(3), fixedNow);
		Assert.assertTrue(service.staffListSqlSegments.get(0).toLowerCase().contains("comp_id not in"));
	}

	@Test
	public void downFaceToDeviceSkipsSaveAndDeviceTaskWhenFaceImageUnchanged() {
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		TestableSmtStaffTaskServiceImpl service = new TestableSmtStaffTaskServiceImpl(
				LocalDateTime.of(2026, 6, 1, 16, 30, 0), imageService, remoteStaffService);
		SmtStaff staff = staffWithFacePic("old-face");
		Mockito.when(imageService.getImageBase64ByCode("old-face")).thenReturn("same-face-data");

		service.downFaceToDevice(staff, "same-face-data");

		Mockito.verify(imageService).getImageBase64ByCode("old-face");
		Mockito.verify(imageService, Mockito.never()).saveImage(Mockito.anyInt(), Mockito.anyString(), Mockito.anyInt());
		Mockito.verify(remoteStaffService, Mockito.never()).syncIscPersonFace(
				Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
		Mockito.verify(remoteStaffService, Mockito.never()).addDeviceTask(Mockito.any(SmtStaffDTO.class), Mockito.anyInt());
		Assert.assertNull(service.updatedStaff);
	}

	@Test
	public void downFaceToDeviceUpdatesSavedImageAndDeviceTaskWhenFaceImageChanged() throws Exception {
		SmtImageService imageService = Mockito.mock(SmtImageService.class);
		RemoteStaffService remoteStaffService = Mockito.mock(RemoteStaffService.class);
		TestableSmtStaffTaskServiceImpl service = new TestableSmtStaffTaskServiceImpl(
				LocalDateTime.of(2026, 6, 1, 16, 30, 0), imageService, remoteStaffService);
		setField(service, "xcParkId", 5000021);
		SmtStaff staff = staffWithFacePic("old-face");
		Mockito.when(imageService.getImageBase64ByCode("old-face")).thenReturn("old-face-data");
		Mockito.when(imageService.saveImage(0, "new-face-data", SmtImageEnum.TYPE_STAFF_FACE.getCode()))
				.thenReturn("new-face");

		service.downFaceToDevice(staff, "new-face-data");

		Assert.assertNotNull(service.updatedStaff);
		Assert.assertEquals(Long.valueOf(2061335354386161665L), service.updatedStaff.getId());
		Assert.assertEquals("new-face", service.updatedStaff.getFacePicId());
		Mockito.verify(imageService).saveImage(0, "new-face-data", SmtImageEnum.TYPE_STAFF_FACE.getCode());
		Mockito.verify(remoteStaffService, Mockito.never()).syncIscPersonFace(
				Mockito.anyString(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString());
		ArgumentCaptor<SmtStaffDTO> staffCaptor = ArgumentCaptor.forClass(SmtStaffDTO.class);
		Mockito.verify(remoteStaffService).addDeviceTask(staffCaptor.capture(),
				Mockito.eq(DeviceTaskActionEnum.UPDATE.getCode()));
		Assert.assertEquals("JA26086", staffCaptor.getValue().getBadge());
		Assert.assertEquals("张珂", staffCaptor.getValue().getName());
		Assert.assertEquals("1458624294763679746", staffCaptor.getValue().getCompId());
		Assert.assertEquals("new-face", staffCaptor.getValue().getFacePicId());
	}

	private static void setField(Object target, String name, Object value) throws Exception {
		Field field = target.getClass().getSuperclass().getDeclaredField(name);
		field.setAccessible(true);
		field.set(target, value);
	}

	private static void assertNoPhotoStaffWindowSql(String sqlSegment, Map<String, Object> paramValues,
													LocalDateTime expectedStartTime, LocalDateTime expectedEndTime) {
		String normalizedSql = sqlSegment.toLowerCase();
		Assert.assertTrue(sqlSegment, normalizedSql.contains("face_pic_id is null"));
		Assert.assertTrue(sqlSegment, normalizedSql.contains("create_time between"));
		Assert.assertFalse(sqlSegment, normalizedSql.contains("create_time <"));
		Assert.assertTrue(paramValues.toString(), paramValues.containsValue(expectedStartTime));
		Assert.assertTrue(paramValues.toString(), paramValues.containsValue(expectedEndTime));
	}

	private static SmtParkBu parkBu(String compId) {
		SmtParkBu parkBu = new SmtParkBu();
		parkBu.setCompId(compId);
		return parkBu;
	}

	private static SmtOrganizeRelation relation(Long id) {
		SmtOrganizeRelation relation = new SmtOrganizeRelation();
		relation.setId(id);
		return relation;
	}

	private static class TestableSmtStaffTaskServiceImpl extends SmtStaffTaskServiceImpl {
		private final LocalDateTime fixedNow;
		private final List<String> requestedUrls = new ArrayList<>();
		private final List<String> staffListSqlSegments = new ArrayList<>();
		private final List<Map<String, Object>> staffListParamValues = new ArrayList<>();
		private SmtStaff updatedStaff;

		TestableSmtStaffTaskServiceImpl(LocalDateTime fixedNow,
										SmtImageService imageService,
										RemoteStaffService remoteStaffService) {
			this(fixedNow,
					imageService,
					remoteStaffService,
					Mockito.mock(SmtParkBuMapper.class),
					Mockito.mock(SmtOrganizeRelationMapper.class));
		}

		TestableSmtStaffTaskServiceImpl(LocalDateTime fixedNow,
										SmtImageService imageService,
										RemoteStaffService remoteStaffService,
										SmtParkBuMapper parkBuMapper,
										SmtOrganizeRelationMapper relationMapper) {
			super(imageService,
					remoteStaffService,
					parkBuMapper,
					relationMapper);
			this.fixedNow = fixedNow;
		}

		@Override
		protected LocalDateTime currentTime() {
			return fixedNow;
		}

		@Override
		protected String postStaffPhotoUrl(String url) {
			requestedUrls.add(url);
			return "{\"ret\":200,\"data\":{\"persons\":[],\"total\":0},\"msg\":\"\"}";
		}

		@Override
		public boolean updateById(SmtStaff entity) {
			this.updatedStaff = entity;
			return true;
		}

		@Override
		public List<SmtStaff> list(Wrapper<SmtStaff> queryWrapper) {
			staffListSqlSegments.add(queryWrapper.getExpression().getSqlSegment());
			staffListParamValues.add(((AbstractWrapper<SmtStaff, ?, ?>) queryWrapper).getParamNameValuePairs());
			return new ArrayList<>();
		}
	}

	private static SmtStaff staffWithFacePic(String facePicId) {
		SmtStaff staff = new SmtStaff();
		staff.setId(2061335354386161665L);
		staff.setBadge("JA26086");
		staff.setName("张珂");
		staff.setCompId("1458624294763679746");
		staff.setFacePicId(facePicId);
		return staff;
	}
}
