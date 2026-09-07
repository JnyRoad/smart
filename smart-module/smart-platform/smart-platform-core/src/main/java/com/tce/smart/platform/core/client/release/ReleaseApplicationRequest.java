package com.tce.smart.platform.core.client.release;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 申请人可填写的业务字段，不包含可信身份、审批人或权限。
 */
public final class ReleaseApplicationRequest {

	private final String title;
	private final String reason;
	private final List<String> materials;
	private final List<String> sealCodes;
	private final String originPostId;
	private final String destinationPostId;

	public ReleaseApplicationRequest(String title, String reason, List<String> materials, List<String> sealCodes,
			String originPostId, String destinationPostId) {
		this.title = title;
		this.reason = reason;
		this.materials = immutableList(materials);
		this.sealCodes = immutableList(sealCodes);
		this.originPostId = originPostId;
		this.destinationPostId = destinationPostId;
	}

	public String getTitle() {
		return title;
	}

	public String getReason() {
		return reason;
	}

	public List<String> getMaterials() {
		return materials;
	}

	public List<String> getSealCodes() {
		return sealCodes;
	}

	public String getOriginPostId() {
		return originPostId;
	}

	public String getDestinationPostId() {
		return destinationPostId;
	}

	private static List<String> immutableList(List<String> values) {
		if (values == null || values.isEmpty()) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(new ArrayList<>(values));
	}
}
