package com.tce.smart.platform.core.dto;

import lombok.Getter;

import java.util.Objects;

/**
 * 保密区权限异步下发上下文。
 *
 * <p>该对象只供保密区专用入口显式传递来源和批次，禁止通用人员下发入口隐式构造。</p>
 */
@Getter
public final class SecurityAuthDispatchContext {

	public static final String SOURCE_TYPE = "SECURITY_AUTH";

	private final Long sourceId;
	private final Long sourceDetailId;
	private final Long batchId;
	private final Long staffId;
	private final Integer authId;

	private SecurityAuthDispatchContext(Long sourceId, Long sourceDetailId, Long batchId,
			Long staffId, Integer authId) {
		this.sourceId = Objects.requireNonNull(sourceId, "保密区申请ID不能为空");
		this.sourceDetailId = Objects.requireNonNull(sourceDetailId, "保密区明细ID不能为空");
		this.batchId = Objects.requireNonNull(batchId, "保密区批次ID不能为空");
		this.staffId = Objects.requireNonNull(staffId, "保密区员工ID不能为空");
		this.authId = Objects.requireNonNull(authId, "保密区权限ID不能为空");
	}

	public static SecurityAuthDispatchContext of(Long sourceId, Long sourceDetailId, Long batchId,
			Long staffId, Integer authId) {
		return new SecurityAuthDispatchContext(sourceId, sourceDetailId, batchId, staffId, authId);
	}

	/** 将来源信息写入真实设备任务，明确不写入入厂申请专用的 applyId。 */
	public void applyTo(DeviceTaskVO task, String deviceCode) {
		Objects.requireNonNull(task, "设备任务不能为空");
		if (!Objects.equals(staffId.toString(), task.getCardNo())) {
			throw new IllegalArgumentException("保密区任务员工与卡号不一致");
		}
		task.setSourceType(SOURCE_TYPE);
		task.setSourceId(sourceId);
		task.setSourceDetailId(sourceDetailId);
		task.setBatchId(batchId);
		task.setIntentKey(SOURCE_TYPE + ":" + staffId + ":" + authId + ":" + deviceCode);
	}
}
