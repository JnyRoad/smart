package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtAuthOperationBatch;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 权限操作批次数据访问接口。
 */
public interface SmtAuthOperationBatchMapper extends BaseMapper<SmtAuthOperationBatch> {

	SmtAuthOperationBatch selectByParkAndIdempotency(@Param("parkId") Integer parkId,
			@Param("idempotencyKey") String idempotencyKey);

	int advanceExpansion(@Param("batchId") Long batchId,
			@Param("previousCursor") Long previousCursor,
			@Param("appendedCount") Integer appendedCount,
			@Param("nextCursor") Long nextCursor,
			@Param("updatedAt") LocalDateTime updatedAt);

	int finishExpansion(@Param("batchId") Long batchId,
			@Param("expectedCount") Integer expectedCount,
			@Param("status") String status,
			@Param("finishedAt") LocalDateTime finishedAt);
}
