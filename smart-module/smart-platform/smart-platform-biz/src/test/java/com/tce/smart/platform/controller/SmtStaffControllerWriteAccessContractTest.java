package com.tce.smart.platform.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.common.security.service.SmartUser;
import com.tce.smart.platform.api.dto.req.AdminStaffPhoneUpdateReqDTO;
import com.tce.smart.platform.api.dto.req.AdminStaffUpdateReqDTO;
import com.tce.smart.platform.api.dto.req.AdminTemporaryStaffQueryReqDTO;
import com.tce.smart.platform.api.dto.resp.AdminTemporaryStaffDetailRespDTO;
import com.tce.smart.platform.service.SmtAppStaffAuthService;
import com.tce.smart.platform.service.SmtDormitoryStaffService;
import com.tce.smart.platform.service.SmtStaffExtService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.platform.service.SmtVehicleService;
import com.tce.smart.platform.service.SmtParkBuService;
import com.tce.smart.platform.service.SmtOrganizeRelationService;
import com.tce.smart.platform.service.impl.SmtStaffServiceImpl;
import com.tce.smart.platform.core.service.SmtImageService;
import com.tce.smart.platform.core.entity.SmtParkBu;
import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.common.core.exception.TCEException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * 员工写入与临时人员查询的访问契约。
 *
 * 这些测试刻意不依赖网关默认鉴权，确保 Controller 的最小请求、管理员园区范围
 * 与不返回 PII 的响应契约不会被后续重构绕过。
 */
public class SmtStaffControllerWriteAccessContractTest {

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
	public void temporaryStaffEndpointsUseProtectedMinimumContracts() throws Exception {
		Method page = SmtStaffController.class.getMethod("temporaryStaffPage", Page.class, AdminTemporaryStaffQueryReqDTO.class);
		Method detail = SmtStaffController.class.getMethod("temporaryStaffDetail", Long.class);
		assertEquals("/admin/temporary/page", page.getAnnotation(PostMapping.class).value()[0]);
		assertEquals("@pms.hasPermission('platform_staff_lookup')", page.getAnnotation(PreAuthorize.class).value());
		assertEquals("@pms.hasPermission('platform_staff_lookup')", detail.getAnnotation(PreAuthorize.class).value());
		assertNotNull(detail);

		Set<String> fields = Arrays.stream(AdminTemporaryStaffDetailRespDTO.class.getDeclaredFields())
				.map(Field::getName).collect(Collectors.toSet());
		assertFalse(fields.contains("certno"));
		assertFalse(fields.contains("phone"));
		assertFalse(fields.contains("faceImg"));
		assertFalse(fields.contains("faceImgUrl"));
		assertNoSensitiveRequestFields(AdminTemporaryStaffQueryReqDTO.class);
	}

	@Test
	public void temporaryStaffPageUsesAuthenticatedAdministratorsParkScope() {
		SmtStaffService service = Mockito.mock(SmtStaffService.class);
		authenticateAsAdmin();

		staffController(service).temporaryStaffPage(new Page(), new AdminTemporaryStaffQueryReqDTO());

		Mockito.verify(service).getTemporaryStaffPageForAdmin(Mockito.any(Page.class),
				Mockito.any(AdminTemporaryStaffQueryReqDTO.class), Mockito.eq(Arrays.asList(10, 20)));
	}

	@Test
	public void adminMutationsUseMinimumRequestsAndAuthenticatedParkScope() throws Exception {
		assertMethodDoesNotExist("updateById");
		assertMethodDoesNotExist("outDormitory");

		SmtStaffService service = Mockito.mock(SmtStaffService.class);
		authenticateAsAdmin();
		AdminStaffPhoneUpdateReqDTO phoneRequest = new AdminStaffPhoneUpdateReqDTO();
		phoneRequest.setStaffId(101L);
		phoneRequest.setNewPhone("13800000000");
		AdminStaffUpdateReqDTO updateRequest = new AdminStaffUpdateReqDTO();
		updateRequest.setStaffId(101L);
		updateRequest.setName("更新后的姓名");

		staffController(service).updateStaffPhone(phoneRequest);
		staffController(service).updateStaff(updateRequest);

		Mockito.verify(service).updateStaffPhoneForAdmin(phoneRequest, Arrays.asList(10, 20));
		Mockito.verify(service).updateStaffForAdmin(updateRequest, Arrays.asList(10, 20));
		assertNoSensitiveRequestFields(AdminStaffUpdateReqDTO.class);
		assertNoSensitiveRequestFields(AdminStaffPhoneUpdateReqDTO.class);
	}

	@Test
	public void adminUpdateRejectsStaffOutsideAuthenticatedParkScope() {
		SmtParkBuService parkBuService = Mockito.mock(SmtParkBuService.class);
		SmtOrganizeRelationService relationService = Mockito.mock(SmtOrganizeRelationService.class);
		SmtStaffMapper mapper = Mockito.mock(SmtStaffMapper.class);
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		ReflectionTestUtils.setField(service, "smtParkBuService", parkBuService);
		ReflectionTestUtils.setField(service, "smtOrganizeRelationService", relationService);
		ReflectionTestUtils.setField(service, "baseMapper", mapper);
		SmtParkBu authorizedBu = new SmtParkBu();
		authorizedBu.setCompId("AUTHORIZED-COMP");
		SmtStaff outsideStaff = new SmtStaff();
		outsideStaff.setId(101L);
		outsideStaff.setCompId("OUTSIDE-COMP");
		Mockito.when(parkBuService.list(Mockito.any())).thenReturn(Collections.singletonList(authorizedBu));
		Mockito.when(relationService.list(Mockito.any())).thenReturn(Collections.emptyList());
		Mockito.when(mapper.selectById(101L)).thenReturn(outsideStaff);
		AdminStaffUpdateReqDTO request = new AdminStaffUpdateReqDTO();
		request.setStaffId(101L);
		request.setName("越权修改");

		try {
			service.updateStaffForAdmin(request, Collections.singletonList(10));
			fail("跨园区管理员不得修改员工资料");
		} catch (TCEException expected) {
			Mockito.verify(mapper, Mockito.never()).updateById(Mockito.any(SmtStaff.class));
		}
	}

	private void authenticateAsAdmin() {
		SmartUser user = new SmartUser(1, 1, "admin", Arrays.asList(10, 20), "N/A",
				true, true, true, true, Collections.emptyList());
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(user, "N/A", Collections.emptyList()));
	}

	private SmtStaffController staffController(SmtStaffService staffService) {
		return new SmtStaffController(staffService,
				Mockito.mock(SmtVehicleService.class),
				Mockito.mock(SmtAppStaffAuthService.class),
				Mockito.mock(SmtImageService.class),
				Mockito.mock(SmtDormitoryStaffService.class),
				Mockito.mock(SmtStaffExtService.class));
	}

	private void assertMethodDoesNotExist(String methodName) {
		for (Method method : SmtStaffController.class.getMethods()) {
			if (methodName.equals(method.getName())) {
				fail("不应保留可接收原始员工实体的公开处理器：" + methodName);
			}
		}
	}

	private void assertNoSensitiveRequestFields(Class<?> requestType) {
		Set<String> fields = Arrays.stream(requestType.getDeclaredFields())
				.map(Field::getName).collect(Collectors.toSet());
		assertFalse(fields.contains("certno"));
		assertFalse(fields.contains("phone"));
		assertFalse(fields.contains("facePicId"));
		assertFalse(fields.contains("faceImg"));
	}
}
