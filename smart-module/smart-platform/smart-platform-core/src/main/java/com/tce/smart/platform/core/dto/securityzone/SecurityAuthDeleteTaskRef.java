package com.tce.smart.platform.core.dto.securityzone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 自动删除任务创建结果的来源引用。
 *
 * <p>调用方必须提供可解析的实际任务主键；错误文本、空值和不存在的主键不能被记录为任务。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityAuthDeleteTaskRef implements Serializable {

	private static final long serialVersionUID = 1L;

	/** 任务来源：NORMAL 对应 SMT_DEVICE_TASK，ISC 对应 SMT_ISC_DEVICE_TASK。 */
	private String taskSource;

	/** 来源任务主键的字符串表示，避免跨表 Long/Integer 差异。 */
	private String taskId;

	/** 设备编码快照。 */
	private String deviceCode;

	/** 任务动作快照。 */
	private Integer action;
}
