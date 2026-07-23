package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.resp.AdminStaffDetailRespDTO;
import com.tce.smart.platform.api.dto.resp.AdminTemporaryStaffRespDTO;
import com.tce.smart.platform.api.dto.resp.StaffLookupRespDTO;
import com.tce.smart.platform.api.dto.resp.StaffSelfCheckInProfileRespDTO;
import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.service.SmtAppStaffAuthService;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtOrganizeRelationService;
import com.tce.smart.platform.service.SmtStaffExtService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.SmtVehicleService;
import com.tce.smart.platform.service.impl.SmtStaffServiceImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 员工查询接口的隐私契约测试。
 *
 * 外部查询响应只能保留业务识别所需的最小字段，且历史上直接返回员工实体的
 * 三个工号查询入口不得以兼容别名继续存在。
 */
public class SmtStaffControllerPrivacyContractTest {

	@Before
	public void initializeMybatisLambdaMetadata() {
		MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
		TableInfoHelper.initTableInfo(assistant, SmtStaff.class);
		TableInfoHelper.initTableInfo(assistant, SmtParkBu.class);
		TableInfoHelper.initTableInfo(assistant, SmtOrganizeRelation.class);
	}

	@After
	public void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	public void staffLookupDtoDoesNotExposeSensitiveProperties() {
		Set<String> names = Arrays.stream(StaffLookupRespDTO.class.getDeclaredFields())
				.map(Field::getName)
				.collect(Collectors.toSet());

		assertEquals(new HashSet<>(Arrays.asList("staffId", "badge", "name", "departmentName")), names);
	}

	@Test
	public void adminStaffDetailDtoDoesNotExposePersonalSensitiveProperties() {
		Set<String> names = Arrays.stream(AdminStaffDetailRespDTO.class.getDeclaredFields())
				.map(Field::getName)
				.collect(Collectors.toSet());

		assertEquals(new HashSet<>(Arrays.asList("staffId", "badge", "name", "sex", "companyName",
				"departmentName", "jobName", "status")), names);
		assertFalse(names.contains("certno"));
		assertFalse(names.contains("phone"));
		assertFalse(names.contains("homeAddress"));
		assertFalse(names.contains("facePicId"));
	}

	@Test
	public void temporaryStaffBatchDtoDoesNotExposePersonalSensitiveProperties() {
		Set<String> names = Arrays.stream(AdminTemporaryStaffRespDTO.class.getDeclaredFields())
				.map(Field::getName)
				.collect(Collectors.toSet());

		assertEquals(new HashSet<>(Arrays.asList("staffId", "badge", "name")), names);
	}

	@Test
	public void adminStaffDetailRejectsAnonymousRequest() {
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);

		try {
			staffController(staffService).adminStaffDetail(101L);
			fail("匿名请求必须被拒绝，不能读取员工详情");
		} catch (AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(staffService);
		}
	}

	@Test
	public void adminStaffDetailUsesOnlyAuthenticatedAdministratorsParkScope() {
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmartUser user = new SmartUser(1, 1, "admin", Arrays.asList(10, 20), "N/A",
				true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "N/A", Collections.emptyList()));
		AdminStaffDetailRespDTO detail = new AdminStaffDetailRespDTO();
		detail.setStaffId(101L);
		Mockito.when(staffService.getAdminStaffDetail(101L, Arrays.asList(10, 20))).thenReturn(detail);

		assertEquals(detail, staffController(staffService).adminStaffDetail(101L).getData());
		Mockito.verify(staffService).getAdminStaffDetail(101L, Arrays.asList(10, 20));
	}

	@Test
	public void legacyBadgeHandlersAreNotPublicApiHandlers() {
		assertMethodDoesNotExist("getById");
		assertMethodDoesNotExist("getByBadge");
		assertMethodDoesNotExist("getOneByBadge");
		assertMethodDoesNotExist("getSimpleSttaffByBadge");
		assertMethodDoesNotExist("getSimpleSttaffById");
		assertMethodDoesNotExist("queryMobile");
		assertMethodDoesNotExist("faceSearchForLogin");
		assertMethodDoesNotExist("getFullByBadge");
		assertMethodDoesNotExist("getBaseinfoByBadge");
		assertMethodDoesNotExist("updatePhone");
	}

	@Test
	public void temporaryStaffLookupUsesAuthorizedParkCompId() {
		SmtParkBuService parkBuService = Mockito.mock(SmtParkBuService.class);
		SmtOrganizeRelationService organizeRelationService = Mockito.mock(SmtOrganizeRelationService.class);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		ReflectionTestUtils.setField(service, "smtParkBuService", parkBuService);
		ReflectionTestUtils.setField(service, "smtOrganizeRelationService", organizeRelationService);
		ReflectionTestUtils.setField(service, "baseMapper", staffMapper);

		SmtOrganizeRelation authorizedRelation = new SmtOrganizeRelation();
		authorizedRelation.setId(901L);
		authorizedRelation.setParkId(10);
		authorizedRelation.setCompId("TEMP-COMP-A");
		Mockito.when(parkBuService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(organizeRelationService.list(Mockito.any()))
				.thenReturn(Collections.singletonList(authorizedRelation));
		SmtStaff temporaryStaff = new SmtStaff();
		temporaryStaff.setId(101L);
		temporaryStaff.setBadge("TEMP-001");
		temporaryStaff.setName("临时员工");
		temporaryStaff.setCompId("TEMP-COMP-A");
		Mockito.when(staffMapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(temporaryStaff));

		assertEquals(1, service.searchStaffForAdmin("TEMP", Collections.singletonList(10)).size());

		ArgumentCaptor<Wrapper<SmtStaff>> staffQueryCaptor = ArgumentCaptor.forClass(Wrapper.class);
		Mockito.verify(staffMapper).selectList(staffQueryCaptor.capture());
		Set<Object> staffQueryValues = queryValues(staffQueryCaptor.getValue());
		assertTrue(staffQueryValues.contains("TEMP-COMP-A"));
		assertFalse(staffQueryValues.contains("901"));

		ArgumentCaptor<Wrapper<SmtOrganizeRelation>> relationQueryCaptor = ArgumentCaptor.forClass(Wrapper.class);
		Mockito.verify(organizeRelationService).list(relationQueryCaptor.capture());
		Set<Object> relationQueryValues = queryValues(relationQueryCaptor.getValue());
		assertTrue(relationQueryValues.contains(10));
		assertFalse(relationQueryValues.contains(11));
	}

	@Test
	public void adminStaffDetailRejectsStaffOutsideAdministratorsParkScope() {
		SmtParkBuService parkBuService = Mockito.mock(SmtParkBuService.class);
		SmtOrganizeRelationService organizeRelationService = Mockito.mock(SmtOrganizeRelationService.class);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		ReflectionTestUtils.setField(service, "smtParkBuService", parkBuService);
		ReflectionTestUtils.setField(service, "smtOrganizeRelationService", organizeRelationService);
		ReflectionTestUtils.setField(service, "baseMapper", staffMapper);

		SmtParkBu authorizedParkBu = new SmtParkBu();
		authorizedParkBu.setCompId("AUTHORIZED-COMP");
		SmtStaff outsideStaff = new SmtStaff();
		outsideStaff.setId(101L);
		outsideStaff.setCompId("OUTSIDE-COMP");
		Mockito.when(parkBuService.list(Mockito.any())).thenReturn(Collections.singletonList(authorizedParkBu));
		Mockito.when(organizeRelationService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(staffMapper.selectById(101L)).thenReturn(outsideStaff);

		assertEquals(null, service.getAdminStaffDetail(101L, Collections.singletonList(10)));
	}

	@Test
	public void adminStaffDetailProjectsOnlyDedicatedNonSensitiveFields() {
		SmtParkBuService parkBuService = Mockito.mock(SmtParkBuService.class);
		SmtOrganizeRelationService organizeRelationService = Mockito.mock(SmtOrganizeRelationService.class);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		ReflectionTestUtils.setField(service, "smtParkBuService", parkBuService);
		ReflectionTestUtils.setField(service, "smtOrganizeRelationService", organizeRelationService);
		ReflectionTestUtils.setField(service, "baseMapper", staffMapper);

		SmtParkBu authorizedParkBu = new SmtParkBu();
		authorizedParkBu.setCompId("AUTHORIZED-COMP");
		SmtStaff staff = new SmtStaff();
		staff.setId(101L);
		staff.setCompId("AUTHORIZED-COMP");
		staff.setBadge("EMP-101");
		staff.setName("员工");
		staff.setCertno("sensitive-cert-no");
		staff.setPhone("sensitive-phone");
		staff.setHomeAddress("sensitive-address");
		staff.setFacePicId("sensitive-face");
		Mockito.when(parkBuService.list(Mockito.any())).thenReturn(Collections.singletonList(authorizedParkBu));
		Mockito.when(organizeRelationService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(staffMapper.selectById(101L)).thenReturn(staff);

		AdminStaffDetailRespDTO response = service.getAdminStaffDetail(101L, Collections.singletonList(10));
		assertEquals(Long.valueOf(101L), response.getStaffId());
		assertEquals("EMP-101", response.getBadge());
		assertEquals("员工", response.getName());
	}

	@Test
	public void temporaryStaffBatchLookupUsesAuthenticatedParkCompanyScope() {
		SmtParkBuService parkBuService = Mockito.mock(SmtParkBuService.class);
		SmtOrganizeRelationService organizeRelationService = Mockito.mock(SmtOrganizeRelationService.class);
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		ReflectionTestUtils.setField(service, "smtParkBuService", parkBuService);
		ReflectionTestUtils.setField(service, "smtOrganizeRelationService", organizeRelationService);
		ReflectionTestUtils.setField(service, "baseMapper", staffMapper);

		SmtParkBu authorizedParkBu = new SmtParkBu();
		authorizedParkBu.setCompId("AUTHORIZED-COMP");
		SmtStaff staff = new SmtStaff();
		staff.setId(101L);
		staff.setBadge("TEMP-001");
		staff.setName("临时员工");
		Mockito.when(parkBuService.list(Mockito.any())).thenReturn(Collections.singletonList(authorizedParkBu));
		Mockito.when(organizeRelationService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(staffMapper.selectList(Mockito.any())).thenReturn(Collections.singletonList(staff));

		List<AdminTemporaryStaffRespDTO> response = service.searchTemporaryStaffForAdmin(
				Collections.singletonList(" TEMP-001 "), Collections.singletonList(10));
		assertEquals(1, response.size());
		assertEquals(Long.valueOf(101L), response.get(0).getStaffId());

		ArgumentCaptor<Wrapper<SmtStaff>> queryCaptor = ArgumentCaptor.forClass(Wrapper.class);
		Mockito.verify(staffMapper).selectList(queryCaptor.capture());
		Set<Object> queryValues = queryValues(queryCaptor.getValue());
		assertTrue(queryValues.contains("AUTHORIZED-COMP"));
		assertTrue(queryValues.contains("TEMP-001"));
	}

	@Test
	public void checkInProfileRejectsAnonymousRequest() {
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);

		try {
			staffController(staffService).myCheckInProfile();
			fail("匿名请求必须被拒绝，不能因缺失认证主体触发空指针异常");
		} catch (AccessDeniedException expected) {
			Mockito.verifyZeroInteractions(staffService);
		}
	}

	@Test
	public void checkInProfileUsesOnlyAuthenticatedUsername() {
		SmtStaffService staffService = Mockito.mock(SmtStaffService.class);
		SmartUser user = new SmartUser(1, 1, "self-badge", Collections.singletonList(10), "N/A",
				true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "N/A", Collections.emptyList()));
		StaffSelfCheckInProfileRespDTO profile = new StaffSelfCheckInProfileRespDTO();
		profile.setName("当前员工");
		Mockito.when(staffService.getCheckInProfileForBadge("self-badge")).thenReturn(profile);

		assertEquals(profile, staffController(staffService).myCheckInProfile().getData());
		Mockito.verify(staffService).getCheckInProfileForBadge("self-badge");
	}

	private SmtStaffController staffController(SmtStaffService staffService) {
		return new SmtStaffController(staffService,
				Mockito.mock(SmtVehicleService.class),
				Mockito.mock(SmtAppStaffAuthService.class),
				Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtDormitoryStaffService.class),
				Mockito.mock(SmtStaffExtService.class));
	}

	private Set<Object> queryValues(Wrapper<?> wrapper) {
		wrapper.getSqlSegment();
		return new HashSet<>(((AbstractWrapper) wrapper).getParamNameValuePairs().values());
	}

	private void assertMethodDoesNotExist(String methodName) {
		for (java.lang.reflect.Method method : SmtStaffController.class.getMethods()) {
			if (methodName.equals(method.getName())) {
				fail("历史员工实体查询入口不应继续暴露：" + methodName);
			}
		}
	}
}
