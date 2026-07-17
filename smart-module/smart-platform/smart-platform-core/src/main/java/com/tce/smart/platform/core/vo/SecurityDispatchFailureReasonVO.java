package com.tce.smart.platform.core.vo;

import lombok.Data;

/**
 * 保密区权限下发失败原因。
 *
 * <p>仅包含管理端渲染所需的人员、设备和脱敏原因，不暴露证件、照片、密钥或 ISC 内部任务标识。</p>
 */
@Data
public class SecurityDispatchFailureReasonVO {

	/** 人员工号 */
	private String staffBadge;
	/** 人员姓名 */
	private String staffName;
	/** 失败设备编码 */
	private String deviceCode;
	/** ISC 任务终态：FAIL、CANCEL 或 EXPIRED */
	private String status;
	/** 脱敏后的 ISC 任务失败备注 */
	private String reason;
}
