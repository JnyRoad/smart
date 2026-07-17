package com.tce.smart.platform.core.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 保密区权限下发命令的受理结果。
 *
 * HTTP 202 仅代表命令已持久化，不代表任何设备已经完成下发。
 */
@Data
@AllArgsConstructor
public class SecurityDispatchAcceptedVO {

	/** 已受理的最新下发批次号 */
	private Long batchId;

	/** 本批次写入待消费命令的人员明细数量 */
	private Integer acceptedCount;

	/** 本次实际接管的 ISC 历史任务数量；Task 2 固定为 0，Task 3 负责真实接管 */
	private Integer takeoverCount;
}
