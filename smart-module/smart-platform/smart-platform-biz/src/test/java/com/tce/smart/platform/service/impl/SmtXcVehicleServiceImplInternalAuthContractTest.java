package com.tce.smart.platform.service.impl;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 许昌车辆同步必须显式请求服务令牌。
 *
 * 这里校验 Feign 调用点而不是只校验 Feign 接口，避免接口新增头参数后调用方遗漏标记，
 * 导致拦截器继续透传用户令牌或在无请求线程中拒绝调用。
 */
public class SmtXcVehicleServiceImplInternalAuthContractTest {

	private static final String SOURCE = "src/main/java/com/tce/smart/platform/service/impl/SmtXcVehicleServiceImpl.java";

	@Test
	public void xcVehicleSynchronizationUsesServiceTokenMarkerForCreateAndDelete() throws IOException {
		String source = new String(Files.readAllBytes(Paths.get(SOURCE)), StandardCharsets.UTF_8)
				.replaceAll("\\s+", " ");
		Assert.assertTrue("新增车辆同步必须显式申请服务令牌", source.contains(
				"remoteXCVehicleService.saveVehicle(xcVehicleAddDTO, SecurityConstants.FROM_IN, "
						+ "SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)"));
		Assert.assertTrue("删除车辆同步必须显式申请服务令牌", source.contains(
				"remoteXCVehicleService.deleteVehicle(xcVehicle.getVehiclePlate(), SecurityConstants.FROM_IN, "
						+ "SecurityConstants.INTERNAL_SERVICE_AUTH_REQUIRED)"));
	}
}
