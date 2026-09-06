package com.tce.smart.admin.api.dto;

/** App 登录人员认证来源的内部契约，只允许系统账号或 DHR 适配器识别所需的来源字段。 */
public class PersonnelAuthSourceDTO {
	private String source;

	public PersonnelAuthSourceDTO() { }
	public PersonnelAuthSourceDTO(String source) { this.source = source; }
	public String getSource() { return source; }
	public void setSource(String source) { this.source = source; }
}
