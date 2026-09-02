package com.tce.smart.common.security.openapi;

/**
 * 后端权威 capability scope 目录的一条只读定义。
 *
 * <p>scope 是安全边界，不应由普通业务字典或前端常量自行扩展；本对象仅用于管理端展示，
 * 实际保存时仍必须由服务端目录校验。</p>
 */
public final class OpenApiScope {

	private final String value;
	private final String label;
	private final boolean deprecated;

	public OpenApiScope(String value, String label, boolean deprecated) {
		this.value = value;
		this.label = label;
		this.deprecated = deprecated;
	}

	public String getValue() {
		return value;
	}

	public String getLabel() {
		return label;
	}

	/**
	 * 已废弃 scope 只用于展示和保留已有客户端，不允许为新客户端或现有客户端新增授予。
	 */
	public boolean isDeprecated() {
		return deprecated;
	}
}
