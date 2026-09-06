package com.tce.smart.platform.client.identity;

import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.service.SmtOrganizeRelationService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.enums.TempCompTypeEnum;
import org.junit.Assert;
import org.junit.Test;
import static org.mockito.Mockito.*;

/** 用工类别仅由员工主数据和组织关系判定；未知类型必须失败关闭。 */
public class ClientPersonnelDirectoryTest {
	@Test
	public void resolvesEmployeeOutsourcedAndDispatchedFromAuthoritativeFields() {
		SmtStaffService staffs = mock(SmtStaffService.class);
		SmtOrganizeRelationService organizations = mock(SmtOrganizeRelationService.class);
		when(staffs.getStaffByBadgeAll("E100")).thenReturn(staff("E100", 1, null));
		when(staffs.getStaffByBadgeAll("O100")).thenReturn(staff("O100", 10, "1001"));
		when(staffs.getStaffByBadgeAll("D100")).thenReturn(staff("D100", 9, "1002"));
		when(organizations.getByBu(1001L)).thenReturn(relation(TempCompTypeEnum.WAI_XIE.getCode()));
		when(organizations.getByBu(1002L)).thenReturn(relation(TempCompTypeEnum.PAI_QIAN.getCode()));
		ClientPersonnelDirectory directory = new ClientPersonnelDirectory(staffs, organizations);
		Assert.assertEquals("employee", directory.require("E100").getEmploymentType());
		Assert.assertEquals("outsourced", directory.require("O100").getEmploymentType());
		Assert.assertEquals("dispatched", directory.require("D100").getEmploymentType());
		Assert.assertEquals("dhr", directory.credentialSource("E100"));
		Assert.assertEquals("system", directory.credentialSource("O100"));
		Assert.assertEquals("system", directory.credentialSource("D100"));
	}

	@Test
	public void rejectsResignedAndUnmappedWorkersInsteadOfGuessing() {
		SmtStaffService staffs = mock(SmtStaffService.class);
		SmtOrganizeRelationService organizations = mock(SmtOrganizeRelationService.class);
		when(staffs.getStaffByBadgeAll("Q100")).thenReturn(staff("Q100", 1, null, 0));
		when(staffs.getStaffByBadgeAll("U100")).thenReturn(staff("U100", 10, null));
		ClientPersonnelDirectory directory = new ClientPersonnelDirectory(staffs, organizations);
		expect(403, () -> directory.require("Q100"));
		expect(403, () -> directory.require("U100"));
	}

	private static SmtStaff staff(String badge, int type, String compId) { return staff(badge, type, compId, 1); }
	private static SmtStaff staff(String badge, int type, String compId, int status) {
		SmtStaff staff = new SmtStaff(); staff.setBadge(badge); staff.setName("测试人员"); staff.setCompName("测试组织"); staff.setEmpType(type); staff.setCompId(compId); staff.setStatus(status); return staff;
	}
	private static SmtOrganizeRelation relation(int type) { SmtOrganizeRelation relation = new SmtOrganizeRelation(); relation.setCompType(type); return relation; }
	private static void expect(int status, Action action) { try { action.run(); Assert.fail("应拒绝"); } catch (ClientApiException failure) { Assert.assertEquals(status, failure.getStatus()); } }
	private interface Action { void run(); }
}
