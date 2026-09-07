package com.tce.smart.platform.core.client.release;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 服务端为新建申请准备的可信上下文。
 *
 * 候选岗位和指派审批人均来自服务端策略，不属于客户端可声明的身份事实。
 */
public final class ReleaseCreationContext {

	private final ReleasePrincipal applicant;
	private final Set<String> candidatePostIds;
	private final String assignedApproverId;

	private ReleaseCreationContext(ReleasePrincipal applicant, Set<String> candidatePostIds,
			String assignedApproverId) {
		this.applicant = applicant;
		this.candidatePostIds = immutableSet(candidatePostIds);
		this.assignedApproverId = assignedApproverId;
	}

	public static ReleaseCreationContext verified(ReleasePrincipal applicant, Set<String> candidatePostIds,
			String assignedApproverId) {
		return new ReleaseCreationContext(applicant, candidatePostIds, assignedApproverId);
	}

	public ReleasePrincipal getApplicant() {
		return applicant;
	}

	public Set<String> getCandidatePostIds() {
		return candidatePostIds;
	}

	public String getAssignedApproverId() {
		return assignedApproverId;
	}

	private static Set<String> immutableSet(Set<String> values) {
		if (values == null || values.isEmpty()) {
			return Collections.emptySet();
		}
		return Collections.unmodifiableSet(new LinkedHashSet<>(values));
	}
}
