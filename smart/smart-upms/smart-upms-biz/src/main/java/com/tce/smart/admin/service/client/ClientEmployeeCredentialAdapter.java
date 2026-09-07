package com.tce.smart.admin.service.client;

/**
 * 正式员工的认证边界。当前 App 认证调用方只认识通过或拒绝；接入 DHR 时替换此
 * 适配器即可，不改变 App、网关或 UPMS 的公开 API。
 */
public interface ClientEmployeeCredentialAdapter {
	boolean verify(String staffNo, String password);
}
