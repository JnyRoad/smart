package com.tce.smart.platform.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.tce.smart.platform.service.impl.SmtStaffServiceImpl;
import com.tce.smart.platform.controller.SmtStaffController;
import com.tce.smart.common.core.model.Result;
import com.tce.smart.platform.api.dto.resp.VehicleAuthDetailRespDTO;
import com.tce.smart.platform.api.feign.RemoteVehicleService;
import com.tce.smart.platform.core.dto.ApplyAuthDTO;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.core.entity.SmtVehicle;
import com.tce.smart.platform.core.entity.SmtVehicleApply;
import com.tce.smart.platform.core.entity.SmtVehicleStaff;
import com.tce.smart.platform.core.entity.SmtPark;
import com.tce.smart.platform.core.mapper.SmtStaffMapper;
import com.tce.smart.platform.service.SmtVehicleService;
import com.tce.smart.platform.service.SmtVehicleStaffService;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

/**
 * App 车辆代理的对象级授权契约。
 *
 * 服务间 OAuth 认证只能确认调用服务，不能替代车牌和申请编号与当前员工的归属校验。
 */
public class VehicleOwnershipContractTest {
	@Before
	public void initializeMybatisLambdaMetadata() {
		MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
		TableInfoHelper.initTableInfo(assistant, SmtStaff.class);
		TableInfoHelper.initTableInfo(assistant, SmtVehicle.class);
		TableInfoHelper.initTableInfo(assistant, SmtVehicleApply.class);
		TableInfoHelper.initTableInfo(assistant, SmtVehicleStaff.class);
	}

	@Test
	public void staffVehicleOperationsRequireTheAuthenticatedBadgeForEachObjectLookup() throws Exception {
		Method parkList = SmtStaffServiceImpl.class.getMethod("getVehicleParkForOwner", String.class, String.class);
		Method detail = SmtStaffServiceImpl.class.getMethod("getVehicleParkByIdForOwner", Integer.class, String.class);
		Method delete = SmtStaffServiceImpl.class.getMethod("deleteVehicleForOwner", String.class, String.class);
		assertNotNull(parkList);
		assertNotNull(detail);
		assertNotNull(delete);
	}

	@Test
	public void controlledVehicleCertificateDetailUsesMinimumContract() throws Exception {
		Set<String> names = Arrays.stream(VehicleAuthDetailRespDTO.class.getDeclaredFields())
				.map(field -> field.getName())
				.collect(Collectors.toSet());
		assertEquals(new HashSet<>(Arrays.asList("vehiclePlate", "vehicleBrand", "vehicleColor", "vehicleType",
				"driverLicenseBase64", "drivingLicenseBase64", "reason")), names);

		Method remoteDetail = RemoteVehicleService.class.getMethod("getVehicleParkById", Integer.class,
				String.class, String.class, String.class, String.class);
		assertEquals("com.tce.smart.common.core.model.Result<com.tce.smart.platform.api.dto.resp.VehicleAuthDetailRespDTO>",
				remoteDetail.getGenericReturnType().getTypeName());

		Method controllerDetail = SmtStaffController.class.getMethod("getVehicleParkById", Integer.class,
				String.class, String.class, String.class);
		assertEquals("com.tce.smart.common.core.model.Result<com.tce.smart.platform.api.dto.resp.VehicleAuthDetailRespDTO>",
				controllerDetail.getGenericReturnType().getTypeName());
	}

	@Test
	public void employeeCannotReadAnotherEmployeesVehicleApplicationByPlate() {
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtVehicleService vehicleService = Mockito.mock(SmtVehicleService.class);
		SmtVehicleStaffService vehicleStaffService = Mockito.mock(SmtVehicleStaffService.class);
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		SmtStaff employee = new SmtStaff();
		employee.setId(101L);
		SmtVehicle otherEmployeesVehicle = new SmtVehicle();
		otherEmployeesVehicle.setId(201L);
		otherEmployeesVehicle.setVehiclePlate("豫A12345");
		otherEmployeesVehicle.setIsDelete(0);
		Mockito.when(staffMapper.selectOne(Mockito.any())).thenReturn(employee);
		Mockito.when(vehicleService.list(Mockito.any())).thenReturn(Collections.singletonList(otherEmployeesVehicle));
		Mockito.when(vehicleStaffService.count(Mockito.any())).thenReturn(0);
		ReflectionTestUtils.setField(service, "baseMapper", staffMapper);
		ReflectionTestUtils.setField(service, "smtVehicleService", vehicleService);
		ReflectionTestUtils.setField(service, "vsService", vehicleStaffService);

		try {
			service.getVehicleParkForOwner("豫A12345", "EMP-101");
			fail("跨员工车牌不能查询入园申请");
		} catch (AccessDeniedException expected) {
			Mockito.verify(vehicleService, Mockito.never()).getById(Mockito.anyLong());
		}
	}

	@Test
	public void employeeCannotReadAnotherEmployeesVehicleCertificateByApplicationId() {
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtVehicleService vehicleService = Mockito.mock(SmtVehicleService.class);
		SmtVehicleStaffService vehicleStaffService = Mockito.mock(SmtVehicleStaffService.class);
		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		SmtStaffServiceImpl service = new SmtStaffServiceImpl();
		SmtStaff employee = new SmtStaff();
		employee.setId(101L);
		SmtVehicleApply application = new SmtVehicleApply();
		application.setId(301);
		application.setVehicleId(201L);
		application.setParkId("10");
		SmtVehicle otherEmployeesVehicle = new SmtVehicle();
		otherEmployeesVehicle.setId(201L);
		otherEmployeesVehicle.setIsDelete(0);
		Mockito.when(staffMapper.selectOne(Mockito.any())).thenReturn(employee);
		Mockito.when(vehicleApplyService.getById(301)).thenReturn(application);
		Mockito.when(vehicleService.getById(201L)).thenReturn(otherEmployeesVehicle);
		Mockito.when(vehicleStaffService.count(Mockito.any())).thenReturn(0);
		ReflectionTestUtils.setField(service, "baseMapper", staffMapper);
		ReflectionTestUtils.setField(service, "smtVehicleService", vehicleService);
		ReflectionTestUtils.setField(service, "smtVehicleApplyService", vehicleApplyService);
		ReflectionTestUtils.setField(service, "vsService", vehicleStaffService);

		try {
			service.getVehicleParkByIdForOwner(301, "EMP-101");
			fail("跨员工申请编号不能读取证照详情");
		} catch (AccessDeniedException expected) {
			Mockito.verify(staffMapper, Mockito.never()).getVehicleParkById(301);
		}
	}

	@Test
	public void employeeCannotReadOwnedVehicleCertificateOutsideCurrentPark() {
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtVehicleService vehicleService = Mockito.mock(SmtVehicleService.class);
		SmtVehicleStaffService vehicleStaffService = Mockito.mock(SmtVehicleStaffService.class);
		SmtVehicleApplyService vehicleApplyService = Mockito.mock(SmtVehicleApplyService.class);
		SmtStaffServiceImpl service = Mockito.spy(new SmtStaffServiceImpl());
		SmtStaff employee = new SmtStaff();
		employee.setId(101L);
		employee.setBadge("EMP-101");
		SmtVehicleApply application = new SmtVehicleApply();
		application.setId(301);
		application.setVehicleId(201L);
		application.setParkId("20");
		SmtVehicle ownedVehicle = new SmtVehicle();
		ownedVehicle.setId(201L);
		ownedVehicle.setIsDelete(0);
		SmtPark authorizedPark = new SmtPark();
		authorizedPark.setId(10);
		Mockito.when(staffMapper.selectOne(Mockito.any())).thenReturn(employee);
		Mockito.when(vehicleApplyService.getById(301)).thenReturn(application);
		Mockito.when(vehicleService.getById(201L)).thenReturn(ownedVehicle);
		Mockito.when(vehicleStaffService.count(Mockito.any())).thenReturn(1);
		Mockito.doReturn(Collections.singletonList(authorizedPark)).when(service).getStaffPark("EMP-101");
		ReflectionTestUtils.setField(service, "baseMapper", staffMapper);
		ReflectionTestUtils.setField(service, "smtVehicleService", vehicleService);
		ReflectionTestUtils.setField(service, "smtVehicleApplyService", vehicleApplyService);
		ReflectionTestUtils.setField(service, "vsService", vehicleStaffService);

		try {
			service.getVehicleParkByIdForOwner(301, "EMP-101");
			fail("当前员工不能读取其无园区权限的车辆证照详情");
		} catch (AccessDeniedException expected) {
			Mockito.verify(staffMapper, Mockito.never()).getVehicleParkById(301);
		}
	}

	@Test
	public void employeeCannotApplyUsingAnotherEmployeesSamePlateVehicle() {
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtVehicleService vehicleService = Mockito.mock(SmtVehicleService.class);
		SmtVehicleStaffService vehicleStaffService = Mockito.mock(SmtVehicleStaffService.class);
		CapturingVehicleParkService service = new CapturingVehicleParkService();
		SmtStaff employee = staff(101L, "EMP-101");
		SmtVehicle otherEmployeesVehicle = vehicle(202L, "豫A12345");
		Mockito.when(staffMapper.selectOne(Mockito.any())).thenReturn(employee);
		Mockito.when(vehicleService.list(Mockito.any())).thenReturn(Collections.singletonList(otherEmployeesVehicle));
		Mockito.when(vehicleStaffService.count(Mockito.any())).thenReturn(0);
		configureVehicleServices(service, staffMapper, vehicleService, vehicleStaffService);

		try {
			service.addVehiclePark(apply("EMP-101", "豫A12345", 10));
			fail("同牌跨园区的他人员工车辆不能被用于申请");
		} catch (AccessDeniedException expected) {
			assertNull(service.capturedOwnedVehicle);
		}
	}

	@Test
	public void employeeApplicationUsesTheVerifiedOwnedVehicleWhenPlatesCollideAcrossParks() {
		SmtStaffMapper staffMapper = Mockito.mock(SmtStaffMapper.class);
		SmtVehicleService vehicleService = Mockito.mock(SmtVehicleService.class);
		SmtVehicleStaffService vehicleStaffService = Mockito.mock(SmtVehicleStaffService.class);
		CapturingVehicleParkService service = new CapturingVehicleParkService();
		SmtStaff employee = staff(101L, "EMP-101");
		SmtVehicle ownedVehicle = vehicle(201L, "豫A12345");
		SmtVehicle samePlateInAnotherPark = vehicle(202L, "豫A12345");
		Mockito.when(staffMapper.selectOne(Mockito.any())).thenReturn(employee);
		Mockito.when(vehicleService.list(Mockito.any())).thenReturn(Arrays.asList(ownedVehicle, samePlateInAnotherPark));
		Mockito.when(vehicleStaffService.count(Mockito.any())).thenReturn(1);
		configureVehicleServices(service, staffMapper, vehicleService, vehicleStaffService);

		Result result = service.addVehiclePark(apply("EMP-101", "豫A12345", 10));

		assertEquals(true, result.getData());
		assertSame(ownedVehicle, service.capturedOwnedVehicle);
	}

	private static ApplyAuthDTO apply(String badge, String plateNumber, Integer parkId) {
		ApplyAuthDTO request = new ApplyAuthDTO();
		request.setBadge(badge);
		request.setPlateNumber(plateNumber);
		request.setParkId(parkId);
		return request;
	}

	private static SmtStaff staff(Long id, String badge) {
		SmtStaff staff = new SmtStaff();
		staff.setId(id);
		staff.setBadge(badge);
		return staff;
	}

	private static SmtVehicle vehicle(Long id, String plateNumber) {
		SmtVehicle vehicle = new SmtVehicle();
		vehicle.setId(id);
		vehicle.setVehiclePlate(plateNumber);
		vehicle.setIsDelete(0);
		return vehicle;
	}

	private static void configureVehicleServices(CapturingVehicleParkService service, SmtStaffMapper staffMapper,
			SmtVehicleService vehicleService, SmtVehicleStaffService vehicleStaffService) {
		ReflectionTestUtils.setField(service, "baseMapper", staffMapper);
		ReflectionTestUtils.setField(service, "smtVehicleService", vehicleService);
		ReflectionTestUtils.setField(service, "vsService", vehicleStaffService);
	}

	/** 仅截获已通过归属校验的车辆，避免单元测试写入 ActiveRecord。 */
	private static class CapturingVehicleParkService extends SmtStaffServiceImpl {
		private SmtVehicle capturedOwnedVehicle;

		@Override
		public List<SmtPark> getStaffPark(String staffBadge) {
			SmtPark park = new SmtPark();
			park.setId(10);
			return Collections.singletonList(park);
		}

		@Override
		protected Result addVehicleParkForOwnedVehicle(SmtStaff staff, SmtVehicle ownedVehicle,
				ApplyAuthDTO smtVehicleApply) {
			capturedOwnedVehicle = ownedVehicle;
			return new Result<>(true);
		}
	}
}
