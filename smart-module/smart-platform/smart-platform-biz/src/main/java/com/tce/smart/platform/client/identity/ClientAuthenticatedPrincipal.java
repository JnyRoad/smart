package com.tce.smart.platform.client.identity;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** 已由资源服务器校验令牌、并由人员目录复核在职状态的 App 操作主体。 */
public final class ClientAuthenticatedPrincipal {
	private final ClientPerson person;
	private final Set<String> permissions;
	private final List<Integer> parkIds;

	ClientAuthenticatedPrincipal(ClientPerson person, Set<String> permissions, List<Integer> parkIds) {
		this.person = person;
		this.permissions = Collections.unmodifiableSet(new LinkedHashSet<>(permissions));
		this.parkIds = parkIds == null ? Collections.<Integer>emptyList()
				: Collections.unmodifiableList(new java.util.ArrayList<>(parkIds));
	}
	public ClientPerson getPerson() { return person; }
	public Set<String> getPermissions() { return permissions; }
	public List<Integer> getParkIds() { return parkIds; }
	public boolean has(String permission) { return permissions.contains(permission); }
}
