package com.tce.smart.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tce.smart.admin.entity.OauthClientTokenRevocationTask;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OAuth 客户端令牌撤销事务待办 Mapper。
 */
public interface OauthClientTokenRevocationTaskMapper extends BaseMapper<OauthClientTokenRevocationTask> {

	/**
	 * 按下次重试时间读取已经到期的有界待办批次。
	 *
	 * @param now 当前调度时间，仅选择已到期任务
	 * @param limit 单批最大任务数
	 * @return 待恢复任务
	 */
	List<OauthClientTokenRevocationTask> selectPendingBatch(@Param("now") LocalDateTime now,
			@Param("limit") int limit);

	/**
	 * 按任务 ID 推迟失败任务；仅允许把时间向后移动，避免并发旧尝试覆盖更新的退避。
	 *
	 * @param taskId 任务 ID
	 * @param nextRetryAt 新的最早重试时间
	 * @return 实际更新行数；并发删除或已有更晚退避时为 0
	 */
	int postponeFailure(@Param("taskId") String taskId,
			@Param("nextRetryAt") LocalDateTime nextRetryAt);

	/**
	 * 查询客户端最早的未完成待办，支持相同 scope 再次提交时同步恢复。
	 *
	 * @param clientId 客户端 ID
	 * @return 最早待办；不存在时返回 null
	 */
	OauthClientTokenRevocationTask selectOldestByClientId(@Param("clientId") String clientId);
}
