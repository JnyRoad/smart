package com.tce.smart.app.service.fore;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * App 离职流程不得再把前端 processId 或 employeeId 直接转发至旧 Platform 接口。
 */
public class LeaveInternalActorContractTest {
	@Test
	public void appLeaveServicesUseActorBoundServiceTokenContracts() throws Exception {
		String application = source("LeaveApplicationServiceImpl.java");
		String handover = source("LeaveHandoverServiceImpl.java");

		assertTrue(application.contains("saveForActor(leaveApplicationDTO, badge"));
		assertTrue(application.contains("getForActor(processId, currentActorBadge()"));
		assertTrue(application.contains("getRecordForActor(recordId, currentActorBadge()"));
		assertTrue(application.contains("getHandoverForActor(processId, currentActorBadge()"));
		assertFalse(application.contains("leaveApplicationVO.getEmployeeId()"));

		assertTrue(handover.contains("getHandoverForAssignee(processId, badge"));
		assertTrue(handover.contains("endHandoverForActor(leaveHandoverDTO, badge"));
		assertTrue(handover.contains("startHandoverForActor(processId, SecurityUtils.getUser().getUsername()"));
		assertTrue(handover.contains("closeHandoverForActor(processId, SecurityUtils.getUser().getUsername()"));
	}

	private String source(String fileName) throws Exception {
		return new String(Files.readAllBytes(Paths.get("src/main/java/com/tce/smart/app/service/fore/impl/" + fileName)),
				StandardCharsets.UTF_8);
	}
}
