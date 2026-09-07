package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.entity.SmtAuthOperationAttempt;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 权限执行尝试数据访问接口。
 */
public interface SmtAuthOperationAttemptMapper extends BaseMapper<SmtAuthOperationAttempt> {

	Integer selectMaxAttemptNo(@Param("targetId") Long targetId);

	SmtAuthOperationAttempt selectByIdAndTarget(@Param("id") Long id, @Param("targetId") Long targetId);

	int bindExternal(@Param("id") Long id, @Param("targetId") Long targetId,
			@Param("externalBatchId") String externalBatchId,
			@Param("externalCommandId") String externalCommandId,
			@Param("updatedAt") LocalDateTime updatedAt);

	int prepareSubmission(@Param("id") Long id, @Param("targetId") Long targetId,
			@Param("leaseToken") String leaseToken, @Param("taskId") String taskId,
			@Param("updatedAt") LocalDateTime updatedAt);

	int markSubmitted(@Param("id") Long id, @Param("targetId") Long targetId,
			@Param("leaseToken") String leaseToken, @Param("taskId") String taskId,
			@Param("externalBatchId") String externalBatchId,
			@Param("externalCommandId") String externalCommandId,
			@Param("updatedAt") LocalDateTime updatedAt);

	int markReceipt(@Param("attemptId") Long attemptId, @Param("targetId") Long targetId,
			@Param("status") String status, @Param("eventId") Long eventId,
			@Param("errorMessage") String errorMessage,
			@Param("updatedAt") LocalDateTime updatedAt);
}
