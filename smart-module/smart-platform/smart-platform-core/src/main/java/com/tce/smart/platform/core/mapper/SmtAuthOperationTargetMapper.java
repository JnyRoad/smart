package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.platform.core.dto.authoperation.AuthOperationStateCount;
import com.tce.smart.platform.core.entity.SmtAuthOperationTarget;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 权限操作目标数据访问接口。
 */
public interface SmtAuthOperationTargetMapper extends BaseMapper<SmtAuthOperationTarget> {

	List<SmtAuthOperationTarget> selectByBatchIdAndTargetKeys(@Param("batchId") Long batchId,
			@Param("targetKeys") List<String> targetKeys);

	int countByBatchId(@Param("batchId") Long batchId);

	int countByBatchIdAndState(@Param("batchId") Long batchId, @Param("state") String state);

	List<AuthOperationStateCount> countByBatchIdGroupByState(@Param("batchId") Long batchId);

	int queueByBatchId(@Param("batchId") Long batchId, @Param("updatedAt") LocalDateTime updatedAt);

	List<SmtAuthOperationTarget> selectClaimCandidates(@Param("parkId") Integer parkId,
			@Param("operationQueue") String operationQueue,
			@Param("now") LocalDateTime now,
			@Param("maxCount") Integer maxCount);

	int claimByLease(@Param("targetId") Long targetId,
			@Param("expectedState") String expectedState,
			@Param("leaseToken") String leaseToken,
			@Param("now") LocalDateTime now,
			@Param("leaseUntil") LocalDateTime leaseUntil);

	List<SmtAuthOperationTarget> selectExactClaimCandidates(@Param("parkId") Integer parkId,
			@Param("operationQueue") String operationQueue, @Param("accessType") String accessType,
			@Param("targetIds") List<Long> targetIds, @Param("now") LocalDateTime now,
			@Param("maxCount") Integer maxCount);

	int markWaitingConfirmByLease(@Param("targetId") Long targetId,
			@Param("leaseToken") String leaseToken, @Param("updatedAt") LocalDateTime updatedAt);

	int updateStateByLease(@Param("targetId") Long targetId,
			@Param("leaseToken") String leaseToken,
			@Param("targetState") String targetState,
			@Param("updatedAt") LocalDateTime updatedAt,
			@Param("failureReason") String failureReason);

	List<SmtAuthOperationTarget> selectExpiredUnfinished(@Param("parkId") Integer parkId,
			@Param("now") LocalDateTime now,
			@Param("maxCount") Integer maxCount);
}
