package com.tce.smart.platform.controller;

import com.tce.smart.common.security.annotation.Inner;
import com.tce.smart.common.security.annotation.OpenApi;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 定时任务 Feign 的目标端点只能由持有 server scope 服务令牌的调用方访问。
 *
 * 管理端仍在使用的园区和员工充值手动入口会拆分专用 internal 路径，
 * 不在这里把原有交互路由错误地标记成内部接口。
 */
public class ScheduleServiceEndpointSecurityContractTest {

	@Test
	public void pureScheduleEndpointsRequireInternalServerAuthentication() throws Exception {
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtReplaceApplicationController", "patchErrorPushMsg");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtDormitoryQuitApplyController", "dealyQuit");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtOutDormitoryStaffController", "refreshOutDormitory");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtSdMeterreadDetailDailyController", "genDailyRecord");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtDeviceController", "deviceStatus");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtRecruitmentController", "refreshRecruitmentById");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtRecruitmentController", "refreshComp");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtSnapPersonController", "checkTemp");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtStaffController", "syncStaff");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtStaffController", "syncStaffImg");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtStaffController", "syncIscPersonFace");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtStaffController", "retryFailedIscPersonFaceSync");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtStaffController", "getSmtStaffList");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.VisitorTaskController", "visitorOverTime");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.VisitorTaskController", "overTimeNoLeave");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.VisitorTaskController", "visitorRemind");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.VisitorTaskController", "toEmail");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.admittance.SmtAdmittanceApplyController", "updateOaStatusTask");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.admittance.SmtAdmittanceApplyController", "visitorOverTime");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.admittance.SmtAdmittanceApplyController", "overTimeNoLeave");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.admittance.SmtAdmittanceApplyController", "visitorComeOnTime");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.admittance.SmtAdmittanceApplyController", "visitorRemind");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.admittance.SmtOaAreaTypeController", "syncOaArea");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.manage.SmtAttendanceSignController", "syncStaff");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.manage.SmtEhrSetUpController", "smg");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.manage.SmtEhrSetUpController", "autoSignTask");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.news.SmtNewsTerminalController", "checkPublic");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.securityzone.SmtSecurityAuthDeleteController", "getConfig");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.securityzone.SmtSecurityAuthApplyController", "sendMessage");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.securityzone.SmtSecurityAuthApplyController", "updateOaStatusTask");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.OAWorkflowController", "cleanExpiredLogs");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtParkLogisticsController", "list");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtParkLogisticsController", "getByCompanyId");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtLeaveApplicationController", "sysnProcessRecord");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtWageSignController", "syncStaff");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtEhrToStaffSettingController", "getListEHR");
		assertInternalServerEndpoint("com.tce.smart.platform.controller.SmtEhrToStaffSettingController", "getListDHR");
	}

	private void assertInternalServerEndpoint(String className, String methodName) throws Exception {
		Class<?> controllerType = Class.forName(className);
		Method endpoint = null;
		for (Method candidate : controllerType.getMethods()) {
			if (methodName.equals(candidate.getName())) {
				endpoint = candidate;
				break;
			}
		}
		assertNotNull(className + "#" + methodName + " 必须存在", endpoint);
		assertNotNull(className + "#" + methodName + " 必须标记 @Inner", endpoint.getAnnotation(Inner.class));
		OpenApi openApi = endpoint.getAnnotation(OpenApi.class);
		assertNotNull(className + "#" + methodName + " 必须要求服务令牌", openApi);
		assertEquals("server", openApi.value());
	}
}
