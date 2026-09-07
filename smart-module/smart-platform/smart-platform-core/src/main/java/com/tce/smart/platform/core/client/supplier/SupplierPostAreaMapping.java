package com.tce.smart.platform.core.client.supplier;

/**
 * 可信岗位目录提供的岗位到通行区域映射。
 *
 * 区域不能由厂牌原文或客户端请求自行声明。
 */
public final class SupplierPostAreaMapping {

	private final String postId;
	private final String areaId;

	private SupplierPostAreaMapping(String postId, String areaId) {
		this.postId = postId;
		this.areaId = areaId;
	}

	public static SupplierPostAreaMapping fromTrustedDirectory(String postId, String areaId) {
		return new SupplierPostAreaMapping(postId, areaId);
	}

	public String getPostId() {
		return postId;
	}

	public String getAreaId() {
		return areaId;
	}
}
