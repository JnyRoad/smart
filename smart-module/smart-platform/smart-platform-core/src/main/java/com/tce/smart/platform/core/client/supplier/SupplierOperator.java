package com.tce.smart.platform.core.client.supplier;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 服务端认证与授权适配层提供的工作人员上下文。
 *
 * 本对象不能由请求正文自称；权限和岗位集合必须来自当前服务端身份。
 */
public final class SupplierOperator {

	private final String operatorId;
	private final Set<String> permissions;
	private final Set<String> authorizedPostIds;

	private SupplierOperator(String operatorId, Set<String> permissions, Set<String> authorizedPostIds) {
		this.operatorId = operatorId;
		this.permissions = immutableSet(permissions);
		this.authorizedPostIds = immutableSet(authorizedPostIds);
	}

	public static SupplierOperator authenticated(String operatorId, Set<String> permissions,
			Set<String> authorizedPostIds) {
		return new SupplierOperator(operatorId, permissions, authorizedPostIds);
	}

	public String getOperatorId() {
		return operatorId;
	}

	public Set<String> getPermissions() {
		return permissions;
	}

	public Set<String> getAuthorizedPostIds() {
		return authorizedPostIds;
	}

	public boolean hasPermission(String permission) {
		return permissions.contains(permission);
	}

	public boolean isAuthorizedForPost(String postId) {
		return authorizedPostIds.contains(postId);
	}

	private static Set<String> immutableSet(Set<String> values) {
		if (values == null || values.isEmpty()) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(new LinkedHashSet<>(values));
	}
}
