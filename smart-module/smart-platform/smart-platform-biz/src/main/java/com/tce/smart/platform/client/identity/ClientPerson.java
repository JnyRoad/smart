package com.tce.smart.platform.client.identity;

/** 从人员主数据得到的最小身份快照，不包含身份证、电话、照片等敏感字段。 */
public final class ClientPerson {
	private final String staffNo;
	private final String displayName;
	private final String organization;
	private final String employmentType;

	ClientPerson(String staffNo, String displayName, String organization, String employmentType) {
		this.staffNo = staffNo; this.displayName = displayName; this.organization = organization;
		this.employmentType = employmentType;
	}
	public String getStaffNo() { return staffNo; }
	public String getDisplayName() { return displayName; }
	public String getOrganization() { return organization; }
	public String getEmploymentType() { return employmentType; }
}
