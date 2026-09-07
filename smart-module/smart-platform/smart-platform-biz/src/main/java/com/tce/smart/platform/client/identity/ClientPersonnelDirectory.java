package com.tce.smart.platform.client.identity;

import com.tce.smart.platform.core.entity.SmtOrganizeRelation;
import com.tce.smart.platform.core.entity.SmtStaff;
import com.tce.smart.platform.service.SmtOrganizeRelationService;
import com.tce.smart.platform.service.SmtStaffService;
import com.tce.smart.tool.enums.TempCompTypeEnum;
import org.springframework.stereotype.Service;

/**
 * App 人员身份统一从已同步的员工主数据和外部组织关系读取。
 * 不使用 token 中的薪资类型，也不允许客户端声明正式、外包或派遣身份。
 */
@Service
public class ClientPersonnelDirectory {
	private final SmtStaffService staffs;
	private final SmtOrganizeRelationService organizations;

	public ClientPersonnelDirectory(SmtStaffService staffs, SmtOrganizeRelationService organizations) {
		this.staffs = staffs; this.organizations = organizations;
	}

	public ClientPerson require(String staffNo) {
		if (!identifier(staffNo)) throw new ClientApiException(401);
		final SmtStaff staff;
		// getStaffByBadge 只查询临时人员；App 还需要识别从 DHR 同步的正式员工。
		// 统一使用未离职员工查询，随后仍由本目录按权威用工类别失败关闭。
		try { staff = staffs.getStaffByBadgeAll(staffNo.trim()); }
		catch (Exception failure) { throw new ClientApiException(503); }
		if (staff == null || staff.getStatus() == null || staff.getStatus().intValue() == 0) throw new ClientApiException(403);
		String employmentType = employmentType(staff);
		String resolvedStaffNo = text(staff.getBadge(), staffNo.trim());
		return new ClientPerson(resolvedStaffNo, text(staff.getName(), resolvedStaffNo),
				text(staff.getCompName(), text(staff.getDepName(), "未设置组织")), employmentType);
	}

	public String displayNameOrStaffNo(String staffNo) {
		try { return require(staffNo).getDisplayName(); }
		catch (ClientApiException ignored) { return staffNo; }
	}

	/**
	 * 认证来源由平台员工主数据裁定：正式员工交给 DHR 适配器，外包和派遣人员
	 * 只使用本系统账户凭据。调用方无法用请求参数改变该结果。
	 */
	public String credentialSource(String staffNo) {
		String employmentType = require(staffNo).getEmploymentType();
		if ("employee".equals(employmentType)) return "dhr";
		if ("outsourced".equals(employmentType) || "dispatched".equals(employmentType)) return "system";
		throw new ClientApiException(403);
	}

	private String employmentType(SmtStaff staff) {
		SmtOrganizeRelation relation = relation(staff.getCompId());
		if (relation != null && TempCompTypeEnum.WAI_XIE.getCode().equals(relation.getCompType())) return "outsourced";
		if (relation != null && TempCompTypeEnum.PAI_QIAN.getCode().equals(relation.getCompType())) return "dispatched";
		if (Integer.valueOf(9).equals(staff.getEmpType())) return "dispatched";
		if (Integer.valueOf(1).equals(staff.getEmpType())) return "employee";
		// 其他 DHR 用工类别尚无经确认的 App 映射，拒绝比猜测权限安全。
		throw new ClientApiException(403);
	}

	private SmtOrganizeRelation relation(String compId) {
		if (compId == null || !compId.matches("[1-9][0-9]{0,18}")) return null;
		try { return organizations.getByBu(Long.valueOf(compId)); }
		catch (Exception failure) { throw new ClientApiException(503); }
	}

	private static boolean identifier(String value) {
		return value != null && value.trim().matches("[A-Za-z0-9][A-Za-z0-9._:-]{0,95}");
	}

	private static String text(String value, String fallback) {
		return value == null || value.trim().isEmpty() ? fallback : value.trim();
	}
}
