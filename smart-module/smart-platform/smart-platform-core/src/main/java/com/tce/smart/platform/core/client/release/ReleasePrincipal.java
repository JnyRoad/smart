package com.tce.smart.platform.core.client.release;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 服务端认证后的操作人上下文。
 *
 * 该对象只能由认证与授权适配层依据服务端会话构造，不能直接从请求正文反序列化。
 */
public final class ReleasePrincipal {

	private final String actorId;
	private final Set<String> permissions;
	private final Set<String> authorizedPostIds;

	private ReleasePrincipal(String actorId, Set<String> permissions, Set<String> authorizedPostIds) {
		this.actorId = actorId;
		this.permissions = immutableSet(permissions);
		this.authorizedPostIds = immutableSet(authorizedPostIds);
	}

	public static ReleasePrincipal authenticated(String actorId, Set<String> permissions,
			Set<String> authorizedPostIds) {
		return new ReleasePrincipal(actorId, permissions, authorizedPostIds);
	}

	public String getActorId() {
		return actorId;
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
