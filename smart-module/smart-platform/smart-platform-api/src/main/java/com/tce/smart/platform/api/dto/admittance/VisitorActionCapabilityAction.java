package com.tce.smart.platform.api.dto.admittance;

/**
 * 访客匿名草稿可执行的受限动作。
 *
 * 动作必须显式参与 capability 的 Redis 比对值，避免一张上传票据被复用于黑名单查询
 * 或人脸裁剪等其他路径。
 */
public enum VisitorActionCapabilityAction {
	FACE_CROP,
	FACE_UPLOAD,
	DOCUMENT_UPLOAD,
	BLACKLIST_CHECK
}
