package com.tce.smart.platform.core.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ActionRow;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.ReviewRow;
import com.tce.smart.platform.core.dto.authgovernance.AuthOperationGovernanceData.TargetSnapshot;
import com.tce.smart.platform.core.entity.SmtAuthGovernanceAction;
import com.tce.smart.platform.core.entity.SmtAuthOperationAttempt;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/** 治理查询与单目标条件写；不暴露动作更新或删除方法。 */
public interface AuthOperationGovernanceMapper {

	LocalDateTime now();
	TargetSnapshot selectTargetScope(@Param("targetId") Long targetId);
	TargetSnapshot lockTarget(@Param("targetId") Long targetId);
	SmtAuthOperationAttempt lockCurrentAttempt(@Param("targetId") Long targetId,
			@Param("attemptId") Long attemptId);
	int hasCurrentSourceBinding(@Param("targetId") Long targetId,
			@Param("resourceId") String resourceId, @Param("operationVersion") Long operationVersion);
	int countAttemptTrace(@Param("attemptId") Long attemptId);
	int expireKnownUnsentAttempt(@Param("attemptId") Long attemptId, @Param("targetId") Long targetId,
			@Param("leaseToken") String leaseToken, @Param("now") LocalDateTime now);
	int releaseKnownUnsentResourceAttempt(@Param("resourceId") String resourceId,
			@Param("operationVersion") Long operationVersion, @Param("targetId") Long targetId,
			@Param("attemptId") Long attemptId, @Param("now") LocalDateTime now);
	int requeueKnownUnsentTarget(@Param("targetId") Long targetId, @Param("leaseToken") String leaseToken,
			@Param("operationVersion") Long operationVersion, @Param("now") LocalDateTime now);
	int insertAction(SmtAuthGovernanceAction action);
	SmtAuthGovernanceAction selectActionByKey(@Param("actorUserId") Integer actorUserId,
			@Param("idempotencyKey") String idempotencyKey, @Param("subjectKey") String subjectKey);
	IPage<ReviewRow> selectParkReviews(IPage<ReviewRow> page, @Param("parkId") Integer parkId);
	IPage<ReviewRow> selectGlobalReviews(IPage<ReviewRow> page);
	IPage<ActionRow> selectTargetActions(IPage<ActionRow> page, @Param("targetId") Long targetId,
			@Param("parkId") Integer parkId);
	ActionRow selectTargetAction(@Param("actionId") Long actionId, @Param("targetId") Long targetId,
			@Param("parkId") Integer parkId);
}
