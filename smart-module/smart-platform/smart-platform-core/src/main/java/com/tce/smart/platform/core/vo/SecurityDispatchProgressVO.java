package com.tce.smart.platform.core.vo;

import lombok.Data;

/**
 * 保密区权限下发批次进度。
 *
 * 数量只统计指定的当前批次，避免旧批次迟到结果污染管理端展示。
 */
@Data
public class SecurityDispatchProgressVO {

	/** 当前下发批次号 */
	private Long batchId;
	/** 本批次人员明细总数 */
	private Integer totalCount;
	/** 等待后台 worker 领取的数量 */
	private Integer waitingCount;
	/** 正在执行的数量 */
	private Integer inWorkCount;
	/** 已成功数量 */
	private Integer successCount;
	/** 已失败数量 */
	private Integer failCount;
}
